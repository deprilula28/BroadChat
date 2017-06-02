package me.deprilula28.broadchat

import me.deprilula28.broadchat.services.BungeeService
import me.deprilula28.broadchat.settings.SettingParser
import me.deprilula28.broadchat.settings.yaml
import me.deprilula28.broadchat.util.readText
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.util.logging.Logger

class BroadChatBungee: Plugin() {

    private lateinit var api: BroadChatAPI

    override fun onEnable() {

        console = BungeeConsole(logger)
        api = BroadChatAPI()
        val config = File(dataFolder, "config.yml")
        if (!config.exists()) {
            if (!config.parentFile.exists()) config.parentFile.mkdirs()
            config.createNewFile()
            config.writeText(javaClass.getResourceAsStream("config.yml").readText())
        }
        api.settings = SettingParser(yaml.load(config.readText())!! as Map<String, Any>, api)
        api.targets.add(BungeeService(api, this))
        info("Finished loading.")

    }

    override fun onDisable() {



    }

}

class BungeeConsole(val logger: Logger): Console {

    override fun println(str: String) = logger.info(ChatColor.translateAlternateColorCodes('&', "&3[BroadChat] >> &r$str"))
    override fun printerr(str: String) = logger.severe(ChatColor.translateAlternateColorCodes('&', "&3[BroadChat] >> &e$str"))
    override fun printwarn(str: String) = logger.warning(ChatColor.translateAlternateColorCodes('&', "&3[BroadChat] >> &e$str"))
    override fun printdebug(str: String) = logger.info(ChatColor.translateAlternateColorCodes('&', "&6[BroadChat DEBUG] >> &e$str"))

}