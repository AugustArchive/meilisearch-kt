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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import dev.floofy.meilisearch.gradle.*
import dev.floofy.utils.gradle.*

plugins {
    kotlin("plugin.serialization")
    id("com.diffplug.spotless")
    id("org.jetbrains.dokka")
    `java-library`
    kotlin("jvm")
}

group = "dev.floofy.meilisearch"
version = "$VERSION"

repositories {
    mavenCentral()
    mavenLocal()
    noel()
}

dependencies {
    // kotlinx.serialization support
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")

    // kotlinx.coroutines support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.3")

    // kotlinx.datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")

    // Noel Extensions
    implementation("dev.floofy.commons:extensions-kotlin:2.1.1")
    implementation("dev.floofy.commons:slf4j:2.1.1")

    // Kotlin stdlib
    api(kotlin("stdlib"))

    // Testing (testcontainer to test the library on a Meilisearch instance)
    testImplementation("org.testcontainers:testcontainers:1.17.2")
    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation(kotlin("test"))
}

spotless {
    kotlin {
        licenseHeaderFile("${rootProject.projectDir}/assets/HEADING")
        trimTrailingWhitespace()
        endWithNewline()

        ktlint()
            .setUseExperimental(true)
            .userData(mapOf(
                "no-consecutive-blank-lines" to "true",
                "no-unit-return" to "true",
                "disabled_rules" to "no-wildcard-imports,colon-spacing,annotation-spacing",
            ))
            .editorConfigOverride(mapOf("indent_size" to "4"))
    }
}

java {
    sourceCompatibility = JAVA_VERSION
    targetCompatibility = JAVA_VERSION
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                "Implementation-Version" to "$VERSION",
                "Implementation-Vendor" to "Noel [cutie@floofy.dev]",
                "Implementation-Title" to "meilisearch-kotlin"
            )
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = JAVA_VERSION.toString()
    }

    dokkaHtml {
        dokkaSourceSets {
            configureEach {
                platform.set(org.jetbrains.dokka.Platform.jvm)
                jdkVersion.set(17)

                sourceLink {
                    remoteLineSuffix by "#L"
                    localDirectory by file("src/main/kotlin")
                    remoteUrl by uri("https://github.com/auguwu/meilisearch-kt/tree/master/${project.name}/src/main/kotlin").toURL()
                }
            }
        }
    }

    withType<Test>().configureEach {
        testLogging {
            events.addAll(listOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED))
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
