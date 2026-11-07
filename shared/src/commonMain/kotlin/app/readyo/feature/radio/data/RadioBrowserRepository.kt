package app.readyo.feature.radio.data

import app.readyo.core.model.AudioItem
import app.readyo.feature.radio.domain.RadioRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RadioBrowserRepository : RadioRepository {
    fun close() {
        client.close()
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 12000
            connectTimeoutMillis = 6000
        }
    }
    private val mirrors = listOf("de1", "at1", "nl1").shuffled()

    override suspend fun stations(
        query: String,
        tag: String,
        country: String,
        language: String,
    ): List<AudioItem> {
        var failure: Exception? = null
        for (mirror in mirrors) try {
            val response =
                client.get("https://$mirror.api.radio-browser.info/json/stations/search") {
                    header("User-Agent", "ReadyO/0.1")
                    parameter("name", query)
                    parameter("tag", tag)
                    parameter("country", country)
                    parameter("language", language)
                    parameter("hidebroken", "true")
                    parameter("order", "clickcount")
                    parameter("reverse", "true")
                    parameter("limit", 40)
                }
            check(response.status.value in 200..299)
            return parseStations(response.bodyAsText())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            failure = exception
        }
        throw failure ?: IllegalStateException("Directory unavailable")
    }
}

internal fun parseStations(json: String): List<AudioItem> =
    Json.parseToJsonElement(json)
        .jsonArray
        .mapNotNull { element ->
            val station = element.jsonObject
            fun field(key: String) = station[key]?.jsonPrimitive?.contentOrNull.orEmpty()
            val uri = field("url_resolved").ifBlank { field("url") }
            if (
                !(uri.startsWith("https://") || uri.startsWith("http://")) ||
                    field("stationuuid").isBlank() ||
                    field("name").isBlank()
            )
                null
            else
                AudioItem(
                    field("stationuuid"),
                    field("name").trim(),
                    listOf(
                            field("country"),
                            field("tags")
                                .split(',')
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString(" · "),
                        )
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    uri,
                    true,
                    field("favicon"),
                )
        }
        .distinctBy { it.id }
