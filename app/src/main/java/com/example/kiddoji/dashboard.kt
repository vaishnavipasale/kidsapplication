package com.example.kiddoji

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        SoundFx.init(this)

        // ── Every category card bounces, pops a friendly sound, then opens its world ──
        openWith(R.id.button1, Alphabets::class.java)
        openWith(R.id.button2, DaysActivity::class.java)
        openWith(R.id.button3, MonthsofyearsActivity::class.java)
        openWith(R.id.button4, AnimalActivity::class.java)
        openWith(R.id.button5, FruitsActivity::class.java)
        openWith(R.id.button6, BirdsActivity::class.java)
        openWith(R.id.button7, VegetablesActivity::class.java)
        openWith(R.id.button8, PoemBookActivity::class.java)
        openWith(R.id.button9, StoryActivity::class.java)
        openWith(R.id.button10, RhymesActivity::class.java) // Rhymes → its own screen with sing-song narration (was previously not wired to anything)

        // ── Welcome-back chime + the whole grid gently pops in row by row ──
        SoundFx.playChime()
        findViewById<ViewGroup>(R.id.dashboardContent).staggerReveal(startDelay = 80L, step = 55L)

        // ── Top badge gently floats forever so the home screen always feels a little alive ──
        findViewById<TextView>(R.id.topBadge).floatLoop(amplitude = 5f, duration = 1800)
    }

    private fun openWith(buttonId: Int, target: Class<*>) {
        findViewById<Button>(buttonId).bounceTap {
            startActivity(Intent(this, target))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
