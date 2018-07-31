package com.alexzaitsev.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super
                .onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log
                .d("Bla", "Value: ${meterView1.value}")

        meterView1.value = 500

        Log
                .d("Bla", "Value: ${meterView1.value}")

        Log
                .d("Bla", "Value: ${meterView2.value}")
    }
}
