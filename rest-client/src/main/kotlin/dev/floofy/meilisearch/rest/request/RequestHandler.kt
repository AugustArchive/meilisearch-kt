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

package dev.floofy.meilisearch.rest.request

import dev.floofy.meilisearch.rest.MeilisearchException
import dev.floofy.meilisearch.rest.RESTClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RequestHandler(val client: RESTClient) {
    suspend inline fun <reified T> request(
        method: HttpMethod,
        url: String,
        noinline override: (HttpRequestBuilder.() -> Unit)? = null
    ): T {
        val res = client.resources.httpClient.request {
            this.method = method
            url("http://${client.resources.endpoint}$url")

            if (client.resources.apiKey != null) {
                header("Authorization", "Bearer ${client.resources.apiKey}")
            }

            if (override != null) override()
        }

        if (!res.status.isSuccess()) {
            throw MeilisearchException.fromResponse(res)
        }

        return res.body()
    }
}
