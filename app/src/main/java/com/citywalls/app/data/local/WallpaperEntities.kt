package com.citywalls.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbUrl: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val artist: String,
    val description: String,
    val licenseName: String,
    val licenseUrl: String,
    val sourcePageUrl: String,
    val attribution: String,
    val categoryKey: String,
    val categoryLabel: String,
    val modificationNote: String,
    val addedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbUrl: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val artist: String,
    val description: String,
    val licenseName: String,
    val licenseUrl: String,
    val sourcePageUrl: String,
    val attribution: String,
    val categoryKey: String,
    val categoryLabel: String,
    val modificationNote: String,
    val localPath: String,
    val sizeBytes: Long,
    val downloadedAt: Long
)
