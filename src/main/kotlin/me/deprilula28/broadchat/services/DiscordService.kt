package me.deprilula28.broadchat.services

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import me.deprilula28.broadchat.api.BroadChatAPI
import me.deprilula28.broadchat.api.BroadChatSource
import me.deprilula28.broadchat.api.ExternalBroadChatService
import me.deprilula28.broadchat.api.ExternalBroadChatSource
import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.settings.ChannelMappings
import me.deprilula28.broadchat.settings.ExternalServiceSettings
import me.deprilula28.broadchat.settings.ServiceSettingsLoader
import me.deprilula28.broadchat.settings.SettingValue
import me.deprilula28.broadchat.util.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.impl.GameImpl
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.utils.SimpleLog
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.util.*
import java.util.function.Function


class DiscordServiceSettingsLoader: ServiceSettingsLoader<DiscordServiceSettings> {
    override fun load(map: Map<*, *>): DiscordServiceSettings =
        DiscordServiceSettings(map["serverIP"] as String, map["inviteID"] as String, (map["token"] as String).toCharArray(),
            map["use"] as Boolean, map["discord-input"] as Map<String, Map<String, String>>, map["discord-output"] as Map<String, Map<String, String>>,
                DiscordService.MessageSendType.valueOf((map["format"] as String).toUpperCase()), map["webhook-url"] as String,
                SettingValue(map["message-format"] as String))

    override fun initService(settings: DiscordServiceSettings, api: BroadChatAPI): ExternalBroadChatService =
        DiscordService(settings.serverIP, settings.inviteID, settings.token, api, ChannelMappings(settings.channelMappingsInput),
                ChannelMappings(settings.channelMappingsOutput), settings.messageSendType,
                if (settings.messageSendType == DiscordService.MessageSendType.WEBHOOK) Optional.of(settings.webhookURL) else Optional.empty(), settings.messageFormat)

}

class DiscordServiceSettings(val serverIP: String, val inviteID: String, val token: CharArray, use: Boolean,
                     val channelMappingsInput: Map<String, Map<String, String>>, val channelMappingsOutput: Map<String, Map<String, String>>,
                     val messageSendType: DiscordService.MessageSendType, val webhookURL: String, val messageFormat: SettingValue):
        ExternalServiceSettings(use, channelMappingsInput)

