package com.alexzaitsev.meternumberpicker


import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView

class MeterView : LinearLayout {

    private var numberOfFirst = DEFAULT_NUMBER_OF_BLACK
    private var numberOfSecond = DEFAULT_NUMBER_OF_RED
    private var firstColor = DEFAULT_BLACK_COLOR
    private var showDivider = false
    private var dividerColor = firstColor
    private var dividerText: CharSequence? = null
    private var secondColor = DEFAULT_RED_COLOR
    private var enabled = DEFAULT_ENABLED

    private var pickerStyleId = -1

    private val pickerList: MutableList<MeterNumberPicker> = mutableListOf()

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
            pickerList
                    .forEachIndexed { index, picker ->
                        val factor = Math
                                .pow(10.0, (pickerList.size - index - 1).toDouble())
                                .toInt()
                        result += picker.value * factor
                    }
            return result
        }
        set(value) {
            var value = value
            pickerList
                    .forEachIndexed { index, picker ->
                        val factor = Math
                                .pow(10.0, (pickerList.size - index - 1).toDouble())
                                .toInt()

                        val number = (value / factor)
                        if (index == 0 && number > 9) {
                            throw IllegalArgumentException("Number of digits cannot be greater then pickers number")
                        }

                        value -= (number * Math.pow(10.0, (pickerList.size - index - 1).toDouble()))
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
            dividerColor = typedArray
                    .getColor(R.styleable.MeterView_mv_dividerColor, firstColor)
            showDivider = typedArray
                    .getBoolean(R.styleable.MeterView_mv_showDivider, false)
            dividerText = typedArray
                    .getText(R.styleable.MeterView_mv_dividerText)
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
        pickerList
                .clear()

        var textSize = -1F
        var textColor = 0
        for (i in 0 until numberOfFirst) {
            val meterNumberPicker = createPicker(context)

            textSize = meterNumberPicker
                    .textSize
            textColor = meterNumberPicker
                    .textColor

            meterNumberPicker
                    .setBackgroundColor(firstColor)

            meterNumberPicker
                    .isEnabled = isEnabled
            val lp = LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp
                    .weight = 1f
            addView(meterNumberPicker, lp)
            pickerList
                    .add(meterNumberPicker)
        }

        if (showDivider) {
            val dividerView = TextView(context)
            dividerText
                    ?.let {
                        dividerView
                                .text = it
                    }

            dividerView
                    .setBackgroundColor(dividerColor)
            dividerView
                    .gravity = Gravity
                    .CENTER
            dividerView
                    .setTextColor(textColor)
            if (textSize > 0) {
                dividerView
                        .setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
            //            dividerView
            //                    .setPadding(8, 0, 8, 0)

            val lp = LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
            addView(dividerView, lp)
        }

        for (i in 0 until numberOfSecond) {
            val meterNumberPicker = createPicker(context)
            meterNumberPicker
                    .setBackgroundColor(secondColor)

            meterNumberPicker
                    .isEnabled = isEnabled
            val lp = LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp
                    .weight = 1f
            addView(meterNumberPicker, lp)
            pickerList
                    .add(meterNumberPicker)
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

        private const val DEFAULT_NUMBER_OF_BLACK = 5
        private const val DEFAULT_NUMBER_OF_RED = 0
        private const val DEFAULT_BLACK_COLOR = -0x1000000
        private const val DEFAULT_RED_COLOR = -0x340000
        private const val DEFAULT_TEXT_COLOR = -0xFFFFFF
        private const val DEFAULT_ENABLED = true
    }
}
