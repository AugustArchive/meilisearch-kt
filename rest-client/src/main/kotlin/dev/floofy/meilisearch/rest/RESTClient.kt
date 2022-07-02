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

import dev.floofy.meilisearch.data.types.Dump
import dev.floofy.meilisearch.data.types.Version
import dev.floofy.meilisearch.rest.apis.DumpAPI
import dev.floofy.meilisearch.rest.request.RequestHandler
import dev.floofy.utils.slf4j.logging
import io.ktor.http.*

fun RESTClient(builder: RESTClientBuilder.() -> Unit = {}): RESTClient = RESTClient(RESTClientBuilder().apply(builder).build())

class RESTClient(val resources: RESTClientResources) {
    internal val requestHandler = RequestHandler(this)

    private val dumpApi = DumpAPI(this)
    private val log by logging<RESTClient>()

    suspend fun createDump(): Dump = dumpApi.create()
    suspend fun getDumpStatus(uid: String): Dump = dumpApi.status(uid)
    suspend fun version(): Version = requestHandler.request(HttpMethod.Get, "/version")
}
