package me.deprilula28.broadchat.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

internal val subcommands = arrayOf(
    CreateSubCommand()
)

class ChatCommand: Command("chat", "broadchat.playerchat", "/chat help", "/chat create <name>", "/chat join <name|id> [password]",
        "/chat <name> - Select a chat to talk on") {

    private val chatMap = mutableMapOf<String, ChatSubCommand>()
    private val help: String

    init {

        val helpBuilder = StringBuilder("&e&m        [&r &aHelp &e&m]       &r\n")
        subcommands.forEach { subCommand ->
            subCommand.aliases.forEach {
                chatMap[it] = subCommand
            }
            helpBuilder.append("&a${subCommand.name}&b: ${subCommand.description} &7(${subCommand.usage})&r\n")
        }
        helpBuilder.append("&e&m                        &r")
        help = ChatColor.translateAlternateColorCodes('&', helpBuilder.toString())

    }

    override fun execute(sender: CommandSender, args: Array<String>) {

        if (args.isEmpty() || !chatMap.contains(args.first())) {
            sender.sendMessage(TextComponent(help))
            return
        }
        if (sender !is ProxiedPlayer) {
           sender.sendMessage(TextComponent("Command not supported for non-players."))
            return
        }

    }

}