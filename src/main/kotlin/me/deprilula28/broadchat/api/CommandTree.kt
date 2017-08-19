package me.deprilula28.broadchat.api

import me.deprilula28.broadchat.chat.ChatCommand
import me.deprilula28.broadchat.chat.CreateSubCommand
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin

class CommandTree(private val plugin: Plugin) {
    private val commands = mutableListOf<TypeCommand>()
    private val help: String

    init {

        // Command Registering
        cmd(ChatCommand) {
            sub(CreateSubCommand)
        }

        // Help
        val helpBuilder = StringBuilder("&e&m        [&r &aHelp &e&m]       &r\n")
        commands.forEach { subCommand ->
            helpBuilder.append("&a${subCommand.name}&b: ${subCommand.description} &7(${subCommand.usage})&r\n")
        }
        helpBuilder.append("&e&m                        &r")
        help = ChatColor.translateAlternateColorCodes('&', helpBuilder.toString())

    }

    fun cmd(cmd: TypeCommand, func: TypeCommand.() -> Unit = {}) {

        plugin.proxy.pluginManager.registerCommand(plugin, object: Command(cmd.name, "broadchat.${cmd.name}", *(cmd.aliases)) {
            override fun execute(sender: CommandSender, args: Array<out String>) {
                if (sender !is ProxiedPlayer) {
                    sender.sendMessage(TextComponent("Commands can only be executed"))
                    return
                }

                try {
                    cmd.doHandle(sender, args.toList())
                } catch (ex: Exception) {
                    if (ex is ArgsException) {
                        sender.sendMessage(TextComponent("${ChatColor.RED}${ex.message}"))
                        return
                    }

                    sender.sendMessage(TextComponent("${ChatColor.RED}An error occured when attempting to run that command!"))
                    ex.printStackTrace()
                }
            }
        })
        func(cmd)
        cmd.registerHelp()
        
    }

    abstract class TypeCommand(val aliases: Array<String>) {
        val map = mutableMapOf<String, TypeCommand>()
        val commands = mutableListOf<TypeCommand>()
        abstract val name: String
        abstract val description: String
        abstract val usage: String
        private lateinit var help: String

        abstract fun handle(player: ProxiedPlayer, args: Arguments)

        fun doHandle(player: ProxiedPlayer, args: List<String>) {

            if (args.isNotEmpty() && map.containsKey(args.first())) {
                map[args.first()]!!.doHandle(player, args.subList(1, args.size))
                return
            }
            handle(player, Arguments(args))

        }

        fun registerHelp() {

            val helpBuilder = StringBuilder("&e&m        [&r &aHelp &e&m]       &r\n")
            commands.forEach { subCommand ->
                helpBuilder.append("&a${subCommand.name}&b: ${subCommand.description} &7(${subCommand.usage})&r\n")
            }
            helpBuilder.append("&e&m                        &r")
            help = ChatColor.translateAlternateColorCodes('&', helpBuilder.toString())

        }

        fun sub(cmd: TypeCommand, func: TypeCommand.() -> Unit = {}) {

            cmd.aliases.forEach {
                map[it] = cmd
            }
            commands.add(cmd)
            func(cmd)
            cmd.registerHelp()

        }
    }


    class ArgsException(override val message: String): Exception()

    class Arguments(private val args: List<String>) {
        private var curIndex = 0

        operator fun invoke(numb: Int = curIndex, backup: String): String {

            curIndex++
            if (numb >= args.size) return backup
            return args[numb]

        }

        operator fun invoke(numb: Int = curIndex): String {

            curIndex++
            if (numb >= args.size) throw ArgsException("Invalid arguments!")
            return args[numb]

        }
    }
}