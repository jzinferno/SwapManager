package com.jzinferno.swapmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.io.File
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeDir = filesDir.absolutePath

        val ramSlider = findViewById<Slider>(R.id.sliderRam)
        val ramSwitch = findViewById<MaterialSwitch>(R.id.switchRam)
        val ramCard = findViewById<CardView>(R.id.cardRam)

        fun InputStream.toFile(to: File) {
            this.use { input ->
                to.outputStream().use { out ->
                    input.copyTo(out)
                }
            }
        }
        assets.open("swap.sh").toFile(File(homeDir, "swap.sh"))

        fun getToast(string: String) {
            Toast.makeText(this, string, Toast.LENGTH_LONG).show()
        }

        fun saveLog(string: String) {
            "sh ${homeDir}/swap.sh log $string".runCommand()
        }

        val swapFile = "sh ${homeDir}/swap.sh".runCommandOutput()
        if (!File("sh ${homeDir}/swap.sh log".runCommandOutput()).exists()) {
            saveLog("--clear")
        }

        fun getSwapSizeGB(): Int {
            val getFile = File(swapFile)
            val sizeInGigaBytes: Int = (getFile.length().toDouble() / 1073741824).toInt()
            return if (getFile.exists()) {
                sizeInGigaBytes
            } else {
                0
            }
        }

        fun getSliderValue(): Float {
            val result = if (File(swapFile).exists() && RootChecker().isRootGranted()) {
                getSwapSizeGB()
            } else {
                2
            }
            return result.toFloat()
        }

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSlider.value = getSliderValue()
                ramSwitch.isEnabled = true
                ramSwitch.isChecked = File(swapFile).exists()
                ramSlider.isEnabled = !ramSwitch.isChecked

                ramSlider.addOnChangeListener { _, value, _ ->
                    ramSwitch.isEnabled = (getRound(getAvailDataSize()).toFloat() >= value)
                }

                ramCard.setOnClickListener {
                    if (ramSwitch.isEnabled) ramSwitch.performClick()
                }

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    ramSwitch.isEnabled = false
                    val swapCommand = if (isChecked) {
                        if (File(swapFile).exists() && getSwapSizeGB() == getSliderValue().toInt()) {
                            "su -c sh ${homeDir}/swap.sh start --restart"
                        } else {
                            "su -c sh ${homeDir}/swap.sh start ${ramSlider.value.toInt()}"
                        }
                    } else {
                        "su -c sh ${homeDir}/swap.sh stop"
                    }

                    thread {
                        val status = swapCommand.runCommandStatus()
                        runOnUiThread {
                            if (status != 0) getToast("Failed")
                            ramSlider.isEnabled = !ramSwitch.isChecked
                            ramSwitch.isEnabled = true
                            updateMemoryInfo()
                        }
                    }
                }
            } else {
                getToast(getString(R.string.root_access_denied))
                saveLog(getString(R.string.root_access_denied))
            }
        } else {
            getToast(getString(R.string.device_isn_t_rooted))
            saveLog(getString(R.string.device_isn_t_rooted))
        }

        findViewById<TextView>(R.id.buttonRefresh).setOnClickListener {
            updateMemoryInfo()
        }

        findViewById<Button>(R.id.buttonLog).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        findViewById<Button>(R.id.linkTelegram).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jzinferno")))
        }

        findViewById<Button>(R.id.linkGithub).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jzinferno")))
        }
    }

    override fun onResume() {
        super.onResume()
        updateMemoryInfo()
    }

    private fun getAvailDataSize(): Long {
        val dataDirectory: File = Environment.getDataDirectory()
        val dataStat = StatFs(dataDirectory.path)
        return dataStat.availableBlocksLong * dataStat.blockSizeLong
    }

    private fun getRound(long: Long): Double {
        return ((long.toDouble() / 1073741824) * 10.0).roundToInt() / 10.0
    }

    private fun updateMemoryInfo() {
        val textViewData = findViewById<TextView>(R.id.textDataValue)
        val textViewRam = findViewById<TextView>(R.id.textRamValue)
        val textViewSwap = findViewById<TextView>(R.id.textSwapValue)

        val availDataMemory = getRound(getAvailDataSize())
        val totalRamMemory = getRound(MemInfo().ramTotal)
        val totalSwapMemory = getRound(MemInfo().swapTotal)

        textViewData.text = "$availDataMemory"
        textViewRam.text = "$totalRamMemory"
        textViewSwap.text = "$totalSwapMemory"
    }
}