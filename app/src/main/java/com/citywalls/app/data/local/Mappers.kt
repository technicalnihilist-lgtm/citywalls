package com.citywalls.app.data.local

import com.citywalls.app.domain.DownloadedWallpaper
import com.citywalls.app.domain.Wallpaper

fun Wallpaper.toFavoriteEntity(addedAt: Long): FavoriteEntity = FavoriteEntity(
    id = id,
    title = title,
    thumbUrl = thumbUrl,
    previewUrl = previewUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    artist = artist,
    description = description,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    sourcePageUrl = sourcePageUrl,
    attribution = attribution,
    categoryKey = categoryKey,
    categoryLabel = categoryLabel,
    modificationNote = modificationNote,
    addedAt = addedAt
)

fun Wallpaper.toDownloadEntity(localPath: String, sizeBytes: Long, downloadedAt: Long): DownloadEntity =
    DownloadEntity(
        id = id,
        title = title,
        thumbUrl = thumbUrl,
        previewUrl = previewUrl,
        fullUrl = fullUrl,
        width = width,
        height = height,
        artist = artist,
        description = description,
        licenseName = licenseName,
        licenseUrl = licenseUrl,
        sourcePageUrl = sourcePageUrl,
        attribution = attribution,
        categoryKey = categoryKey,
        categoryLabel = categoryLabel,
        modificationNote = modificationNote,
        localPath = localPath,
        sizeBytes = sizeBytes,
        downloadedAt = downloadedAt
    )

fun FavoriteEntity.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    title = title,
    thumbUrl = thumbUrl,
    previewUrl = previewUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    artist = artist,
    description = description,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    sourcePageUrl = sourcePageUrl,
    attribution = attribution,
    categoryKey = categoryKey,
    categoryLabel = categoryLabel,
    modificationNote = modificationNote
)

fun DownloadEntity.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    title = title,
    thumbUrl = thumbUrl,
    previewUrl = previewUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    artist = artist,
    description = description,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    sourcePageUrl = sourcePageUrl,
    attribution = attribution,
    categoryKey = categoryKey,
    categoryLabel = categoryLabel,
    modificationNote = modificationNote
)

fun DownloadEntity.toDownloadedWallpaper(): DownloadedWallpaper = DownloadedWallpaper(
    wallpaper = toWallpaper(),
    localPath = localPath,
    sizeBytes = sizeBytes,
    downloadedAt = downloadedAt
)
