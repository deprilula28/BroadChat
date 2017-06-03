package me.deprilula28.broadchat.chat

import me.deprilula28.broadchat.BroadChatSource
import me.deprilula28.broadchat.ccDataFolder
import me.deprilula28.broadchat.debug
import me.deprilula28.broadchat.util.api
import me.deprilula28.broadchat.util.gson
import java.io.File

class Chat(val creator: BroadChatSource, val members: MutableList<BroadChatSource>, val permissions: MutableMap<BroadChatSource,
        ChatPermissionGroup>, val id: String, var name: String = "") {

    fun sendToAll(source: BroadChatSource, message: String) = members.forEach { api.sendMessage(source, message, id) }

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

data class ChatPermissionGroup(val name: String, val perms: List<String>)