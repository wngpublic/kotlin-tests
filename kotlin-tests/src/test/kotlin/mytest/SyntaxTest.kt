package mytest

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.time.Instant
import java.util.Random
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors
import kotlin.random.nextLong
import kotlin.streams.asSequence
import kotlin.streams.toList
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import myutils.mytest.GCTRVAR
import myutils.mytest.applyThenReturnUtil2
import myutils.mytest.getCharSeq
import myutils.mytest.greet

class SyntaxTest {
    fun echo(msg: String?): String = "syntax: $msg"

    @Test
    public fun testTopLevelFunctionImport() {
        assert(echo("hello") == "syntax: hello")
        assert(myutils.mytest.echo("hello") == "toplevel: hello")
        assert(greet("you") == "toplevel: hello you")
        GCTRVAR = 1
        // mytest.getGCTRVAL = 2  // not allowed
    }
    @Test
    public fun testVars() {
        val v_readonly: Int = 10        // same is true in class declaration! which means const
        var v_modifiable: Int = 10
        var v_inferred = 10
        var v_cannotAssignNull: Int = 10
        var v_canAssignNull: Int? = 10
        var v_inferredNotNull = 10
        var v_inferredNull = null

        var v1 = 1
        var v2 = 2
        v1 = v2.also { v2 = v1 }        // also is subsequent stage
        assert(v1 == 2 && v2 == 1)
    }
    @Test
    public fun testNull() {
        var sc4: SimpleClass4 = SimpleClass4(null, null, null, null)
        var res: Int? = null

        res = try {
            // must use !! because vi1 is non-null. !! converts to non-null
            // return vi if no exception, else return NullPointerException
            var vi1: Int = sc4.vi1!!
            vi1
        } catch(e: NullPointerException) {
            100
        }
        assert(res == 100)

        res = try {
            // elvis operator assigns else if null
            var vi1: Int = sc4.vi1?: 5
            vi1
        } catch(e: NullPointerException) {
            100
        }
        assert(res == 5)

        // more elvis operator
        res = sc4.vi1?:sc4.vi2?:15
        assert(res == 15)

        sc4.vi2 = 10

        // sc4.vi2 is non-null, so it should be 10, not 20
        res = sc4.vi1?:sc4.vi2?:20
        assert(res == 10)

        res = try {
            // sc4.vi2 was assigned 10, so should not NPE
            var vi1: Int = sc4.vi2!!
            vi1
        } catch(e: Exception) {
            200
        }
        assert(res == 10)

        var sc4n: SimpleClass4? = null

        res = sc4n?.vi1
        assert(res == null)

        res = sc4?.vi1
        assert(res == null)

        res = sc4?.vi2
        assert(res == 10)

        res = sc4n?.vi1?:sc4?.vi2
        assert(res == 10)

        var res2: Int? = null
        res = sc4?.vi1
        res?.let {
            // if res not null, do this block
            res2 = 10
        }
        assert(res2 == null)

        res = sc4?.vi2
        res2 = res?.let {
            10
        }
        assert(res2 == 10)

        res2 = res?.let {
            10
        } ?: 20
        assert(res2 == 10)

        res2 = res?.let {
            null
        } ?: 20
        assert(res2 == 20)

        sc4 = SimpleClass4(1,2,"hi","bye")
        sc4 = SC4(1,2,"hi","bye")
    }
    @Test
    fun testStrings() {
        val v1 = 10
        val s1: String = "v1 = $v1"
    }
    fun f1Add(v1: Int, v2: Int = 10, v3: Int?): Int {
        return v1+v2+ (v3 ?: 0)
    }
    fun f2Add(v1: Int?, v2: Int?, v3: Int?): Int {
        var sum: Int?
        sum = v1 ?: return -1   // if v1 is null, return -1
        sum += v2 ?: return -2  // if v2 is null, return -2
        sum += v3 ?: 0          // if v3 is null, add 0
        return sum
    }
    @Test
    fun testFunctions() {
        //var z = UtilFuncs.add1(10,20)
        fun innerF1() = 42
        fun innerF2(): Int {
            return 42
        }
        assert(innerF1() == innerF2())

        fun innerF3(v: Int): Int = when(v) {
            1 -> 100
            2 -> 200
            else -> 300
        }
        assert(innerF3(2) == 200)
        assert(innerF3(3) == 300)

        assert(f1Add(1,2,null) == 3)
        assert(f1Add(1,2,3) == 6)
        assert(f2Add(null,null,null) == -1)
        assert(f2Add(1,null,null) == -2)
        assert(f2Add(1,2,null) == 3)

        fun innerF4(v: Int): Int {
            return v*10
        }
        fun innerF5(v: Int): Int = v*10
        fun innerF6(v: Int): Int = innerF5(v)
        fun innerF7(v: Int): Int {
            return innerF6(v)
        }

        assert(innerF4(2) == innerF5(2))
        assert(innerF6(2) == innerF5(2))
        assert(innerF7(2) == innerF5(2))
    }
    @Test
    fun testInstanceTypes() {
        var vi: Int = 10
        var vs: String = "10"
        var vany: Any = vs

        var result: Int? = null

        when(vany) {
            is Int -> result = 0
            is String -> result = 1
            else -> result = -1
        }

        vany = vi

        result = null
        when(vany) {
            is Int -> result = 0
            is String -> result = 1
            else -> result = -1
        }

        assert(result == 0)
    }
    @Test
    fun testStructuresSimple() {
        val l = listOf(1,2,3)   // read only list
        val m = mapOf("a" to 1, "b" to 2, "c" to 3)
        val sb: StringBuilder = StringBuilder()

        for((k,v) in m) {
            sb.append("$k:$v, ")
        }
        val s = sb.toString()
        assert(s == "a:1, b:2, c:3, ")

        val m2a = HashMap<Int,Int>()
        val m2b: Map<Int,Int> = HashMap<Int,Int>()
        val m2c: MutableMap<Int,Int> = mutableMapOf()
        m2a.put(1,2)
        m2c.put(1,2)
        println(m2a)
    }
    @Test
    fun testList() {
        var l1 = listOf(1,2,3,4,5)
        var l2 = listOf(4,5,6,7,8)

        // l2 = {1,2,3} // this is not list syntax, but is a lamba, return syntax,
        // and even then, cannot have 1,2,3

        var s1 = l1.toSet() + l2.toSet()
        assert(s1.size == 8)
        assert(s1 == setOf(1,2,3,4,5,6,7,8))
        assert(l1.contains(3))
        assert(!l1.contains(9))
        assert(l1.containsAll(listOf(3,4,5)))
        assert(l1.count() == 5)

        var r1: Double = l1.average()
        assert(r1 == 3.0)

        var ml2 = mutableListOf<Int>()
        l1.forEach { i -> ml2.add(i) }
        assert(ml2 == listOf(1,2,3,4,5))
        ml2.clear()
        l2.forEachIndexed { i,v -> ml2.add(i) }
        assert(ml2 == listOf(0,1,2,3,4))
        ml2.clear()
        for(i in l2.indices) {
            ml2.add(i)
        }
        assert(ml2 == listOf(0,1,2,3,4))

        var l3: List<Int> = listOf()
        assert(l3.size == 0)
        l3 += 1
        l3 += 2
        l3 += 3
        assert(l3 == listOf(1,2,3))

        var b1: Boolean
        var l4: List<Int> = listOf(1,2,3,4,5)
        b1 = l4.any { it == 5 }
        assert(b1)
        b1 = l4.any { it == 8 }
        assert(!b1)
        var i1 = l4.getOrElse(1) { 0 }
        assert(i1 == 2)
        var i2: Int? = l4.getOrNull(9)
        assert(i2 == null)
        i2 = l4.getOrNull(1)
        assert(i2 == 2)

        var intRange = l4.indices
        assert(intRange == 0..4)
        assert(intRange.first == 0 && intRange.last == 4)
        assert(l4.lastIndex == 4)

        var ll1: List<List<Int>> = listOf(listOf(1,2,3),listOf(3,4,5),listOf(5,6,7))
        b1 = ll1.any { it.contains(5) }
        assert(b1)
        b1 = ll1.any { it.contains(8) }
        assert(!b1)
        var lf = ll1.flatten()
        assert(lf == listOf(1,2,3,3,4,5,5,6,7))
        var l5 = ll1.getOrElse(1) { 0 }
        assert(l5 == listOf(3,4,5))
        l5 = ll1.getOrElse(3) { 0 }
        assert(l5 == 0)


        var ml1: MutableList<Int> = mutableListOf()
        ml1 += 1
        ml1 += 2
        ml1 += 3
        assert(ml1 == listOf(1,2,3))
        assert(ml1 == mutableListOf(1,2,3))

        ml1.addAll(l4)
        assert(ml1 == listOf(1,2,3,1,2,3,4,5))
        assert(ml1.all { it < 10 })
        assert(!ml1.all { it > 10 })

        var al1: ArrayList<Int> = arrayListOf()
        al1 += 1
        al1 += 2
        al1 += 3
        assert(al1 == arrayListOf(1,2,3))

        l3 = listOf(1,2,2,3,4,5,5)
        assert(l3.distinct() == listOf(1,2,3,4,5))
        var r2: Int?

        r2 = l3.first { it == 5 }
        assert(r2 == 5)
        r2 = l3.firstOrNull { it == 5 }
        assert(r2 == 5)
        r2 = l3.first()
        assert(r2 == 1)

        var flag = false
        try {
            r2 = l3.first { it == 9 }       // this is null, var r2: Int?
        } catch(e: NoSuchElementException) {
            flag = true
        }
        assert(flag)
        var r3 = l3.find { it == 9 }        // this is null, but r3 not declared as null?
        assert(r3 == null)

        r2 = l3.firstOrNull { it == 9 }
        assert(r2 == null)

        l3 = listOf(1,2,3,4,5)
        l4 = l3.drop(1)
        assert(l4 == listOf(2,3,4,5))
        l4 = l3.drop(0)
        assert(l4 == listOf(1,2,3,4,5))
        l4 = l3.drop(4)
        assert(l4 == listOf(5))
        l4 = l3.drop(5)
        assert(l4 == emptyList<Int>())
        l4 = l3.dropLast(1)
        assert(l4 == listOf(1,2,3,4))
        r2 = l3.find { it == 3 }
        assert(r2 == 3)

        var ba1: ByteArray = byteArrayOf()
        var bl1: List<Byte> = ba1.toList()
        assert(ba1.size == 0 && bl1.size == 0)
        ba1 += 0x12
        ba1 += 0x23
        ba1 += 0x34
        bl1 = ba1.toList()
        assert(ba1.size == 3 && bl1.size == 3)

        // random list of 10 values
        var lr1 = (1..10).map {
            Random().nextInt(100)
        }
        var lr2 = IntArray(10) {
            Random().nextInt(100)
        }.asList()
    }

