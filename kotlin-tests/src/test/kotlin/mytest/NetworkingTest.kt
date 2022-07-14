package mytest

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking

class NetworkingTest {
    val URL = "http://localhost:8086/echo"

    suspend fun get(client: HttpClient, url: String): HttpResponse = client.get(url)

    suspend inline fun <reified T> post(client: HttpClient, url: String, body: T): HttpResponse = client.post(url) {
        headers {
            append(HttpHeaders.Accept, "application/json")
        }
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    @Test
    fun testGet() {
        val client = KTorClient.client
        runBlocking {
            val httpResponse: HttpResponse = get(client , URL)
            print("response: ${httpResponse.status}\n")
            print("response: ${httpResponse.body<String>()}\n")
        }
    }

    @Test
    fun testPost() {
        val client = KTorClient.client
        val url = URL
        // bad inputs begin
        // val payload = "{ \"payload\": { \"k1\": \"v1\", \"k2\": \"v2\"  } }"
        // val payload = "{ \"payload\": \"hello world\" }"
        // val payload = "{ \"payload\": \"{ \"k1\": \"v1\", \"k2\": \"v2\" }\" }"
        // val payload = "{ \"payload\": \"{ 'k1': 'v1', 'k2': 'v2' }\" }"
        // val payload = "{ \"payload\": { \"k1\": \"v1\", \"k2\": \"v2\" } }"
        // val payload = "{ \"\": false, \"payload\": '{ \"k1\": \"v1\", \"k2\": \"v2\" }' }"
        // val payload = "{ \"is_json_payload\": false, \"payload\": \"{ \"k1\": \"v1\", \"k2\": \"v2\" }\" }"
        // bad inputs end

        // val payload = "{ \"is_json_payload\": false, \"payload\": \"hello world\" }" // this works
        // val payload = "{ \"is_json_payload\": false, \"payload\": \"{ 'k1': 'v1', 'k2': 'v2' }\" }" // this works
        // val payload = "{ \"is_json_payload\": true, \"payload\": \"{ \\\"k1\\\": \\\"v1\\\", \\\"k2\\\": \\\"v2\\\" }\" }" // this works
        val payload = "{ \"payload\": \"hello world\" }" // this works

        runBlocking {
            print("payload: $payload\n")
            val httpResponse: HttpResponse = post(client, url, payload) // no need for post<T>(...) !!
            print("response: ${httpResponse.status}\n")
            print("response: ${httpResponse.body<String>()}\n")
        }
    }

    @Test
    fun testPostDelayBlocking() {
        val client = KTorClient.client
        val url = URL + "/echo-delay"
        val payload = "{ \"msSleep\": 1000, \"payload\": \"hello world\" }"
        runBlocking {
            var httpResponse: HttpResponse
            val ms = measureTimeMillis {
                httpResponse = post(client, url, payload)
            }
            // the first call always takes longer, subsequent ones shorter
            print("response: status: ${httpResponse.status} elapsed: $ms \n\tpayload: ${httpResponse.body<String>()}\n")
        }
    }

    @Test
    fun testPostDelayBlockingLoop() {
        val client = KTorClient.client
        val url = URL + "/echo-delay"
        val max = 9
        val payload = "{ \"msSleep\": 1000, \"payload\": \"hello world\" }"
        val times = mutableListOf<Long>()
        var numJobsProcessed = 0
        runBlocking {
            val ms1 = measureTimeMillis {
                for (i in 0..max) {
                    var response: HttpResponse
                    val ms = measureTimeMillis {
                        response = post(client, url, payload)
                    }
                    times.add(ms)
                    if(response.status.value != 200)
                        print("response status: ${response.status}\n")
                    else
                        numJobsProcessed++
                }
            }
            print("total time: $ms1 total jobs: $numJobsProcessed\n")
            times.forEach {
                print("time result: ${it}\n")
            }
        }
    }

    @Test
    fun testPostDelayAsyncLoop() {
        val client = KTorClient.client
        val url = URL + "/echo-delay"
        val max = 9
        val payload = "{ \"msSleep\": 1000, \"payload\": \"hello world\" }"
        val jobs = mutableListOf<Deferred<HttpResponse>>()
        var numJobsProcessed = 0
        runBlocking {
            val ms1 = measureTimeMillis {
                for (i in 0..max) {
                    val job = async {
                        post(client, url, payload)
                    }
                    jobs.add(job)
                }
                jobs.forEach {
                    val response = it.await()
                    if(response.status.value != 200)
                        print("response status: ${response.status}\n")
                    else
                        numJobsProcessed++
                }
            }
            print("total time: $ms1 total jobs: $numJobsProcessed\n")
        }

    }

    @Test
    fun testMultigetWrong1() {
        val client = KTorClient.client
        val httpResponses = mutableListOf<HttpResponse>()
        val jobs = mutableListOf<Deferred<Boolean>>()
        val max = 9
        val url = URL
        runBlocking {
            for(i in 0..max) {
                val job = async {
                    val httpResponse: HttpResponse = get(client, url)
                    // this is WRONG. get doesnt return yet, so nothing gets added to httpResponses
                    httpResponses.add(httpResponse)
                }
                jobs.add(job)
                // job becomes Deferred<Boolean>, not Deferred<HttpResponse>
                // because httpResponses.add returns a Boolean, true, even though nothing is added!
            }
            print("httpResponses.size ${httpResponses.size}\n")
            assert(httpResponses.size < (max+1)) // extremely unlikely a remote URL returns in time for this
            print("jobResponses size: ${jobs.size}\n")
            jobs.forEach {
                print("response status: ${it.await()}\n")
            }
        }
    }

    @Test
    fun testMultiget() {
        val client = KTorClient.client
        val jobs = mutableListOf<Deferred<HttpResponse>>()
        val max = 9
        val url = URL
        val t = measureTimeMillis {
            runBlocking {
                for(i in 0..max) {
                    val job = async {
                        get(client, url)
                    }
                    jobs.add(job)
                }
                assert(jobs.size == max+1)
                jobs.forEach {
                    val response = it.await()
                    if(response.status.value != 200) print("response status: ${response.status}\n")
                }
            }
        }
        print("timeMillis: $t\n")
    }

    @Test
    fun testMultigetWithContextWrong1() {
        val client = KTorClient.client
        val jobs = mutableListOf<Deferred<HttpResponse>>()
        val max = 9
        val url = URL
        val t = measureTimeMillis {
            runBlocking {
                for(i in 0..max) {
                    val coroutineContext = CoroutineName("$i")
                    val job = async(coroutineContext) {
                        get(client, url)
                    }
                    jobs.add(job)
                    // print("coroutineContext ${coroutineContext.name}\n")
                }
                assert(jobs.size == max+1)
                jobs.forEach {
                    val response = it.await()
                    // print("context: ${it.get<CoroutineContext.Element>(CoroutineName)}") // how do i get the name value?
                    // print("context: ${it.key}\n")
                    if(response.status.value != 200) print("response status: ${response.status}\n")
                }
            }
        }
        print("timeMillis: $t\n")
    }
}