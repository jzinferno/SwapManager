package com.jzinferno.swapmanager

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.content.ContextWrapper
import android.content.res.AssetManager

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log

import android.widget.TextView
import android.widget.Toast

import com.google.android.material.slider.Slider
import java.io.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ramSlider = findViewById<Slider>(R.id.sliderRam)
        val ramSwitch = findViewById<SwitchCompat>(R.id.switchRam)

        val homeDir = ContextWrapper(this).filesDir.absolutePath
        val scriptsDir = "${homeDir}/scripts"
        val sliderValue= "${homeDir}/value"

        File(scriptsDir).mkdirs()
        copyAssets(scriptsDir)
        if (swapExist) ramSwitch.isChecked = true

        ramSlider.value = getSliderValue(sliderValue).toFloat()
        ramSlider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
            Shell().execute("echo ${slider.value.toInt()} > $sliderValue", false)
        })

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSlider.isEnabled = !ramSwitch.isChecked
                ramSwitch.isEnabled = true
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        ramSwitch.isEnabled = false
                        if (Shell().getReturnValue("sh ${scriptsDir}/start.sh ${getSliderValue(sliderValue)}", true) == 0) {
                            ramSwitch.isEnabled = true
                        } else {
                            getToast("Failed")
                        }
                    }

                    if (!isChecked) {
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

    private fun copyAssets(string: String) {
        val assetManager: AssetManager = assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }
        for (filename in files!!) {
            var `in`: InputStream?
            var out: OutputStream?
            try {
                `in` = assetManager.open(filename)
                val outDir: String = string
                val outFile = File(outDir, filename)
                out = FileOutputStream(outFile)
                copyFile(`in`, out)
                `in`.close()
                out.flush()
                out.close()
            } catch (e: IOException) {
                Log.e("tag", "Failed to copy asset file: $filename", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}