    @Test
    fun testArray() {
        var ai = arrayOf(1,2,3)
        assert(ai[1] == 2)
        assert(ai.size == 3)

        ai = arrayOf<Int>(1,2,3)
        assert(ai[1] == 2)
        assert(ai.size == 3)

        ai = Array(3, { i->i+1 })
        assert(ai[1] == 2)
        assert(ai.size == 3)

        var ia: IntArray = intArrayOf(1,2,3)
        assert(ia[1] == 2)
        assert(ia.size == 3)

        var aai = arrayOf(
            arrayOf(1,2,3),
            arrayOf(4,5,6)
        )

        assert(aai[1][1] == 5)
        assert(aai.size == 2)
        assert(aai[1].size == 3)
        assert(aai[1].contentToString() == "[4, 5, 6]")

        var iaa = arrayOfNulls<IntArray>(3)
        assert(iaa.size == 3)
        iaa[0] = intArrayOf(1,2)
        assert(iaa[0]?.size == 2)
        assert(iaa[1] == null)

        var m = Array(3) { Array(2) {0} }
        assert(m.size == 3)
        assert(m[2].size == 2)
        assert(m[2][1] == 0)
    }

    @Test
    fun testMapSet() {

    }

    @Test
    fun testScopedFunctions() {
        // let, run, with, also, apply

        fun doLet1(): String {
            val p = Person().let {
                it.nameFirst = "food"
                return@let "name is ${it.nameFirst}"
            }
            return p
        }

        fun doLet2(): String {
            // because let uses it, it can be renamed
            val p = Person().let {
                foo -> foo.nameFirst = "food"
                return "name is ${foo.nameFirst}"
            }
            return p
        }

        fun doLet3(): String {
            // it is referring to mytest.Person().nameFirst
            val p = Person().nameFirst?.let {
                "name is $it"
            }
            return p ?: "blank"
        }
        assert(doLet2() == doLet1())
        assert(doLet3() == "name is first")

        // run uses this instead of it
        // run can be used to initialize
        fun doRun1(): String {
            // this keyword is implicit and can be omitted
            return Person().run {
                nameFirst = "newFirst"
                nameLast = "newLast"
                return@run show()   // needs the @run
            }
        }
        assert(doRun1() == "(1, newFirst, newLast)")

        fun doRun2(): String? {
            // this refers to mytest.Person().nameFirst
            return Person().nameFirst?.run {
                "firstName is $this"
            }
        }
        assert(doRun2() == "firstName is first")

        fun doRun3(): String? {
            val p1: Person? = null
            return p1?.nameFirst?.run {
                "firstName is $this"
            }
        }
        assert(doRun3()?:"blank" == "blank")

        fun doRun4(): String? {
            val p1: Person? = null
            p1?.run {
                nameFirst = "first1"
                nameLast = "last1"
            }

            val p2: Person? = Person()
            p2?.run {
                nameFirst = "first2"
                nameLast = "last2"
            }

            val r: String? = p1?.nameFirst ?: p2?.nameFirst
            return r
        }
        assert(doRun4() == "first2")

        // with uses this
        // apply uses this instead of it

        // also is similar to let, using it and not this, which
        // means lambda can be used
        // also does not accept a return statement though
    }
    @Test
    fun testLazyInit() {
        val sb: StringBuilder = StringBuilder()
        var o1Immediate: Object1 = Object1(1, sb)
        var o2Immediate: Object1 = Object1(null, sb)

        // lazy must use val, not var (syntax error), so it's fixed...
        // lazy means o3Lazy object init is deferred til first use
        val o3Lazy: Object1 by lazy {
            sb.append("point2,")
            Object1(3, sb)
        }
        sb.append("point1,")
        o3Lazy.vi1 = 10
        var res = sb.toString()

        // point1 happens before id:3 because of lazy
        assert(res == "id:1,id:null,point1,point2,id:3,")

        // lateinit. every non-nullable property should be initialized,
        // else compiler says Property must be initialized or be abstract.
        // lateinit is deferred init. this is useful for dynamic or dependency injection
        // saying variable is lateinit frees compiler from checking this var
        // lateinit is only for Objects, not primitive types

        lateinit var o4LateInit: Object1
        // compiler doesnt complain because o4LateInit is not class variable.
        // but this is just to illustrate its use
        o4LateInit = Object1(20, sb)
    }
    class StringHolder1(val s1: String)
    class StringHolder2(val s1: String) {
        override fun equals(other: Any?): Boolean {
            return when(other) {
                is StringHolder2 -> s1 == other.s1
                else -> false
            }
        }
    }
    @Test
    fun testString() {
        var vi1: Int? = null
        var vi2: Int? = 10
        var vs1: String?
        vs1 = "the val is $vi2"
        assert(vs1 == "the val is 10")
        var vs2: String = "a" +
                "b"
        assert(vs2 == "ab")
        val byteArray: ByteArray = "hello1234".toByteArray() // == byte[] = string.getBytes()

        vs1 = "the cat in the hat"
        vs2 = "the cat in the hat"
        assert(vs1 == vs2)
        assert(vs1.equals(vs1))

        vs2 = "the bot in the pot"
        assert(vs1 != vs2)
        assert(!vs1.equals(vs2))

        var sh1a = StringHolder1(vs1)
        var sh1b = StringHolder1(vs1)
        assert(sh1a != sh1b)
        assert(!sh1a.equals(sh1b))

        var sh2a = StringHolder2(vs1)
        var sh2b = StringHolder2(vs1)
        assert(sh2a == sh2b)
        assert(sh2a.equals(sh2b))
    }
    @Test
    fun testGetCharsetSeq() {
        var s = getCharSeq(100, true)
        println(s)
    }
    @Test
    fun testForLoop() {
        var sb = StringBuilder()
        for(i in 0..5) {
            sb.append(if(i == 0) "$i" else ",$i")
        }
        assert(sb.toString() == "0,1,2,3,4,5")

        var l = (0..5).toList()
        assert(l == listOf(0,1,2,3,4,5))

        sb.clear()
        l.forEach {
            sb.append(if(it == 0) "$it" else ",$it")
        }
        assert(sb.toString() == "0,1,2,3,4,5")

        var ls = listOf("a1","b1","c1","d1","e1")
        sb.clear()
        for(s in ls) {
            sb.append(s)
        }
        assert(sb.toString() == "a1b1c1d1e1")

        sb.clear()
        ls.forEachIndexed {
            i, v ->
            sb.append("$i:$v ")
        }
        assert(sb.toString() == "0:a1 1:b1 2:c1 3:d1 4:e1 ")

        var s = "abcde"
        var as2l = s.toList()   // to list of char, not string
        var es2lc = listOf('a','b','c','d','e')     // list of char
        var es2ls = listOf("a","b","c","d","e")     // list of string
        assert(as2l == es2lc)
        assert(as2l != es2ls)
        sb.clear()
        for(c in s) {
            sb.append(c)
        }
        assert(sb.toString() == "abcde")

        sb.clear()
        for(i in 0..5) {
            sb.append("$i,")
        }
        assert(sb.toString() == "0,1,2,3,4,5,")

        var lc = "abcde".toList()
        sb.clear()
        for((i,c) in lc.withIndex()) {
            sb.append("$i:$c,")
        }
        assert(sb.toString() == "0:a,1:b,2:c,3:d,4:e,")

        sb.clear()
        repeat(5) {
            i -> sb.append("$i,")
        }
        assert(sb.toString() == "0,1,2,3,4,")
    }
    @Test
    fun testByteArray() {
        var li1 = listOf(0x11,0x22,0x33)
        var li2 = li1
        assert(li1 == listOf(0x11,0x22,0x33))
        assert(li1 !== listOf(0x11,0x22,0x33))
        assert(li1 === li2)
        assert(li1.equals(listOf(0x11,0x22,0x33)))

        var ba1 = byteArrayOf(0x10,0x20,0x30,0x40,0x50)
        var ba2 = byteArrayOf(0x11,0x22,0x33,0x44,0x55)
        var ba3 = ba1+ba2
        assert(ba3.contentEquals(byteArrayOf(0x10,0x20,0x30,0x40,0x50,0x11,0x22,0x33,0x44,0x55)))

        var ba4 = ba2.copyOf()
        ba4.set(1,0x23)
        assert(ba2.contentEquals(byteArrayOf(0x11,0x22,0x33,0x44,0x55)))
        assert(ba4.contentEquals(byteArrayOf(0x11,0x23,0x33,0x44,0x55)))

        // list of Byte to ByteArray
        var lob1: List<Byte> = listOf(0x12,0x23,0x34)
        // cannot use listOf!, use byteArrayOf and convert to list....
        assert(lob1 != listOf(0x12,0x23,0x34))
        assert(lob1 == byteArrayOf(0x12,0x23,0x34).toList())
        assert(!lob1.equals(listOf(0x12,0x23,0x34)))
        assert(lob1.equals(byteArrayOf(0x12,0x23,0x34).toList()))

        var ba5: ByteArray = lob1.toByteArray()
        var lob2: List<Byte> = ba5.toList()
        assert(lob2 != listOf(0x12,0x23,0x34))
        assert(lob1 == byteArrayOf(0x12,0x23,0x34).toList())
        assert(ba5.contentEquals(byteArrayOf(0x12,0x23,0x34)))

        // get a slice as List<Byte>
        var slice1: List<Byte> = ba4.slice(1..3)
        assert(ba4.contentEquals(byteArrayOf(0x11,0x23,0x33,0x44,0x55)))
        assert(!slice1.equals(listOf(0x23,0x33,0x44)))
        assert(slice1 != listOf(0x23,0x33,0x44))
        assert(slice1 == byteArrayOf(0x23,0x33,0x44).toList())

        // get a slice as ByteArray
        var slice2: ByteArray = ba4.sliceArray(1..3)
        assert(ba4.contentEquals(byteArrayOf(0x11,0x23,0x33,0x44,0x55)))
        assert(slice2.contentEquals(byteArrayOf(0x23,0x33,0x44)))
        assert(slice2 != byteArrayOf(0x23,0x33,0x44))

        // get slice to end
        slice2 = ba4.sliceArray(1..ba4.size-1)
        assert(slice2.contentEquals(byteArrayOf(0x23,0x33,0x44,0x55)))

        // get slice of beginning to mid
        slice2 = ba4.sliceArray(0..3)
        assert(slice2.contentEquals(byteArrayOf(0x11,0x23,0x33,0x44)))
    }
    @Test
    fun testIntStream() {
        var l = (0..5).toList()
        var seq: Sequence<Int> = l.stream().asSequence()
        var s: String

        s = l.joinToString { "$it" }
        assert(s == "0, 1, 2, 3, 4, 5")

        s = seq.joinToString(",")
        assert(s == "0,1,2,3,4,5")
        assert(s.toByteArray().contentEquals(byteArrayOf(48,44,49,44,50,44,51,44,52,44,53)))

        seq = l.stream().asSequence()
        s = seq.joinToString { i -> "$i" }
        assert(s.toByteArray().contentEquals(byteArrayOf(48,44,32,49,44,32,50,44,32,51,44,32,52,44,32,53)))
        assert(s == "0, 1, 2, 3, 4, 5")

        var l1 = l.stream().toList()
        assert(l1 == listOf(0,1,2,3,4,5))

        s = l.stream().map { i -> "$i" }.collect(Collectors.toList()).toString()
        assert(s == "[0, 1, 2, 3, 4, 5]")

        var sb = StringBuilder()
        l.stream().map { i -> sb.append(if(i == 0) "$i" else ",$i") }
        s = sb.toString()
        assert(s == "")

        l.stream().forEach { sb.append(if(it == 0) "$it" else ",$it") }
        s = sb.toString()
        assert(s == "0,1,2,3,4,5")

        l.stream().map { sb.append(if(it == 0) "$it" else ",$it") }
        s = sb.toString()
        assert(s == "0,1,2,3,4,5")

        s = l.stream().map { it -> it.toString() }.collect(Collectors.toList()).toString()
        assert(s == "[0, 1, 2, 3, 4, 5]")
        return
    }
    @Test
    fun testTakeIf() {
        // takeIf can be called on any non-null object and takes predicate as arg
        var o2a: Object2 = Object2(1)
        var o2b: Object2 = Object2(2)

        var res: Object2? = null

        res = if(o2a.isIdOdd()) o2a else null
        assert(res != null)
        res = o2a.takeIf { it.isIdOdd() }
        assert(res != null)
        res = o2a.takeIf { !it.isIdOdd() }
        assert(res == null)
        res = o2b.takeIf { it.isIdOdd() }
        assert(res == null)
    }
    @Test
    fun testStringSplit() {
        var l: List<String>
        l = " the cat in the  hat".split(" ")
        assert(l == listOf<String>("","the","cat","in","the","","hat"))
        l = " the cat in the  hat".split("\\s+")
        assert(l == listOf(" the cat in the  hat"))
        l = " the cat in the  hat".split(Regex("\\s+"))
        assert(l == listOf("","the","cat","in","the","hat"))
    }

