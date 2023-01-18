package com.jzinferno.swapmanager

import java.io.File

class Swap {
    private val swapPath = "/data/local/jzinferno"
    private val swapFile = "${swapPath}/swapfile"
    private val swapExist = File(swapFile).exists()


    fun exist(): Boolean {
        return swapExist
    }


    fun start(int: Int) {
        var cmd = "mkdir -p ${swapPath}\n"
        if (!swapExist) {
            cmd += "fallocate -l ${int}G ${swapFile}\n" +
                    "chmod 600 ${swapFile}\n" +
                    "mkswap ${swapFile}\n"
        }
        cmd += "swapon $swapFile"
        Shell().execute(cmd, true)
    }


    fun stop() {
        var cmd = ""
        if (swapExist) {
            cmd += "swapoff ${swapFile}\n"
        }
        cmd += "rm -rf $swapFile"
        Shell().execute(cmd, true)
    }
}
