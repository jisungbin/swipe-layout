package com.cascade.widget.ui

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cascade.widget.ui.transition.SimpleAnimationListener
import com.cascade.widget.ui.view.Listener
import kotlinx.android.synthetic.main.activity_detail.*
import android.animation.ValueAnimator
import android.transition.Transition
import android.transition.TransitionSet
import android.view.ViewPropertyAnimator
import androidx.core.app.SharedElementCallback
import com.cascade.widget.R
import com.cascade.widget.ui.transition.ChangeRoundedImageTransform
import com.cascade.widget.ui.transition.SimpleTransitionListener
import com.cascade.widget.ui.view.RoundedImageView

class DetailActivity : AppCompatActivity() {
    companion object {
        private const val PROGRESS_DURATION_IN_MS = 3000L
    }

    private var sharedElementEnterLeft: Int = 0
    private var sharedElementEnterTop: Int = 0
    private var sharedElementEnterRadius: Int = 0

    private var isInformationUiHidden = false
    private var isFlingSwiped = false
    private var progressAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViews(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()

        pauseProgress()
    }

    override fun onStart() {
        super.onStart()

        initializeTransitionCallback()
    }

    override fun onResume() {
        super.onResume()

        resumeProgress()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        initializeSystemUi()
    }

    private fun initializeViews(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_detail)

        buttonBack.setOnClickListener {
            supportFinishAfterTransition()
        }

        swipeLayout.listener = object : Listener {
            override fun onSwipeStarted() {
                pauseProgress()
            }

            override fun onSwipeCancelled() {
            }

            override fun onSwiped() {
                pauseProgress()

                swipeLayout.autoReset = false
                startExitAnimation()
            }

            override fun onSwipedFling() {
                isFlingSwiped = true
            }

            override fun onSwipeReset() {
                if (isFlingSwiped)
                    supportFinishAfterTransition()
                else
                    resumeProgress()
            }
        }
        swipeLayout.setOnClickListener {
            if (!swipeLayout.isTransitionPresent()) {
                if (!isInformationUiHidden) {
                    supportFinishAfterTransition()
                } else {
                    swipeLayout.swipeEnabled = true
                    isInformationUiHidden = false

                    showInformationUi().setListener(object : SimpleAnimationListener() {
                        override fun onAnimationEnd(animator: Animator) {
                            super.onAnimationEnd(animator)
                            resumeProgress()
                        }
                    })
                }
            }
        }
        swipeLayout.setOnLongClickListener {
            if (!swipeLayout.isTransitionPresent()) {
                swipeLayout.swipeEnabled = false
                isInformationUiHidden = true

                pauseProgress()
                hideInformationUi()
            }

            false
        }

        if (savedInstanceState != null)
            startProgressAnimation()
    }

    private fun initializeTransitionCallback() {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                sharedElementNames: MutableList<String>,
                sharedElements: MutableList<View>,
                sharedElementSnapshots: MutableList<View>
            ) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots)

                for (view in sharedElements) {
                    if (view is RoundedImageView) {
                        sharedElementEnterLeft = view.left
                        sharedElementEnterTop = view.top
                        break
                    }
                }
            }
        })

        val sharedElementTransition = window.sharedElementEnterTransition
        sharedElementTransition!!.addListener(object : SimpleTransitionListener() {
            override fun onTransitionStart(p0: Transition?) {
                super.onTransitionStart(p0)

                val roundedImageTransform: ChangeRoundedImageTransform? =
                    if (p0 is TransitionSet) {
                        var roundedImageTransform: ChangeRoundedImageTransform? = null
                        for (i in 0..p0.transitionCount) {
                            val transition = p0.getTransitionAt(i)
                            if (transition is ChangeRoundedImageTransform) {
                                roundedImageTransform = transition
                                break
                            }
                        }
                        roundedImageTransform
                    } else if (p0 is ChangeRoundedImageTransform) {
                        p0
                    } else {
                        null
                    }
                sharedElementEnterRadius = roundedImageTransform?.getFromRadius() ?: sharedElementEnterRadius
            }

            override fun onTransitionEnd(p0: Transition?) {
                startProgressAnimation()
            }
        })
    }

    private fun initializeSystemUi() {
        window!!.decorView.systemUiVisibility += (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun pauseProgress() {
        progressAnimator?.pause()
    }

    private fun resumeProgress() {
        progressAnimator?.resume()
    }

    private fun showInformationUi(): ViewPropertyAnimator {
        return layoutInformation.animate().alpha(1f)

    }

    private fun hideInformationUi(): ViewPropertyAnimator {
        return layoutInformation.animate().alpha(0f)
    }

    private fun startProgressAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, progressBar.max)
        progressAnimator?.duration = PROGRESS_DURATION_IN_MS
        progressAnimator?.addUpdateListener { animator ->
            val progress = animator.animatedValue as Int
            progressBar.progress = progress
        }
        progressAnimator?.addListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)

                if (!isFinishing)
                    supportFinishAfterTransition()
            }
        })
        progressAnimator?.start()
    }

    private fun startExitAnimation() {
        swipeLayout.moveCircleToState(
            sharedElementEnterLeft,
            sharedElementEnterTop,
            sharedElementEnterRadius,
            0
        )?.addListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)

                if (!isFinishing)
                    finish()
            }
        })
    }
}