    @Test
    fun testUseObjectElseOtherOperation() {

        class ObjLocal(val s: String)

        fun processStringLocal(s: String): ObjLocal = ObjLocal(s)

        fun returnList(l: List<String>?): List<String>? = l

        var strval = "the cat in the hat"
        var l: List<String>? = strval.split(" ")

        var ro: ObjLocal

        // does not work
        //ro = l?.firstOrNull { s -> s == "hat" }.also { it -> ObjLocal(it) } ?: processStringLocal(strval)

        // does not work
        //ro = l?.firstOrNull { s -> s == "hat" }.map { it -> ObjLocal(it) } ?: processStringLocal(strval)

        ro = l?.firstOrNull { s -> s == "hat" }?.let { it -> ObjLocal(it) } ?: processStringLocal(strval)
        assert(ro.s == "hat")

        ro = l?.firstOrNull { s -> s == "hat" }?.let { ObjLocal(it) } ?: processStringLocal(strval)
        assert(ro.s == "hat")

        ro = l?.firstOrNull { it == "hat" }?.let { ObjLocal(it) } ?: processStringLocal(strval)
        assert(ro.s == "hat")

        ro = l?.firstOrNull { it == "dog" }?.let { ObjLocal(it) } ?: processStringLocal(strval)
        assert(ro.s == "the cat in the hat")

        l = returnList(null)
        ro = l?.firstOrNull { s -> s == "hat" }?.let { ObjLocal(it) } ?: processStringLocal(strval)
        assert(ro.s == "the cat in the hat")


        return
    }
    /*
     *  scope functions
     *
     *  let             it      return lambda result
     *  run             this    return lambda result
     *  with            this    return lambda result
     *  apply           this    return context obj this
     *  also            it      return context obj it
     *
     *  takeIf          it      return Boolean
     *  takeUnless      it      return Boolean
     */
    /*
     * inline fun  with(receiver: T, block: T.() -> R): R
     * {
     *     return receiver.block()
     * }
     */
    @Test
    fun testWith() {

    }
    /*
     * inline fun  T.apply(block: T.() -> Unit): T
     * {
     *     block()
     *     return this
     * }
     *
     * with runs without an object whereas apply needs one object to run
     * apply runs on the object reference, but with simply passes it as the argument
     */
    @Test
    fun testApply() {
        var cat = "cat"
        var res: TransformString.Companion = TransformString.apply {
            addHi(cat)
            addBye(cat)
        }

        var res1: String = TransformString.addHi(cat)
        assert(res1 == "hi cat")
        res1 = TransformString.addHi(cat).also {
            TransformString.addBye(it)
        }.also {
            TransformString.addMyName(it)
        }
        assert(res1 == "hi cat")

        var sb = StringBuilder()
        TransformString.apply {
            addHi(cat, sb)
            addBye(cat, sb)
        }

        assert(sb.toString() == "hi catbye cat")
        return
    }

    @Test
    fun testLateInit() {
        var v1: String
        lateinit var v2: String

        // this will cannot compile, will say it must be initialized first. comment out
        //assert(v1 == null)

        // this can compile because lateinit
        var b = false
        try {
            assert(v2 != null)
        } catch(e: UninitializedPropertyAccessException) {
            b = true
        }
        assert(b)
    }
    /*
     * by keyword as in provided by
     *
     * class MyClass: SomeInterface by SomeImplementation, SomeOtherInterface
     *
     * This code is saying: 'I am class MyClass and I offer functions of interface SomeInterface
     * which are provided by SomeImplementation. I'll implement SomeOtherInterface by myself
     */
    @Test
    fun testBy() {

    }

    /*
     * typealias NodeSet = Set<Network.Node>
     * typealias FileTable<K> = MutableMap<K, MutableList<File>>
     * typealias MyHandler = (Int, String, Any) -> Unit
     * typealias Predicate<T> = (T) -> Boolean
     * typealias AInner = A.Inner
     */
    @Test
    fun testTypeAliases() {

    }

