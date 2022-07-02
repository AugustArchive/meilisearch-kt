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

package dev.floofy.meilisearch.data.types

import kotlinx.serialization.SerialName

/**
 * Represents the status of an asynchronous task.
 */
@kotlinx.serialization.Serializable
enum class TaskStatus {
    /**
     * The default task status, should not exist.
     */
    UNKNOWN,

    /**
     * The task request has been received and will be processed soon.
     */
    @SerialName("enqueued")
    ENQUEUED,

    /**
     * The task is being processed
     */
    @SerialName("processing")
    PROCESSING,

    /**
     * The task has been successfully processed.
     */
    @SerialName("succeeded")
    SUCCEEDED,

    /**
     * A failure occurred when processing the task, no changes were made to the database.
     */
    @SerialName("failed")
    FAILED;
}
