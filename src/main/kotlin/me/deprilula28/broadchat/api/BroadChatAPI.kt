package me.deprilula28.broadchat.api

import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.util.info
import java.util.*

class BroadChatAPI {

    private val services = mutableListOf<BroadChatService>()
    val chats = mutableMapOf<String, Chat>()
    val playerChats = mutableMapOf<UUID, List<Chat>>()
    lateinit var settings: me.deprilula28.broadchat.settings.SettingParser

    init {
    }

    internal fun sendMessage(chatSource: BroadChatSource, message: String, channel: String) {

        services.forEach {
            if (it == chatSource.service) return@forEach
            it.sendMessage(chatSource, message, channel)
        }

    }

    internal fun sendMessage(chatSource: BroadChatSource, message: String, chat: Chat) {

        services.forEach {
            if (it == chatSource.service) return@forEach
            it.sendChatMessage(chatSource, message, chat)
        }

    }

    internal fun addService(service: BroadChatService) {

        services.add(service)
        info("Registered service '${service.name}'.")

    }

    internal fun unloadServices() {

        services.forEach {
            info("Unregistering ${it.name}...")
            it.disable()
        }
        services.clear()

        System.gc()
        info("Unregistered services.")

    }

}