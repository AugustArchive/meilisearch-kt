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

import dev.floofy.meilisearch.gradle.*
import dev.floofy.utils.gradle.*
import java.io.StringReader
import java.util.Properties

plugins {
    id("org.jetbrains.dokka")
    `maven-publish`
    kotlin("jvm")
}

// Get the `publishing.properties` file from the `gradle/` directory
// in the root project.
val publishingPropsFile = file("${rootProject.projectDir}/gradle/publishing.properties")
val publishingProps = Properties()

// If the file exists, let's get the input stream
// and load it.
if (publishingPropsFile.exists()) {
    publishingProps.load(publishingPropsFile.inputStream())
} else {
    // Check if we do in environment variables
    val accessKey = System.getenv("NOEL_PUBLISHING_ACCESS_KEY") ?: ""
    val secretKey = System.getenv("NOEL_PUBLISHING_SECRET_KEY") ?: ""

    if (accessKey.isNotEmpty() && secretKey.isNotEmpty()) {
        val data = """
        |s3.accessKey=$accessKey
        |s3.secretKey=$secretKey
        """.trimMargin()

        publishingProps.load(StringReader(data))
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assemble Kotlin documentation with Dokka"

    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

publishing {
    publications {
        create<MavenPublication>("meilisearch") {
            from(components["kotlin"])

            artifactId = project.name
            group = "dev.floofy.meilisearch"
            version = "$VERSION"

            artifact(sourcesJar.get())
            artifact(dokkaJar.get())

            pom {
                description by "Type-safe and Kotlin Coroutine-based client for Meilisearch"
                name by project.name
                url by "https://meili.floofy.dev"

                developers {
                    developer {
                        name by "Noel"
                        email by "cutie@floofy.dev"
                        url by "https://floofy.dev"
                    }
                }

                issueManagement {
                    system by "GitHub"
                    url by "https://github.com/auguwu/meilisearch-kt/issues"
                }

                licenses {
                    license {
                        name by "MIT"
                        url by "https://mit-license.org"
                    }
                }

                scm {
                    connection by "scm:git:ssh://github.comauguwu/meilisearch-kt.git"
                    developerConnection by "scm:git:ssh://git@github.com:auguwu/meilisearch-kt.git"
                    url by "https://github.com/auguwu/meilisearch-kt"
                }
            }
        }
    }

    repositories {
        maven("s3://maven.floofy.dev/repo/releases") {
            credentials(AwsCredentials::class) {
                accessKey = publishingProps.getProperty("s3.accessKey") ?: System.getenv("NOEL_PUBLISHING_ACCESS_KEY") ?: ""
                secretKey = publishingProps.getProperty("s3.secretKey") ?: System.getenv("NOEL_PUBLISHING_SECRET_KEY") ?: ""
            }
        }
    }
}
