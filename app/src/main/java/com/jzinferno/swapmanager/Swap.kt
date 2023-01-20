package com.jzinferno.swapmanager

import java.io.File
import java.io.IOException

class Swap {
    private val swapPath = "/data/local/jzinferno"
    private val swapFile = "${swapPath}/swapfile"
    private val swapExist = File(swapFile).exists()


    fun exist(): Boolean {
        return swapExist
    }


    fun getSliderValue(filePath: String): Int {
        val res = if (File(filePath).exists()) {
            Shell().getOutput("cat $filePath")
        } else {
            if (RootChecker().isRootGranted() && File("${swapFile}_size").exists()) {
                Shell().getOutput("su -c cat ${swapFile}_size")
            } else {
                "2"
            }
        }
        return res.toInt()
    }


    fun saveSliderValue(int: Int, filePath: String) {
        Shell().execute("echo $int > $filePath", false)
    }


    fun start(int: Int): Boolean {
        var cmd = "mkdir -p ${swapPath}\n"
        if (!swapExist) {
            cmd += "fallocate -l ${int}G ${swapFile}\n" +
                    "chmod 600 ${swapFile}\n" +
                    "mkswap ${swapFile}\n" +
                    "echo $int > ${swapFile}_size\n"
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
        var cmd = "mkdir -p ${swapPath}\n"
        if (swapExist) {
            cmd += "swapoff ${swapFile}\n" +
                    "rm -rf ${swapFile}\n"
        }
        cmd += "rm -rf ${swapFile}_size"

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
