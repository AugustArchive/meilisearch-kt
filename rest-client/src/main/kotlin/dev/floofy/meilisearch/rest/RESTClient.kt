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
import dev.floofy.meilisearch.data.types.Index
import dev.floofy.meilisearch.data.types.Task
import dev.floofy.meilisearch.data.types.Version
import dev.floofy.meilisearch.rest.request.RequestHandler
import dev.floofy.meilisearch.rest.search.SearchRequest
import dev.floofy.meilisearch.rest.search.SearchResponse
import dev.floofy.meilisearch.rest.task.TaskResult
import dev.floofy.utils.slf4j.logging
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun RESTClient(builder: RESTClientBuilder.() -> Unit = {}): RESTClient = RESTClient(RESTClientBuilder().apply(builder).build())

class RESTClient(val resources: RESTClientResources) {
    val requestHandler = RequestHandler(this)
    private val log by logging<RESTClient>()

    /**
     * Creates a dump and returns metadata about it. The [finishedAt][Dump.finishedAt] will be `null`
     * since it's not a property when creating a dump.
     *
     * @return The [Dump] metadata.
     */
    suspend fun createDump(): Dump = requestHandler.request(HttpMethod.Post, "/dumps")

    /**
     * Returns the status of a specific dump via its [uid].
     * @param uid The unique identifier that was fetched via [#createDump()][createDump].
     * @return The [Dump] metadata.
     */
    suspend fun getDumpStatus(uid: String): Dump = requestHandler.request(HttpMethod.Get, "/dumps/$uid/status")

    /**
     * Returns the version metadata about the Meilisearch instance the client is using.
     * @return The [Version] metadata.
     */
    suspend fun version(): Version = requestHandler.request(HttpMethod.Get, "/version")

    /**
     * Returns all the asynchronous tasks
     * @return All the [tasks][Task].
     */
    suspend fun tasks(): TaskResult = requestHandler.request(HttpMethod.Get, "/tasks")

    /**
     * Returns the given task by its [uid], returns `null` if the task wasn't found.
     * @param uid The unique identifier that was fetched by any asynchronous method.
     * @return The [Task] object.
     */
    suspend fun task(uid: Long): Task = requestHandler.request(HttpMethod.Get, "/tasks/$uid")

    /**
     * Lists all the given indexes that are available in Meilisearch.
     * @return a list of [indexes][Index].
     */
    suspend fun indexes(): List<Index> = requestHandler.request(HttpMethod.Get, "/indexes")

    /**
     * Returns a specific index, or `null` if the index wasn't found.
     * @param uid The index's unique identifier.
     * @return The retrieved [Index], if any. Otherwise, `null`.
     */
    suspend fun index(uid: String): Index? {
        return try {
            requestHandler.request<Index>(HttpMethod.Get, "/indexes/$uid")
        } catch (e: MeilisearchException) {
            if (e.httpStatusCode != null && e.httpStatusCode == 404) return null

            throw e
        }
    }

    /**
     * Creates the index, and returns the [Task] that the index is creating. You can track it
     * via [#task(uid)][task].
     */
    suspend fun createIndex(uid: String, primaryKey: String): Task = requestHandler.request(HttpMethod.Post, "/indexes") {
        headers.append("Content-Type", "application/json")
        setBody(
            buildJsonObject {
                put("uid", uid)
                put("primaryKey", primaryKey)
            }
        )
    }

    /**
     * Updates an index's primary key and returns the asynchronous task.
     * @param uid The index's unique identifier
     * @param primaryKey The primary key of this task.
     */
    suspend fun updateIndex(uid: String, primaryKey: String): Task = requestHandler.request(HttpMethod.Put, "/indexes/$uid") {
        headers.append("Content-Type", "application/json")
        setBody(
            buildJsonObject {
                put("primaryKey", primaryKey)
            }
        )
    }

