package com.jzinferno.swapmanager

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.lang.ProcessBuilder.Redirect

fun String.runCommand() {
    val process = ProcessBuilder(*split(" ").toTypedArray())
        .directory(File("/"))
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
    if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroy()
    }
}

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
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
    if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroy()
    }

    val output = StringBuilder()
    try {
        BufferedReader(InputStreamReader(process.inputStream)).use {reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return output.toString()
}