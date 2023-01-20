package com.jzinferno.swapmanager

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.content.ContextWrapper

import android.os.Bundle
import android.os.Environment
import android.os.StatFs

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ramSlider = findViewById<Slider>(R.id.sliderRam)
        val ramSwitch = findViewById<SwitchCompat>(R.id.switchRam)

        val homeDir = ContextWrapper(this).filesDir.path.toString()
        Shell().execute("mkdir -p $homeDir", false)

        if (!Swap().exist()) ramSwitch.isChecked = false

        ramSlider.value = Swap().getSliderValue("${homeDir}/value").toFloat()

        ramSlider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
            Swap().saveSliderValue(slider.value.toInt(), "${homeDir}/value")
        })

        if (RootChecker().isRootPresent()) {
            if (RootChecker().isRootGranted()) {
                ramSlider.isEnabled = !ramSwitch.isChecked
                ramSwitch.isEnabled = true
                getToast(":)")

                ramSwitch.setOnCheckedChangeListener { _, isChecked ->
                    getToast("Wait...")

                    if (isChecked) {
                        ramSwitch.isEnabled = false
                        if (Swap().start(Swap().getSliderValue("${homeDir}/value"))) {
                            ramSwitch.isEnabled = true
                        }
                    } else {
                        ramSwitch.isEnabled = false
                        if (Swap().stop()) {
                            ramSwitch.isEnabled = true
                        }
                    }

                    ramSlider.isEnabled = !ramSwitch.isChecked
                    updateMemoryInfo()
                }

            } else {
                getToast(":(")
            }
        } else {
            getToast("Device isn't Rooted")
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
}