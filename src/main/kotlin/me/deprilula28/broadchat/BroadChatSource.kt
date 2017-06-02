package me.deprilula28.broadchat

import java.awt.Color
import java.util.*
import java.util.function.Function



abstract class ExternalBroadChatService(name: String, hoverMessage: String, val clickURL: String,
                                    specificHoverMessage: Function<BroadChatSource, Optional<String>>,
                                    val specificClickURL: Function<BroadChatSource, Optional<String>>): BroadChatService(name, hoverMessage, specificHoverMessage)

abstract class BroadChatService(val name: String, val hoverMessage: String, val specificHoverMessage: Function<BroadChatSource, Optional<String>>) {
    abstract fun sendMessage(source: BroadChatSource, content: String, messageChannel: String)
}

abstract class ExternalBroadChatSource(val name: String, val extService: ExternalBroadChatService) : BroadChatSource(extService) {

    override fun name(): String = name
    override fun description(target: BroadChatSource): Optional<String> = service.specificHoverMessage.apply(target)
    fun url(target: BroadChatSource): Optional<String> = extService.specificClickURL.apply(target)

    override fun broadchat(message: String, api: BroadChatAPI, messageChannel: String) {

        api.targets.forEach {
            if (it == extService) return@forEach
            it.sendMessage(this@ExternalBroadChatSource, message, messageChannel)
        }

    }

}

abstract class BroadChatSource(val service: BroadChatService) {

    abstract fun name(): String
    abstract fun color(): Color
    abstract fun description(target: BroadChatSource): Optional<String>
    abstract fun profileImageURL(): String?

    abstract fun broadchat(message: String, api: BroadChatAPI, messageChannel: String)

}
