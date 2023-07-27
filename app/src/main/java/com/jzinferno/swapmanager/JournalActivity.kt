package com.jzinferno.swapmanager

import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import java.io.File
import kotlin.concurrent.thread

class JournalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        val journalTextView = findViewById<TextView>(R.id.journalTextView)

        thread {
            val journalFile = File("sh ${filesDir.absolutePath}/swap.sh log".runCommandOutput())
            val journalFileRead = if (journalFile.exists()) journalFile.readText() else ""
            runOnUiThread {
                if (journalFileRead.isNotEmpty()) journalTextView.text = journalFileRead
            }
        }

        journalTextView.setOnLongClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(
                "journal_data",
                journalTextView.text.toString()
            )
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            true
        }
    }
}