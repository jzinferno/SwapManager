package com.jzinferno.swapmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.io.File
import java.io.InputStream
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ramSlider = findViewById<Slider>(R.id.sliderRam)
        val ramSwitch = findViewById<MaterialSwitch>(R.id.switchRam)

        val homeDir = filesDir.absolutePath
        val scriptsDir = "${homeDir}/scripts"

        fun InputStream.toFile(to: File) {
            this.use { input ->
                to.outputStream().use { out ->
                    input.copyTo(out)
                }
            }
        }

        if (File(scriptsDir).exists()) {
            File(scriptsDir).deleteRecursively()
        }

        File(scriptsDir).mkdirs()
        assets.open("start.sh").toFile(File(scriptsDir, "start.sh"))
        assets.open("stop.sh").toFile(File(scriptsDir, "stop.sh"))

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSwitch.isChecked = ("su -c grep \"$swapFile\" /proc/swaps".runCommandStatus() == 0)
                ramSlider.value = getSliderValue().toFloat()

                ramSwitch.isEnabled = getRound(getAvailDataSize()).toInt() > ramSlider.value
                ramSlider.isEnabled = !ramSwitch.isChecked
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        ramSwitch.isEnabled = false
                        if ("su -c sh ${scriptsDir}/start.sh ${ramSlider.value.toInt()}".runCommandStatus() != 0) {
                            getToast("Failed")
                        }
                        ramSwitch.isEnabled = true
                    } else {
                        ramSwitch.isEnabled = false
                        if ("su -c sh ${scriptsDir}/stop.sh".runCommandStatus() != 0) {
                            getToast("Failed")
                        }
                        ramSwitch.isEnabled = true
                    }

                    ramSlider.isEnabled = !ramSwitch.isChecked
                    updateMemoryInfo()
                }

            } else {
                getToast(":(")
            }
        } else {
            getToast("Device isn't Rooted!")
        }

        val textRefresh = findViewById<TextView>(R.id.textRefresh)
        textRefresh.setOnClickListener {
            updateMemoryInfo()
        }

        findViewById<ImageView>(R.id.imageTelegram).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jzinferno")))
        }

        findViewById<ImageView>(R.id.imageGithub).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jzinferno")))
        }
    }

    override fun onResume() {
        super.onResume()
        updateMemoryInfo()
    }

    private val swapFile = "/data/local/jzinferno/swapfile"

    private fun getSwapSizeGB(): Int {
        val getFile = File(swapFile)
        val sizeInGigaBytes: Int = (getFile.length().toDouble() / 1073741824).toInt()
        return if (getFile.exists()) {
            sizeInGigaBytes
        } else {
            0
        }
    }

    private fun getSliderValue(): Int {
        return if (File(swapFile).exists() && RootChecker().isRootGranted()) {
            getSwapSizeGB()
        } else {
            2
        }
    }

    private fun getAvailDataSize(): Long {
        val dataDirectory: File = Environment.getDataDirectory()
        val dataStat = StatFs(dataDirectory.path)
        return dataStat.availableBlocksLong * dataStat.blockSizeLong
    }

    private fun getRound(long: Long): Double {
        return ((long.toDouble() / 1073741824) * 10.0).roundToInt() / 10.0
    }

    private fun getToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
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