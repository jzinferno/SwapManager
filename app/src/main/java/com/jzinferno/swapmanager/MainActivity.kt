package com.jzinferno.swapmanager

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.content.ContextWrapper

import android.os.Bundle
import android.os.Environment
import android.os.StatFs

import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.material.slider.Slider
import java.io.File
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
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
        val textViewData = findViewById<TextView>(R.id.textViewData)
        val textViewRam = findViewById<TextView>(R.id.textViewRam)
        val textViewSwap = findViewById<TextView>(R.id.textViewSwap)

        val availStorageMemory = getRound(getAvailDataSize())
        textViewData.text = "$availStorageMemory"

        val totalRamMemory = getRound(MemInfo().ramTotal)
        textViewRam.text = "$totalRamMemory"

        val totalSwapMemory = getRound(MemInfo().swapTotal)
        textViewSwap.text = "$totalSwapMemory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appFilesDir = ContextWrapper(this).filesDir.path.toString()
        Shell().execute("mkdir -p $appFilesDir", false)

        val ramSlider = findViewById<Slider>(R.id.slider)
        val ramSwitch = findViewById<SwitchCompat>(R.id.switch1)
        ramSwitch.isChecked = Swap().exist()

        if (!RootChecker().isRootGranted()) {
            ramSlider.isEnabled = false
            ramSwitch.isClickable = false
        }

        if (!File("${appFilesDir}/value").exists()) {
            Shell().execute("echo 2 > ${appFilesDir}/value", false)
        }
        ramSlider.value = Shell().getOutput("cat ${appFilesDir}/value").toFloat()

        ramSlider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ -> Shell().execute(
            "echo ${slider.value.toInt()} > ${appFilesDir}/value",
            false
        ) })

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    getToast("Wait...")

                    if (isChecked) {
                        ramSwitch.isClickable = false
                        if (Swap().start(Shell().getOutput("cat ${appFilesDir}/value").toInt())) {
                            ramSwitch.isClickable = true
                            getToast("Done")
                        }

                        ramSlider.isEnabled = false
                    } else {
                        ramSwitch.isClickable = false
                        if (Swap().stop()) {
                            ramSwitch.isClickable = true
                            getToast("Done")
                        }

                        ramSlider.isEnabled = true
                    }
                    updateMemoryInfo()
                }

            } else {
                getToast(":(")
            }
        } else {
            getToast("Device isn't Rooted")
        }

        val buttonGetUpdate = findViewById<Button>(R.id.buttonUpdate)
        buttonGetUpdate.setOnClickListener {
            updateMemoryInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        updateMemoryInfo()
    }
}