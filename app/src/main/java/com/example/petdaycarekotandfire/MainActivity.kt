package com.example.petdaycarekotandfire

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("my_pref", Context.MODE_PRIVATE)
        if (!sharedPref.contains("is_app_opened_before")) {
            setContentView(R.layout.activity_main)
            sharedPref.edit().putBoolean("is_app_opened_before", true).apply()
        }else{
            setContentView(R.layout.fragment_splash)
            Handler().postDelayed({
                val i = Intent(applicationContext,Login::class.java)
                startActivity(i)
            },3000)

        }

    }

    override fun onBackPressed() {
        Utilities.makeToast(this,"No puedes volver hacia atrás, desliza")
    }
}