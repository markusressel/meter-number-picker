package com.alexzaitsev.meternumberpicker


import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

class MeterView : LinearLayout {

    private var numberOfFirst = DEFAULT_NUMBER_OF_BLACK
    private var numberOfSecond = DEFAULT_NUMBER_OF_RED
    private var firstColor = DEFAULT_BLACK_COLOR
    private var secondColor = DEFAULT_RED_COLOR
    private var enabled = DEFAULT_ENABLED

    private var pickerStyleId = -1

    /**
     * Returns current value of the widget. Works only if "mnp_max" is not bigger then 9.
     * For other cases you have to extend this view for now.
     */
    /**
     * Sets current value to the widget. Works only if "mnp_max" is not bigger then 9.
     * For other cases you have to extend this view for now.
     */
    var value: Int
        get() {
            var result = 0
            var koeff = childCount
            for (i in 0 until childCount) {
                val picker = getChildAt(i) as MeterNumberPicker
                result += (picker.value * Math.pow(10.0, (--koeff).toDouble()))
                        .toInt()
            }
            return result
        }
        set(value) {
            var value = value
            var koeff = childCount
            for (i in 0 until childCount) {
                val picker = getChildAt(i) as MeterNumberPicker
                val number = (value / Math.pow(10.0, (--koeff).toDouble()))
                        .toInt()
                if (i == 0 && number > 9) {
                    throw IllegalArgumentException("Number of digits cannot be greater then pickers number")
                }
                value -= (number * Math.pow(10.0, koeff.toDouble()))
                        .toInt()
                picker
                        .value = number
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = LinearLayout
                .HORIZONTAL
        if (attrs != null) {
            val typedArray = context
                    .obtainStyledAttributes(attrs, R.styleable.MeterView, 0, 0)
            numberOfFirst = typedArray
                    .getInt(R.styleable.MeterView_mv_numberOfFirst, numberOfFirst)
            numberOfSecond = typedArray
                    .getInt(R.styleable.MeterView_mv_numberOfSecond, numberOfSecond)
            firstColor = typedArray
                    .getColor(R.styleable.MeterView_mv_firstColor, firstColor)
            secondColor = typedArray
                    .getColor(R.styleable.MeterView_mv_secondColor, secondColor)
            pickerStyleId = typedArray
                    .getResourceId(R.styleable.MeterView_mv_pickerStyle, pickerStyleId)
            enabled = typedArray
                    .getBoolean(R.styleable.MeterView_mv_enabled, enabled)
            typedArray
                    .recycle()
        }
        populate(context)
    }

    private fun populate(context: Context) {
        for (i in 0 until numberOfFirst + numberOfSecond) {
            val meterNumberPicker = createPicker(context)
            meterNumberPicker
                    .setBackgroundColor(if (i < numberOfFirst) firstColor else secondColor)
            meterNumberPicker
                    .isEnabled = isEnabled
            val lp = LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp
                    .weight = 1f
            addView(meterNumberPicker, lp)
        }
    }

    private fun createPicker(context: Context): MeterNumberPicker {
        return if (pickerStyleId == -1) MeterNumberPicker(context) else MeterNumberPicker(context, pickerStyleId)
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this
                .enabled = enabled
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return !enabled || super.onInterceptTouchEvent(ev)
    }

    fun setNumbersOf(numberOfFirst: Int, numberOfSecond: Int) {
        this
                .numberOfFirst = numberOfFirst
        this
                .numberOfSecond = numberOfSecond
        removeAllViews()
        init(context, null)
    }

    companion object {

        private val DEFAULT_NUMBER_OF_BLACK = 5
        private val DEFAULT_NUMBER_OF_RED = 0
        private val DEFAULT_BLACK_COLOR = -0x1000000
        private val DEFAULT_RED_COLOR = -0x340000
        private val DEFAULT_ENABLED = true
    }
}
