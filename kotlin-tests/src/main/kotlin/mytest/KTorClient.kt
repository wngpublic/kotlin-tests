package mytest

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

object KTorClient {
    var client: HttpClient
    init {
        client = HttpClient(Apache) {
            install(Logging) {
                logger = Logger.DEFAULT
            }
            install(ContentNegotiation)
        }
    }
    // companion object not allowed inside singleton
}