    /**
     * Deletes an index from Meilisearch, and returns the asynchronous task.
     * @param uid The index's unique identifier
     */
    suspend fun deleteIndex(uid: String): Task = requestHandler.request(HttpMethod.Delete, "/indexes/$uid")

    /**
     * Returns all the tasks that the index by its [uid] has created.
     * @param uid The task's unique identifier.
     * @return The result of all the tasks.
     */
    suspend fun indexTasks(uid: String): TaskResult = requestHandler.request(HttpMethod.Get, "/indexes/$uid/tasks")

    /**
     * Returns a specific task that was executed by an index.
     * @param indexUid The index's unique identifier.
     * @param taskUid The task's unique identifier.
     * @return The [Task] if any, or `null` if none was found.
     */
    suspend fun taskFromIndex(indexUid: String, taskUid: String): Task? {
        return try {
            requestHandler.request(HttpMethod.Get, "/indexes/$indexUid/tasks/$taskUid")
        } catch (e: MeilisearchException) {
            if (e.httpStatusCode != null && e.httpStatusCode == 404) return null
            throw e
        }
    }

    /**
     * Returns a document from a specified index.
     * @param indexUid The index's unique identifier
     * @param uid The document's unique identifier.
     * @return A [JsonObject] since the document is dynamic, rather than static content.
     */
    @JvmName("getDocumentDynamically")
    suspend fun document(indexUid: String, uid: String): JsonObject = requestHandler.request(HttpMethod.Get, "/indexes/$indexUid/documents/$uid")

    /**
     * Returns the document via a class that is serializable (with the [@Serializable][kotlinx.serialization.Serializable] annotation).
     * @param indexUid The index's unique identifier
     * @param uid The document's unique identifier.
     * @return The object serialized as [T] since the document is dynamic rather than static.
     */
    @JvmName("getDocumentViaSerialization")
    suspend inline fun <reified T> document(indexUid: String, uid: String): T = requestHandler.request(HttpMethod.Get, "/indexes/$indexUid/documents/$uid")

    /**
     * Returns all the documents in the specified index.
     * @param index The index's unique identifier.
     * @param limit How many documents should show up
     * @param offset Number of documents to skip
     * @param attributes The document's attributes to show.
     */
    @JvmName("getDocumentsDynamically")
    suspend fun documents(index: String, limit: Int = 25, offset: Int = 0, attributes: String = "*"): List<JsonObject> =
        requestHandler.request(HttpMethod.Get, "/indexes/$index/documents?limit=$limit&offset=$offset&attributesToRetrieve=$attributes")

    /**
     * Returns all the documents in the specified index via an object class as [T] to be serialized from.
     * @param index The index's unique identifier.
     * @param limit How many documents should show up
     * @param offset Number of documents to skip
     * @param attributes The document's attributes to show.
     */
    @JvmName("getDocumentsViaSerialization")
    suspend inline fun <reified T> documents(index: String, limit: Int = 25, offset: Int = 0, attributes: String = "*"): List<T> =
        requestHandler.request(HttpMethod.Get, "/indexes/$index/documents?limit=$limit&offset=$offset&attributesToRetrieve=$attributes")

    /**
     * Creates or replaces a list of documents in the specified index. This varies from [createOrUpdateDocuments]
     * since the documents will be replaced instead of updated, view the [documentation](https://docs.meilisearch.com/reference/api/documents.html#add-or-replace-documents) for more information.
     *
     * @param index The index's unique identifier
     * @param data The data to insert.
     * @param primaryKey The primary key, if any.
     */
    @JvmName("createOrReplaceDocumentsDynamically")
    suspend fun createOrReplaceDocuments(index: String, data: List<JsonObject>, primaryKey: String? = null): Task =
        requestHandler.request(HttpMethod.Post, "/indexes/$index/documents${if (primaryKey != null) "?primaryKey=$primaryKey" else ""}") {
            headers.append("Content-Type", "application/json")
            setBody(data)
        }

