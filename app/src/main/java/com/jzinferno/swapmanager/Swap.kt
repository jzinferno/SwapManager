package com.jzinferno.swapmanager

import java.io.File
import java.io.IOException

class Swap {
    private val swapPath = "/data/local/jzinferno"
    private val swapFile = "${swapPath}/swapfile"
    private val swapExist = File(swapFile).exists()


    fun getSliderValue(filePath: String): Int {
        val res = if (File(filePath).exists()) {
            Shell().getOutput("cat $filePath")
        } else {
            "2"
        }
        return res.toInt()
    }


    fun saveSliderValue(int: Int, filePath: String) {
        Shell().execute("echo $int > $filePath", false)
    }


    fun exist(): Boolean {
        return swapExist
    }


    fun start(int: Int): Boolean {
        var cmd = "mkdir -p ${swapPath}\n"
        if (!swapExist) {
            cmd += "fallocate -l ${int}G ${swapFile}\n" +
                    "chmod 600 ${swapFile}\n" +
                    "mkswap ${swapFile}\n"
        }
        cmd += "swapon $swapFile"

        val status: Boolean = try {
            Shell().execute(cmd, true)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
        return status
    }


    fun stop(): Boolean {
        var cmd = ""
        if (swapExist) {
            cmd += "swapoff ${swapFile}\n"
        }
        cmd += "rm -rf $swapFile"

        val status: Boolean = try {
            Shell().execute(cmd, true)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
        return status
    }
}
