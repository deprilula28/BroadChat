package me.deprilula28.broadchat

import me.deprilula28.broadchat.settings.SettingParser

class BroadChatAPI {

    val targets = mutableListOf<BroadChatService>()
    lateinit var settings: SettingParser

    init {

    }

}