package com.example.kiddoji

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Locale

class PoemBookActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private val poem1Lines = listOf(
        "The sun wakes up and starts to glow,",
        "It paints the sky with a golden show!",
        "The birds go tweet and the bees go buzz,",
        "Morning is magic — it always was!",
        "Jump out of bed and wiggle your toes,",
        "The whole big world is where fun goes!"
    )

    private val poem2Lines = listOf(
        "The flowers dance and the butterflies spin,",
        "Nature is bursting with colour and din!",
        "The trees reach up to tickle the sky,",
        "And fluffy white clouds go floating by.",
        "Rain goes drip and the rainbow appears,",
        "The world is the best gift — hip hip CHEERS!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.poem)
        findViewById<android.view.ViewGroup>(R.id.screenRoot).staggerReveal(startDelay = 100L, step = 60L)

        tts = TextToSpeech(this, this, "com.google.android.tts")

        setupAnimations()
        setupClickListeners()
    }

    // ── CLICK LISTENERS ──────────────────────────────────────────────────────

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnPlayPoem1).setOnClickListener {
            bounceButton(it)
            speakPoem(poem1Lines)
        }
        findViewById<View>(R.id.btnPlayPoem2).setOnClickListener {
            bounceButton(it)
            speakPoem(poem2Lines)
        }
        findViewById<CardView>(R.id.cardPoem1).setOnClickListener {
            wobbleCard(it)
            speakPoem(poem1Lines)
        }
        findViewById<CardView>(R.id.cardPoem2).setOnClickListener {
            wobbleCard(it)
            speakPoem(poem2Lines)
        }
    }

    // ── TTS INIT ─────────────────────────────────────────────────────────────

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return

        val available = tts.voices

        // Log all local en-US voices — remove before release
        available
            ?.filter { it.locale == Locale.US && !it.isNetworkConnectionRequired }
            ?.forEach { Log.d("TTS_VOICE", "${it.name} q=${it.quality}") }

        // Preferred order — tpf first (user preferred)
        val preferredVoices = listOf(
            "en-us-x-tpf-local",
            "en-us-x-iob-local",
            "en-us-x-iom-local",
            "en-us-x-sfg-local",
            "en-us-x-tpd-local",
            "en-us-x-iog-local",
            "en-us-x-tpc-local",
            "en-us-x-iol-local",
            "en-US-language"
        )

        val best = preferredVoices
            .mapNotNull { name -> available?.find { it.name == name } }
            .firstOrNull()

        if (best != null) {
            tts.voice = best
            Log.d("TTS_VOICE", "Selected: ${best.name}")
        } else {
            val fallback = available
                ?.filter {
                    it.locale == Locale.US &&
                            !it.isNetworkConnectionRequired &&
                            !it.name.contains("network", ignoreCase = true)
                }
                ?.maxByOrNull { it.quality }

            if (fallback != null) {
                tts.voice = fallback
                Log.d("TTS_VOICE", "Fallback: ${fallback.name}")
            } else {
                tts.setLanguage(Locale.US)
                Log.d("TTS_VOICE", "Using default language")
            }
        }

        ttsReady = true
    }

    // ── SPEAK ────────────────────────────────────────────────────────────────

    private fun speakPoem(lines: List<String>) {
        if (!ttsReady) return
        tts.stop()

        tts.setSpeechRate(0.78f)
        tts.setPitch(1.3f)

        lines.forEachIndexed { index, line ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(line, mode, null, "line_$index")
            tts.playSilentUtterance(270, TextToSpeech.QUEUE_ADD, "pause_$index")
        }
    }

    // ── ANIMATIONS ───────────────────────────────────────────────────────────

    private fun setupAnimations() {
        val title = findViewById<View>(R.id.tvTitle)
        title.alpha = 0f
        title.translationY = -80f
        title.animate()
            .alpha(1f).translationY(0f)
            .setDuration(700)
            .setInterpolator(BounceInterpolator())
            .start()

        animateCardIn(findViewById(R.id.cardPoem1), 300)
        animateCardIn(findViewById(R.id.cardPoem2), 550)

        floatStar(R.id.tvStar1, 0f, -25f, 1800)
        floatStar(R.id.tvStar2, 0f, -18f, 2200)
        floatStar(R.id.tvStar3, 0f, -22f, 1600)
    }

    private fun animateCardIn(card: View, delayMs: Long) {
        card.alpha = 0f
        card.translationX = -120f
        card.animate()
            .alpha(1f).translationX(0f)
            .setStartDelay(delayMs).setDuration(500)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun floatStar(id: Int, fromY: Float, toY: Float, duration: Long) {
        val star = findViewById<View>(id)
        val up = ObjectAnimator.ofFloat(star, "translationY", fromY, toY)
        val down = ObjectAnimator.ofFloat(star, "translationY", toY, fromY)
        up.duration = duration
        down.duration = duration
        val set = AnimatorSet()
        set.playSequentially(up, down)
        set.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                set.start()
            }
        })
        set.start()
    }

    private fun bounceButton(btn: View) {
        btn.animate()
            .scaleX(0.75f).scaleY(0.75f).setDuration(100)
            .withEndAction {
                btn.animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(BounceInterpolator())
                    .setDuration(400).start()
            }.start()
    }

    private fun wobbleCard(card: View) {
        ObjectAnimator.ofFloat(card, "rotation", 0f, -3f, 3f, -2f, 2f, 0f)
            .apply { duration = 500 }.start()
    }

    // ── LIFECYCLE ────────────────────────────────────────────────────────────

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}