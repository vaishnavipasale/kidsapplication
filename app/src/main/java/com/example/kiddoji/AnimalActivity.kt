package com.example.kiddoji

import android.os.Bundle
import android.view.ViewGroup

class AnimalActivity : KidsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animal)
        // ✅ Every animal card now bounces, pops a sound, and speaks its name when tapped
        makeCardsFun()
        findViewById<ViewGroup>(R.id.screenRoot).staggerReveal(startDelay = 60L, step = 45L)
    }
}
