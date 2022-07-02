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

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

// TODO: automate this via ./gradlew :generateErrors
// TODO: also finish the rest
enum class MeilisearchErrorCode(val key: String, val description: String) {
    INDEX_CREATION_FAILED("index_creation_failed", "An error occurred while trying to create an index, check out our guide on index creation."),
    INDEX_ALREADY_EXISTS("index_already_exists", "An index with this UID already exists, check out our guide on index creation."),
    INDEX_NOT_FOUND("index_not_found", "An index with this UID was not found, check out our guide on index creation."),
    INVALID_INDEX_UID("invalid_index_uid", "There is an error in the provided index format, check out our guide on index creation."),
    INDEX_NOT_ACCESSIBLE("index_not_accessible", "An internal error occurred while trying to access the requested index."),
    INVALID_STATE("invalid_state", "The database is in an invalid state. Deleting the database and re-indexing should solve the problem."),
    PRIMARY_KEY_INFERENCE_FAILED("primary_key_inference_failed", "The first provided document contains no fields with the substring id. Manually designate the primary key or add id to one of your fields so it can be used as the primary key during inference. We recommend manually setting the primary key."),
    DOCUMENT_FIELDS_LIMIT_EXCEEDED("document_fields_limit_reached", "A document exceeds the maximum limit of 65,535 fields."),
    MISSING_DOCUMENT_ID("missing_document_id", "A document does not contain any value for the required primary key, and is thus invalid. Check documents in the current addition for the invalid ones."),
    INVALID_DOCUMENT_ID("invalid_document_id", "The provided document identifier does not meet the format requirements. A document identifier must be of type integer or string, composed only of alphanumeric characters (a-z A-Z 0-9), hyphens (-), and underscores (_)."),
    INVALID_FILTER("invalid_filter", "The filter provided with the search is invalid. This may be due to syntax errors in the filter parameter, using reserved fields as filter expressions, or neglecting to add the filtering attributes to filterableAttributes. For troubleshooting, check our guide on filtering."),
    INVALID_SORT("invalid_sort", "The sort value is invalid. This may be due to syntax errors in the sort parameter, using reserved fields as sort expressions, or neglecting to add the sorting attributes to sortableAttributes. For troubleshooting, check our guide on sorting."),
    BAD_REQUEST("bad_request", "The request is invalid, check the error message for more information."),
    DOCUMENT_NOT_FOUND("document_not_found", "The requested document can't be retrieved. Either it doesn't exist, or the database was left in an inconsistent state."),
    INTERNAL("internal", "Meilisearch experienced an internal error. Check the error message, and open an issue (opens new window)if necessary."),
    MISSING_AUTHORIZATION_HEADER(
        "missing_authorization_header",
        """
    |The requested resources are protected with an API key that was not provided in the request header. Check our guide on security for more information.
    |OR
    |You are using the wrong authorization header for your version. v0.24 and below use X-MEILI-API-KEY: apiKey, whereas v0.25 and above use Authorization: Bearer apiKey.
        """.trimMargin()
    ),
    NOT_FOUND("not_found", "The requested resources could not be found"),
    PAYLOAD_TOO_LARGE("payload_too_large", "The payload sent to the server was too large. Check out this guide to customize the maximum payload size accepted by Meilisearch."),
    UNRETRIEVABLE_DOCUMENT("unretrievable_document", "The payload sent to the server was too large. Check out this guide to customize the maximum payload size accepted by Meilisearch."),
    INVALID_CONTENT_TYPE("invalid_content_type", "The Content-Type header is not supported by Meilisearch. Currently, Meilisearch only supports JSON, CSV, and NDJSON."),
    MISSING_CONTENT_TYPE("missing_content_type", "The payload does not contain a Content-Type header. Currently, Meilisearch only supports JSON, CSV, and NDJSON."),
    MALFORMED_PAYLOAD("malformed_payload", "The Content-Type header does not match the request body payload format or the format is invalid."),
    MISSING_PAYLOAD("missing_payload", "The Content-Type header was specified, but no request body was sent to the server or the request body is empty."),
    DUMP_ALREADY_PROCESSING("dump_already_processing", "Dump creation is already in progress. A new dump creation process can be triggered after the current one has been completed."),
    DUMP_NOT_FOUND("dump_not_found", "The requested dump could not be found.");
}

class MeilisearchException(message: String): Exception(message) {
    var httpStatusCode: Int? = null
    var code: MeilisearchErrorCode? = null

    companion object {
        suspend fun fromResponse(res: HttpResponse): MeilisearchException {
            val body = res.body<JsonObject>()
            val code = body["code"]?.jsonPrimitive?.content
            val meiliCode = if (code != null) {
                MeilisearchErrorCode.values().singleOrNull { it.key == code }
            } else {
                null
            }

            return MeilisearchException(
                """
            |Received ${res.status} on ${res.request.method.value} ${res.request.url}:
            |$body
            |${if (meiliCode != null) "How to fix: ${meiliCode.description}" else ""}
                """.trimMargin()
            ).also { it.httpStatusCode = res.status.value; it.code = meiliCode }
        }
    }
}
