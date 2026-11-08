package app.readyo.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AudioItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val uri: String,
    val radio: Boolean,
    val artwork: String = "",
    val genre: String? = null,
)
