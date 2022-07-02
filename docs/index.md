# Meilisearch for Kotlin
This is a Kotlin-based library for Meilisearch to handle requests via [Ktor](https://ktor.io), with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

## Installation
You can receive the current release via [GitHub Releases](https://github.com/auguwu/meilisearch-kt/releases)!

### Gradle (Groovy)
```groovy
repositories {
    maven 'https://maven.floofy.dev/repo/releases'
}

dependencies {
    implementation 'dev.floofy.meilisearch:rest-client:<VERSION>'
}
```

### Gradle (Kotlin)
```kotlin
repositories {
    maven("https://maven.floofy.dev/repo/releases")
}

dependencies {
    implementation("dev.floofy.meilisearch:rest-client:<VERSION>")
}
```
