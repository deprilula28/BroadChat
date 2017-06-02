package me.deprilula28.broadchat

import com.coalesce.plugin.CoLogger
import com.coalesce.plugin.CoPlugin
import me.deprilula28.broadchat.services.BukkitService
import me.deprilula28.broadchat.settings.SettingParser
import me.deprilula28.broadchat.settings.yaml
import me.deprilula28.broadchat.util.readText
import java.io.File

class BroadChatSpigot: CoPlugin() {

    private lateinit var api: BroadChatAPI

    override fun onPluginEnable() {

        updateCheck("deprilula28", "BroadChat", true)

        console = SpigotConsole(coLogger)
        api = BroadChatAPI()
        val config = File(dataFolder, "config.yml")
        if (!config.exists()) {
            if (!config.parentFile.exists()) config.parentFile.mkdirs()
            config.createNewFile()
            config.writeText(javaClass.getResourceAsStream("/config.yml").readText())
        }
        api.settings = SettingParser(yaml.load(config.readText())!! as Map<String, Any>, api)
        api.targets.add(BukkitService(api, this))
        info("Finished loading.")

    }

    override fun onPluginDisable() {

    }

}

class SpigotConsole(val logger: CoLogger): Console {

    override fun println(str: String) = logger.info(str)
    override fun printerr(str: String) = logger.error(str)
    override fun printwarn(str: String) = logger.warn(str)
    override fun printdebug(str: String) = logger.debug(str)

}