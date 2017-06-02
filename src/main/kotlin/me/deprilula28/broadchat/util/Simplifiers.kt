package me.deprilula28.broadchat.util

import me.deprilula28.broadchat.err
import java.io.InputStream
import java.util.*

typealias AWTColor = java.awt.Color

fun quietly(func: () -> Unit) {
    try {
        func()
    } catch (ex: Exception) { /* IGNORE */ }
}

fun errorLog(message: String, func: () -> Unit) =
        try {
            func()
        } catch (ex: Exception) {
            err("$message:")
            ex.printStackTrace()
        }

fun InputStream.readText(): String {

    val scanner = Scanner(this)
    val lines = mutableListOf<String>()
    while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine())
    }

    quietly { close() }
    return lines.joinToString(separator = "\n")

}