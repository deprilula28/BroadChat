package me.deprilula28.broadchat.util

import java.io.File

interface Console {

    fun println(str: String)
    fun printerr(str: String)
    fun printwarn(str: String)
    fun printdebug(str: String)

}

var runningSpigot = false

private var consoleNullable: Console? = null
internal var console: Console
    get() = consoleNullable!!
    set(console) { consoleNullable = console }

private var dataFolderNullable: File? = null
internal var ccDataFolder: File
    get() = dataFolderNullable!!
    set(folderData) { dataFolderNullable = folderData }

internal fun info(str: String) = console.println(str)
internal fun err(str: String) = console.printerr(str)
internal fun warn(str: String) = console.printwarn(str)
internal fun debug(str: String) = console.printdebug(str)

interface Permission {



}