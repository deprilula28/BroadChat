package me.deprilula28.broadchat

import me.deprilula28.broadchat.chat.Chat
import me.deprilula28.broadchat.settings.SettingParser

class BroadChatAPI {

    private val targets = mutableListOf<BroadChatService>()
    val chats = mutableMapOf<String, Chat>()
    lateinit var settings: SettingParser

    init {

    }

    fun sendMessage(chatSource: BroadChatSource, message: String, channel: String) {

        targets.forEach {
            if (it == chatSource.service) return@forEach
            it.sendMessage(chatSource, message, channel)
        }

    }

    fun sendMessage(chatSource: BroadChatSource, message: String, chat: Chat) {

        targets.forEach {
            if (it == chatSource.service) return@forEach
            it.sendChatMessage(chatSource, message, chat)
        }

    }

    fun addService(service: BroadChatService) {

        info("Registered service '${service.name}'.")
        targets.add(service)

    }

}