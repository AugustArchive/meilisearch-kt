# 🔍 Meilisearch for Kotlin
> *Type-safe and Kotlin Coroutine-based client for Meilisearch*

**Meilisearch for Kotlin** is a Meilisearch client for Kotlin developers to take advantage of the Kotlin libraries like
[kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines) and more.

This is a work in progress, the library currently doesn't support the [Settings API](https://docs.meilisearch.com/reference/api/settings.html), [Health API](https://docs.meilisearch.com/reference/api/health.html), [Keys API](https://docs.meilisearch.com/reference/api/keys.html), or
[Stats API](https://docs.meilisearch.com/reference/api/stats.html#get-stats-of-an-index)

## WHY
It sounded like fun, so why not? If any Meilisearch developer or team wants to take care of this project, you can contact
me via Email (`cutie@floofy.dev`) and if I don't have enough time to maintain it, and it can become an official library.

The library was also built to defunct the official Java library, so it can support the Kotlin libraries us Kotlin developers
use and love today.

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

## License
**Meilisearch for Kotlin** is released under the [MIT License](/LICENSE) by Noel.
