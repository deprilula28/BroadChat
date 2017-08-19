package me.deprilula28.broadchat.chat

import me.deprilula28.broadchat.api.CommandTree
import net.md_5.bungee.api.connection.ProxiedPlayer

object ChatCommand: CommandTree.TypeCommand(arrayOf("chat", "broadchat")) {

    override val name = "Chat"
    override val description = "Manage chat"
    override val usage = "/chat"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        

    }

}

object CreateSubCommand: CommandTree.TypeCommand(arrayOf("create", "make", "new")) {

    override val name = "Create"
    override val description = "Create a player chat"
    override val usage = "/chat create <name> [password]"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        val name = args()
        val password = args(backup = "")

        // TODO chat amount checks


    }

}