    @Test
    fun testRegex() {
        var s: String
        var re: Regex
        var re1: Regex
        var matchResult: MatchResult?
        s = "this is prefix1: the cat 11 in the hat 222 is back"
        var res1 = """this is prefix1:.*\s+cat\s+(\d+).*\s+hat\s+(\d+)"""
        re = res1.toRegex()
        matchResult = re.find(s)
        assert(matchResult != null)
        assert(matchResult!!.groups!!.size == 3)
        assert(matchResult!!.groups[1]!!.value == "11")
        assert(matchResult!!.groups[2]!!.value == "222")

        re1 = Regex.fromLiteral(res1)
        matchResult = re1.find(s)
        assert(matchResult == null)
        //assert(matchResult!!.groups!!.size == 3)
        //assert(matchResult!!.groups[1]!!.value == "11")
        //assert(matchResult!!.groups[2]!!.value == "222")

        re1 = Regex(res1)
        matchResult = re1.find(s)
        assert(matchResult != null)
        assert(matchResult!!.groups!!.size == 3)
        assert(matchResult!!.groups[1]!!.value == "11")
        assert(matchResult!!.groups[2]!!.value == "222")

        var result: Int?
        result = matchResult?.groups[2]?.value!!.toInt() - matchResult?.groups[1]?.value!!.toInt()
        result = (matchResult?.groups[2]?.value?.toInt() ?: 0) - (matchResult?.groups[1]?.value?.toInt() ?: 0)
        result = (matchResult?.groups?.get(2)?.value?.toInt() ?: 0) - (matchResult?.groups?.get(1)?.value?.toInt() ?: 0)
        // result = matchResult?.groups?.get(2)?.value?.toInt() - matchResult?.groups?.get(1)?.value?.toInt()

        var res2 = """this is prefix1:.*\s+cat\s+(\d+).*\s+hat\s+(\w)"""
        re = res2.toRegex()
        s = "this is prefix1: the cat 11 in the hat w222 is back"
        matchResult = re.find(s)

        var b = false
        try {
            result = matchResult?.groups?.get(2)?.value?.toInt() ?: 1
        } catch(e: NumberFormatException) {
            b = true    // because get(2) == w222
        }
        assert(b)

        var s1: String? = s
        //matchResult = takeIf { s1 != null }?.run {
        //    re1.find(s1)
        //}

        s = "this is not prefix: the cat 22 in the hat 33 is back"
        matchResult = re.find(s)
        assert(matchResult == null)

    }

    @Test
    fun testRunLetApplyWithAlso() {
        /*
         *          rescope
         * run:     this    returns anything
         * let:     it      returns anything, but it is local and this ref is outer scope unchanged
         *                      useful when need to use local it, and external this
         * apply:   this    returns receiver/this (which is useful for builder)
         * also     it      use with apply, and not shadow this
         * with     this    returns last expression
         */
        data class LObjInner(val v1: String? = null, val v2: Int? = null)
        data class LObjNullable(val v1: String? = null, val v2: Int? = null, val obj: LObjInner? = null)
        data class P(val k: String, val v: String)
        val lobj = listOf(
            null,
            LObjNullable(),
            LObjNullable(v1 = "v", v2 = 1, obj = LObjInner()),
            LObjNullable(v1 = "v", v2 = 1, obj = LObjInner(v1 = "vi", v2 = 11))
        )

        if(true) {
            // val r = lobj[10]?: "null" // results in ArrayIndexOutOfBoundsException
            val r = lobj.getOrNull(10) ?: "null" // safe way to get out of bounds
            assert(r == "null")
        }

        // cannot have java style { code }, as it's interpreted as trailing lambda arg
        if(true) {
            val resEval = mutableListOf<P>()
            val resRet = mutableListOf<String>()
            val resVal = mutableListOf<String>()
            lobj.forEach { o ->
                val r1 = o?.v1?.let {
                    resEval.add(P("v1", it))
                    "x"
                }
                resRet.add(r1?.javaClass?.name ?: "null")
                resVal.add(r1 ?: "null")
            }

            assert(resEval.size == 2 && listOf(P("v1", "v"), P("v1", "v")).equals(resEval))
            assert(resEval.size == 2 && !listOf(P("v1", "v"), P("v1", "x")).equals(resEval))
            assert(resRet.size == 4 && listOf("null", "null", "java.lang.String", "java.lang.String").equals(resRet))
            assert(resVal.size == 4 && listOf("null", "null", "x", "x").equals(resVal))
        }

        if(true) {
            val resEval = mutableListOf<P>()
            val resRet = mutableListOf<String>()
            val resVal = mutableListOf<String>()
            lobj.forEach { o ->
                val r1 = o?.v1?.run {
                    resEval.add(P("v1", this))
                    "x"
                }
                resRet.add(r1?.javaClass?.name ?: "null")
                resVal.add(r1 ?: "null")
            }
            assert(resEval.size == 2 && listOf(P("v1", "v"), P("v1", "v")).equals(resEval))
            assert(resEval.size == 2 && !listOf(P("v1", "v"), P("v1", "x")).equals(resEval))
            assert(resRet.size == 4 && listOf("null", "null", "java.lang.String", "java.lang.String").equals(resRet))
            assert(resVal.size == 4 && listOf("null", "null", "x", "x").equals(resVal))

        }

        if(true) {
            val resEval = mutableListOf<P>()
            val resRet = mutableListOf<String>()
            val resVal = mutableListOf<String>()
            lobj.forEach { o ->
                val r1 = o?.v1?.apply {
                    resEval.add(P("v1", this))
                    this
                }
                resRet.add(r1?.javaClass?.name ?: "null")
                resVal.add(r1 ?: "null")
            }
            assert(resEval.size == 2 && listOf(P("v1", "v"), P("v1", "v")).equals(resEval))
            assert(resEval.size == 2 && !listOf(P("v1", "v"), P("v1", "x")).equals(resEval))
            assert(resRet.size == 4 && listOf("null", "null", "java.lang.String", "java.lang.String").equals(resRet))
            assert(resVal.size == 4 && listOf("null", "null", "v", "v").equals(resVal))
        }

        if(true) {
            val resEval = mutableListOf<P>()
            val resRet = mutableListOf<String>()
            val resVal = mutableListOf<String>()
            lobj.forEach { o ->
                // with is mainly for abbreviating and accessing methods of the with(OBJ), usually in construction
                // inline fun <T, R> with(receiver: T, block: T.() -> R): R (source)
                // T.() -> R: It's called function literal with receiver.
                // a lambda that can access the receiver's members without any additional qualifiers.
                val r1 = with(o) {
                    this?.v1?.apply {
                        resEval.add(P("v1", this))
                        this
                    }
                }
                resRet.add(r1?.javaClass?.name ?: "null")
                resVal.add(r1 ?: "null")
            }
            assert(resEval.size == 2 && listOf(P("v1", "v"), P("v1", "v")).equals(resEval))
            assert(resEval.size == 2 && !listOf(P("v1", "v"), P("v1", "x")).equals(resEval))
            assert(resRet.size == 4 && listOf("null", "null", "java.lang.String", "java.lang.String").equals(resRet))
            assert(resVal.size == 4 && listOf("null", "null", "v", "v").equals(resVal))
        }
    }
    @Test
    fun testEnumConversion() {
        val lStr = listOf("mytest.Foo", "foo", "FOO", "bee", "", "mytest.Bar", "BAR", "bar", "Boo", "BOO")
        var lEnumRes = mutableListOf<LEC>() // Foo, Bar, BAR, Boo, Moo

        lStr.forEach {
            try {
                lEnumRes.add(LEC.valueOf(it))
                print("testEnumConversion enum conversion OK    with $it\n")
            } catch(e: IllegalArgumentException) {
                print("testEnumConversion enum conversion error with $it\n")
            }
        }
        assertEquals(listOf(
            LocalEnumConversion.BAR,
            LocalEnumConversion.Boo
        ), lEnumRes, "assert failure with lEnumRes")

        val lEnum = listOf(
            LocalEnumConversion.Foo,
            LocalEnumConversion.Bar,
            LocalEnumConversion.BAR,
            LocalEnumConversion.Boo
        )
        var lStrRes = mutableListOf<String>()
        lEnum.forEach {
            try {
                lStrRes.add(it.toString())
                print("testEnumConversion str  conversion OK    with $it\n")
            } catch(e: IllegalArgumentException) {
                print("testEnumConversion str  conversion error with $it\n")
            }
        }
        //assertEquals(listOf("mytest.Foo","mytest.Bar","BAR","Boo"), lStrRes, "assert failure with lStrRes")
        assertEquals(listOf("Foo","Bar","BAR","Boo"), lStrRes, "assert failure with lStrRes")
    }
    @Test
    fun testTransformList1() {
        var li = listOf(1, 2, 3, 4, 5)
        var ls = li.map {
            "$it"
        }
        assertEquals(listOf("1", "2", "3", "4", "5"), ls)
    }

    @Test
    fun testTransformList2() {
        fun toString(i: Int): String = "$i"
        var li = listOf(1, 2, 3, 4, 5)
        var ls = li.map {
            toString(it)
        }
        assertEquals(listOf("1", "2", "3", "4", "5"), ls)
    }

    @Test
    fun testReturnAlso() {
        fun shell1(s1: String, s2: String, sb: StringBuilder): String {
            return "$s1 $s2"
                .also { sb.append(it) }
        }
        fun shell2(s1: String, s2: String, sb: StringBuilder): String {
            var r = shell1("foo", "bar", sb)
            assert(r == "foo bar")
            return r.also { "$r $r" }
        }
        fun shell3(s1: String, s2: String, sb: StringBuilder): String {
            var r = shell1("foo", "bar", sb)
            assert(r == "foo bar")
            return r.run { "$r $r" }
        }
        fun shell4(s1: String, s2: String, sb: StringBuilder): String {
            return shell1("foo", "bar", sb)
                .run { "$this $this" }
                .run { "$this $this" }
        }
        fun shell5(s1: String, s2: String, sb: StringBuilder): String {
            return shell1("foo", "bar", sb)
                .let { "$it $it" }
                .let { "$it $it" }
        }
        fun shell6(s1: String, s2: String, sb: StringBuilder): String {
            return "$s1 $s2"
                .also { "$it $it" }
        }

        fun shell7(s1: String, s2: String, sb: StringBuilder, foo: Foo? = null): FooFoo =
            FooFoo(s = s1).also { it.foo = foo }

        var sb = StringBuilder()
        var r: String

        r = shell1("foo", "bar", sb)
        assert(r == "foo bar")

        r = shell2("foo", "bar", sb)
        assert(r == "foo bar")

        r = shell3("foo", "bar", sb)
        assert(r == "foo bar foo bar")

        r = shell4("foo", "bar", sb)
        assert(r == "foo bar foo bar foo bar foo bar")

        r = shell5("foo", "bar", sb)
        assert(r == "foo bar foo bar foo bar foo bar")

        r = shell6("foo", "bar", sb)
        assert(r == "foo bar")

        var rf: FooFoo
        rf = shell7("foo", "bar", sb)
        assert(rf.foo?.let { it.s == "bad" } ?: true)
        rf = shell7("foo", "bar", sb, Foo("bar"))
        assert(rf.foo?.let { it.s == "bar" } ?: false)

    }
}
// file scope only
private enum class LocalEnumConversion {
    Foo, Bar, BAR, Boo, Moo
}
private typealias LEC = LocalEnumConversion


