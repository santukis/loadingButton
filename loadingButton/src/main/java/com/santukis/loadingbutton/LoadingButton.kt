package com.santukis.loadingbutton

import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt

abstract class LoadingButton : AppCompatButton, Drawable.Callback {

    protected enum class State {
        PROGRESS, IDLE
    }

    protected var state: State = State.IDLE

    protected var initialText: String? = null
    protected var runningText: String? = null
    protected var finalText: String? = null

    protected var progressDrawableColors: IntArray = intArrayOf(Color.WHITE)

    protected var initialBackground: Drawable? = null
    protected var finalBackground: Drawable? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
        runningText = attributes.getString(R.styleable.LoadingButton_running_text)
        finalText = attributes.getString(R.styleable.LoadingButton_final_text)
        finalBackground = attributes.getDrawable(R.styleable.LoadingButton_final_background)
        initialBackground = background
        attributes.recycle()
    }

    fun setProgressColors(@ColorInt vararg colors: Int) {
        progressDrawableColors = colors
    }

    @CallSuper
    open fun start() {
        if (state != State.IDLE) {
            return
        }

        state = State.PROGRESS
        initialText = text.toString()
        text = null
        isClickable = false

        initialBackground?.apply {
            background = this
        }
    }

    @CallSuper
    open fun reset() {
        if (state != State.PROGRESS) {
            return
        }

        state = State.IDLE
        isClickable = true
    }

    open fun finish(animationListener: AnimatorListenerAdapter? = null) {
        if (state != State.PROGRESS) {
            return
        }

        state = State.IDLE
        isClickable = true

        finalBackground?.apply {
            background = this
        }

        finalText?.apply {
            text = this
        }
    }

    override fun invalidateDrawable(drawable: Drawable) {
        invalidate()
    }
}