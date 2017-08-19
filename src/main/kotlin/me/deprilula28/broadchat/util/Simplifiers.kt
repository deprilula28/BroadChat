package me.deprilula28.broadchat.util

import com.google.gson.GsonBuilder
import me.deprilula28.broadchat.api.BroadChatAPI
import java.io.InputStream
import java.math.BigDecimal
import java.util.*

typealias AWTColor = java.awt.Color

private var apiNullable: BroadChatAPI? = null
internal var api: BroadChatAPI
    get() = apiNullable!!
    set(api) {
        apiNullable = api
    }

val gson = GsonBuilder().apply {
    setPrettyPrinting()
    disableHtmlEscaping()
}.create()!!

fun quietly(func: () -> Unit) {
    try {
        func()
    } catch (ex: Exception) { /* IGNORE */
    }
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

fun Double.round(decimals: Int = 0, roundingMode: Int = BigDecimal.ROUND_HALF_UP) = BigDecimal(this).setScale(decimals, roundingMode).toString()

fun Long.bytes(): String {
    val sizes = arrayOf("B", "KB", "MB", "GB", "TB")
    val weightIncrease = 1024

    sizes.forEachIndexed { index, name ->
        val min = index * weightIncrease
        if (this in min .. (index + 1) * weightIncrease) return (this.toDouble() / (min / 10)).round(1) + name
    }
    return "waaaaaaay too big"
}
