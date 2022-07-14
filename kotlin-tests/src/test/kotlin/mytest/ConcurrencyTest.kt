package mytest

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class ConcurrencyTest {
    @Test
    fun testThreadCreate() {
        var counter = 0
        val numberOfThreads = 100_000
        val time = measureTimeMillis {
            for (i in 1..numberOfThreads) {
                thread() {
                    counter += 1
                }
            }
        }
        println("Created ${numberOfThreads} threads in ${time}ms.")
    }
    @Test
    fun testCoroutinesCreate() {
        var counter = 0
        val numberOfCoroutines = 100_000
        val time = measureTimeMillis {
            runBlocking {
                for (i in 1..numberOfCoroutines) {
                    launch {
                        counter += 1
                    }
                }
            }
        }
        println("Created ${numberOfCoroutines } threads in ${time}ms.")
    }
    /*
     * coroutine notes
     * -
     */
}