class FunctionTests {
    @Test
    fun testLambdaVsRun() {
        var vi1 = 1
        var vi2 = 2
        val l1 = {
            val r = vi1+vi2
            r
        }
        // run is inline function that supports braces
        val v1 = run {
            val r = vi1+vi2
            r
        }
        val v2 = {
            val r = v1+vi2
            r
        }()
        assert(l1() == 3)
        assert(v1 == 3)
        assert(v2 == 5)
    }
    @Test
    fun testLambdaBasic() {
        /*
         * lambda is:   val lambdaName: Type = { parameterList -> code }
         *
         *                              -type-------------
         *                  -name-----  -in type-    -ret-     -params---------    -code-
         *              val lambdaName: (Int,Int) -> Int   = { i1: Int, i2: Int -> i1+i2 }
         *
         * Unit ret means void
         *
         *
         */

        var res: Int?

        val vi1: Int?
        var vi2: Int?
        // cannot do lambda with uninitialized vi1 and vi2
        // val l1 = { (vi1 ?: 0) + (vi2 ?: 0) }


        var vi3InitVar: Int = 1
        val vi4InitVal: Int = 2
        // A closure is a scope of variables that can be accessed in the body of the function.
        val l1a = { vi3InitVar + vi4InitVal }   // closure example
        val l1b = { 3 }
        val l1c: () -> Int = { 3 }
        assert(l1a() == 3)
        assert(l1b() == 3)
        assert(l1c() == 3)

        val l2: (Int,Int,Int) -> Int = { i1:Int, i2:Int, i3:Int-> i1+i2+i3 }
        assert(l2(1,2,3) == 6)

        val l3: (Int,Int) -> String = { i1:Int, i2:Int -> "($i1,$i2)" }
        assert(l3(1,2) == "(1,2)")

        val l4: (Int,Int) -> Unit = { i1:Int, i2:Int -> res = i1+i2 }
        res = null
        l4(1,2)
        assert(res == 3)

        val l5: (Int, SimpleClassObject) -> Unit = { i1:Int, o: SimpleClassObject -> o.v1 = i1 }
        val sco = SimpleClassObject(3)
        l5(1,sco)
        assert(sco.v1 == 1)

        // not legal syntax
        //val l6: (i1: Int, i2: Int) -> Int = { i1+i2 }

        // pass a function with ::
        fun f6(i1: Int, i2:Int): Int = i1+i2
        fun f7(i1: Int, f:(Int,Int) -> Int): Int = f(i1,i1)
        assert(f7(3, ::f6) == 6)

        val f8a: (Int,Int) -> Int = { i1:Int,i2:Int -> i1+i2 }
        val f8b: (Int,Int) -> Int = { i1,i2 -> i1+i2 }
        val f8c = { i1:Int, i2:Int -> i1+i2 }
        val f8d: Int.(Int) -> Int = { x -> this+x }
        fun f8e(i1:Int, i2:Int): Int = i1+i2
        fun f8f(i1:Int, i2:Int): Int { return i1 + i2 }
        val f8g = { i1:Int,i2:Int -> i1+i2 }
        val f8i = { i1:Int,i2:Int ->
            var r = i1+i2
            r
        }
        assert(f8a(2,3) == f8b(2,3))
        assert(f8a(2,3) == f8c(2,3))
        assert(f8a(2,3) == f8d(2,3))
        assert(f8a(2,3) == f8e(2,3))
        assert(f8a(2,3) == f8f(2,3))
        assert(f8a(2,3) == f8g(2,3))
        assert(f8a(2,3) == f8i(2,3))

    }

    /*
     * inline operator fun <reified S : Task> TaskContainer.invoke(configureAction: Action<in S>) =
     * withType(S::class.java, configureAction)
     *
     * project.tasks<Task> { ... }
     *
     */

    /*
     * lambda here:
     *
     * fun <T> X<T>.y(
     *      a: A,
     *      f: (Z.(T) -> Unit)
     * )
     *
     *      Z.(T) -> Unit
     *      T.() -> Unit
     *          extension function type with receiver
     *
     * inline fun <R> run(block: () -> R): R { return block() }
     *
     * inline fun <T,R> T.run(block: T.() -> R): R { return block() }
     *
     * inline fun<T> with(t: T, block: T.() -> Unit) { t.block() }
     *
     * inline fun <T,R> with(t: T, block: T.() -> R): R { return t.block() }
     *
     * inline fun <T> T.apply(block: T.() -> Unit): T { block(); return this }
     *
     * inline fun <T> T.also(block: (T) -> Unit): T { block(); return this }
     *
     * inline fun <T, R> T.let(block: (T) -> R): R { return block(this) }
     *
     * inline fun <T> T.takeIf(predicate: (T) -> Boolean): T? { return if (predicate(this)) this else null }
     *
     * function literal with receiver:
     *      t: T.() -> Unit = {}
     *          () -> Unit      function type
     *          T               receiver
     */

    inline fun<T> with1(t: T, body: T.() -> Unit): Unit {
        t.body()
    }
    inline fun<T> with2(t: T, body: T.() -> T): T {
        t.body()
        return t
    }
    inline fun<R,T> with4(t: T, r: R, body: T.() -> Unit): R {
        t.body()
        return r
    }
    inline fun<T> with5a(t1: T, t2: T, body: T.() -> Unit): T {
        t1.body()
        return t1
    }
    inline fun<T> with5b(t1: T, t2: T, body: T.() -> Unit): T {
        t1.body()
        return t2
    }
    inline fun<T> with6a(t: T, body: T.() -> T): Unit {
        t.body()
    }
    inline fun<T> with6b(t: T, body: T.() -> Unit): T {
        t.body()
        return t
    }
    inline fun<T> with6c(t: T, body: T.(s: String) -> Unit): T {
        t.body("11")
        return t
    }
    inline fun<T> with6d(t: T, body: T.(s: String) -> T): Unit {
        t.body("11")
    }
    // inline fun<R,T> f1a(t: T, f: (r: R, t: T) -> R)

    @Test
    fun testInlineWith1() {
        val fb = FooBar("20", 10)
        val fbc = FooBar("", 0)
        val r1 = with1(fb) {
            fbc.s = s
        }
        val r21 = with2(fb) {
            fbc.i = i+100
            fb                      // why is this required?
        }
        val r22 = with2(fb) {
            fbc.i = i+200
            fbc                     // why is this required when not even used?
        }
        val r41 = with4(fb,fbc) { fbc.i = i+300 }
        val r5a1 = with5a(fb,fbc) { fbc.i = i+100 }
        val r5b1 = with5b(fb,fbc) { fbc.i = i+100 }
        assert(r1 == Unit)
        assert(r21 == fb)
        assert(r22 == fb)
        assert(r41 == fbc)
        assert(r5a1 == fb)
        assert(r5b1 == fbc)
    }

    @Test
    fun testLamba2() {
        fun <T> processWithHandler(v1: Int, v2: Int, f: (Int) -> T): T {
            val v3 = v1 + v2
            val isOdd = v3 % 2 == 1
            val res = if(isOdd) 1 else 0
            return when {
                isOdd -> {
                    10
                }
                else -> {
                    30
                }
            }.let { v -> f(v) }
        }

        var res = processWithHandler(2,3) { foo -> SimpleClassObject(foo) }
        assert(res.doubleStringVal() == "20")

        // the f in processWithHandler is not passed, so following is invalid syntax!
        // val l1 = { v1:Int -> v1*100 }
        // res = processWithHandler(2,3, l1) { foo -> mytest.SimpleClassObject(foo) }


    }
    /*
     * functional interface is an interface with only 1 method. this enables shortened
     * definition syntax
     */
    @Test
    fun testFunctionalInterfaces() {

    }
    /*
     * fun<T> f(v: String, f: (String) -> T): T { .. } ?
     *
     * pass a function in of type string, returns type t
     *
     * called a trailing lambda:
     *
     * if the last parameter of a function is a function, then a lambda expression
     * passed as the corresponding argument can be placed outside the parentheses
     */
    @Test
    fun testFunctionComposition() {
        //fun <T,mytest.V> f1(v: mytest.V, f: (mytest.V) -> T): T { }
        fun <T> fa2(v: String, f: (String) -> T): T {
            var res = "(fa2=$v)"
            return f(res)
        }
        fun fa3(v: String): String {
            var res = "(fa3=$v)"
            return res
        }
        fun fa4(v: String): ByteArray {
            var res = "(fa4=$v)"
            return res.toByteArray()
        }
        fun testfa2() {
            var res1: String = fa2("hello") {
                    r -> fa3(r)
            }
            assert(res1 == "(fa3=(fa2=hello))")
            var res2: ByteArray = fa2("hello") {
                    r -> fa4(r)
            }
            var res3: String = fa2("hello", ::fa3)
            assert(res3 == "(fa3=(fa2=hello))")
        }

        fun <T> fb1(v1: String, v2: String, f: (String) -> T): T {
            var res = "(fb1=$v1)"
            return f("$v2,$res")
        }
        fun testfb1() {
            // the function is implemented here
            var res1: String = fb1("hello","bye") {
                    r -> fa3(r)
            }
            assert(res1 == "(fa3=bye,(fb1=hello))")
            var res2: String = fb1("hello", "bye", ::fa3)
            assert(res2 == "(fa3=bye,(fb1=hello))")
        }

        fun <T,U> fc1(v1:T,v2:U, f1:(T,U)->U, f2:(U)->T): T = f2(f1(v1,v2))
        fun fc2(v1: String, v2: Int): Int = v1.toInt()+v2
        fun fc3(v: Int): String = "$v:$v"
        fun fc4(v1: String, v2: Int): Int {
            var res: Int
            try {
                res = v1.toInt()
            } catch(e: NumberFormatException) {
                res = 1
            }
            return res+v2
        }
        fun testfc1() {
            var res1: String = fc1("10",5,::fc2,::fc3)
            assert(res1 == "15:15")
            var res2: String = fc1("hi",5,::fc4,::fc3)
            assert(res2 == "6:6")
            var res3: String = fc1("10",5,::fc4) {
                    v -> "$v"
            }
            assert(res3 == "15")
        }

        fun <T> fd1(v1: String, v2: String, f: (String, String) -> T): T = f(v1,v2)
        fun testfd1() {
            var res1: String = fd1("hi","bye") {
                    a,b -> "$a:$b"
            }
            assert(res1 == "hi:bye")

        }
        testfa2()
        testfb1()
        testfc1()
        testfd1()
    }
    @Test
    fun testQualifiedReturnSyntax() {

    }

