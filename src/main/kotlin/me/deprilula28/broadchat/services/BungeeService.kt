package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.api.BroadChatAPI
import me.deprilula28.broadchat.api.BroadChatService
import me.deprilula28.broadchat.api.BroadChatSource
import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.util.debug
import me.deprilula28.broadchat.util.errorLog
import me.deprilula28.broadchat.util.toAWT
import me.deprilula28.broadchat.util.warn
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.awt.Color
import java.util.*
import java.util.function.Function

class BungeeService(private val api: BroadChatAPI, val bungeeCordPlugin: Plugin):
        BroadChatService(
                name = "Minecraft",
                id = "bungee",
                hoverMessage = "&aChat for the network.",
                specificHoverMessage = Function { Optional.empty() }
        ), Listener {

    private val proxy = bungeeCordPlugin.proxy

    init {

        proxy.pluginManager.registerListener(bungeeCordPlugin, this)

    }

    @EventHandler
    fun onChat(event: ChatEvent) {

        val sender = event.sender
        if (event.isCommand || sender !is ProxiedPlayer) return
        BungeePlayerTarget(sender, this).broadchat(event.message, api, sender.server.info.name)

    }

    override fun sendChatMessage(source: BroadChatSource, content: String, chat: Chat) {

        errorLog("Failed to handle chat message") {
            chat.members.forEach {
                if (it is BungeePlayerTarget) {
                    it.player.sendMessage(TextComponent(api.settings["message-format-chat"][mapOf(
                            "chat_color" to chat.color.toString(),
                            "chat_name" to chat.name,
                            "name" to it.name,
                            "message" to content
                    )]))
                }
            }
        }

    }

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        var finalContent = content
        if (finalContent.length > 100) finalContent = finalContent.substring(0 .. 100) + "..."
        if (finalContent.isEmpty()) return

        debug("Sending message: $content to bungeecord")
        errorLog("Failed to handle message") {
            if (messageChannel == "*") {
                val msg = api.settings["message-format-external"][mapOf(
                        "name" to source.name,
                        "service" to source.service.name.toLowerCase().capitalize(),
                        "message" to finalContent
                )]
                proxy.players.forEach {
                    it.sendMessage(TextComponent(msg))
                }
            } else if (proxy.servers.contains(messageChannel)) {
                val msg = api.settings["message-format-external"][mapOf(
                        "name" to source.name,
                        "service" to source.service.name.toLowerCase().capitalize(),
                        "message" to finalContent
                )]
                proxy.servers[messageChannel]!!.players.forEach {
                    it.sendMessage(TextComponent(msg))
                }
            } else {
                warn("Requested proxy not found: $messageChannel")
            }
        }

    }

}

class BungeePlayerTarget(val player: ProxiedPlayer, bungeeService: BungeeService): BroadChatSource(bungeeService) {

    override val name: String
        get() = player.displayName

    override val description: Optional<String>
        get() = Optional.of("&ePlayer on the network.")

    override val color: Color
        get() {
            if (player.displayName.startsWith("§")) {
                val color = ChatColor.getByChar(player.displayName.substring(1, 2).toCharArray().first())
                if (color == null) return Color.WHITE
                else return color.toAWT()
            }
            return Color.WHITE
        }

    override val profileImageUrl: String
        get() = "https://crafatar.com/avatars/${player.uniqueId}"

}