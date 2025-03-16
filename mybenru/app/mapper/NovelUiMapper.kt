package com.mybenru.app.mapper

import com.mybenru.app.model.NovelUiModel
import com.mybenru.domain.model.Novel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NovelUiMapper @Inject constructor() {

    /**
     * Memetakan model domain [Novel] ke model UI [NovelUiModel].
     *
     * Menggabungkan logika dari keempat bagian kode, termasuk:
     * - Pembersihan dan pemangkasan deskripsi (menghilangkan tag HTML)
     * - Pemetaan status novel ke representasi UI yang lebih ramah
     * - Format rating dan perhitungan bab yang belum dibaca
     * - Mapping properti‑properti lain seperti judul, penulis, genre, dll.
     */
    fun mapToUiModel(novel: Novel): NovelUiModel {
        return NovelUiModel(
            id = novel.id,
            sourceId = novel.sourceId,
            title = novel.title,
            // Gunakan pemrosesan deskripsi agar menghilangkan HTML dan memotong jika terlalu panjang
            description = processDescription(novel.description),
            coverUrl = novel.coverUrl,
            // Mengkonversi koleksi penulis dan genre menjadi list jika diperlukan
            authors = novel.authors.toList(),
            genres = novel.genres.toList(),
            // Memetakan status menggunakan helper untuk mendapatkan tampilan yang sesuai
            status = mapNovelStatus(novel.status),
            // Format rating agar ditampilkan dengan satu desimal atau "N/A" jika tidak ada
            rating = formatRating(novel.rating),
            // Properti‑properti lainnya
            isInLibrary = novel.isInLibrary,
            totalChapters = novel.totalChapters, // Jika pada domain ada chapterCount, pastikan konsistensi properti
            lastReadChapter = novel.lastReadChapter,
            lastReadChapterTitle = novel.lastReadChapterTitle,
            // Hitung jumlah bab yang belum dibaca menggunakan helper
            unreadChapterCount = calculateUnreadChapters(novel.totalChapters, novel.lastReadChapter),
            updateCount = novel.updateCount,
            lastUpdated = novel.lastUpdated,
            downloadedChapters = novel.downloadedChapters,
            readChapters = novel.readChapters,
            readingProgress = novel.readingProgress,
            dateAddedToLibrary = novel.dateAddedToLibrary,
            alternativeTitles = novel.alternativeTitles.toList(),
            language = novel.language,
            popularity = novel.popularity ?: 0,
            contentRating = novel.contentRating ?: "Unknown",
            yearOfRelease = novel.yearOfRelease,
            tags = novel.tags.toList(),
            // Properti tambahan dari bagian pertama
            lastReadPosition = novel.lastReadPosition,
            isNew = novel.isNew,
            isUpdated = novel.isUpdated
        )
    }

    /**
     * Memetakan daftar model domain [Novel] menjadi daftar model UI [NovelUiModel]
     */
    fun mapToUiModels(novels: List<Novel>): List<NovelUiModel> {
        return novels.map { mapToUiModel(it) }
    }

    /**
     * Memetakan model UI [NovelUiModel] kembali ke model domain [Novel].
     *
     * Menggabungkan logika dari bagian ketiga dan keempat kode.
     */
    fun mapToDomainModel(novelUiModel: NovelUiModel): Novel {
        return Novel(
            id = novelUiModel.id,
            sourceId = novelUiModel.sourceId,
            title = novelUiModel.title,
            description = novelUiModel.description,
            coverUrl = novelUiModel.coverUrl,
            // Konversi koleksi ke bentuk set sesuai dengan model domain
            authors = novelUiModel.authors.toSet(),
            genres = novelUiModel.genres.toSet(),
            status = novelUiModel.status,
            rating = novelUiModel.rating,
            // Perhatikan perbedaan penamaan properti: isInLibrary / inLibrary
            inLibrary = novelUiModel.isInLibrary,
            totalChapters = novelUiModel.totalChapters,
            lastReadChapter = novelUiModel.lastReadChapter,
            lastReadChapterTitle = novelUiModel.lastReadChapterTitle,
            unreadChapters = novelUiModel.unreadChapterCount,
            updateCount = novelUiModel.updateCount,
            lastUpdated = novelUiModel.lastUpdated,
            downloadedChapters = novelUiModel.downloadedChapters,
            readChapters = novelUiModel.readChapters,
            readingProgress = novelUiModel.readingProgress,
            dateAddedToLibrary = novelUiModel.dateAddedToLibrary,
            alternativeTitles = novelUiModel.alternativeTitles.toSet(),
            language = novelUiModel.language,
            popularity = novelUiModel.popularity,
            contentRating = novelUiModel.contentRating,
            yearOfRelease = novelUiModel.yearOfRelease,
            tags = novelUiModel.tags.toSet()
        )
    }

    /**
     * Memetakan daftar model UI [NovelUiModel] kembali ke daftar model domain [Novel]
     */
    fun mapToDomainModels(novelUiModels: List<NovelUiModel>): List<Novel> {
        return novelUiModels.map { mapToDomainModel(it) }
    }

    // === Helper Functions ===

    /**
     * Membersihkan deskripsi dari konten HTML dan memotong panjangnya jika melebihi batas.
     */
    private fun processDescription(description: String?): String {
        if (description.isNullOrBlank()) {
            return "No description available"
        }
        var processedDesc = description.replace(Regex("<.*?>"), "")
        val maxDescriptionLength = 300
        if (processedDesc.length > maxDescriptionLength) {
            processedDesc = processedDesc.substring(0, maxDescriptionLength - 3) + "..."
        }
        return processedDesc
    }

    /**
     * Memetakan status novel ke representasi UI yang lebih ramah.
     */
    private fun mapNovelStatus(status: String?): String {
        return when (status?.lowercase()) {
            "ongoing" -> "Ongoing"
            "completed" -> "Completed"
            "hiatus" -> "On Hiatus"
            "abandoned" -> "Abandoned"
            else -> status ?: "Unknown"
        }
    }

    /**
     * Memformat rating untuk ditampilkan di UI.
     */
    private fun formatRating(rating: Float?): String {
        return if (rating != null && rating > 0) {
            String.format("%.1f", rating)
        } else {
            "N/A"
        }
    }

    /**
     * Menghitung jumlah bab yang belum dibaca.
     */
    private fun calculateUnreadChapters(total: Int?, lastRead: Int?): Int {
        if (total == null || lastRead == null) return 0
        val unread = total - lastRead
        return if (unread > 0) unread else 0
    }
}
