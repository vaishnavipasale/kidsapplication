package com.example.kiddoji

import android.animation.ObjectAnimator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Locale

class RhymesActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private var playingUtteranceId: String? = null
    private var activePulse: ObjectAnimator? = null
    private var activeIcon: TextView? = null
    private var activeButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rhymes)
        findViewById<android.view.ViewGroup>(R.id.screenRoot).staggerReveal(startDelay = 100L, step = 60L)

        SoundFx.init(this)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(0.85f)   // a little slower & sing-song for rhymes
                tts.setPitch(1.15f)
                ttsReady = true
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = runOnUiThread { onNarrationFinished() }
            override fun onError(utteranceId: String?) = runOnUiThread { onNarrationFinished() }
        })

        setupAnimations()

        wireRhyme(R.id.cardRhyme1, R.id.btnPlayRhyme1, R.id.iconRhyme1, R.id.textRhyme1, "rhyme1")
        wireRhyme(R.id.cardRhyme2, R.id.btnPlayRhyme2, R.id.iconRhyme2, R.id.textRhyme2, "rhyme2")
        wireRhyme(R.id.cardRhyme3, R.id.btnPlayRhyme3, R.id.iconRhyme3, R.id.textRhyme3, "rhyme3")
    }

    private fun wireRhyme(cardId: Int, btnId: Int, iconId: Int, textId: Int, utteranceId: String) {
        val card = findViewById<CardView>(cardId)
        val button = findViewById<TextView>(btnId)
        val icon = findViewById<TextView>(iconId)
        val text = findViewById<TextView>(textId).text.toString()

        val play: () -> Unit = { toggleNarration(button, icon, text, utteranceId) }
        button.bounceTap(playSound = false) { play() }
        card.bounceTap {
            wobbleCard(card)
            play()
        }
    }

    private fun toggleNarration(button: TextView, icon: TextView, text: String, utteranceId: String) {
        if (!ttsReady) return

        if (playingUtteranceId == utteranceId) {
            tts.stop()
            onNarrationFinished()
            return
        }
        if (playingUtteranceId != null) {
            tts.stop()
            onNarrationFinished()
        }

        SoundFx.playTap()
        playingUtteranceId = utteranceId
        activeButton = button
        activeIcon = icon
        button.text = "❚❚"
        activePulse = icon.startPulse()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun onNarrationFinished() {
        activeButton?.text = "▶"
        activeIcon?.stopPulse(activePulse)
        activePulse = null
        activeIcon = null
        activeButton = null
        playingUtteranceId = null
    }

    private fun wobbleCard(card: View) {
        ObjectAnimator.ofFloat(card, "rotation", 0f, -3f, 3f, -2f, 2f, 0f)
            .apply { duration = 500 }.start()
    }

    private fun setupAnimations() {
        val title = findViewById<View>(R.id.tvRhymesTitle)
        title.alpha = 0f
        title.translationY = -80f
        title.animate().alpha(1f).translationY(0f)
            .setDuration(700).setInterpolator(BounceInterpolator()).start()

        animateCardIn(R.id.cardRhyme1, 300)
        animateCardIn(R.id.cardRhyme2, 480)
        animateCardIn(R.id.cardRhyme3, 660)

        floatNote(R.id.tvNote1, -25f, 1800)
        floatNote(R.id.tvNote2, -18f, 2200)
        floatNote(R.id.tvNote3, -22f, 1600)
    }

    private fun animateCardIn(cardId: Int, delayMs: Long) {
        val card = findViewById<View>(cardId)
        card.alpha = 0f
        card.translationX = -120f
        card.animate().alpha(1f).translationX(0f)
            .setStartDelay(delayMs).setDuration(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
    }

    private fun floatNote(id: Int, toY: Float, duration: Long) {
        val note = findViewById<View>(id)
        val up = ObjectAnimator.ofFloat(note, "translationY", 0f, toY)
        val down = ObjectAnimator.ofFloat(note, "translationY", toY, 0f)
        up.duration = duration
        down.duration = duration
        val set = android.animation.AnimatorSet()
        set.playSequentially(up, down)
        set.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                set.start()
            }
        })
        set.start()
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
