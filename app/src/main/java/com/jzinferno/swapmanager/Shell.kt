package com.jzinferno.swapmanager

import java.io.File
import java.util.concurrent.TimeUnit
import java.lang.ProcessBuilder.Redirect

fun String.runCommandStatus(): Int {
    val process = ProcessBuilder(*split(" ").toTypedArray())
        .directory(File("/"))
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
    if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroy()
    }
    return process.exitValue()
}

fun String.runCommandOutput(): String {
    val process = ProcessBuilder(*split(" ").toTypedArray())
        .directory(File("/"))
        .redirectOutput(Redirect.PIPE)
        .redirectError(Redirect.PIPE)
        .start()
    if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroy()
    }
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}
