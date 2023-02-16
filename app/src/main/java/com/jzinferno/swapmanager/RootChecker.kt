package com.jzinferno.swapmanager

import com.topjohnwu.superuser.Shell
import java.io.File

class RootChecker {
    fun isRootGranted(): Boolean {
        return Shell.cmd("su -c true").exec().isSuccess
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