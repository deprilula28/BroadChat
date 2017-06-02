package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.*
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
        ExternalBroadChatService(
                name = "BungeeCord",
                hoverMessage = "&aChat for the network.",
                clickURL = "http://minecraft.net",
                specificClickURL = Function { Optional.empty() },
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

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        errorLog("Failed to handle message") {
            if (messageChannel == "*") {
                proxy.broadcast(api.settings["message-format-external"][mapOf(
                        "name" to source.name(),
                        "service" to source.service.name,
                        "message" to content
                )])
            } else {
                proxy.servers[messageChannel]!!.players.forEach {
                    it.sendMessage(api.settings["message-format-external"][mapOf(
                            "name" to source.name(),
                            "service" to source.service.name,
                            "message" to content
                    )])
                }
            }
        }

    }

}

class BungeePlayerTarget(val player: ProxiedPlayer, bungeeService: BungeeService) : ExternalBroadChatSource(player.name, bungeeService) {

    override fun color(): Color {

        if (player.displayName.startsWith("§")) {
            val color = ChatColor.getByChar(player.displayName.substring(1, 2).toCharArray().first())
            if (color == null) return Color.WHITE
            else return color.toAWT()
        }
        return Color.WHITE

    }
    override fun profileImageURL(): String? = "https://crafatar.com/avatars/${player.uniqueId}"

}