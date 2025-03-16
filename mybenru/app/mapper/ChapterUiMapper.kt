package com.mybenru.app.mapper

import com.mybenru.app.model.ChapterUiModel
import com.mybenru.domain.model.Chapter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper untuk mengonversi model Chapter (domain) ke ChapterUiModel (UI) dan sebaliknya.
 * Penggabungan fungsi-fungsi dari kedua bagian kode.
 */
@Singleton
class ChapterUiMapper @Inject constructor() {

    // Menggunakan pola tanggal dari bagian 2
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Map sebuah Chapter (domain) ke ChapterUiModel (UI).
     * Menggabungkan logika dari kedua bagian, dengan dukungan parameter opsional currentPosition.
     */
    fun mapToUiModel(chapter: Chapter, currentPosition: Int = 0): ChapterUiModel {
        return ChapterUiModel(
            id = chapter.id,
            novelId = chapter.novelId,
            url = chapter.url,
            title = if (chapter.title.isNullOrBlank()) "Chapter ${chapter.number}" else formatChapterTitle(chapter),
            number = chapter.number,
            dateUpload = formatDate(chapter.dateUpload),
            dateFetch = formatDate(chapter.dateFetch),
            sourceOrder = chapter.sourceOrder, // properti dari bagian 1
            isRead = chapter.isRead,
            isDownloaded = chapter.isDownloaded,
            isBookmarked = chapter.isBookmarked,
            currentPosition = currentPosition, // properti dari bagian 1
            // Jika totalWords di Chapter tidak tersedia, hitung dari content
            totalWords = chapter.totalWords ?: (chapter.content?.split("\\s+".toRegex())?.size ?: 0),
            estimatedReadingTime = calculateReadingTime(chapter.content), // dari bagian 1
            isNew = chapter.isNew, // dari bagian 1
            wordCount = chapter.wordCount, // dari bagian 2
            content = chapter.content,     // dari bagian 2
            previousChapterId = chapter.previousChapterId, // dari bagian 2
            nextChapterId = chapter.nextChapterId,         // dari bagian 2
            sourceId = chapter.sourceId,                   // dari bagian 2
            readingPosition = chapter.readingPosition,     // dari bagian 2
            readingProgress = chapter.readingProgress,     // dari bagian 2
            lastRead = chapter.lastRead,                   // dari bagian 2
            readDuration = chapter.readDuration            // dari bagian 2
        )
    }

    /**
     * Map list Chapter (domain) ke list ChapterUiModel (UI).
     * Menggunakan currentPosition dengan nilai default 0.
     */
    fun mapToUiModels(chapters: List<Chapter>, currentPosition: Int = 0): List<ChapterUiModel> {
        return chapters.map { mapToUiModel(it, currentPosition) }
    }

    /**
     * Map sebuah ChapterUiModel (UI) kembali ke Chapter (domain).
     */
    fun mapToDomainModel(chapterUiModel: ChapterUiModel): Chapter {
        return Chapter(
            id = chapterUiModel.id,
            novelId = chapterUiModel.novelId,
            url = chapterUiModel.url,
            title = chapterUiModel.title,
            number = chapterUiModel.number,
            isRead = chapterUiModel.isRead,
            isBookmarked = chapterUiModel.isBookmarked,
            isDownloaded = chapterUiModel.isDownloaded,
            dateUpload = parseDate(chapterUiModel.dateUpload),
            dateFetch = parseDate(chapterUiModel.dateFetch),
            wordCount = chapterUiModel.wordCount,
            content = chapterUiModel.content,
            previousChapterId = chapterUiModel.previousChapterId,
            nextChapterId = chapterUiModel.nextChapterId,
            sourceId = chapterUiModel.sourceId,
            readingPosition = chapterUiModel.readingPosition,
            readingProgress = chapterUiModel.readingProgress,
            totalWords = chapterUiModel.totalWords,
            lastRead = chapterUiModel.lastRead,
            readDuration = chapterUiModel.readDuration,
            sourceOrder = chapterUiModel.sourceOrder, // properti dari bagian 1
            isNew = chapterUiModel.isNew             // properti dari bagian 1
        )
    }

    /**
     * Map list ChapterUiModel (UI) ke list Chapter (domain).
     */
    fun mapToDomainModels(chapterUiModels: List<ChapterUiModel>): List<Chapter> {
        return chapterUiModels.map { mapToDomainModel(it) }
    }

    /**
     * Format judul chapter untuk tampilan.
     */
    private fun formatChapterTitle(chapter: Chapter): String {
        val chapterNumber = chapter.number
        val chapterTitle = chapter.title?.trim() ?: ""
        return if (chapterTitle.isEmpty()) {
            "Chapter $chapterNumber"
        } else if (chapterTitle.startsWith("Chapter", ignoreCase = true)) {
            chapterTitle
        } else {
            "Chapter $chapterNumber: $chapterTitle"
        }
    }

    /**
     * Format timestamp ke string tanggal yang dapat dibaca.
     * Menggunakan pola dari dateFormatter.
     */
    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Unknown"
        return try {
            dateFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Parse string tanggal ke timestamp (Long).
     */
    private fun parseDate(dateString: String): Long {
        if (dateString == "Unknown") return 0
        return try {
            dateFormatter.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Hitung estimasi waktu membaca (dalam menit) berdasarkan konten.
     * Menggunakan kecepatan baca rata-rata 200 kata per menit.
     */
    private fun calculateReadingTime(content: String?): Int {
        if (content.isNullOrEmpty()) return 0
        val wordCount = content.split("\\s+".toRegex()).size
        val readingSpeedWpm = 200
        val minutes = wordCount / readingSpeedWpm
        return if (minutes < 1) 1 else minutes
    }
}
