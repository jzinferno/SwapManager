package com.jzinferno.swapmanager

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.DataOutputStream

class Shell {
    fun execute(command: String, runAsRoot: Boolean) {
        val shell = if (runAsRoot) "su" else "sh"
        try {
            val process = Runtime.getRuntime().exec(shell)
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            try {
                process.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getOutput(command: String): String {
        val output = StringBuilder()
        val p: Process
        try {
            p = Runtime.getRuntime().exec(command)
            p.waitFor()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String
            while (reader.readLine().also { line = it } != null) {
                output.append(line)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return output.toString()
    }

    fun getReturnValue(command: String, runAsRoot: Boolean): Int {
        val shell = if (runAsRoot) "su" else "sh"
        var value = 1
        try {
            val process = Runtime.getRuntime().exec(shell)
            val stdin: OutputStream = process.outputStream
            stdin.write("$command\n".toByteArray())
            stdin.write("exit\n".toByteArray())
            stdin.flush()
            stdin.close()
            process.waitFor()
            process.destroy()
            value = process.exitValue()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return value
    }
}