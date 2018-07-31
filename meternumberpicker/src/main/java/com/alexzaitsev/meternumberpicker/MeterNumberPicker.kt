package com.alexzaitsev.meternumberpicker


import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import java.util.*

class MeterNumberPicker : View {

    /**
     * Minimum height of the view
     */
    var minHeight = dpToPx(DEFAULT_MIN_HEIGHT_DP.toFloat())
            .toInt()
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * Minimum width of the view
     */
    var minWidth = dpToPx(DEFAULT_MIN_WIDTH_DP.toFloat())
            .toInt()
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * Minimum value
     */
    var minValue: Int = DEFAULT_MIN_VALUE
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("minValue must be >= 0")
            }
            field = value
            if (this.value < minValue) {
                this
                        .value = minValue
            }
            invalidate()
        }

    /**
     * Maximum value
     */
    var maxValue = DEFAULT_MAX_VALUE
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("maxValue must be >= 0")
            }
            field = value
            if (this.value > maxValue) {
                this
                        .value = maxValue
            }
            invalidate()
        }

    /**
     * The current value
     */
    var value: Int = DEFAULT_VALUE
        set(value) {
            if (value < minValue) {
                throw IllegalArgumentException("value must be >= minValue")
            }
            if (value > maxValue) {
                throw IllegalArgumentException("value must be <= maxValue")
            }
            field = value
            invalidate()
        }

    private var textPaint: Paint = Paint()

    /**
     * The text size
     */
    var textSize: Float
        get() {
            return textPaint
                    .textSize
        }
        set(value) {
            textPaint
                    .textSize = value
            invalidate()
        }

    /**
     * The text color
     */
    var textColor: Int
        get() {
            return textPaint
                    .color
        }
        set(value) {
            textPaint
                    .color = value
            invalidate()
        }

    /**
     * The typeface used for the text
     */
    var typeface: Typeface = Typeface
            .DEFAULT
        set(value) {
            field = value
            textPaint
                    .typeface = this
                    .typeface
        }
    /**
     * Current Y scroll offset
     */
    private var currentScrollOffset: Int = 0
    /**
     * Current value offset
     */
    private var currentValueOffset: Int = 0
    /**
     * The height of the text itself excluding paddings
     */
    var textHeight: Int = 0
    /**
     * Internal horizontal (left and right) padding
     */
    var paddingHorizontal = dpToPx(DEFAULT_PADDING.toFloat())
            .toInt()
        set(value) {
            field = value
            requestLayout()
        }
    /**
     * Internal vertical (top and bottom) padding
     */
    var paddingVertical = dpToPx(DEFAULT_PADDING.toFloat())
            .toInt()
        private set(value) {
            field = value
            requestLayout()
        }

    /**
     * The Y position of the last down event
     */
    var lastDownEventY: Float = 0
            .toFloat()
    /**
     * The Y position of the last down or move event
     */
    var lastDownOrMoveEventY: Float = 0
            .toFloat()
    /**
     * The [Scroller] responsible for adjusting the selector
     */
    private lateinit var adjustScroller: Scroller
    /**
     * The [Scroller] responsible for flinging the selector
     */
    private lateinit var flingScroller: Scroller
    /**
     * The last Y position of adjustment scroller
     */
    private var scrollerLastY = 0
    /**
     * Determines speed during touch scrolling
     */
    private var velocityTracker: VelocityTracker? = null
    /**
     * @see ViewConfiguration.getScaledMinimumFlingVelocity
     */
    private var minimumFlingVelocity: Int = 0
    /**
     * @see ViewConfiguration.getScaledMaximumFlingVelocity
     */
    private var maximumFlingVelocity: Int = 0

    constructor(context: Context) : super(context) {
        initWithAttrs(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initWithAttrs(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initWithAttrs(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initWithAttrs(context, attrs, defStyleAttr, defStyleRes)
    }

    constructor(context: Context, @StyleRes styleId: Int) : super(context) {
        initWithStyle(context, styleId)
    }

    private fun initWithAttrs(context: Context, attrs: AttributeSet?, defStyleAttrs: Int, @StyleRes defStyleRes: Int) {
        val attributesArray = context
                .obtainStyledAttributes(attrs, R.styleable.MeterNumberPicker, defStyleAttrs, defStyleRes)
        init(context, attributesArray)
        attributesArray
                .recycle()
    }

    private fun initWithStyle(context: Context, @StyleRes styleId: Int) {
        val styleTypedArray = context
                .obtainStyledAttributes(styleId, R.styleable.MeterNumberPicker)
        init(context, styleTypedArray)
        styleTypedArray
                .recycle()
    }

    private fun init(context: Context, attributesArray: TypedArray?) {
        if (attributesArray == null) {
            textSize = spToPx(DEFAULT_TEXT_SIZE_SP)
        } else {
            minValue = attributesArray
                    .getInt(R.styleable.MeterNumberPicker_mnp_min, DEFAULT_MIN_VALUE)
            maxValue = attributesArray
                    .getInt(R.styleable.MeterNumberPicker_mnp_max, DEFAULT_MAX_VALUE)
            value = attributesArray
                    .getInt(R.styleable.MeterNumberPicker_mnp_value, DEFAULT_VALUE)
            textColor = attributesArray
                    .getColor(R.styleable.MeterNumberPicker_mnp_textColor, DEFAULT_TEXT_COLOR)
            textSize = attributesArray
                    .getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_textSize, spToPx(DEFAULT_TEXT_SIZE_SP).toInt())
                    .toFloat()
            typeface = Typeface
                    .create(attributesArray.getString(R.styleable.MeterNumberPicker_mnp_typeface), Typeface.NORMAL)
            minWidth = attributesArray
                    .getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_minWidth, dpToPx(DEFAULT_MIN_WIDTH_DP.toFloat()).toInt())
            minHeight = attributesArray
                    .getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_minHeight, dpToPx(DEFAULT_MIN_HEIGHT_DP.toFloat()).toInt())
            paddingHorizontal = attributesArray
                    .getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_paddingHorizontal, dpToPx(paddingHorizontal.toFloat()).toInt())
            paddingVertical = attributesArray
                    .getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_paddingVertical, dpToPx(paddingVertical.toFloat()).toInt())
        }

        textPaint
                .isAntiAlias = true
        textPaint
                .textAlign = Paint
                .Align
                .CENTER

        val configuration = ViewConfiguration
                .get(context)
        minimumFlingVelocity = configuration
                .scaledMinimumFlingVelocity
        maximumFlingVelocity = configuration.scaledMaximumFlingVelocity / MAX_FLING_VELOCITY_ADJUSTMENT

        flingScroller = Scroller(context, null, true)
        adjustScroller = Scroller(context, DecelerateInterpolator(2.5f))
    }

    // =============================================================================================
    // -------------------------------------- MEASURING --------------------------------------------
    // =============================================================================================

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = measureWidth(widthMeasureSpec)
        val heightSize = measureHeight(heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val specMode = View
                .MeasureSpec
                .getMode(widthMeasureSpec)
        val specSize = View
                .MeasureSpec
                .getSize(widthMeasureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            else -> Math.max(minWidth, calculateTextWidthWithInternalPadding()) + paddingLeft + paddingRight
        }
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val specMode = View
                .MeasureSpec
                .getMode(heightMeasureSpec)
        val specSize = View
                .MeasureSpec
                .getSize(heightMeasureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            else -> Math.max(minHeight, calculateTextHeightWithInternalPadding()) + paddingTop + paddingBottom
        }
    }

    private fun calculateTextWidthWithInternalPadding(): Int {
        return calculateTextWidth() + paddingHorizontal * 2
    }

    private fun calculateTextHeightWithInternalPadding(): Int {
        return calculateTextHeight() + paddingVertical * 2
    }

    private fun calculateTextWidth(): Int {
        var maxDigitWidth = 0f
        for (i in 0..9) {
            val digitWidth = textPaint
                    .measureText(formatNumberWithLocale(i))
            if (digitWidth > maxDigitWidth) {
                maxDigitWidth = digitWidth
            }
        }
        var numberOfDigits = 0
        var current = maxValue
        while (current > 0) {
            numberOfDigits++
            current /= 10
        }
        return (numberOfDigits * maxDigitWidth)
                .toInt()
    }

    private fun calculateTextHeight(): Int {
        val bounds = Rect()
        textPaint
                .getTextBounds("0", 0, 1, bounds)
        textHeight = bounds
                .height()
        return textHeight
    }

    // =============================================================================================
    // -------------------------------------- DRAWING ----------------------------------------------
    // =============================================================================================

    override fun onDraw(canvas: Canvas) {
        super
                .onDraw(canvas)
        val measuredHeight = measuredHeight

        val x = ((right - left) / 2)
                .toFloat()
        val y = ((bottom - top) / 2 + textHeight / 2)
                .toFloat()

        val currentValueStart = (y + currentScrollOffset)
                .toInt()
        val prevValueStart = currentValueStart - measuredHeight
        val nextValueStart = currentValueStart + measuredHeight

        canvas
                .drawText(getValue(currentValueOffset + 1).toString() + "", x, prevValueStart.toFloat(), textPaint)
        canvas
                .drawText(getValue(currentValueOffset).toString() + "", x, currentValueStart.toFloat(), textPaint)
        canvas
                .drawText(getValue(currentValueOffset - 1).toString() + "", x, nextValueStart.toFloat(), textPaint)
    }

    // =============================================================================================
    // ----------------------------------- TOUCH & SCROLL ------------------------------------------
    // =============================================================================================

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker
                    .obtain()
        }
        velocityTracker!!
                .addMovement(event)

        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!flingScroller.isFinished) {
                    flingScroller
                            .forceFinished(true)
                }
                if (!adjustScroller.isFinished) {
                    adjustScroller
                            .forceFinished(true)
                }

                lastDownEventY = event
                        .y

                // Disallow ScrollView to intercept touch events.
                this
                        .parent
                        .requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                lastDownOrMoveEventY = event
                        .y
                val rawScrollOffset = (lastDownOrMoveEventY - lastDownEventY)
                        .toInt()
                calculateCurrentOffsets(rawScrollOffset, measuredHeight)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker!!
                        .computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                val initialVelocity = velocityTracker!!
                        .yVelocity
                        .toInt()
                if (Math.abs(initialVelocity) > minimumFlingVelocity) {
                    fling(initialVelocity)
                } else {
                    val rawScrollOffset = (lastDownOrMoveEventY - lastDownEventY)
                            .toInt()
                    val measuredHeight = measuredHeight
                    val adjustedValueOffset = calculateAdjustedValueOffset(rawScrollOffset, measuredHeight)
                    calculateCurrentOffsets(rawScrollOffset, measuredHeight)
                    value = getValue(adjustedValueOffset)
                    adjust(measuredHeight, adjustedValueOffset)
                }
                invalidate()
                velocityTracker!!
                        .recycle()
                velocityTracker = null

                // Allow ScrollView to intercept touch events.
                this
                        .parent
                        .requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun computeScroll() {
        var scroller = flingScroller
        if (scroller.isFinished) {
            scroller = adjustScroller
            if (scroller.isFinished) {
                return
            }
        }
        scroller
                .computeScrollOffset()
        val currentScrollerY = scroller
                .currY
        val diffScrollY = scrollerLastY - currentScrollerY
        currentScrollOffset -= diffScrollY
        scrollerLastY = currentScrollerY

        if (adjustScroller.isFinished) {
            if (flingScroller.isFinished) {
                if (currentScrollOffset != 0) {
                    val measuredHeight = measuredHeight
                    val adjustedValueOffset = calculateAdjustedValueOffset(measuredHeight)
                    value = getValue(adjustedValueOffset)
                    adjust(measuredHeight, adjustedValueOffset)
                }
            } else {
                val newScrollOffset = currentScrollOffset % measuredHeight
                if (newScrollOffset != currentScrollOffset) {
                    val numberOfValuesScrolled = (currentScrollOffset - newScrollOffset) / measuredHeight
                    currentValueOffset += numberOfValuesScrolled
                    currentScrollOffset = newScrollOffset
                }
            }
        }

        invalidate()
    }

    private fun calculateCurrentOffsets(rawScrollOffset: Int, measuredHeight: Int) {
        currentValueOffset = rawScrollOffset / measuredHeight
        currentScrollOffset = Math.abs(rawScrollOffset) - Math.abs(currentValueOffset) * measuredHeight
        currentScrollOffset *= if (rawScrollOffset < 0) -1 else 1
    }

    private fun calculateAdjustedValueOffset(rawScrollOffset: Int, measuredHeight: Int): Int {
        val currentValueOffset = rawScrollOffset.toDouble() / measuredHeight.toDouble()
        return (currentValueOffset + 0.5 * if (currentValueOffset < 0) -1.0 else 1.0)
                .toInt()
    }

    /**
     * Calculating adjusted value offset based only on the current scroll offset
     *
     * @return currentValueOffset if no changes should be applied, currentValueOffset + 1 or currentValueOffset - 1
     */
    private fun calculateAdjustedValueOffset(measuredHeight: Int): Int {
        return if (Math.abs(currentScrollOffset) < measuredHeight / 2) {
            currentValueOffset
        } else {
            currentValueOffset + if (currentScrollOffset < 0) -1 else 1
        }
    }

    private fun adjust(measuredHeight: Int, adjustedValueOffset: Int) {
        if (adjustedValueOffset != currentValueOffset) {
            if (currentScrollOffset < 0) {
                currentScrollOffset += measuredHeight
            } else {
                currentScrollOffset -= measuredHeight
            }
        }
        scrollerLastY = currentScrollOffset
        currentValueOffset = 0
        adjustScroller
                .startScroll(0, currentScrollOffset, 0, -currentScrollOffset, ADJUSTMENT_DURATION_MILLIS)
    }

    private fun fling(velocity: Int) {
        if (velocity > 0) {
            scrollerLastY = 0
            flingScroller
                    .fling(0, scrollerLastY, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
        } else {
            scrollerLastY = Integer
                    .MAX_VALUE
            flingScroller
                    .fling(0, scrollerLastY, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
        }
    }

    // =============================================================================================
    // -------------------------------------- UTILS ------------------------------------------------
    // =============================================================================================

    private fun getValue(offset: Int): Int {
        var checkedOffset = offset
        checkedOffset %= maxValue - minValue
        if (value + checkedOffset < minValue) {
            return maxValue - (Math.abs(checkedOffset) - (value - minValue)) + 1
        } else if (value + checkedOffset > maxValue) {
            return minValue + checkedOffset - (maxValue - value) - 1
        }
        return value + checkedOffset
    }

    private fun formatNumberWithLocale(value: Int): String {
        return String
                .format(Locale.getDefault(), "%d", value)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return sp * resources.displayMetrics.scaledDensity
    }

    // =============================================================================================
    // --------------------------------- GETTERS & SETTERS -----------------------------------------
    // =============================================================================================

    /**
     * Set text color
     * @param colorRes color resource
     */
    fun setTextColorRes(@ColorRes colorRes: Int) {
        textColor = ContextCompat
                .getColor(context, colorRes)
    }

    /**
     * Set text size
     * @param textSizeRes text size
     */
    fun setTextSizeRes(@DimenRes textSizeRes: Int) {
        textSize = resources
                .getDimensionPixelSize(textSizeRes)
                .toFloat()
    }

    /**
     * Set typeface
     * @param string text
     * @param style text style
     */
    @JvmOverloads
    fun setTypeface(string: String, style: Int = Typeface.NORMAL) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        typeface = Typeface
                .create(string, style)
    }

    /**
     * Set typeface
     * @param stringRes text
     * @param style text style
     */
    @JvmOverloads
    fun setTypeface(@StringRes stringRes: Int, style: Int = Typeface.NORMAL) {
        setTypeface(resources.getString(stringRes), style)
    }

    /**
     * Set minimum width
     * @param width
     */
    fun setMinWidthRes(@DimenRes width: Int) {
        minWidth = resources
                .getDimensionPixelSize(width)
    }

    /**
     * Set minimum height
     * @param height
     */
    fun setMinHeightRes(@DimenRes height: Int) {
        minHeight = resources
                .getDimensionPixelSize(height)
    }

    /**
     * Set vertical padding
     * @param padding
     */
    fun setVerticalPaddingRes(@DimenRes padding: Int) {
        paddingVertical = resources
                .getDimensionPixelSize(padding)
    }

    /**
     * Set horizontal padding
     */
    fun setHorizontalPaddingRes(@DimenRes padding: Int) {
        paddingHorizontal = resources
                .getDimensionPixelSize(padding)
    }

    companion object {

        private const val DEFAULT_MIN_HEIGHT_DP = 20
        private const val DEFAULT_MIN_WIDTH_DP = 14
        private const val DEFAULT_MAX_VALUE = 9
        private const val DEFAULT_MIN_VALUE = 0
        private const val DEFAULT_VALUE = 0
        private const val DEFAULT_TEXT_COLOR = -0x1000000
        private const val DEFAULT_TEXT_SIZE_SP = 25f
        /**
         * The default internal padding for the text (do not mix up with view paddings -
         * this is separate thing)
         */
        private const val DEFAULT_PADDING = 2
        private const val ADJUSTMENT_DURATION_MILLIS = 800
        /**
         * The coefficient by which to adjust (divide) the max fling velocity.
         */
        private const val MAX_FLING_VELOCITY_ADJUSTMENT = 6
    }
}
