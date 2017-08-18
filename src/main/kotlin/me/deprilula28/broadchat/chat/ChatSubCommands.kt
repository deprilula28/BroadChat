package me.deprilula28.broadchat.chat

import net.md_5.bungee.api.connection.ProxiedPlayer

interface ChatSubCommand {

    val name: String
    val description: String
    val usage: String
    val aliases: List<String>

    fun exec(player: ProxiedPlayer, args: Array<String>)

}

class CreateSubCommand: ChatSubCommand {

    override val name = "Create"
    override val description = "Create a player chat"
    override val usage = "/chat create <name> [password]"
    override val aliases = listOf("create", "make", "new")

    override fun exec(player: ProxiedPlayer, args: Array<String>) {



    }

}