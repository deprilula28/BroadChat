package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.api.BroadChatAPI
import me.deprilula28.broadchat.api.BroadChatService
import me.deprilula28.broadchat.api.BroadChatSource
import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.util.findChatColor
import me.deprilula28.broadchat.util.toAWT
import me.deprilula28.broadchat.util.warn
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin
import java.awt.Color
import java.util.*
import java.util.function.Function

class BukkitService(private val api: BroadChatAPI, plugin: Plugin):
        BroadChatService(
                name = "Minecraft",
                id = "spigot",
                hoverMessage = "&aChat for the server.",
                specificHoverMessage = Function { Optional.empty() }
        ), Listener {

    init {

        Bukkit.getPluginManager().registerEvents(this, plugin)

    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) =
        BukkitPlayerTarget(event.player, this).broadchat(event.message, api, event.player.world.name)

    override fun sendChatMessage(source: BroadChatSource, content: String, chat: Chat) =
        warn("Chats shouldn't work on Spigot...")

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) {

        val msg = api.settings["message-format-external"][mapOf(
                "name" to source.name,
                "service" to source.service.name.toLowerCase().capitalize(),
                "message" to content
        )]
        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage(msg)
        }

    }

}

class BukkitPlayerTarget(val player: Player, bukkitService: BukkitService) : BroadChatSource(bukkitService) {

    override val name: String
        get() = player.name

    override val color: Color
        get() {

            if (player.displayName.startsWith("§")) {
                val color = player.displayName.substring(1, 2).toCharArray().first().findChatColor()
                if (color == null) return Color.WHITE
                else return color.toAWT()
            }
            return Color.WHITE

        }

    override val description: Optional<String>
        get() = Optional.of("&ePlayer on the server.")

    override val profileImageUrl: String
        get() = "https://crafatar.com/avatars/${player.uniqueId}"

}