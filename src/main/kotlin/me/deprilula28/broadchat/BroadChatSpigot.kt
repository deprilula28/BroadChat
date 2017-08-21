package me.deprilula28.broadchat

import me.deprilula28.broadchat.api.BroadChatAPI
import me.deprilula28.broadchat.services.BukkitService
import me.deprilula28.broadchat.settings.SettingParser
import me.deprilula28.broadchat.settings.yaml
import me.deprilula28.broadchat.util.*
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

class BroadChatSpigot: JavaPlugin() {

    override fun onEnable() {

        runningSpigot = true
        ccDataFolder = dataFolder
        console = SpigotConsole(logger)
        api = BroadChatAPI()
        val config = File(dataFolder, "config.yml")
        if (!config.exists()) {
            if (!config.parentFile.exists()) config.parentFile.mkdirs()
            config.createNewFile()
            config.writeText(javaClass.getResourceAsStream("/config.yml").readText())
        }
        api.settings = SettingParser(yaml.load(config.readText())!! as Map<String, Any>, api)
        val service = BukkitService(api, this)
        api.addService(service)
        api.minecraftService = service
        info("Finished loading.")

    }

    override fun onDisable() {

        info("Disabling BroadChat...")
        api.unloadServices()

    }

}

class SpigotConsole(val logger: Logger): Console {

    override fun println(str: String) = logger.info(str)
    override fun printerr(str: String) = logger.info("§cError: $str")
    override fun printwarn(str: String) = logger.info("§6Warning: $str")
    override fun printdebug(str: String) = logger.info("§3Debug: $str")

}