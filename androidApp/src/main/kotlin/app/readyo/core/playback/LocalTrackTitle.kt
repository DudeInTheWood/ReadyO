package app.readyo.core.playback

internal fun localTrackTitle(tag: String?, fallback: String): String {
    val title = tag?.trim()
    return if (title.isNullOrEmpty() || title.equals("<unknown>", ignoreCase = true)) fallback
    else title
}
