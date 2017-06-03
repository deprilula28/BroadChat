package me.deprilula28.broadchat.services

import com.google.gson.reflect.TypeToken
import me.deprilula28.broadchat.*
import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.settings.ChannelMappings
import me.deprilula28.broadchat.settings.ExternalServiceSettings
import me.deprilula28.broadchat.settings.ServiceSettingsLoader
import me.deprilula28.broadchat.util.api
import me.deprilula28.broadchat.util.gson
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.impl.GameImpl
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.utils.SimpleLog
import java.awt.Color
import java.io.File
import java.time.LocalDate
import java.util.*
import java.util.function.Function


class DiscordServiceSettingsLoader: ServiceSettingsLoader<DiscordServiceSettings> {

    override fun load(map: Map<*, *>): DiscordServiceSettings =
        DiscordServiceSettings(map["serverIP"] as String, map["inviteID"] as String, (map["token"] as String).toCharArray(),
            map["use"] as Boolean, map["channel-mappings"] as Map<String, Map<String, String>>)

    override fun initService(settings: DiscordServiceSettings, api: BroadChatAPI): ExternalBroadChatService =
        DiscordService(settings.serverIP, settings.inviteID, settings.token, api, ChannelMappings(settings.channelMappings))

}

class DiscordServiceSettings(val serverIP: String, val inviteID: String, val token: CharArray, use: Boolean, channelMappings:
    Map<String, Map<String, String>>): ExternalServiceSettings(use, channelMappings)

class DiscordService constructor(serverIP: String, invite: String, token: CharArray, api: BroadChatAPI,
             private val channelMappings: ChannelMappings):
    ExternalBroadChatService(
        name = "Discord",
        id = "discord",
        hoverMessage = "&3Free chat service for gamers alike.",
        clickURL = "http://discord.gg/$invite",
        specificClickURL = Function { Optional.of("http://discord.gg/$invite") },
        specificHoverMessage = Function { Optional.of("&aJoin our services by clicking or going to this link:\n&bdiscord.gg/$invite") }
    ) {

    private val jda: JDA
    private val channelMap: MutableMap<Chat, Long>

    init {

        info("Connecting to Discord servers...")
        SimpleLog.LEVEL = SimpleLog.Level.FATAL
        SimpleLog.addListener(Logger())
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

        if (ccDataFolder.exists() && File(ccDataFolder, "discordChatChannels.json").exists()) {
            val type = object: TypeToken<MutableMap<String, Long>>() {}
            channelMap = gson.fromJson(File(ccDataFolder, "discordChatChannels.json").readText(), type.type)
        } else channelMap = mutableMapOf()

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
                        .setAuthor(source.name, source.profileImageUrl, null)
                        .setDescription(content)
                        .setTimestamp(LocalDate.now())
                        .build()).queue()


    }

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        (jda.getTextChannelById(channelMappings[messageChannel] ?: return) ?: run {
            warn("Discord text channel with ID '$messageChannel' not found.")
            return@sendMessage
        }).sendMessage(
                EmbedBuilder()
                    .setColor(source.color)
                    .setAuthor(source.name, source.profileImageUrl, null)
                    .setDescription(content)
                    .setTimestamp(LocalDate.now())
                    .build()).queue()

    }

}

class Logger: SimpleLog.LogListener {

    override fun onLog(log: SimpleLog, level: SimpleLog.Level, p2: Any) {
        if (p2 is String) {
            if (level.isError) err("§r[§3JDA §cError§r] §f$p2")
            else if (level == SimpleLog.Level.INFO) info("§r[§3Discord§r] §r$p2")
        }
    }

    override fun onError(log: SimpleLog, err: Throwable) { }

}

private class JDAListener(private val service: DiscordService, private val api: BroadChatAPI): ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) =
            DiscordUserTarget(event.member, service).broadchat(event.message.rawContent, api, event.channel.id)

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