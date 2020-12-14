package com.santukis.loadingbutton

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

class MorphLoadingButton: LoadingButton {

    private var animatorSet: AnimatorSet? = null

    private var isMorphingInProgress = false

    private var backgroundDrawable: GradientDrawable? = null

    private var progressDrawable: CircularProgressDrawable? = null

    private var initialWidth = 0
    private var initialHeight = 0

    private var finalWidth = 0
    private var finalHeight = 0

    private var initialCornerRadius = 0f
    private var finalCornerRadius = 1000f

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MorphLoadingButton)
        finalWidth = attributes.getDimension(R.styleable.MorphLoadingButton_final_width, 0f).toInt()
        finalHeight = attributes.getDimension(R.styleable.MorphLoadingButton_final_height, 0f).toInt()
        initialCornerRadius = attributes.getDimension(R.styleable.MorphLoadingButton_initial_corner_radius, 0f)
        finalCornerRadius = attributes.getDimension(R.styleable.MorphLoadingButton_final_corner_radius, 1000f)
        attributes.recycle()
    }

    override fun start() {
        super.start()
        isMorphingInProgress = true

        initialWidth = width
        initialHeight = height

        finalWidth = if (finalWidth == 0) initialHeight else finalWidth
        finalHeight = if (finalHeight == 0) initialHeight else finalHeight

        initializeBackgroundDrawable()

        val cornerAnimation = getCornerAnimation(initialCornerRadius, finalCornerRadius)
        val widthAnimation = getSizeAnimation(initialWidth, finalWidth, true)
        val heightAnimation = getSizeAnimation(initialHeight, finalHeight)

        playAnimations(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                isMorphingInProgress = false
            }
        }, cornerAnimation, widthAnimation, heightAnimation)
    }

    override fun reset() {
        super.reset()
        shrinkButton(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                text = initialText
                background = initialBackground
            }
        })
    }

    override fun finish(animationListener: AnimatorListenerAdapter?) {
        super.finish(animationListener)
        shrinkButton(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                text = finalText
                background = initialBackground
                animationListener?.onAnimationEnd(animation)
            }
        })
    }

    private fun shrinkButton(animationListener: AnimatorListenerAdapter?) {
        isMorphingInProgress = true

        val initialWidth = width
        val initialHeight = height

        val finalWidth = this.initialWidth
        val finalHeight = this.initialHeight

        val cornerAnimation = getCornerAnimation(finalCornerRadius, initialCornerRadius)
        val widthAnimation = getSizeAnimation(initialWidth, finalWidth, true)
        val heightAnimation = getSizeAnimation(initialHeight, finalHeight)

        playAnimations(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                isMorphingInProgress = false
                animationListener?.onAnimationEnd(animation)
            }
        }, cornerAnimation, widthAnimation, heightAnimation)
    }

    private fun initializeBackgroundDrawable() {
        backgroundDrawable = try {
            background as GradientDrawable

        } catch (exception: Exception) {
            ContextCompat.getDrawable(context, R.drawable.default_button_drawable) as GradientDrawable
        }

        backgroundDrawable?.cornerRadius = initialCornerRadius

        background = backgroundDrawable
    }

    private fun initializeProgressDrawable(canvas: Canvas) {
        if (progressDrawable == null || progressDrawable?.isRunning == false) {
            progressDrawable = CircularProgressDrawable(context).apply {

                setStyle(CircularProgressDrawable.LARGE)
                setColorSchemeColors(*progressDrawableColors)

                val offset = (width - height) / 2

                val right = width - offset
                val bottom = height
                val top = 0

                setBounds(offset, top, right, bottom)

                callback = this@MorphLoadingButton

                start()
            }
        } else {
            progressDrawable?.draw(canvas)
        }
    }

    private fun getCornerAnimation(from: Float, to: Float) =
        ObjectAnimator.ofFloat(backgroundDrawable ?: GradientDrawable(), "cornerRadius", from, to)

    private fun getSizeAnimation(from: Int, to: Int, animateWidth: Boolean = false) =
        ValueAnimator.ofInt(from, to).apply {
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                val layoutParams = layoutParams

                if (animateWidth) {
                    layoutParams.width = value

                } else {
                    layoutParams.height = value
                }
                setLayoutParams(layoutParams)
            }
        }

    private fun playAnimations(listener: AnimatorListenerAdapter, vararg animations: Animator) {
        animatorSet?.cancel()
        animatorSet = AnimatorSet().apply {
            duration = 300
            playTogether(*animations)
            addListener(listener)
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (state == State.PROGRESS && !isMorphingInProgress) {
            initializeProgressDrawable(canvas)
        }
    }
}