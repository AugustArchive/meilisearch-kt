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

package dev.floofy.meilisearch.rest.task

import dev.floofy.meilisearch.data.types.Task
import dev.floofy.meilisearch.data.types.TaskStatus
import dev.floofy.meilisearch.rest.RESTClient
import dev.floofy.meilisearch.rest.TaskFailedException
import dev.floofy.utils.slf4j.logging
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a coroutine job to call [#task(uid)][RESTClient.task], checks the task status,
 * and returns the succeeded task if it was ever a success. After 10 attempts, the job will
 * cancel and a [JobCancellationException] will be emitted.
 *
 * @param coroutineScope The coroutine scope to execute the job in
 * @param doNotCancelOnAttempts This parameter will not cancel the coroutine if 10 attempts were done to check the
 *                              task's status. This is useful if you have a lot of documents.

 * @param client The REST client to use.
 * @throws Exception Any other exception except [TaskFailedException] or [JobCancellationException] has been thrown.
 * @throws TaskFailedException If the task has ever failed
 * @throws JobCancellationException If after 10 attempts, it's still enqueuing/processing, the job will
 *                                  stop and this will be thrown. You can recall [waitForCompletion] again if this
 *                                  ever happens. This will never happen if [doNotCancelOnAttempts] is used.
 */
@OptIn(DelicateCoroutinesApi::class)
suspend fun Task.waitForCompletion(
    coroutineScope: CoroutineScope = GlobalScope,
    doNotCancelOnAttempts: Boolean = false,
    client: RESTClient
): Task {
    if (!listOf(TaskStatus.PROCESSING, TaskStatus.ENQUEUED).contains(status)) {
        throw IllegalStateException("Task must be in a processing/enqueued status to call #waitForCompletion(CoroutineScope, RESTClient).")
    }

    var causedException: Exception? = null
    var succeededTask: Task? = null
    var attempts = 10
    var success = false
    val log by logging("dev.floofy.meilisearch.rest.task.TaskEnqueueMethodsKt")

    log.debug("Enqueued coroutine job for task $uid!")
    val job = coroutineScope.launch {
        while (!success) {
            delay(5.seconds.inWholeMilliseconds)

            log.debug("Attempt #$attempts on task #${this@waitForCompletion.uid}")
            attempts--
            if (!doNotCancelOnAttempts && attempts < 0) {
                causedException = CancellationException("Task #${this@waitForCompletion.uid} is still processing after 10 attempts (50 minutes)")
                break
            }

            val task = try {
                client.task(uid)
            } catch (e: Exception) {
                causedException = e
                break
            }

            when (task.status) {
                TaskStatus.ENQUEUED -> {} // do nothing
                TaskStatus.SUCCEEDED -> {
                    success = true
                    succeededTask = task
                    break // break the while loop and let the job complete
                }

                TaskStatus.FAILED -> throw TaskFailedException(task.error!!)
                TaskStatus.PROCESSING -> {} // do nothing, keep waiting.
                else -> error("Unknown task status was being processed.")
            }
        }
    }

    job.join()
    if (succeededTask == null) {
        throw IllegalStateException("Unable to aim until completion for task $uid", causedException)
    }

    return succeededTask!!
}
