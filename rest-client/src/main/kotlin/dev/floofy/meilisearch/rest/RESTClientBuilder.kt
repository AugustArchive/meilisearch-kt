/*
 * 🔍 Meilisearch for Kotlin: Type-safe and Kotlin Coroutine-based client for Meilisearch
 * Copyright (c) 2022 Noel <cutie@floofy.dev>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.floofy.meilisearch.rest

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Represents the resources to construct a [RESTClient].
 */
data class RESTClientResources(
    val httpClient: HttpClient,
    val endpoint: String,
    val apiKey: String?,
    val json: Json
)

class RESTClientBuilder {
    private var httpClient: HttpClient? = null
    var json: Json = Json {
        ignoreUnknownKeys = false
        isLenient = true
    }

    var endpoint: String? = null
    var apiKey: String? = null

    fun findKeyInEnvironment(key: String = "MEILISEARCH_API_KEY"): RESTClientBuilder {
        val envKey = System.getenv(key) ?: throw IllegalStateException("Missing `MEILISEARCH_API_KEY` in system environment variable.")

        apiKey = envKey
        return this
    }

    fun useHttpClient(client: HttpClient): RESTClientBuilder {
        if (httpClient != null) {
            throw IllegalStateException("Can't call #useHttpClient(io.ktor.http.HttpClient) if the HTTP client was already set!")
        }

        httpClient = client
        return this
    }

    fun useDefaultHttpClient(): RESTClientBuilder {
        if (httpClient != null) {
            throw IllegalStateException("Can't call #useDefaultHttpClient() if the HTTP client was already set.")
        }

        httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    followSslRedirects(true)
                    followRedirects(true)
                }
            }

            install(ContentNegotiation) {
                json(this@RESTClientBuilder.json)
            }
        }

        return this
    }

    fun build(): RESTClientResources {
        checkNotNull(endpoint) { "RESTClientBuilder.endpoint must be set." }
        if (httpClient == null) useDefaultHttpClient()

        return RESTClientResources(httpClient!!, endpoint!!, apiKey, json)
    }
}
