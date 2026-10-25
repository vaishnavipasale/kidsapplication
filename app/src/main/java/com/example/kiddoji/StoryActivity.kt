package com.example.kiddoji

import android.animation.ObjectAnimator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Locale

class StoryActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    // which story is currently narrating, so tapping it again can stop it
    private var playingUtteranceId: String? = null
    private var activePulse: ObjectAnimator? = null
    private var activeIcon: TextView? = null
    private var activeButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.story)
        findViewById<android.view.ViewGroup>(R.id.screenRoot).staggerReveal(startDelay = 100L, step = 60L)

        SoundFx.init(this)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(0.92f)
                ttsReady = true
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = runOnUiThread { onNarrationFinished() }
            override fun onError(utteranceId: String?) = runOnUiThread { onNarrationFinished() }
        })

        setupAnimations()

        wireStory(R.id.cardStory1, R.id.btnPlayStory1, R.id.iconStory1, R.id.textStory1, "story1")
        wireStory(R.id.cardStory2, R.id.btnPlayStory2, R.id.iconStory2, R.id.textStory2, "story2")
        wireStory(R.id.cardStory3, R.id.btnPlayStory3, R.id.iconStory3, R.id.textStory3, "story3")
    }

    private fun wireStory(cardId: Int, btnId: Int, iconId: Int, textId: Int, utteranceId: String) {
        val card = findViewById<CardView>(cardId)
        val button = findViewById<TextView>(btnId)
        val icon = findViewById<TextView>(iconId)
        val text = findViewById<TextView>(textId).text.toString()

        val play: () -> Unit = { toggleNarration(button, icon, text, utteranceId) }
        button.bounceTap(playSound = false) { play() }
        card.bounceTap { play() }
    }

    private fun toggleNarration(button: TextView, icon: TextView, text: String, utteranceId: String) {
        if (!ttsReady) return

        // Tapping the story that's already reading stops it.
        if (playingUtteranceId == utteranceId) {
            tts.stop()
            onNarrationFinished()
            return
        }

        // Switching to a new story stops whatever was reading before.
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

    private fun setupAnimations() {
        val title = findViewById<View>(R.id.tvStoryTitle)
        title.alpha = 0f
        title.translationY = -60f
        title.animate().alpha(1f).translationY(0f).setDuration(600)
            .setInterpolator(DecelerateInterpolator()).start()

        animateCardIn(R.id.cardStory1, 250)
        animateCardIn(R.id.cardStory2, 420)
        animateCardIn(R.id.cardStory3, 590)

        findViewById<View>(R.id.tvSparkle1).gentleSway(10f, 1600)
        findViewById<View>(R.id.tvSparkle2).gentleSway(8f, 1900)
    }

    private fun animateCardIn(cardId: Int, delayMs: Long) {
        val card = findViewById<View>(cardId)
        card.alpha = 0f
        card.translationX = -140f
        card.animate().alpha(1f).translationX(0f)
            .setStartDelay(delayMs).setDuration(520)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
