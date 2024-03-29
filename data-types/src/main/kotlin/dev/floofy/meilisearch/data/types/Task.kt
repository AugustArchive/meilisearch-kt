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

/**
 * Represents information about an asynchronous method.
 *
 * Docs: https://docs.meilisearch.com/learn/advanced/asynchronous_operations.html
 */
@kotlinx.serialization.Serializable
data class Task(
    val status: TaskStatus = TaskStatus.UNKNOWN,
    val uid: Long,
    val indexUid: String,
    val type: String,
    val error: MeilisearchError? = null,
    val duration: String? = null,
    val enqueuedAt: String,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val details: TaskDetails? = null
)
