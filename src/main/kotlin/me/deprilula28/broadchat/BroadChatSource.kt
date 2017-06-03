package me.deprilula28.broadchat

import me.deprilula28.broadchat.chat.Chat
import java.awt.Color
import java.util.*
import java.util.function.Function

abstract class ExternalBroadChatService(name: String, id: String, hoverMessage: String, val clickURL: String,
                                    specificHoverMessage: Function<BroadChatSource, Optional<String>>,
                                    val specificClickURL: Function<BroadChatSource, Optional<String>>):
        BroadChatService(name, id, hoverMessage, specificHoverMessage)

abstract class BroadChatService(val id: String, val name: String, val hoverMessage: String, val specificHoverMessage: Function<BroadChatSource, Optional<String>>) {
    abstract fun sendMessage(source: BroadChatSource, content: String, messageChannel: String)
    abstract fun sendChatMessage(source: BroadChatSource, content: String, chat: Chat)
}

abstract class ExternalBroadChatSource(val extService: ExternalBroadChatService) : BroadChatSource(extService) {

    fun url(target: BroadChatSource): Optional<String> = extService.specificClickURL.apply(target)

}

abstract class BroadChatSource(val service: BroadChatService) {

    abstract val name: String
    abstract val color: Color
    abstract val description: Optional<String>
    abstract val profileImageUrl: String

    fun broadchat(message: String, api: BroadChatAPI, messageChannel: String) =
            api.sendMessage(this, message, messageChannel)

}
