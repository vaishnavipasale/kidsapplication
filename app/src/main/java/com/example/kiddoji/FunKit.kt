package com.example.kiddoji

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * SoundFx — one tiny, dependency-free sound engine for the whole app.
 * Uses only platform SoundPool, so no new Gradle libraries are required.
 */
object SoundFx {

    private var soundPool: SoundPool? = null
    private var popId = 0
    private var chimeId = 0
    private var welcomeId = 0
    private var ready = false

    fun init(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val pool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        pool.setOnLoadCompleteListener { _, _, status -> if (status == 0) ready = true }

        popId = pool.load(context, R.raw.click_pop, 1)
        chimeId = pool.load(context, R.raw.success_chime, 1)
        welcomeId = pool.load(context, R.raw.welcome_chime, 1)
        soundPool = pool
    }

    fun playTap() = play(popId)
    fun playChime() = play(chimeId)
    fun playWelcome() = play(welcomeId)

    private fun play(id: Int) {
        if (!ready || id == 0) return
        soundPool?.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        ready = false
    }
}

/** A tiny gentle buzz kids can feel when they tap something — makes taps feel "real". */
fun View.tinyVibrate() {
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * A friendly "pop and bounce back" tap animation + tap sound + tiny vibration,
 * then runs [action]. Drop-in replacement for setOnClickListener that instantly
 * makes any button or card feel alive to a child.
 */
fun View.bounceTap(playSound: Boolean = true, action: () -> Unit) {
    setOnClickListener {
        if (playSound) SoundFx.playTap()
        tinyVibrate()
        animate().cancel()
        animate()
            .scaleX(0.86f).scaleY(0.86f)
            .setDuration(90)
            .withEndAction {
                animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(BounceInterpolator())
                    .setDuration(260)
                    .withEndAction { action() }
                    .start()
            }.start()
    }
}

/**
 * Plays every direct-and-nested CardView in this view tree into a "tap it, hear it"
 * moment: bounce animation + pop sound + the card's own label read aloud by TTS.
 * Works on any screen (letters, days, months, animals, fruits, birds, veggies…)
 * with zero XML changes — it walks the tree and finds the label text automatically.
 */
fun ViewGroup.attachFunToAllCards(tts: TextToSpeech?) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is CardView) {
            val label = collectCardSpeech(child)
            child.bounceTap {
                label?.let {
                    tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "card_tts_$i")
                }
            }
        }
        if (child is ViewGroup) child.attachFunToAllCards(tts)
    }
}

/** Gathers all readable text inside a card — for a letter card that's just "A",
 *  for a fruit/animal card it's the name, for a story card it's the whole tale. */
private fun collectCardSpeech(view: View, into: StringBuilder = StringBuilder()): String? {
    if (view is TextView && !view.text.isNullOrBlank()) {
        if (into.isNotEmpty()) into.append(". ")
        into.append(view.text.toString())
    } else if (view is ViewGroup) {
        for (i in 0 until view.childCount) collectCardSpeech(view.getChildAt(i), into)
    }
    return if (into.isEmpty()) null else into.toString()
}

/** Reveals every direct child of this ViewGroup one after another — turns a static list into a lively entrance. */
fun ViewGroup.staggerReveal(startDelay: Long = 60L, step: Long = 70L) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        child.alpha = 0f
        child.scaleX = 0.7f
        child.scaleY = 0.7f
        child.translationY = 40f
        child.animate()
            .alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setStartDelay(startDelay + i * step)
            .setDuration(360)
            .setInterpolator(OvershootInterpolator(1.6f))
            .start()
    }
}

/** Starts an infinite gentle pulse — used to show an icon is "alive" while narration plays.
 *  Call the returned Animator's .cancel() when speech ends, then reset scale to 1f. */
fun View.startPulse(): android.animation.ObjectAnimator {
    val anim = android.animation.ObjectAnimator.ofPropertyValuesHolder(
        this,
        android.animation.PropertyValuesHolder.ofFloat("scaleX", 1f, 1.22f, 1f),
        android.animation.PropertyValuesHolder.ofFloat("scaleY", 1f, 1.22f, 1f)
    )
    anim.duration = 520
    anim.repeatCount = android.animation.ObjectAnimator.INFINITE
    anim.start()
    return anim
}

/** Stops a pulse started by [startPulse] and snaps the view back to normal size. */
fun View.stopPulse(anim: android.animation.ObjectAnimator?) {
    anim?.cancel()
    scaleX = 1f
    scaleY = 1f
}

/** A slow, continuous side-to-side sway — nice for a sleepy sheep, a floating book, etc. */
fun View.gentleSway(amplitude: Float = 8f, duration: Long = 1400) {
    val sway = android.animation.ObjectAnimator.ofFloat(this, "rotation", -amplitude, amplitude)
    sway.duration = duration
    sway.repeatMode = android.animation.ObjectAnimator.REVERSE
    sway.repeatCount = android.animation.ObjectAnimator.INFINITE
    sway.start()
}
/** A slow, continuous up-down float — makes an emoji/icon feel alive without being distracting. */
fun View.floatLoop(amplitude: Float = 14f, duration: Long = 1600) {
    val floatAnim = android.animation.ObjectAnimator.ofFloat(this, "translationY", 0f, -amplitude, 0f)
    floatAnim.duration = duration
    floatAnim.repeatCount = android.animation.ObjectAnimator.INFINITE
    floatAnim.start()
}

/** A cheerful one-shot "pop in with a little spin" — great for headers, badges and titles on screen entry. */
fun View.popIn(delay: Long = 0L, duration: Long = 500) {
    alpha = 0f
    scaleX = 0.4f
    scaleY = 0.4f
    rotation = -8f
    animate()
        .alpha(1f).scaleX(1f).scaleY(1f).rotation(0f)
        .setStartDelay(delay)
        .setDuration(duration)
        .setInterpolator(OvershootInterpolator(2.2f))
        .start()
}

/**
 * Sets up sound + a friendly text-to-speech voice once, and gives every
 * screen a one-line way to turn its cards into tap-and-hear fun:
 *     makeCardsFun()
 * No screen has to wire up TTS, SoundPool or click listeners by hand anymore.
 */
abstract class KidsActivity : androidx.appcompat.app.AppCompatActivity() {

    protected var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        SoundFx.init(this)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.95f)
                ttsReady = true
            }
        }
    }

    /** Call this once after setContentView() to make every card on the screen tappable & talkative. */
    protected fun makeCardsFun() {
        val root = findViewById<ViewGroup>(android.R.id.content)
        root.attachFunToAllCards(tts)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
