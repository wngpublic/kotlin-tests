package mytest

import java.util.Random
import java.util.ArrayDeque
import org.junit.jupiter.api.Test

class ProcessingStreamTest {
    class Req(val id: Int, val tsDay: Int, val v: Int)
    class Rsp(val id: Int, val tsDay: Int, val v: Int, val req1: Req, val req2: Req)

    // extension function cannot override member function.
    // need to put in class itself and override
    fun Req.toString() = "does not work {id:$id, tsDay:$tsDay, v:$v}"
    fun Req.stringVal() = "{id:${id}, tsDay:$tsDay, v:$v}"

    var CID = 0
    enum class FactoryType {
        REQ, RSP, EMPTY
    }

    // inner class declaration allows outer class variable access
    // but inner class declaration does not allow companion object
    // because inner class has implicit reference to outer class instance
    // which means a static variable is dependent on a class instance,
    // which is not allowed

    class Factory {
        companion object {
            val u = Utils(2)
            fun create(t: FactoryType) = when(t) {
                FactoryType.REQ -> {
                    Req(u.nextId(), u.day(), u.randAvg())
                }
                else -> throw IllegalArgumentException("invalid type: $t")
            }
        }
    }

    class Utils(val id: Int, val window: Int = 10) {
        val r = Random()
        var avg = 100
        var sum = 0
        val q = ArrayDeque<Int>(window)
        var tsDay = 0
        var idGen = 0

        fun ri(min: Int, max: Int) = r.nextInt(max-min) + min
        fun rb() = r.nextBoolean()
        fun randAvg(v: Int? = null): Int {
            val diff = (avg * 0.1).toInt()
            val res = ri(avg-diff,avg+diff)
            // when doesnt have "it", so it's when(val sz = q.size)
            return when(val sz = q.size) {
                window -> {
                    sum = sum - q.removeFirst() + res
                    avg = sum / sz
                    q.add(res)
                    return res
                }
                else -> {
                    sum += res
                    avg = sum / (sz+1)
                    q.add(res)
                    return res
                }
            }
        }
        fun nextId() = idGen++
        fun day(inc: Boolean = false) = when(inc) {
            true -> tsDay++
            false -> tsDay
        }
    }
    companion object {
    }

    val u = Utils(++CID)

    @Test
    fun testUtils() {
        val numCases = 100
        val l = mutableListOf<Req>()
        for(i in 1..numCases) {
            l.add(Factory.create(FactoryType.REQ))
        }
        l.stream().forEach {
            // println(it.toString()) // doesnt work
            println(it.stringVal())
        }
    }
}