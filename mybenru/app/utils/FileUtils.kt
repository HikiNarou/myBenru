package com.mybenru.app.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for file operations
 */
object FileUtils {

    /**
     * Get external cache directory path
     */
    fun getExternalCacheDir(context: Context): File? {
        return context.externalCacheDir
    }

    /**
     * Get internal cache directory path
     */
    fun getInternalCacheDir(context: Context): File {
        return context.cacheDir
    }

    /**
     * Get file from cache directory
     */
    fun getCacheFile(context: Context, fileName: String): File {
        val cacheDir = getInternalCacheDir(context)
        return File(cacheDir, fileName)
    }

    /**
     * Get temporary file for image capture
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /**
     * Get content URI from file
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Read text from file
     */
    fun readFileAsText(file: File): String {
        return try {
            file.readText()
        } catch (e: IOException) {
            Timber.e(e, "Error reading file: ${file.absolutePath}")
            ""
        }
    }

    /**
     * Write text to file
     */
    fun writeTextToFile(file: File, content: String): Boolean {
        return try {
            file.writeText(content)
            true
        } catch (e: IOException) {
            Timber.e(e, "Error writing to file: ${file.absolutePath}")
            false
        }
    }

    /**
     * Delete file
     */
    fun deleteFile(file: File): Boolean {
        return if (file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting file: ${file.absolutePath}")
                false
            }
        } else {
            true
        }
    }

    /**
     * Delete directory recursively
     */
    fun deleteDirectory(directory: File): Boolean {
        if (directory.exists()) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
            return directory.delete()
        }
        return true
    }

    /**
     * Get file size
     */
    fun getFileSize(file: File): Long {
        return if (file.exists()) {
            file.length()
        } else {
            0L
        }
    }

    /**
     * Format file size to human readable format
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            "%.1f %s",
            size / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    /**
     * Get directory size recursively
     */
    fun getDirSize(dir: File): Long {
        var size: Long = 0
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    /**
     * Create directory if it doesn't exist
     */
    fun createDirIfNotExists(dir: File): Boolean {
        return if (dir.exists()) {
            dir.isDirectory
        } else {
            dir.mkdirs()
        }
    }

    /**
     * Copy file
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            Timber.e(e, "Error copying file from ${source.absolutePath} to ${destination.absolutePath}")
            false
        }
    }

    /**
     * Get files from directory that match filter
     */
    fun listFiles(directory: File, filter: FileFilter?): List<File> {
        val files = mutableListOf<File>()
        if (!directory.exists() || !directory.isDirectory) {
            return files
        }

        directory.listFiles(filter)?.forEach { file ->
            files.add(file)
        }
        return files
    }

    /**
     * List files by extension
     */
    fun listFilesByExtension(directory: File, extension: String): List<File> {
        return listFiles(directory, object : FileFilter {
            override fun accept(file: File): Boolean {
                return file.isFile && file.name.endsWith(extension, ignoreCase = true)
            }
        })
    }

    /**
     * Read from content URI
     */
    fun readContentUri(contentResolver: ContentResolver, uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: IOException) {
            Timber.e(e, "Error reading from content URI: $uri")
            null
        }
    }

    /**
     * Save data from URI to file
     */
    fun saveContentUriToFile(contentResolver: ContentResolver, sourceUri: Uri, destFile: File): Boolean {
        return try {
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: IOException) {
            Timber.e(e, "Error saving content URI to file: $sourceUri -> ${destFile.absolutePath}")
            false
        }
    }
}