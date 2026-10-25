package com.example.kiddoji

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Alphabets : KidsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabets)
        // ✅ Every letter card now bounces, pops a sound, and speaks its letter when tapped
        makeCardsFun()
        findViewById<ViewGroup>(R.id.itemGrid).staggerReveal(startDelay = 120L, step = 25L)
    }
}