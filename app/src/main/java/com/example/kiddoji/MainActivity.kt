package com.example.kiddoji

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.kiddoji.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SoundFx.init(this)

        // ✅ Logo name pops in with a playful bounce instead of just appearing
        binding.appName.alpha = 0f
        binding.appName.scaleX = 0.4f
        binding.appName.scaleY = 0.4f
        binding.appName.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(250)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(2f))
            .withEndAction { SoundFx.playWelcome() }
            .start()

        // ✅ TextToSpeech (Welcome voice)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.speak(
                    "Welcome to Kiddoji",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "kiddoji_tts"
                )
            }
        }

        // ✅ Navigate to Dashboard with a smooth cross-fade instead of a hard cut
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 4000)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
