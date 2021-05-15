package com.example.werkstuk.ui.home

/*
    GENERATE SOUND CODE FROM:

    https://www.tutorialspoint.com/how-to-play-an-arbitrary-tone-in-android-using-kotlin
 */

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.werkstuk.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.experimental.and
import kotlin.math.sin

class HomeFragment : Fragment() {
    private var bpm = 60

    private val duration = 0.2
    private val sampleRate = 8000
    private val numSamples = duration * sampleRate
    private val sample = DoubleArray(numSamples.toInt())
    private val generatedSnd = ByteArray((2 * numSamples).toInt())
    private val handler: Handler = Handler()
    private var on = false

    private lateinit var homeViewModel: HomeViewModel

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val switch: Switch = root.findViewById(R.id.metronomeSwitch)
        val lower: FloatingActionButton = root.findViewById(R.id.lowerBpm)
        val raise: FloatingActionButton = root.findViewById(R.id.raiseBpm)
        val bpmText: TextView = root.findViewById(R.id.bpm)

        switch.setOnClickListener {
            on = !on
            startNoise()
        }

        lower.setOnClickListener {
            if (bpm > 5) {
                bpm -= 5
                bpmText.text = bpm.toString()
            }
        }

        raise.setOnClickListener {
            if (bpm < 300) {
                bpm += 5
                bpmText.text = bpm.toString()
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        startNoise()
    }

    private fun startNoise() {
        var delay = 60 / bpm.toDouble() * 1000
        if (on) {
            //Toast.makeText(activity, delay.toString(),Toast.LENGTH_SHORT).show()
            makeSound()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                startNoise()
            }, delay.toLong())
        } else {
            on = false;
        }
    }

    private fun makeSound() {
        val thread = Thread(Runnable {
            genTone()
            handler.post { playSound() }
        })
        thread.start()
    }

    private fun genTone() {
        for (i in 0 until numSamples.toInt()) {
            val freqOfTone = 600.0
            sample[i] =
                    sin(2 * Math.PI * i / (sampleRate / freqOfTone))
        }
        var idx = 0
        for (dVal in sample) {
            val `val` = (dVal * 32767).toShort()
            generatedSnd[idx++] = `val`.and(0x00ff).toByte()
            generatedSnd[idx++] = `val`.and(0xff00 ushr 8).toByte()
        }
    }
    private fun playSound() {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
    }
}