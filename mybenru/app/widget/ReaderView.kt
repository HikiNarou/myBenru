package com.mybenru.app.widget

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ScrollView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import com.mybenru.app.R
import com.mybenru.app.model.ReaderSettingsUiModel
import kotlin.math.abs

/**
 * Custom view for displaying novel content with advanced reading features
 */
class ReaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    // Content container
    private val contentContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(32, 32, 32, 32)
    }

    // Content paragraphs
    private val contentTextViews = mutableListOf<TextView>()

    // Text content
    private var content: List<String> = emptyList()

    // Gesture detector for tap detection
    private val gestureDetector = GestureDetector(context, GestureListener())

    // Search highlight
    private var searchResults = mutableListOf<SearchResult>()
    private var currentHighlightIndex = -1
    private var searchQuery = ""

    // Settings
    private var currentTextSize = 18f
    private var currentLineSpacing = 1.5f
    private var currentFontFamily = "Default"
    private var currentAlignment = ReaderSettingsUiModel.TextAlignment.JUSTIFIED
    private var currentBackgroundColor = Color.WHITE
    private var currentTextColor = Color.BLACK
    private var currentParagraphSpacing = 16

    // Callback for tap events
    private var onTapListener: ((TapRegion) -> Unit)? = null

    // Callback for scroll events
    private var onScrollListener: ((Int, Int) -> Unit)? = null

    // Represents a search match within the text
    data class SearchResult(
        val textViewIndex: Int,      // Index of the TextView containing the match
        val startPos: Int,           // Start position of match in the text
        val endPos: Int              // End position of match in the text
    )

    // Regions for tap detection
    enum class TapRegion {
        LEFT, CENTER, RIGHT
    }

    init {
        // Add content container to this ScrollView
        addView(contentContainer)

        // Set up default properties
        isSmoothScrollingEnabled = true
        isVerticalScrollBarEnabled = true

        // Set scrollbar style
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
    }

    /**
     * Set the text content to be displayed
     */
    fun setContent(paragraphs: List<String>) {
        // Save content
        content = paragraphs

        // Clear existing text views
        contentTextViews.clear()
        contentContainer.removeAllViews()

        // Add each paragraph as a TextView
        paragraphs.forEach { paragraph ->
            val textView = createParagraphTextView(paragraph)
            contentContainer.addView(textView)
            contentTextViews.add(textView)
        }

        // Clear any existing search results
        clearHighlights()
    }

    /**
     * Create a TextView for a paragraph with appropriate styling
     */
    private fun createParagraphTextView(text: String): TextView {
        return TextView(context).apply {
            val parsedText = parseHtmlText(text)
            setText(parsedText, TextView.BufferType.SPANNABLE)
            textSize = currentTextSize
            setLineSpacing(0f, currentLineSpacing)
            setTextColor(currentTextColor)

            // Apply font family
            typeface = when (currentFontFamily) {
                "Serif" -> android.graphics.Typeface.SERIF
                "Sans-Serif" -> android.graphics.Typeface.SANS_SERIF
                "Monospace" -> android.graphics.Typeface.MONOSPACE
                else -> android.graphics.Typeface.DEFAULT
            }

            // Apply text alignment
            textAlignment = when (currentAlignment) {
                ReaderSettingsUiModel.TextAlignment.LEFT -> TextView.TEXT_ALIGNMENT_TEXT_START
                ReaderSettingsUiModel.TextAlignment.CENTER -> TextView.TEXT_ALIGNMENT_CENTER
                ReaderSettingsUiModel.TextAlignment.RIGHT -> TextView.TEXT_ALIGNMENT_TEXT_END
                ReaderSettingsUiModel.TextAlignment.JUSTIFIED -> TextView.TEXT_ALIGNMENT_TEXT_START
            }

            // Apply paragraph spacing
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = currentParagraphSpacing
            }
            layoutParams = params
        }
    }

    /**
     * Parse HTML formatting in text
     */
    private fun parseHtmlText(text: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text)
        }
    }

    /**
     * Apply reader settings
     */
    fun applySettings(settings: ReaderSettingsUiModel) {
        // Save settings
        currentTextSize = settings.textSize.toFloat()
        currentLineSpacing = settings.lineSpacing
        currentFontFamily = settings.fontFamily
        currentAlignment = settings.textAlignment
        currentParagraphSpacing = settings.paragraphSpacing

        // Apply theme
        when (settings.theme) {
            ReaderSettingsUiModel.ReaderTheme.LIGHT -> {
                currentBackgroundColor = Color.WHITE
                currentTextColor = Color.BLACK
            }
            ReaderSettingsUiModel.ReaderTheme.DARK -> {
                currentBackgroundColor = Color.parseColor("#303030")
                currentTextColor = Color.parseColor("#E0E0E0")
            }
            ReaderSettingsUiModel.ReaderTheme.SEPIA -> {
                currentBackgroundColor = Color.parseColor("#FBF0D9")
                currentTextColor = Color.parseColor("#5B4636")
            }
            ReaderSettingsUiModel.ReaderTheme.BLACK -> {
                currentBackgroundColor = Color.BLACK
                currentTextColor = Color.parseColor("#AAAAAA")
            }
        }

        // Apply settings to container
        contentContainer.setBackgroundColor(currentBackgroundColor)
        contentContainer.setPadding(
            settings.marginHorizontal,
            settings.marginVertical,
            settings.marginHorizontal,
            settings.marginVertical
        )

        // Apply settings to all TextViews
        contentTextViews.forEach { textView ->
            textView.textSize = currentTextSize
            textView.setLineSpacing(0f, currentLineSpacing)
            textView.setTextColor(currentTextColor)

            // Apply font family
            textView.typeface = when (currentFontFamily) {
                "Serif" -> android.graphics.Typeface.SERIF
                "Sans-Serif" -> android.graphics.Typeface.SANS_SERIF
                "Monospace" -> android.graphics.Typeface.MONOSPACE
                else -> android.graphics.Typeface.DEFAULT
            }

            // Apply text alignment
            textView.textAlignment = when (currentAlignment) {
                ReaderSettingsUiModel.TextAlignment.LEFT -> TextView.TEXT_ALIGNMENT_TEXT_START
                ReaderSettingsUiModel.TextAlignment.CENTER -> TextView.TEXT_ALIGNMENT_CENTER
                ReaderSettingsUiModel.TextAlignment.RIGHT -> TextView.TEXT_ALIGNMENT_TEXT_END
                ReaderSettingsUiModel.TextAlignment.JUSTIFIED -> TextView.TEXT_ALIGNMENT_TEXT_START
            }

            // Apply paragraph spacing
            val params = textView.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = currentParagraphSpacing
            textView.layoutParams = params
        }

        // Re-apply highlights if there are any
        if (searchResults.isNotEmpty()) {
            highlightSearchResults(searchQuery)
        }

        // Apply background color to parent view
        setBackgroundColor(currentBackgroundColor)
    }

    /**
     * Set a listener for tap events
     */
    fun setOnTapListener(listener: (TapRegion) -> Unit) {
        onTapListener = listener
    }

    /**
     * Set a listener for scroll events
     */
    fun setOnScrollListener(listener: (Int, Int) -> Unit) {
        onScrollListener = listener
    }

    /**
     * Highlight search results in the text
     * Returns the number of matches found
     */
    fun highlightSearchResults(query: String): Int {
        if (query.isBlank() || contentTextViews.isEmpty()) {
            return 0
        }

        // Save the query for reapplying highlights later if needed
        searchQuery = query.lowercase()

        // Clear previous highlights
        clearHighlights()

        // Search for matches in each TextView
        contentTextViews.forEachIndexed { index, textView ->
            val text = textView.text.toString().lowercase()
            val spannable = textView.text.toSpannable()

            var startPos = 0
            while (startPos != -1) {
                startPos = text.indexOf(searchQuery, startPos)
                if (startPos != -1) {
                    val endPos = startPos + searchQuery.length

                    // Add highlight span
                    val highlightSpan = BackgroundColorSpan(
                        ContextCompat.getColor(context, R.color.search_highlight)
                    )
                    spannable.setSpan(
                        highlightSpan,
                        startPos,
                        endPos,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // Save this result
                    searchResults.add(SearchResult(index, startPos, endPos))

                    // Move to next position
                    startPos = endPos
                }
            }

            // Update the TextView with the highlighted text
            textView.text = spannable
        }

        // If results were found, highlight the first one
        if (searchResults.isNotEmpty()) {
            currentHighlightIndex = 0
            highlightCurrentSearchResult()
        }

        return searchResults.size
    }

    /**
     * Navigate to the next search result
     */
    fun navigateToNextSearchResult() {
        if (searchResults.isEmpty()) return

        // Move to the next result
        currentHighlightIndex = (currentHighlightIndex + 1) % searchResults.size
        highlightCurrentSearchResult()
    }

    /**
     * Navigate to the previous search result
     */
    fun navigateToPreviousSearchResult() {
        if (searchResults.isEmpty()) return

        // Move to the previous result
        currentHighlightIndex = (currentHighlightIndex - 1 + searchResults.size) % searchResults.size
        highlightCurrentSearchResult()
    }

    /**
     * Highlight the current search result and scroll to it
     */
    private fun highlightCurrentSearchResult() {
        if (currentHighlightIndex < 0 || currentHighlightIndex >= searchResults.size) return

        // Reset all highlights to normal color
        searchResults.forEach { result ->
            val textView = contentTextViews[result.textViewIndex]
            val spannable = textView.text as SpannableString

            // Find all BackgroundColorSpan
            val spans = spannable.getSpans(
                0, spannable.length,
                BackgroundColorSpan::class.java
            )

            // Remove highlighted spans and add normal ones
            spans.forEach { span ->
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                spannable.removeSpan(span)
                spannable.setSpan(
                    BackgroundColorSpan(ContextCompat.getColor(context, R.color.search_highlight)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Highlight the current result with a different color
        val currentResult = searchResults[currentHighlightIndex]
        val textView = contentTextViews[currentResult.textViewIndex]
        val spannable = textView.text as SpannableString

        // Add the highlighted span
        spannable.setSpan(
            BackgroundColorSpan(ContextCompat.getColor(context, R.color.search_highlight_current)),
            currentResult.startPos, currentResult.endPos,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Scroll to the highlighted position
        post {
            val tvTop = textView.top
            val lineHeight = textView.lineHeight
            val lineCount = textView.lineCount

            // Calculate the position of the highlight in the textview
            var lineIndex = 0
            var charCount = 0
            for (i in 0 until lineCount) {
                val lineEnd = textView.layout.getLineEnd(i)
                if (charCount + lineEnd >= currentResult.startPos) {
                    lineIndex = i
                    break
                }
                charCount += lineEnd
            }

            val scrollY = tvTop + lineIndex * lineHeight
            smoothScrollTo(0, scrollY)
        }
    }

    /**
     * Clear all search highlights
     */
    fun clearHighlights() {
        searchResults.clear()
        currentHighlightIndex = -1
        searchQuery = ""

        // Remove highlights from all text views
        contentTextViews.forEach { textView ->
            val text = textView.text
            if (text is SpannableString) {
                val spans = text.getSpans(
                    0, text.length,
                    BackgroundColorSpan::class.java
                )
                spans.forEach { text.removeSpan(it) }
                textView.text = text
            }
        }
    }

    /**
     * Handle touch events for tap detection
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Let the gesture detector handle the event
        val gestureHandled = gestureDetector.onTouchEvent(event)

        // If the gesture detector didn't handle it, let the parent handle it
        return gestureHandled || super.onTouchEvent(event)
    }

    /**
     * Handle scroll events
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        // Notify the scroll listener
        onScrollListener?.invoke(t, height)
    }

    /**
     * Gesture detector for tap events
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Calculate which region was tapped
            val width = width.toFloat()
            val x = e.x
            val region = when {
                x < width / 3 -> TapRegion.LEFT
                x > width * 2 / 3 -> TapRegion.RIGHT
                else -> TapRegion.CENTER
            }

            // Notify the tap listener
            onTapListener?.invoke(region)

            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Implement fling behavior if needed
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Double tap functionality could be added here
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            // Long press functionality could be added here
        }
    }
}