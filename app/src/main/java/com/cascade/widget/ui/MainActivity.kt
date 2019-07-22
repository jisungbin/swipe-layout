package com.cascade.widget.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.cascade.widget.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        layoutUserStatus.setOnClickListener {
            val transitionName = getString(R.string.transition_name)
            val intent = Intent(this, DetailActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView, transitionName)
            startActivity(intent, options.toBundle())
        }
    }
}