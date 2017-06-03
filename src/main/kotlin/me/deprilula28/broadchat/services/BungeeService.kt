package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.*
import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.util.errorLog
import me.deprilula28.broadchat.util.toAWT
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.awt.Color
import java.util.*
import java.util.function.Function

class BungeeService(private val api: BroadChatAPI, bungeeCordPlugin: Plugin):
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

        if (event.isCommand || event.sender !is ProxiedPlayer || event.receiver !is ProxyServer) return
        BungeePlayerTarget(event.sender as ProxiedPlayer, this).broadchat(event.message, api, (event.receiver as ProxyServer).name)

    }

    override fun sendChatMessage(source: BroadChatSource, content: String, chat: Chat) {

        errorLog("Failed to handle chat message") {

        }

    }

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        errorLog("Failed to handle message") {
            if (messageChannel == "*") {
                proxy.broadcast(api.settings["message-format-external"][mapOf(
                        "name" to source.name,
                        "service" to source.service.name,
                        "message" to content
                )])
            } else if (proxy.servers.contains(messageChannel)) {
                proxy.servers[messageChannel]!!.players.forEach {
                    it.sendMessage(api.settings["message-format-external"][mapOf(
                            "name" to source.name,
                            "service" to source.service.name,
                            "message" to content
                    )])
                }
            } else {
                warn("Requested proxy not found: $messageChannel")
            }
        }

    }

}

class BungeePlayerTarget(val player: ProxiedPlayer, bungeeService: BungeeService) : BroadChatSource(bungeeService) {

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