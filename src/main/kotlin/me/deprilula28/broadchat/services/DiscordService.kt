package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.*
import me.deprilula28.broadchat.settings.ExternalServiceSettings
import me.deprilula28.broadchat.settings.ServiceSettingsLoader
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.impl.GameImpl
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.utils.SimpleLog
import java.awt.Color
import java.time.LocalDate
import java.util.*
import java.util.function.Function

class DiscordServiceSettingsLoader: ServiceSettingsLoader<DiscordServiceSettings> {

    override fun load(map: Map<*, *>): DiscordServiceSettings =
        DiscordServiceSettings(map["serverIP"] as String, map["inviteID"] as String, (map["token"] as String).toCharArray(),
            map["use"] as Boolean, map["channel-mappings"] as Map<String, Map<String, String>>)

    override fun initService(settings: DiscordServiceSettings, api: BroadChatAPI): ExternalBroadChatService =
        DiscordService(settings.serverIP, settings.inviteID, settings.token, api)

}

class DiscordServiceSettings(val serverIP: String, val inviteID: String, val token: CharArray, use: Boolean, channelMappings:
    Map<String, Map<String, String>>): ExternalServiceSettings(use, channelMappings)

class DiscordService constructor(serverIP: String, invite: String, token: CharArray, api: BroadChatAPI):
    ExternalBroadChatService(
        name = "Discord",
        hoverMessage = "&3Free chat service for gamers alike.",
        clickURL = "http://discord.gg/$invite",
        specificClickURL = Function { Optional.of("http://discord.gg/$invite") },
        specificHoverMessage = Function { Optional.of("&aJoin our services by clicking or going to this link:\n&bdiscord.gg/$invite") }
    ) {

    private val jda: JDA

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

    }

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) =
        (jda.getTextChannelById(messageChannel) ?: jda.guilds.first().publicChannel).sendMessage(
                EmbedBuilder()
                .setColor(source.color())
                .setAuthor(source.name(), source.profileImageURL(), null)
                .setDescription(content)
                .setTimestamp(LocalDate.now())
                .build()).queue()

}

private class JDAListener(private val service: DiscordService, private val api: BroadChatAPI): ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) =
            DiscordUserTarget(event.member, service).broadchat(event.message.rawContent, api, event.channel.id)

}

class DiscordUserTarget(val member: Member, discordService: DiscordService) : ExternalBroadChatSource(member.effectiveName, discordService) {

    override fun color(): Color = member.roles.first().color
    override fun profileImageURL(): String? = member.user.avatarUrl

}