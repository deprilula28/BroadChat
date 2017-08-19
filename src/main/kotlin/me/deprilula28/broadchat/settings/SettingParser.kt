package me.deprilula28.broadchat.settings

import me.deprilula28.broadchat.api.BroadChatAPI
import me.deprilula28.broadchat.api.ExternalBroadChatService
import me.deprilula28.broadchat.services.DiscordServiceSettingsLoader
import me.deprilula28.broadchat.util.errorLog
import me.deprilula28.broadchat.util.info
import me.deprilula28.broadchat.util.runningSpigot
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
                        info("Loading service '$k'...")

                        errorLog("Failed to load service '$k'") {
                            val service = initService(serviceSettings, api)
                            api.addService(service)
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

        var returnValue = value.toString()

        fun color(m: String) = if (runningSpigot) me.deprilula28.broadchat.util.BukkitUtils.colored(m) else me.deprilula28.broadchat.util.BungeeUtils.coloredBng(m)
        returnValue = color(returnValue)
        arguments.forEach { k, v -> returnValue = returnValue.replace("_%${k.toUpperCase()}%_", v) }

        return color(returnValue)

    }

}

class ChannelMappings(private val mappings: Map<String, Map<String, String>>) {

    operator fun get(string: String): String? {

        val tag = if (runningSpigot) "spigot" else "bungee"
        val map = mappings[tag]!!

        if (map.containsKey(string)) {
            return map[string]
        } else if (map.containsKey("*")) {
            return map["*"]
        }

        return null

    }

}