    /**
     * Creates or partially updates a list of documents in the specified index. This varies from [createOrReplaceDocuments]
     * since the documents will be replaced instead of partially updated,
     * view the [documentation](https://docs.meilisearch.com/reference/api/documents.html#add-or-replace-documents) for more information.
     *
     * @param index The index's unique identifier
     * @param data The data to insert.
     * @param primaryKey The primary key, if any.
     */
    @JvmName("createOrReplaceDocumentsViaSerialization")
    suspend inline fun <reified T> createOrReplaceDocuments(
        index: String,
        data: List<T>,
        primaryKey: String? = null
    ): Task = requestHandler.request(HttpMethod.Post, "/indexes/$index/documents${if (primaryKey != null) "?primaryKey=$primaryKey" else ""}") {
        headers.append("Content-Type", "application/json")
        setBody(data)
    }

    /**
     * Creates or replaces a list of documents in the specified index. This varies from [createOrUpdateDocuments]
     * since the documents will be replaced instead of updated, view the [documentation](https://docs.meilisearch.com/reference/api/documents.html#add-or-replace-documents) for more information.
     *
     * @param index The index's unique identifier
     * @param data The data to insert.
     * @param primaryKey The primary key, if any.
     */
    @JvmName("createOrUpdateDocumentsDynamically")
    suspend fun createOrUpdateDocuments(index: String, data: List<JsonObject>, primaryKey: String? = null): Task =
        requestHandler.request(HttpMethod.Post, "/indexes/$index/documents${if (primaryKey != null) "?primaryKey=$primaryKey" else ""}") {
            headers.append("Content-Type", "application/json")
            setBody(data)
        }

    /**
     * Creates or partially updates a list of documents in the specified index. This varies from [createOrReplaceDocuments]
     * since the documents will be replaced instead of partially updated,
     * view the [documentation](https://docs.meilisearch.com/reference/api/documents.html#add-or-replace-documents) for more information.
     *
     * @param index The index's unique identifier
     * @param data The data to insert.
     * @param primaryKey The primary key, if any.
     */
    @JvmName("createOrUpdateDocumentsViaSerialization")
    suspend inline fun <reified T> createOrUpdateDocuments(
        index: String,
        data: List<T>,
        primaryKey: String? = null
    ): Task = requestHandler.request(HttpMethod.Post, "/indexes/$index/documents${if (primaryKey != null) "?primaryKey=$primaryKey" else ""}") {
        headers.append("Content-Type", "application/json")
        setBody(data)
    }

    /**
     * Deletes all the documents, this is unrecoverable after it succeeds!
     * @param index The index's unique identifier
     */
    suspend fun deleteDocuments(index: String): Task = requestHandler.request(HttpMethod.Delete, "/indexes/$index/documents")

    suspend fun deleteDocument(index: String, uid: String): Task = requestHandler.request(HttpMethod.Delete, "/indexes/$index/documents/$uid")

    suspend fun batchDeleteDocuments(index: String, ids: List<String>): Task = requestHandler.request(HttpMethod.Post, "/indexes/$index/documents/delete-batch") {
        headers.append("Content-Type", "application/json")
        setBody(ids)
    }

    @JvmName("searchDynamically")
    suspend fun search(index: String, query: String, request: SearchRequest.() -> Unit = {}): SearchResponse<JsonObject> =
        requestHandler.request(HttpMethod.Post, "/indexes/$index/search") {
            headers.append("Content-Type", "application/json")
            setBody(SearchRequest(query).apply(request))
        }

    @JvmName("searchViaSerialization")
    suspend inline fun <reified T> search(index: String, query: String, crossinline request: SearchRequest.() -> Unit = {}): SearchResponse<T> =
        requestHandler.request(HttpMethod.Post, "/indexes/$index/search") {
            headers.append("Content-Type", "application/json")
            setBody(SearchRequest(query).apply(request))
        }
}
