package me.deprilula28.broadchat.chat

import me.deprilula28.broadchat.CommandTree
import me.deprilula28.broadchat.services.BungeePlayerTarget
import me.deprilula28.broadchat.services.BungeeService
import me.deprilula28.broadchat.util.RandomStringGenerator
import me.deprilula28.broadchat.util.api
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object ChatCommand: CommandTree.TypeCommand(arrayOf("chat", "broadchat")) {

    override val name = "Chat"
    override val description = "Manage chat"
    override val usage = "/chat"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        player.sendMessage(TextComponent(help))

    }

}

object CreateSubCommand: CommandTree.TypeCommand(arrayOf("create", "make", "new")) {

    override val name = "Create"
    override val description = "Create a player chat"
    override val usage = "/chat create <name> [password]"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        val name = args()
        val password = args(backup = "")

        // Checking chat counts
        val joinedCount = api.playerChats[player.uniqueId]?.size ?: 0
        val chatCount = api.playerChats[player.uniqueId]?.filter { it is PlayerChat && it.creator is BungeePlayerTarget && it.creator.player == player }?.size ?: 0
        val chatCountCap = api.settings["chat-count-cap"].value as Int
        val chatJoinedCap = api.settings["chat-joined-cap"].value as Int

        if (chatCountCap >= 0 && chatCount + 1 > chatCountCap && !player.hasPermission("broadchat.chat.bypassCreatedCap")) {
            throw CommandTree.ArgsException("You have created too many chats!")
        }
        if (chatJoinedCap >= 0 && joinedCount + 1 > chatJoinedCap && !player.hasPermission("broadchat.chat.bypassJoinedCap")) {
            throw CommandTree.ArgsException("You have joined too many chats!")
        }

        // Creating the chat
        val source = BungeePlayerTarget(player, api.minecraftService as BungeeService)
        val chat = PlayerChat(source, mutableListOf(source), mutableMapOf(), RandomStringGenerator.next(ThreadLocalRandom.current()), name, password,
                ChatColor.WHITE)

        api.chats[name] = chat
        api.playerChats[player.uniqueId] = (api.playerChats[player.uniqueId] ?: mutableListOf()).apply { add(chat) }

        player.sendMessage(TextComponent("${ChatColor.GREEN}Your chat was created!"))
    }

}

object PermissionSubCommand: CommandTree.TypeCommand(arrayOf("permissions", "permission", "perms", "perm")) {

    override val name = "Create"
    override val description = "Create a player chat"
    override val usage = "/chat create <name> [password]"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        player.sendMessage(TextComponent(ChatCommand.help))

    }

}

object PermissionCommandUtility {
    fun extract(info: List<String>): Triple<List<String>, ChatColor?, String?> {
        val regularInfo = mutableListOf<String>()
        var color: ChatColor? = null
        var tag: String? = null

        info.forEach {
            if (it.startsWith("color=")) {
                color = ChatColor.valueOf(it.substring("color=".length).toUpperCase())
            } else if (it.startsWith("tag=")) {
                tag = it.substring("tag=".length)
            } else regularInfo.add(it)
        }

        return Triple(regularInfo, color, tag)
    }
}

object PermissionsCreateRoleSubCommand: CommandTree.TypeCommand(arrayOf("createrole", "makerole", "creategroup", "makegroup", "roleadd", "groupadd")) {

    override val name = "CreateRole"
    override val description = "Create and add a new group to the chat"
    override val usage = "/chat permission createrole <chat> <name> <...permission OR color=<color> OR tag=<tag>>"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        val chat = api.chats[args()] ?: throw CommandTree.ArgsException("That chat doesn't exist!")
        val name = args()
        val (permissions, color, tag) = PermissionCommandUtility.extract(args.vararg)
        val role = ChatPermissionGroup(name, permissions.map { Permission.valueOf(it.toUpperCase()) }.toMutableList(), color ?: ChatColor.WHITE, Optional.ofNullable(tag))

        chat.groups[name] = role

    }

}

object PermissionsEditRoleSubCommand: CommandTree.TypeCommand(arrayOf("editrole", "editgroup")) {

    override val name = "EditRole"
    override val description = "Configure permissions for a role"
    override val usage = "/chat permission editrole <chat> <name> <...+/- permission OR color=<color> OR tag=<tag>>"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        val chat = api.chats[args()] ?: throw CommandTree.ArgsException("That chat doesn't exist!")
        val name = args()
        val (permissions, color, tag) = PermissionCommandUtility.extract(args.vararg)

        val role = chat.groups[name] ?: throw CommandTree.ArgsException("That role doesn't exist!")
        permissions.forEach {
            val perm = Permission.valueOf(it.substring(1))
            if (it.startsWith("+")) role.perms.add(perm)
            else if (it.startsWith("-")) role.perms.remove(perm)
        }
        if (color != null) role.color = color
        if (tag != null) role.tag = Optional.of(tag)

    }

}

object PermissionsEditPlayerRolesSubCommand: CommandTree.TypeCommand(arrayOf("editplayerroles", "editplayergroups", "playerroles", "playergroups")) {

    override val name = "PlayerRoles"
    override val description = "Set a player's role"
    override val usage = "/chat permission playerroles <chat> <player> [role]"

    override fun handle(player: ProxiedPlayer, args: CommandTree.Arguments) {

        val chat = api.chats[args()] ?: throw CommandTree.ArgsException("That chat doesn't exist!")
        val target = (api.minecraftService as BungeeService).bungeeCordPlugin.proxy.getPlayer(args()) ?: throw CommandTree.ArgsException("Player not found!")
        val fullTarget = BungeePlayerTarget(target, api.minecraftService as BungeeService)
        val role = args(backup = "remove")
        if (role == "remove") chat.permissions.remove(fullTarget)
        else chat.permissions[fullTarget] = chat.groups[role] ?: throw CommandTree.ArgsException("Role '$role' doesn't exist!")
    }

}