package com.mybenru.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mybenru.app.R
import kotlin.math.min

/**
 * Custom view for displaying novel covers with progress indicators and badges
 */
class NovelCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Cover image
    private var coverDrawable: Drawable? = null

    // Text
    private var title: String? = null
    private var author: String? = null

    // Progress
    private var progress: Float = 0f

    // Badge
    private var badgeText: String? = null
    private var badgeColor: Int = Color.RED

    // Paints
    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
        style = Paint.Style.FILL
    }
    private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = context.resources.getDimensionPixelSize(R.dimen.novel_cover_title_text_size).toFloat()
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }
    private val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = context.resources.getDimensionPixelSize(R.dimen.novel_cover_author_text_size).toFloat()
        setShadowLayer(2f, 0f, 0f, Color.BLACK)
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = context.resources.getDimensionPixelSize(R.dimen.novel_cover_badge_text_size).toFloat()
        textAlign = Paint.Align.CENTER
    }

    // Measurements
    private val progressHeight = context.resources.getDimensionPixelSize(R.dimen.novel_cover_progress_height).toFloat()
    private val badgePadding = context.resources.getDimensionPixelSize(R.dimen.novel_cover_badge_padding).toFloat()
    private val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.novel_cover_corner_radius).toFloat()

    // Rectangles for drawing
    private val coverRect = RectF()
    private val progressRect = RectF()
    private val badgeRect = RectF()
    private val textBounds = Rect()

    init {
        // Read attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NovelCoverView)
        try {
            coverDrawable = typedArray.getDrawable(R.styleable.NovelCoverView_coverDrawable)
            title = typedArray.getString(R.styleable.NovelCoverView_title)
            author = typedArray.getString(R.styleable.NovelCoverView_author)
            progress = typedArray.getFloat(R.styleable.NovelCoverView_progress, 0f)
            badgeText = typedArray.getString(R.styleable.NovelCoverView_badgeText)
            badgeColor = typedArray.getColor(R.styleable.NovelCoverView_badgeColor, Color.RED)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Update rectangles based on new size
        coverRect.set(0f, 0f, width.toFloat(), height.toFloat())
        progressRect.set(0f, height - progressHeight, width.toFloat() * progress, height.toFloat())

        // Prepare badge rect (will be positioned in onDraw)
        badgeRect.set(0f, 0f, 0f, 0f)  // Will be set in onDraw if badge is present
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw cover background
        canvas.drawRoundRect(coverRect, cornerRadius, cornerRadius, coverPaint)

        // Draw cover image
        coverDrawable?.let {
            it.setBounds(0, 0, width, height)
            it.draw(canvas)
        }

        // Draw title and author text with shadow
        drawText(canvas)

        // Draw progress indicator
        if (progress > 0) {
            canvas.drawRect(progressRect, progressPaint)
        }

        // Draw badge if present
        drawBadgeIfNeeded(canvas)
    }

    private fun drawText(canvas: Canvas) {
        title?.let {
            titlePaint.getTextBounds(it, 0, it.length, textBounds)

            // Draw title at the bottom of the cover with shadow
            canvas.drawText(
                it,
                paddingStart.toFloat(),
                height - paddingBottom.toFloat() - progressHeight - textBounds.height(),
                titlePaint
            )

            // Draw author name if available
            author?.let { authorText ->
                canvas.drawText(
                    authorText,
                    paddingStart.toFloat(),
                    height - paddingBottom.toFloat() - progressHeight - textBounds.height() - authorPaint.textSize - 4,
                    authorPaint
                )
            }
        }
    }

    private fun drawBadgeIfNeeded(canvas: Canvas) {
        badgeText?.let {
            if (it.isNotBlank()) {
                // Calculate badge size
                badgeTextPaint.getTextBounds(it, 0, it.length, textBounds)
                val badgeWidth = textBounds.width() + 2 * badgePadding
                val badgeHeight = textBounds.height() + 2 * badgePadding

                // Position badge in top-right corner
                badgeRect.set(
                    width - badgeWidth,
                    0f,
                    width.toFloat(),
                    badgeHeight
                )

                // Draw badge background
                badgePaint.color = badgeColor
                canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, badgePaint)

                // Draw badge text
                canvas.drawText(
                    it,
                    badgeRect.centerX(),
                    badgeRect.centerY() + textBounds.height() / 2,
                    badgeTextPaint
                )
            }
        }
    }

    // Public methods to update the view's properties

    /**
     * Set cover drawable
     */
    fun setCoverDrawable(drawable: Drawable?) {
        coverDrawable = drawable
        invalidate()
    }

    /**
     * Set title text
     */
    fun setTitle(text: String?) {
        title = text
        invalidate()
    }

    /**
     * Set author text
     */
    fun setAuthor(text: String?) {
        author = text
        invalidate()
    }

    /**
     * Set progress value (0.0f to 1.0f)
     */
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        progressRect.right = width.toFloat() * progress
        invalidate()
    }

    /**
     * Set badge text
     */
    fun setBadgeText(text: String?) {
        badgeText = text
        invalidate()
    }

    /**
     * Set badge color
     */
    fun setBadgeColor(color: Int) {
        badgeColor = color
        invalidate()
    }
}