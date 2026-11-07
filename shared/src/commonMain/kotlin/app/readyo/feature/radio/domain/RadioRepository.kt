package app.readyo.feature.radio.domain

import app.readyo.core.model.AudioItem

interface RadioRepository {
    suspend fun stations(
        query: String = "",
        tag: String = "",
        country: String = "",
        language: String = "",
    ): List<AudioItem>
}
