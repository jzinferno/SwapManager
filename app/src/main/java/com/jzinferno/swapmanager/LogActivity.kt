package com.jzinferno.swapmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.File
import kotlin.concurrent.thread

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        thread {
            val logFile = File("sh ${filesDir.absolutePath}/swap.sh log".runCommandOutput())
            val logFileRead = if (logFile.exists()) logFile.readText() else ""
            runOnUiThread {
                if (logFileRead.isNotEmpty()) findViewById<TextView>(R.id.textViewLog).text = logFileRead
            }
        }
    }
}