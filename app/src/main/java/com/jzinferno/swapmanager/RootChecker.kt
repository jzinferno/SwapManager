package com.jzinferno.swapmanager

import java.io.File

class RootChecker {
    fun isRootGranted(): Boolean {
        val result = "su -c whoami".runCommandOutput()
        return result == "root"
    }

    fun isRootPresent() : Boolean {
        if (File("/system/app/Superuser.apk").exists()) {
            return true
        }
        val paths = arrayOf(
            "/sbin/",
            "/su/bin/",
            "/su/xbin/",
            "/system/bin/",
            "/system/xbin/",
            "/data/local/xbin/",
            "/data/local/bin/")
        var search: File
        for (path: String in paths) {
            search = File(path, "su")
            if (search.exists()) {
                return true
            }
        }
        return false
    }
}