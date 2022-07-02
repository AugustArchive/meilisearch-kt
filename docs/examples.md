# Creating a REST Client
```kotlin
import dev.floofy.meilisearch.rest.*

suspend fun main(args: Array<String>) {
    val client = RESTClient {
        endpoint = "127.0.0.1:7700"
    }
    
    client.version()
    // => Version{"pkgVersion":"0.27.0","commitSha":"...","commitDate":"..."}
}
```

# Using an existing HTTP client
```kotlin
import dev.floofy.meilisearch.rest.*
import io.ktor.http.*

suspend fun main(args: Array<String>) {
    val myHttpClient = HttpClient(CIO) {}
    val client = RESTClient {
        endpoint = "127.0.0.1:7700"
        useHttpClient(myHttpClient)
    }
    
    client.version()
    // => Version{"pkgVersion":"0.27.0","commitSha":"...","commitDate":"..."}
}
```
