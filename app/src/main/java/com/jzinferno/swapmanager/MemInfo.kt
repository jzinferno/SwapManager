package com.jzinferno.swapmanager

import java.io.File

class MemInfo {
    private fun getMemInfoValue(string: String): Long {
        val memValue = File("/proc/meminfo").readLines().firstOrNull { it.matches(Regex("${string}:.*")) }
        return memValue?.split(Regex("\\s+"))?.get(1)?.toLong()?.times(1024) ?: -1
    }

    val ramTotal: Long = getMemInfoValue("MemTotal")
    val swapTotal: Long = getMemInfoValue("SwapTotal")
}