    @Test
    fun testLambdaWithReceivers() {
        // purpose of lambda with receiver is to make more readable
        class CInternal1(val l: MutableList<String> = mutableListOf())
        fun CInternal1.add(s: String) = l.add(s)
        fun CInternal1.convertString(): String = l.joinToString()

        fun <T> T.applyThenReturn1(f: (T) -> Unit): T {
            f(this)
            return this
        }
        fun <T> T.applyThenReturn2(f: T.() -> Unit): T {
            f()
            return this
        }

        // need to reference "it" because receiver type (T)
        var l = mutableListOf("abc","def")
        var ci1a = CInternal1(l)
        var ci1b = ci1a.applyThenReturn1 { it.add("ghi") }
        assert(ci1a.convertString() == ci1b.convertString() && ci1a.convertString() == "abc, def, ghi")

        // no need to reference "it" because receiver type T.(), CInternal1 is receiver
        l = mutableListOf("abc","def")
        ci1a = CInternal1(l)
        ci1b = ci1a.applyThenReturn2 { add("ghi") }
        assert(ci1a.convertString() == ci1b.convertString() && ci1a.convertString() == "abc, def, ghi")

        // use built-in lambda apply for same effect
        l = mutableListOf("abc","def")
        ci1a = CInternal1(l)
        ci1b = ci1a.apply { add("ghi") }
        assert(ci1a.convertString() == ci1b.convertString() && ci1a.convertString() == "abc, def, ghi")

        // use util inline lambda with receiver object, same as applyThenReturn2 above
        l = mutableListOf("abc","def")
        ci1a = CInternal1(l)
        ci1b = ci1a.applyThenReturnUtil2 { add("ghi") }
        assert(ci1a.convertString() == ci1b.convertString() && ci1a.convertString() == "abc, def, ghi")
    }
    @Test
    fun testTryCatchEither() {
        //var r1: Try<String>
        // use arrow
    }

    // how to instance of generic class and return it??
    //
    // fun <T> produceT(): () -> T {
    //     // this is not possible syntax
    //     val t = T<T>()
    //     return t
    // }

    fun <T> produceT1(f: () -> T): T {
        // this is how to instance generic class and return...
        val t = f()
        return t
    }

    fun <R,T> produceT2(r: R, f: (R) -> T): T {
        val t = f(r)
        return t
    }

    /*
     * List<out T> == List<? extends T>
     * List<in T> == List<? super T>
     */
    fun <T> addIterable(l: Iterable<out T>): List<String> {
        val ls = l.map { "$it" }
        return ls
    }

    fun <R,T> addIterableReturn(l: Iterable<out T>, f: (T) -> R): List<R> {
        // cannot create an R directly, so pass in a function f: (T) -> R to create R
        return l.map { f(it) }
    }

    private data class PrivateFoo(val s: String)
    private data class PrivateBar(val i: Int)

    // must be private because PrivateBar is private
    private fun privateFoo2Bar(f: PrivateFoo): PrivateBar = PrivateBar(f.s.toInt())
    private fun privateBar2Foo(b: PrivateBar): PrivateFoo = PrivateFoo(b.i.toString())

    fun foo2bar(f: Foo): Bar = Bar(f.s.toInt())
    fun bar2foo(b: Bar): Foo = Foo(b.i.toString())
    fun i2s(i: Int): String = i.toString()
    fun s2i(s: String): Int = s.toInt()

    @Test
    fun testAddIterableReturn() {
        var li = listOf(1,2,3)
        var lfoo = li.map { Foo(i2s(it)) }
        var lbar = lfoo.map { produceT2(it, ::foo2bar) }
        var lfoo2 = addIterableReturn(lbar, ::bar2foo)
        var lbar2 = addIterableReturn(lfoo, ::foo2bar)
        return
    }

    /*
     * List<out T> == List<? extends T>     T is covariant      can produce T but not consume T
     * List<in T> == List<? super T>        T is contravariant  can consume T but not produce T
     *
     * consumer in, producer out
     */
    @Test
    fun testInOutGeneric() {
        val ld: MutableList<Double> = mutableListOf(1.0, 2.0)
        val ln: MutableList<Number> = mutableListOf(1.0, 2.0)
        val li: MutableList<Int> = mutableListOf()
        val la: MutableList<Any> = mutableListOf()

        class C1<in T> {
            // val l: MutableList<T> = mutableListOf<T>()  // not allowed, syntax error
        }
        class C2<out T> {
            // val l: MutableList<T> = mutableListOf<T>()  // not allowed, syntax error
        }
        class C3<T> {
            val l: MutableList<T> = mutableListOf()      // allowed, obviously
        }


    }

    open class LA() {

        val l = mutableListOf<Any>()

        fun add(v: Any) = l.add(v)

        fun getClassNames1(): List<String> = l.map { it::class.java.simpleName }

        fun <T> getTypeOf1(t: Class<T>): List<T> {
            val r = mutableListOf<T>()
            val rAny = l.filter { it::class.java == t }.forEach { r.add(it as T) } // this works, rAny is Unit
            return r
        }

        fun <T> getTypeOf2(t: Class<T>): List<T> =
            l.filter { it::class.java == t }.map { it as T } // this works

        inline fun <reified T> getReifiedTypeOf1(): List<T> =
            l.filter { it is T }.map { it as T } // this works and looks nicer

        fun <T> getTypeOf2a(t: Class<T>): List<T> =
            l.filter { v -> v::class.java == t }.map { it as T } // this works, remap it -> v, useful for inner it

        inline fun <reified T> getReifiedTypeOf2(): List<T> =
            getTypeOf2(T::class.java)

        // reified MUST be inline. this is syntax error
        // fun <reified T> getReifiedTypeOf3(): List<T> = getTypeOf2(T::class.java)

        fun <T> getTypeOf3(t: Class<T>): List<T> {
            val r = mutableListOf<T>()
            l.forEach { if(it::class == t) { r.add(it as T) } } // doesnt work
            return r
        }

        fun <T> getTypeOf4(t: Class<T>): List<T> {
            val r = mutableListOf<T>()
            l.forEach { if(it::class.java == t) { r.add(it as T) } } // this works
            return r
        }
    }

    @Test
    fun testFilterGeneric() {
        // reified use case: to not pass in class type during runtime
        // generics are compile time, and the type is lost in runtime (type erasure).
        // so to identify type being passed in, have to explicitly pass in class type.
        // method 2 is define as reified inline function, so type is identified in runtime too.

        val l = listOf(Foo("1"), Foo("2"), Bar(1), Bar(2), FooBar("1", 1), FooBar("2", 2))
        val la = LA()
        l.forEach { la.add(it) }
        val lrefFoo = listOf(l.get(0),l.get(1))
        val lrefBar = listOf(l.get(2),l.get(3))

        var lcn1 = la.getClassNames1()
        // assertEquals(listOf("mytest.Foo","mytest.Foo","mytest.Bar","mytest.Bar","mytest.FooBar","mytest.FooBar"), lcn1)
        assertEquals(listOf("Foo","Foo","Bar","Bar","FooBar","FooBar"), lcn1)
        var lf1 = la.getTypeOf1(Foo::class.java)
        var lf2 = la.getTypeOf2(Foo::class.java)
        var lb1 = la.getTypeOf1(Bar::class.java)
        var lrf1 = la.getReifiedTypeOf1<Foo>()
        var lrf2 = la.getReifiedTypeOf2<Foo>()
        var l2 = l.filterIsInstance<Foo>()
        assertEquals(lrefFoo, lf1)
        assertEquals(lrefFoo, lf2)
        assertEquals(lrefBar, lb1)
        assertEquals(lrefFoo, lrf1)
        assertEquals(lrefFoo, lrf2)
        assertEquals(lrefFoo, l2)
    }

    open class LAReified: LA() {

