package me.deprilula28.broadchat

import net.dv8tion.jda.core.utils.SimpleLog

interface Console {

    fun println(str: String)
    fun printerr(str: String)
    fun printwarn(str: String)
    fun printdebug(str: String)

}

private var consoleNullable: Console? = null
internal var console: Console
    get() = consoleNullable!!
    set(console) { consoleNullable = console }

internal fun info(str: String) = console.println(str)
internal fun err(str: String) = console.printerr(str)
internal fun warn(str: String) = console.printwarn(str)
internal fun debug(str: String) = console.printdebug(str)

class Logger: SimpleLog.LogListener {

    override fun onLog(log: SimpleLog, level: SimpleLog.Level, p2: Any) {
        if (p2 is String) {
            if (level.isError) err("§r[§fJDA §cError§r] §f$p2")
            else if (level == SimpleLog.Level.INFO) info("§r[§fJDA§r] §r$p2")
        }
    }

    override fun onError(log: SimpleLog, err: Throwable) { }

}