package com.mybenru.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.mybenru.app.R
import kotlin.math.max
import kotlin.math.min

/**
 * Custom view for displaying library tabs with indicator
 */
class LibraryTabsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Tab data
    private var tabs = listOf("All", "Unread", "Completed", "Downloaded")
    private var selectedTabIndex = 0
    private var tabWidths = FloatArray(tabs.size)
    private var tabPositions = FloatArray(tabs.size + 1) // +1 for end position

    // Listeners
    private var onTabSelectedListener: ((Int) -> Unit)? = null

    // Paints
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 16 * resources.displayMetrics.scaledDensity
    }

    private val indicatorPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.colorAccent)
        style = Paint.Style.FILL
    }

    // Cached measurements
    private val textBounds = Rect()

    // Constants
    private val indicatorHeight = 4f * resources.displayMetrics.density
    private val padding = 16f * resources.displayMetrics.density

    init {
        calculateTabWidths()
    }

    /**
     * Set the tabs to display
     */
    fun setTabs(tabs: List<String>) {
        this.tabs = tabs
        selectedTabIndex = 0
        calculateTabWidths()
        invalidate()
    }

    /**
     * Set the selected tab index
     */
    fun setSelectedTabIndex(index: Int) {
        if (index in tabs.indices && index != selectedTabIndex) {
            selectedTabIndex = index
            invalidate()
            onTabSelectedListener?.invoke(selectedTabIndex)
        }
    }

    /**
     * Set tab selected listener
     */
    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelectedListener = listener
    }

    /**
     * Calculate the width for each tab based on text size
     */
    private fun calculateTabWidths() {
        if (tabs.isEmpty()) return

        tabWidths = FloatArray(tabs.size)
        var totalFixedWidth = 0f

        // Measure each tab's text width
        for (i in tabs.indices) {
            textPaint.getTextBounds(tabs[i], 0, tabs[i].length, textBounds)
            tabWidths[i] = textBounds.width() + (padding * 2)
            totalFixedWidth += tabWidths[i]
        }

        // Calculate positions
        var currentPosition = 0f
        tabPositions = FloatArray(tabs.size + 1)

        for (i in tabs.indices) {
            tabPositions[i] = currentPosition
            currentPosition += tabWidths[i]
        }

        // Add end position
        tabPositions[tabs.size] = currentPosition
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get the width based on the measure spec
        val width = MeasureSpec.getSize(widthMeasureSpec)

        // Calculate height based on the font height and padding
        textPaint.getTextBounds("Tg", 0, 2, textBounds) // "Tg" to include ascent and descent
        val fontHeight = textBounds.height()
        val desiredHeight = fontHeight + (padding * 2) + indicatorHeight

        val height = max(
            desiredHeight.toInt(),
            (suggestedMinimumHeight + paddingTop + paddingBottom)
        )

        // Set measured dimensions
        setMeasuredDimension(
            width,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )

        // Recalculate positions based on the actual width
        val totalTabs = tabs.size
        if (totalTabs > 0) {
            val tabWidth = width.toFloat() / totalTabs

            for (i in 0 until totalTabs) {
                tabPositions[i] = i * tabWidth
            }
            tabPositions[totalTabs] = width.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (tabs.isEmpty()) return

        // Calculate vertical position for text
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = textHeight / 2 - textPaint.descent()
        val centerY = height / 2f

        // Draw each tab
        for (i in tabs.indices) {
            val tabCenter = (tabPositions[i] + tabPositions[i + 1]) / 2

            textPaint.typeface = if (i == selectedTabIndex) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textPaint.color = if (i == selectedTabIndex) {
                ContextCompat.getColor(context, R.color.colorAccent)
            } else {
                ContextCompat.getColor(context, android.R.color.darker_gray)
            }

            // Draw the tab text
            canvas.drawText(tabs[i], tabCenter, centerY + textOffset, textPaint)
        }

        // Draw the indicator for the selected tab
        if (selectedTabIndex in tabs.indices) {
            val left = tabPositions[selectedTabIndex]
            val right = tabPositions[selectedTabIndex + 1]

            // Draw a rectangle at the bottom
            canvas.drawRect(
                left,
                height - indicatorHeight,
                right,
                height.toFloat(),
                indicatorPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Determine which tab was clicked
            val x = event.x
            for (i in tabs.indices) {
                if (x >= tabPositions[i] && x < tabPositions[i + 1]) {
                    setSelectedTabIndex(i)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}