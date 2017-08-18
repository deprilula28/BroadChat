package me.deprilula28.broadchat.util

import org.bukkit.ChatColor

private val colorMap = mapOf(
        ChatColor.BLACK to AWTColor(0x000000),
        ChatColor.DARK_BLUE to AWTColor(0x00002A),
        ChatColor.DARK_GREEN to AWTColor(0x002A00),
        ChatColor.DARK_AQUA to AWTColor(0x002A2A),
        ChatColor.DARK_RED to AWTColor(0x2A0000),
        ChatColor.DARK_PURPLE to AWTColor(0x2A002A),
        ChatColor.GOLD to AWTColor(0x2A2A00),
        ChatColor.GRAY to AWTColor(0x151515),
        ChatColor.DARK_GRAY to AWTColor(0x15153F),
        ChatColor.BLUE to AWTColor(0x153F15),
        ChatColor.AQUA to AWTColor(0x153F3F),
        ChatColor.RED to AWTColor(0x3F1515),
        ChatColor.LIGHT_PURPLE to AWTColor(0x3F153F),
        ChatColor.YELLOW to AWTColor(0x3F3F15),
        ChatColor.WHITE to AWTColor(0x3F3F3F)
)

fun Char.findChatColor(): ChatColor? {
    ChatColor.values().forEach { if (it.char == this) return@findChatColor it }
    return null
}

object BukkitUtils {
    fun colored(str: String): String = ChatColor.translateAlternateColorCodes('&', str)
}
fun ChatColor.toAWT(): AWTColor = colorMap[this]!!