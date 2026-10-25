package com.example.kiddoji

import android.os.Bundle
import android.view.ViewGroup

class MonthsofyearsActivity : KidsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthsofyears)
        // ✅ Every month card now bounces, pops a sound, and speaks its name when tapped
        makeCardsFun()
        findViewById<ViewGroup>(R.id.itemGrid).staggerReveal(startDelay = 120L, step = 35L)
    }
}
