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
        val sliderValue= "${homeDir}/value"

        fun InputStream.toFile(to: File) {
            this.use { input->
                to.outputStream().use { out->
                    input.copyTo(out)
                }
            }
        }

        if (!File(scriptsDir).exists()) {
            File(scriptsDir).mkdirs()
            assets.open("start.sh").toFile(File(scriptsDir, "start.sh"))
            assets.open("stop.sh").toFile(File(scriptsDir, "stop.sh"))
        }

        if (swapExist) ramSwitch.isChecked = true

        ramSlider.value = getSliderValue(sliderValue).toFloat()
        ramSlider.addOnChangeListener { _, value, _ ->
            Shell().execute("echo ${value.toInt()} > $sliderValue", false)
            ramSwitch.isEnabled = getRound(getAvailDataSize()).toInt() > getSliderValue(sliderValue)
        }

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSwitch.isEnabled = getRound(getAvailDataSize()).toInt() > getSliderValue(sliderValue)
                ramSlider.isEnabled = !ramSwitch.isChecked
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        ramSwitch.isEnabled = false
                        if (Shell().getReturnValue("sh ${scriptsDir}/start.sh ${getSliderValue(sliderValue)}", true) == 0) {
                            ramSwitch.isEnabled = true
                        } else {
                            getToast("Failed")
                        }
                    } else {
                        ramSwitch.isEnabled = false
                        if (Shell().getReturnValue("sh ${scriptsDir}/stop.sh", true) == 0) {
                            ramSwitch.isEnabled = true
                        }
                        else {
                            getToast("Failed")
                        }
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
    private val swapExist = File(swapFile).exists()

    private fun getSliderValue(string: String): Int {
        val res = if (File(string).exists()) {
            Shell().getOutput("cat $string")
        } else {
            if (RootChecker().isRootGranted() && File("${swapFile}_size").exists()) {
                Shell().getOutput("su -c cat ${swapFile}_size")
            } else {
                "2"
            }
        }
        return res.toInt()
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