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

package dev.floofy.meilisearch.rest.tests

import dev.floofy.meilisearch.data.types.TaskStatus
import dev.floofy.meilisearch.rest.task.waitForCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertEquals

class MeilisearchTests: AbstractMeilisearchTest() {
    @Test
    fun `can we get meilisearch version`() {
        val client = createClient()
        runBlocking {
            val version = client.version()
            assertEquals("0.27.2", version.pkgVersion)
        }
    }

    @Test
    fun `create index and await the task completion`() {
        val client = createClient()

        runBlocking {
            val task = client.createIndex("noel-logs", "id")
            assertEquals(task.status, TaskStatus.ENQUEUED)

            val completedTask = task.waitForCompletion(client = client)
            assertEquals(TaskStatus.SUCCEEDED, completedTask.status)
        }
    }

    @Test
    fun `update index primary key and await task completion`() {
        val client = createClient()

        runBlocking {
            val task = client.updateIndex("noel-logs", "owos")
            assertEquals(task.status, TaskStatus.ENQUEUED)

            val completedTask = task.waitForCompletion(client = client)
            assertEquals(TaskStatus.SUCCEEDED, completedTask.status)
        }
    }

    @Test
    fun `get index tasks`() {
        val client = createClient()
        runBlocking {
            val tasks = client.indexTasks("noel-logs")
            assertEquals(2, tasks.results.size)
        }
    }

    @Test
    fun `list all documents`() {
        val client = createClient()
        runBlocking {
            val documents = client.documents("noel-logs")
            assertEquals(12, documents.size)
        }
    }

    @Test
    fun `add one document to tree`() {
        val client = createClient()
        runBlocking {
            val task = client.createOrUpdateDocuments(
                "noel-logs",
                listOf(
                    buildJsonObject {
                        put("owos", "yay")
                        put("message", "Hello, world!")
                        put("@timestamp", Clock.System.now().toString())
                    }
                ),
                "owos"
            )

            val completedTask = task.waitForCompletion(client = client)
            assertEquals(TaskStatus.SUCCEEDED, completedTask.status)
        }
    }

    @Test
    fun `(stress test) add 10 documents`() {
        val client = createClient()
        runBlocking {
            val tasks = mutableListOf<JsonObject>()
            for (i in 0..10) {
                tasks.add(
                    buildJsonObject {
                        put("owos", "owo-$i")
                        put("message", "Hello from task $i!")
                        put("@timestamp", Clock.System.now().toString())
                    }
                )
            }

            val task = client.createOrUpdateDocuments("noel-logs", tasks, "owos")
            val completedTask = task.waitForCompletion(client = client)
            assertEquals(TaskStatus.SUCCEEDED, completedTask.status)
        }
    }

    @Test
    fun `search through noel-logs index`() {
        val client = createClient()
        runBlocking {
            val response = client.search("noel-logs", "hello")
            assertEquals(0, response.hits.size)
        }
    }
}
