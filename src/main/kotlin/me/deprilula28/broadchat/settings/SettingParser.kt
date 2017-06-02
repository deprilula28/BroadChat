package me.deprilula28.broadchat.settings

import me.deprilula28.broadchat.BroadChatAPI
import me.deprilula28.broadchat.ExternalBroadChatService
import me.deprilula28.broadchat.services.DiscordServiceSettingsLoader
import me.deprilula28.broadchat.util.colored
import me.deprilula28.broadchat.util.errorLog
import org.yaml.snakeyaml.Yaml

val yaml = Yaml()
val serviceSettingsMap = mapOf(
        "discord" to DiscordServiceSettingsLoader()
)

interface ServiceSettingsLoader<T: ExternalServiceSettings> {

    fun load(map: Map<*, *>): T
    fun initService(settings: T, api: BroadChatAPI): ExternalBroadChatService

}

open class ExternalServiceSettings(val use: Boolean, val channelMappings: Map<String, Map<String, String>>)

class SettingParser(private val map: Map<String, Any>, api: BroadChatAPI) {

    init {

        map.forEach { k, v ->
            if (v is Map<*, *> && serviceSettingsMap.containsKey(k)) {
                serviceSettingsMap[k]!!.apply {
                    val serviceSettings = load(v)
                    if (serviceSettings.use) {
                        println("Initializing service $k...")

                        errorLog("Failed to load service $k") {
                            val service = initService(serviceSettings, api)
                            api.targets.add(service)
                        }
                    }
                }
            }
        }

    }

    operator fun get(name: String): SettingValue {

        val value = map[name]
        return SettingValue(value!!)

    }

}

class SettingValue(val value: Any) {

    operator fun get(arguments: Map<String, String>): String {

        if (value !is String) return value.toString()

        val returnValue = value
        arguments.forEach { k, v -> returnValue.replace("_%${k.toUpperCase()}%_", v.colored()) }

        return returnValue

    }

}