class DiscordService constructor(serverIP: String, invite: String, token: CharArray, api: BroadChatAPI,
                                 private val output: ChannelMappings, internal val input: ChannelMappings, private val messageSendType: MessageSendType,
                                 private val webhook: Optional<String>, private val messageFormat: SettingValue):
    ExternalBroadChatService(
        name = "Discord",
        id = "discord",
        hoverMessage = "&3Free chat service for gamers alike.",
        clickURL = "http://discord.gg/$invite",
        specificClickURL = Function { Optional.of("http://discord.gg/$invite") },
        specificHoverMessage = Function { Optional.of("&aJoin our services by clicking or going to this link:\n&bdiscord.gg/$invite") }
    ) {

    private class JDALogger: SimpleLog.LogListener {

        override fun onLog(log: SimpleLog, level: SimpleLog.Level, p2: Any) {
            if (p2 is String) {
                if (level.isError) err("§r[§3JDA §cError§r] §f$p2")
                else if (level == SimpleLog.Level.INFO) info("§r[§3Discord§r] §r$p2")
            }
        }

        override fun onError(log: SimpleLog, err: Throwable) { }

    }

    private val jda: JDA
    private val channelMap: MutableMap<Chat, Long>

    init {

        fun getLink(id: String) = "https://discordapp.com/oauth2/authorize?client_id=$id&scope=bot"

        info("Connecting to Discord servers...")
        SimpleLog.LEVEL = SimpleLog.Level.FATAL
        SimpleLog.addListener(JDALogger())
        jda = JDABuilder(AccountType.BOT).apply {
            setGame(GameImpl("Loading...", null, Game.GameType.DEFAULT))
            setToken(String(token))
            setStatus(OnlineStatus.DO_NOT_DISTURB)
        }.buildBlocking()
        jda.presence.apply {
            status = OnlineStatus.ONLINE
            game = GameImpl(serverIP, null, Game.GameType.DEFAULT)
        }
        jda.addEventListener(JDAListener(this, api))
        info("Connected to Discord.")

        if (jda.guilds.isEmpty()) {
            warn("Seems like the bot isn't in any servers yet! Here's the OAuth URL to add it: " + getLink(jda.selfUser.id))
        }

        if (ccDataFolder.exists() && File(ccDataFolder, "discordChatChannels.json").exists()) {
            val type = object: TypeToken<MutableMap<String, Long>>() {}
            channelMap = gson.fromJson(File(ccDataFolder, "discordChatChannels.json").readText(), type.type)
        } else channelMap = mutableMapOf()

    }

    override fun disable() {

        info("Disconnecting with Discord...")
        jda.shutdown()
        info("Disconnected successfully.")

    }

    override fun sendChatMessage(source: BroadChatSource, content: String, chat: Chat) {

        if (!channelMap.containsKey(chat)) {
            warn("Discord channel-chat map doesn't contain a text channel for chat ${chat.name}.")
            return
        }

        jda.getTextChannelById(channelMap[chat]!!).sendMessage(
                EmbedBuilder()
                        .setColor(source.color)
                        .setTitle(chat.name, null)
                        .setAuthor(source.name, null, source.profileImageUrl)
                        .setDescription(content)
                        .setTimestamp(LocalDate.now())
                        .build()).queue()


    }

    data class WebhookMessage(
            val username: String,
            val content: String,
            @SerializedName("avatar_url") val avatarURL: String
    )

    private fun sendWebhookMessage(source: BroadChatSource, content: String) {
        HttpRequest.post(URL(webhook.get())).apply {
            acceptJson()
            contentType("application/json")
            header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")

            send(gson.toJson(WebhookMessage("${source.name} [${source.service.name.toLowerCase().capitalize()}]", content, source.profileImageUrl)))

            val response = body()
            if (response.isNotEmpty()) {
                val responseJson = JSONObject(response)
                if (responseJson["message"] == "You are being rate limited.") {
                    val lock = java.lang.Object()
                    synchronized(lock) {
                        lock.wait(responseJson["retryAfter"] as Long)
                        sendWebhookMessage(source, content)
                    }
                }
            }
        }
    }

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        debug("Sending message: $content to discord")
        if (webhook.isPresent) {
            Thread {
                errorLog("Failed to send webhook message") {
                    sendWebhookMessage(source, content)
                }
            }.apply {
                name = "Webhook message sender"
                isDaemon = false
                start()
            }
            return
        }

        (jda.getTextChannelById(output[messageChannel] ?: run {
            warn("Message channel '$messageChannel' needs an output mapping.")
            return@sendMessage
        }) ?: run {
            warn("Discord text channel with ID '$messageChannel' not found.")
            return@sendMessage
        }).apply {
            when(messageSendType) {
                MessageSendType.EMBED -> {
                    sendMessage(
                            EmbedBuilder()
                                    .setColor(source.color)
                                    .setAuthor(source.name, null, source.profileImageUrl)
                                    .setDescription(content)
                                    .build()).queue()
                }
                MessageSendType.MESSAGE -> {
                    sendMessage(messageFormat[mapOf(
                        "name" to source.name,
                        "service" to source.service.name.toLowerCase().capitalize(),
                        "message" to content
                    )]).queue()
                }
                else -> return
            }
        }

    }

    enum class MessageSendType {
        EMBED, MESSAGE, WEBHOOK
    }

}

private class JDAListener(private val service: DiscordService, private val api: BroadChatAPI): ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel !is TextChannel) return
        if (event.author.isBot) return
        val input = service.input[event.channel.id] ?: return
        DiscordUserTarget(event.member, service).broadchat(event.message.rawContent, api, input)
    }

}

class DiscordUserTarget(val member: Member, discordService: DiscordService) : ExternalBroadChatSource(discordService) {

    override val color: Color
        get() = member.roles.first().color

    override val profileImageUrl: String
        get() = member.user.avatarUrl

    override val name: String
        get() = member.effectiveName

    override val description: Optional<String>
        get() = Optional.of("&bDiscord user.")

}