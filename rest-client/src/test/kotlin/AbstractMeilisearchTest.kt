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

import dev.floofy.utils.slf4j.logging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class AbstractMeilisearchTest {
    var container: GenericContainer<*>? = null
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private val log by logging<AbstractMeilisearchTest>()

    @BeforeTest
    fun startContainer() {
        if (container != null) {
            throw IllegalStateException("Cannot call #startContainer() more than once.")
        }

        log.info("Starting container...")
        val image = DockerImageName.parse("getmeili/meilisearch:v0.27.2")
        container = GenericContainer(image)
            .withExposedPorts(7700)

        container!!.setWaitStrategy(HttpWaitStrategy().forPort(7700))
        container!!.start()
    }

    @AfterTest
    fun destroyContainer() {
        if (closed.get()) {
            throw IllegalStateException("Cannot call #destroyContainer() more than once.")
        }

        if (container == null) {
            throw IllegalStateException("Can't destroy Meilisearch container without calling #startContainer()")
        }

        container!!.stop()
        closed.set(true)
    }
}