        // inline functions inline lambda expressions
        // so what about inline non-reified functions?
        // usually of form:
        // inline fun <T> foo(f: ()->T): T { ... }
        // - inline affects function and lambdas passed to it, all to inline
        // - use noinline f: () -> R to make a lambda non-inline (in inline function)

    }

    @Test
    fun testReified() {
        val la = LA()
        la.add(10)
        // var s = la.reifiedTransform2<Int,String> {
        //     it -> it
        // }
    }

    fun <R, T> compareClassType1(r: Class<R>, t: T, compareWhen: Int = 0): Boolean =
        when(compareWhen) {
            0 -> t!!::class.java == r
            1 -> t!!::class.java === r
            // 2 -> t!!::class is r // this is reified syntax only
            // 2 -> t is r // this is reified syntax only
            else -> false
        }
    // fun <R, T> compareGenericType(r: R, t: T): Boolean =


    @Test
    fun testClassCompare() {
        var fb1 = FooBar("1", 1)
        var fb2 = FooBar("2", 2)
        var fb3 = FooBar("1", 1)
        var bf1 = BarFoo(1, "1")
        var bf2 = BarFoo(2, "2")

        assert(bf1::class == bf2::class)
        assert(bf1::class != fb1::class)
        assert(fb1 != fb2)
        assert(fb1 == fb3)
    }

    @Test
    fun testReturnMultiVars() {
        data class DCL1(var v1: String, var v2: String, var v3: String)
        var dcl = DCL1("hi","bye","foo")
        var (hi,bye,foo) = dcl
        assert(hi == "hi" && bye == "bye" && foo == "foo")

        fun returnDC(): DCL1 { return DCL1("hi","bye","foo") }
        var (hi1,bye1,foo1) = returnDC()
        assert(hi1 == "hi" && bye1 == "bye" && foo1 == "foo")

        fun returnListOf3a() = arrayOf("hi","bye","foo")
        var (hi2,bye2,foo2) = returnListOf3a()
        assert(hi2 == "hi" && bye2 == "bye" && foo2 == "foo")

        fun returnListOf3b() = listOf("hi","bye","foo")
        var (hi3,bye3,foo3) = returnListOf3b()
        assert(hi3 == "hi" && bye3 == "bye" && foo3 == "foo")

    }
}

data class Foo(val s: String)
data class Bar(val i: Int)
data class FooFoo(var s: String? = null, var foo: Foo? = null)
data class FooBar(var s: String, var i: Int)
data class BarFoo(var i: Int, var s: String)

data class FooWithCompanion(val s: String) {
    companion object {
        @JvmStatic fun staticMethod(s1: String): String { return s1 }
        fun nonStaticMethod(s1: String): String { return s1 }
    }
}
fun Foo.toBarNonStatic() = Bar(s.toInt())
fun FooWithCompanion.Companion.toBarStatic(s1: String) = Bar(s1.toInt())    // as this is not allowed

