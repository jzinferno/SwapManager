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
import com.topjohnwu.superuser.Shell
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
            assets.open("getValue.sh").toFile(File(scriptsDir, "getValue.sh"))
            Shell.cmd("chmod 755 ${scriptsDir}/*").submit()
        }

        if (swapExist) ramSwitch.isChecked = true

        ramSlider.value = getSliderValue("${scriptsDir}/getValue.sh" , sliderValue).toFloat()
        ramSlider.addOnChangeListener { _, value, _ ->
            Shell.cmd("echo ${value.toInt()} > $sliderValue").submit()
            ramSwitch.isEnabled = getRound(getAvailDataSize()).toInt() > getSliderValue("${scriptsDir}/getValue.sh", sliderValue)
        }

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSwitch.isEnabled = getRound(getAvailDataSize()).toInt() > getSliderValue("${scriptsDir}/getValue.sh", sliderValue)
                ramSlider.isEnabled = !ramSwitch.isChecked
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    run {
                        if (isChecked) {
                            buttonView.isEnabled = false
                            if (Shell.cmd("su -c ${scriptsDir}/start.sh ${getSliderValue("${scriptsDir}/getValue.sh", sliderValue)}").exec().isSuccess) {
                                buttonView.isEnabled = true
                            } else {
                                getToast("Failed")
                            }
                        }
                        if (!isChecked) {
                            buttonView.isEnabled = false
                            if (Shell.cmd("su -c ${scriptsDir}/stop.sh").exec().isSuccess) {
                                buttonView.isEnabled = true
                            } else {
                                getToast("Failed")
                            }
                        }

                        ramSlider.isEnabled = !isChecked
                        updateMemoryInfo()
                    }
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

    private fun getSliderValue(script: String, string: String): Int {
        val res = if (File(string).exists()) {
            Shell.cmd("$script $string").exec().code
        } else {
            if (RootChecker().isRootGranted() && File("${swapFile}_size").exists()) {
                Shell.cmd("su -c $script ${swapFile}_size").exec().code
            } else {
                2
            }
        }
        return res
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