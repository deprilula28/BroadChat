package me.deprilula28.broadchat.chat

import me.deprilula28.broadchat.api.BroadChatSource
import me.deprilula28.broadchat.util.ccDataFolder
import me.deprilula28.broadchat.util.debug
import me.deprilula28.broadchat.util.api
import me.deprilula28.broadchat.util.gson
import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.io.File

class PlayerChat(val creator: BroadChatSource,
                 members: MutableList<BroadChatSource>,
                 permissions: MutableMap<BroadChatSource, ChatPermissionGroup>,
                 id: String,
                 name: String = "",
                 color: ChatColor):
        Chat(members, permissions, id, name, color)

open class Chat(val members: MutableList<BroadChatSource>,
                val permissions: MutableMap<BroadChatSource, ChatPermissionGroup>,
                val id: String,
                private var innerName: String,
                private var innerColor: ChatColor) {

    val name: String
        get() = innerName
    val color: ChatColor
        get() = innerColor

    fun sendToAll(source: BroadChatSource, message: String) = api.sendMessage(source, message, this)

    fun save() {

        debug("Saving chat $id...")
        if (!ccDataFolder.exists()) ccDataFolder.mkdirs()
        val file = File(ccDataFolder, "$id.json")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.writeText(gson.toJson(this))
        debug("Done.")

    }


}

data class ChatPermissionGroup(val name: String, val perms: List<Permission>, val color: Color, val tag: String)
enum class Permission(val permName: String, val descripton: String) {

    SPEAK("speak", "Allows you to talk"),
    KICK("kick", "Allows you to kick chat members"),
    SOFT_MUTE("softmute", "Allows you to mute other members for up to 14 days"),
    MUTE("mute", "Allows you to mute other members permanently"),
    SOFT_BAN("softban", "Allows you to ban other members (kick + not allowing to join) for up to 7 days"),
    BAN("ban", "Allows you to ban other members (kick + not allowing to join) permanently"),
    MANAGE_PERMISSIONS("permmanage", "Grants access to managing group permissions"),
    MANAGE_CHAT("chatadmin", "Grants access to managing chat information such as name")

}