class AsyncTests {
    @org.junit.jupiter.api.Test
    fun testSyncDelayedCtr() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val syncDelayedCtr = SyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        for(i in 0..numCases) {
            m[i] = syncDelayedCtr.addAndGetVal()
        }
        // expected to be non-overlapped =~ numCases * randomDelayed
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${syncDelayedCtr.vTotal.get()} vMax:${syncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) { }
        assert(flag)
        assert(td > syncDelayedCtr.vTotal.get())
    }
    @org.junit.jupiter.api.Test
    fun testSyncDelayedCtrWithUselessBlocking() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val syncDelayedCtr = SyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        // runBlocking means async items are encapsulated inside here,
        // and outside of here, thinsg are synchronous
        runBlocking {
            for(i in 0..numCases) {
                launch {
                    m[i] = syncDelayedCtr.addAndGetVal()
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")

            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag)
        }

        // expected to be non-overlapped =~ numCases * randomDelayed
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${syncDelayedCtr.vTotal.get()} vMax:${syncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) { }
        assert(flag)
        assert(td > syncDelayedCtr.vTotal.get())
    }
    @org.junit.jupiter.api.Test
    fun testAsynchronousDelayedCtrThreadSleep() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                launch {
                    m[i] = asyncDelayedCtr.addAndGetVal()
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped > numCases * randomDelayed because it's Thread.sleep
        // which suspends the thread, instead of allowing the thread to be reused
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
        }
        assert(flag == true)
        assert(td > asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsynchronousDelayedCtrDelaySuspend() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                // launch usually starts when suspend function ends, which can be
                // when runBlocking ends
                launch {
                    m[i] = asyncDelayedCtr.addAndGetValDelayGetAfterDelay()
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
            flag = true
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }
    @org.junit.jupiter.api.Test
    fun testAsynchronousDelayedCtrDelaySuspendBeforeDelay() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                launch {
                    m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsynchronousDelayedCtrDelaySuspendBeforeDelayShortDelay() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 0)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                launch {
                    m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td > asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendExplicitLaunch() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                val j = launch(){
                    m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                }
                // can do j.cancel(), j.join()
                l.add(j)
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutLessThanRandom() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                try {
                    // if add timeout > random delay, then it seems to serialize.
                    withTimeout(10) {
                        val j = launch(){
                            m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                        }
                        // can do j.cancel(), j.join()
                        l.add(j)
                    }
                } catch(e: Exception) {
                    println("exception ${e.message}")
                }
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                println("${e.message}")
                flag = true
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
        } catch(e: Exception) {
            // withTimeoutOrNull is set to 50. if any delay > 50, then return null
            println("exception: ${e.message}")
            flag = true
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutGreaterRandom() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                try {
                    // if add timeout > random delay, then it seems to serialize.
                    withTimeout(200) {
                        val j = launch(){
                            m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                        }
                        // can do j.cancel(), j.join()
                        l.add(j)
                    }
                } catch(e: Exception) {
                    println("exception ${e.message}")
                }
            }

            // this confirms serialized launch and not async launch
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
                flag = true
            } catch(e: Exception) {
                println("${e.message}")
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // withTimeoutOrNull is set to 50. if any delay > 50, then return null
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td > asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutInsideLaunch() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                try {
                    // if add timeout > random delay, then it seems to serialize.
                    // but if inside the launch, then seems the launch is async
                    val j = launch(){
                        // it's 150 because the actual method may have max random of 100,
                        // but may take more time to execute
                        withTimeout(150) {
                            m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                        }
                    }
                    // can do j.cancel(), j.join()
                    l.add(j)
                } catch(e: Exception) {
                    println("exception ${e.message}")
                }
            }

            // expected to be < numCases * randomDelayed
            // this should confirm withTimeout runs async because launch is async
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                println("${e.message}")
                flag = true
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutOrNullSerialGTDelay() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                // this causes launch not to be asynchronous
                withTimeoutOrNull(150) {
                    val j = launch(){
                        m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                    }
                    // can do j.cancel(), j.join()
                    l.add(j)
                }
            }

            // expected to be < numCases * randomDelayed
            // exception because timeout is smaller than random delay
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
                flag = true
            } catch(e: Exception) {
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // withTimeoutOrNull is set to 50. if any delay > 50, then return null
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td > asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutOrNullAsync() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                // withTimeout inside launch means launch is async
                val j = launch(){
                    withTimeoutOrNull(150) {
                        m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                    }
                }
                // can do j.cancel(), j.join()
                l.add(j)
            }

            // expected to be < numCases * randomDelayed
            // exception because async isn't done yet
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // withTimeoutOrNull is set to 50. if any delay > 50, then return null
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }

    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendWithTimeoutOrNullSerial() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                // this causes launch not to be asynchronous
                withTimeoutOrNull(10) {
                    val j = launch(){
                        m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                    }
                    // can do j.cancel(), j.join()
                    l.add(j)
                }
            }

            // expected to be < numCases * randomDelayed
            // exception because timeout is smaller than random delay
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
        } catch(e: Exception) {
            // withTimeoutOrNull is set to 50. if any delay > 50, then return null
            println("exception: ${e.message}")
            flag = true
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }
    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelaySuspendTimeout() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        var tb = Instant.now().toEpochMilli()
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false
        runBlocking {
            for(i in 0..numCases) {
                val j = launch(){
                    m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                }
                // can do j.cancel(), j.join()
                l.add(j)
            }

            // expected to be < numCases * randomDelayed
            var td = Instant.now().toEpochMilli() - tb
            println("$methodName inside blocking td: $td")
            try {
                flag = false
                for(i in 0..numCases) {
                    val r = m[i]
                    if(r != (i+1)) {
                        throw Exception("$r != ${i+1}")
                    }
                }
            } catch(e: Exception) {
                flag = true
            }
            assert(flag == true)
        }

        // expected to be overlapped =~ max(randomDelayed)
        var td = Instant.now().toEpochMilli() - tb
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }
    @org.junit.jupiter.api.Test
    fun testAsyncDelayedCtrDelayMeasureTimeMillis() {
        val methodName = object{}.javaClass.enclosingMethod.name
        val asyncDelayedCtr = AsyncDelayedCtr(0, 100)
        val numCases = 100
        val m = ConcurrentHashMap<Int, Int>()
        val l = ArrayBlockingQueue<Job>(numCases+1)
        var flag = false

        val td = measureTimeMillis {
            runBlocking {
                for(i in 0..numCases) {
                    val j = launch(){
                        m[i] = asyncDelayedCtr.addAndGetValDelayGetBeforeDelay()
                    }
                    // can do j.cancel(), j.join()
                    l.add(j)
                }

                // expected to be < numCases * randomDelayed
                try {
                    flag = false
                    for(i in 0..numCases) {
                        val r = m[i]
                        if(r != (i+1)) {
                            throw Exception("$r != ${i+1}")
                        }
                    }
                } catch(e: Exception) {
                    flag = true
                }
                assert(flag == true)
            }

        }

        // expected to be overlapped =~ max(randomDelayed)
        println("$methodName outside blocking td: $td vTotal:${asyncDelayedCtr.vTotal.get()} vMax:${asyncDelayedCtr.vmax}")
        try {
            flag = false
            for(i in 0..numCases) {
                val r = m[i]
                if(r != (i+1)) {
                    throw Exception("$r != ${i+1}")
                }
            }
            flag = true
        } catch(e: Exception) {
            // the addAndGetValDelayGetAfterDelay returns int val after random delay
            // so something else could have modified the value by the time read
            println("exception: ${e.message}")
        }
        assert(flag)
        assert(td < asyncDelayedCtr.vTotal.get())
    }



    class SyncDelayedCtr(val minSleep: Long = 0, val maxSleep: Long = 100) {
        val ctr: AtomicInteger
        val numPending: AtomicInteger
        val longRange: LongRange
        var vmax: Long = 0
        val vTotal: AtomicLong
        init {
            ctr = AtomicInteger(0)
            numPending = AtomicInteger(0)
            longRange = minSleep..maxSleep
            vTotal = AtomicLong(0)
        }

        fun addAndGetVal(v: Int = 1): Int {
            numPending.incrementAndGet()
            val vs = kotlin.random.Random.nextLong(longRange)
            vmax = java.lang.Long.max(vmax, vs)
            vTotal.addAndGet(vs)
            Thread.sleep(vs)
            numPending.decrementAndGet()
            return ctr.addAndGet(v)
        }
        fun getNumPending(): Int = numPending.get()
        fun reset() = ctr.set(0)
    }

    class AsyncDelayedCtr(minSleep: Long = 0, maxSleep: Long = 100) {
        val ctr: AtomicInteger
        val numPending: AtomicInteger
        val longRange: LongRange
        var vmax: Long = 0
        val vTotal: AtomicLong
        init {
            ctr = AtomicInteger(0)
            numPending = AtomicInteger(0)
            longRange = minSleep..maxSleep
            vTotal = AtomicLong(0)
        }
        suspend fun addAndGetVal(v: Int = 1): Int {
            numPending.incrementAndGet()
            val vs = kotlin.random.Random.nextLong(longRange)
            vmax = java.lang.Long.max(vmax, vs)
            vTotal.addAndGet(vs)
            // this doesn't work with suspend
            Thread.sleep(vs)
            numPending.decrementAndGet()
            return ctr.addAndGet(v)
        }
        suspend fun addAndGetValDelayGetAfterDelay(v: Int = 1): Int {
            numPending.incrementAndGet()
            val vs = kotlin.random.Random.nextLong(longRange)
            vmax = java.lang.Long.max(vmax, vs)
            vTotal.addAndGet(vs)
            // this goes with suspend
            delay(vs)
            numPending.decrementAndGet()
            val r = ctr.addAndGet(v)
            return r
        }
        suspend fun addAndGetValDelayGetBeforeDelay(v: Int = 1): Int {
            numPending.incrementAndGet()
            val vs = kotlin.random.Random.nextLong(longRange)
            vmax = java.lang.Long.max(vmax, vs)
            vTotal.addAndGet(vs)

            // this shouldnt suspend until delay is reached,
            // so the get method should be guaranteed...
            val r = ctr.addAndGet(v)

            // this goes with suspend
            delay(vs)

            numPending.decrementAndGet()
            return r
        }

        suspend fun addAndGetValDelayFixed(v: Int = 1, delay: Long = 0): Int {
            numPending.incrementAndGet()
            vmax = java.lang.Long.max(vmax, delay)
            vTotal.addAndGet(delay)

            // this shouldnt suspend until delay is reached,
            // so the get method should be guaranteed...
            val r = ctr.addAndGet(v)

            // this goes with suspend
            delay(delay)

            numPending.decrementAndGet()
            return r
        }
        suspend fun addAndGetValYieldBefore(v: Int = 1): Int {
            numPending.incrementAndGet()
            // can use yield instead of suspend
            // different from yield(v), which is used only in sequences
            yield()
            val r = ctr.addAndGet(v)
            numPending.decrementAndGet()
            return r
        }
        suspend fun addAndGetValYieldAfter(v: Int = 1): Int {
            numPending.incrementAndGet()
            val r = ctr.addAndGet(v)
            yield()
            numPending.decrementAndGet()
            return r
        }
        fun getNumPending(): Int = numPending.get()
        fun reset() = ctr.set(0)
    }
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testLaunchAsync1() {
        // GlobalScope.async is bad practice, use coroutineScope instead
        // you must explicitly opt-in into using GlobalScope with @OptIn(DelicateCoroutinesApi::class).
        fun fooInt1() = GlobalScope.async {
            delay(300)
            10
        }
        fun fooInt2() = GlobalScope.async {
            delay(400)
            20
        }
        fun fooInt3() = GlobalScope.async {
            delay(500)
            30
        }
        // this seems like the way to declare async, to avoid GlobalScope
        suspend fun fooInt4() = coroutineScope {
            async {
                delay(300)
                40
            }
        }
        suspend fun fooInt5() = coroutineScope {
            async {
                delay(400)
                50
            }
        }
        suspend fun fooInt6() = coroutineScope {
            async {
                delay(500)
                60
            }
        }
        // val (d1,d2,d3) = (fooInt1(), fooInt2(), fooInt3()) // bad syntax
        runBlocking {
            val d1 = fooInt1()  // doesnt have to be in runBlocking
            val d2 = fooInt2()
            val d3 = fooInt3()
            val ms = measureTimeMillis {
                val (v1,v2,v3) = awaitAll(d1, d2, d3) // has to be in runBlocking
                assert(v1 == 10 && v2 == 20 && v3 == 30)
            }
            print("timeElapsed 1: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                val (v1,v2,v3) = awaitAll(fooInt1(), fooInt2(), fooInt3())
                assert(v1 == 10 && v2 == 20 && v3 == 30)
            }
            print("timeElapsed 2: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                // this seems to be serialized
                val (v1,v2,v3) = awaitAll(fooInt4(), fooInt5(), fooInt6())
                assert(v1 == 40 && v2 == 50 && v3 == 60)
            }
            print("timeElapsed 3: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                coroutineScope {
                    // this is also serialized
                    async {
                        val (v1,v2,v3) = awaitAll(fooInt4(), fooInt5(), fooInt6())
                        assert(v1 == 40 && v2 == 50 && v3 == 60)
                    }
                }
            }
            print("timeElapsed 4: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                coroutineScope {
                    // this is also serialized. weird syntax..
                    val j1 = async { fooInt4() }
                    val j2 = async { fooInt5() }
                    val j3 = async { fooInt6() }
                    val v1 = j1.await().await() // because FooInt4() is already await { .. }
                    val v2 = j2.await().await()
                    val v3 = j3.await().await()
                    assert(v1 == 40 && v2 == 50 && v3 == 60)
                }
            }
            print("timeElapsed 5: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                coroutineScope {
                    // this is the right syntax for parallel runs, not case 4,5
                    val (v1,v2,v3) = awaitAll(fooInt4(), fooInt5(), fooInt6())
                    assert(v1 == 40 && v2 == 50 && v3 == 60)
                }
            }
            print("timeElapsed 6: $ms\n")
        }
    }
    @Test
    fun testTimeoutAsync() {
        suspend fun fooInt1(timeout: Long = -1): Deferred<Int> = coroutineScope {
            when(timeout) {
                -1L -> {
                    async {
                        delay(500)
                        50
                    }
                }
                else -> {
                    withTimeout(timeout) {
                        async {
                            delay(500)
                            50
                        }
                    }
                }
            }
        }
        suspend fun fooInt2(timeout: Long = -1): Deferred<Int>? = coroutineScope {
            when(timeout) {
                -1L -> {
                    async {
                        delay(500)
                        50
                    }
                }
                else -> {
                    withTimeoutOrNull(timeout) {
                        async {
                            delay(500)
                            50
                        }
                    }
                }
            }
        }
        runBlocking {
            val ms = measureTimeMillis {
                var b = false
                coroutineScope {
                    try {
                        val v1 = fooInt1(100).await()
                        assert(false)
                    } catch(e: TimeoutCancellationException) {
                        b = true
                        print("timeout expected\n")
                    }
                    assert(b)
                }
            }
            print("timeElapsed 1: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                var b = true
                coroutineScope {
                    try {
                        val v1 = fooInt1(1000).await()
                        assert(v1 == 50)
                    } catch(e: TimeoutCancellationException) {
                        b = false
                        print("timeout unexpected\n")
                    }
                    assert(b)
                }
            }
            print("timeElapsed 2: $ms\n")
        }
        runBlocking {
            val ms = measureTimeMillis {
                var b = true
                coroutineScope {
                    try {
                        val v1 = fooInt2(100)?.await()
                        assert(v1 == null)
                    } catch(e: TimeoutCancellationException) {
                        b = false
                        print("timeout unexpected\n")
                    }
                    assert(b)
                }
            }
            print("timeElapsed 3: $ms\n")
        }
    }
}


class IntrospectionTests {
    /*
     * - create a class with a couple of methods
     * - annotate methods
     * - process the class based on annotations
     * - create another class with a couple of methods and annotate methods
     * - create a filtered method class, which processes only certain annotation
     * - scan all the classes (registered) and apply the filtered method class to applicable ones
     *
     */
    class ICFooA {

    }
    class ICFooB {

    }

    annotation class Ifc1 { }

    val lc = mutableListOf<Any>()

}
