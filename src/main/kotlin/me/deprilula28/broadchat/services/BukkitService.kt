package me.deprilula28.broadchat.services

import me.deprilula28.broadchat.BroadChatAPI
import me.deprilula28.broadchat.BroadChatSource
import me.deprilula28.broadchat.ExternalBroadChatService
import me.deprilula28.broadchat.ExternalBroadChatSource
import me.deprilula28.broadchat.util.colored
import me.deprilula28.broadchat.util.findChatColor
import me.deprilula28.broadchat.util.toAWT
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin
import sun.audio.AudioPlayer.player
import java.awt.Color
import java.util.*
import java.util.function.Function

class BukkitService(private val api: BroadChatAPI, plugin: Plugin):
        ExternalBroadChatService(
                name = "Minecraft",
                hoverMessage = "&aChat for the server.",
                clickURL = "http://minecraft.net",
                specificClickURL = Function { Optional.empty() },
                specificHoverMessage = Function { Optional.empty() }
        ), Listener {

    init {

        Bukkit.getPluginManager().registerEvents(this, plugin)

    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) =
        BukkitPlayerTarget(event.player, this).broadchat(event.message, api, "")

    //"&r<${source.name()}&r> ".colored() + content)

    override fun sendMessage(source: BroadChatSource, content: String, messageChannel: String) =
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(api.settings["message-format-external"][mapOf(
                "name" to source.name(),
                "service" to source.service.name,
                "message" to content
        )]) }

}

class BukkitPlayerTarget(val player: Player, bukkitService: BukkitService) : ExternalBroadChatSource(player.name, bukkitService) {

    override fun color(): Color {

        if (player.displayName.startsWith("§")) {
            val color = player.displayName.substring(1, 2).toCharArray().first().findChatColor()
            if (color == null) return Color.WHITE
            else return color.toAWT()
        }
        return Color.WHITE

    }
    override fun profileImageURL(): String? = "https://crafatar.com/avatars/${player.uniqueId}"

}