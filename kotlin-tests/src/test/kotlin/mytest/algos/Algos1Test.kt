package mytest.algos

import mytest.U
import org.junit.jupiter.api.Test

class Algos1Test {
    enum class DType { A, B, C }
    data class PIntRange(val idMin: Int, val idMax: Int)
    data class P<A,B>(val one: A, val two: B)
    data class Triple<A,B,C>(
        val one: A? = null,
        val two: B? = null,
        val three: C? = null
    )
    data class MutableTriple<A,B,C>(
        var one: A? = null,
        var two: B? = null,
        var three: C? = null
    )
    data class Quad<A,B,C,D>(
        val one: A? = null,
        val two: B? = null,
        val three: C? = null,
        val four: D? = null
    )
    data class ObjI(
        val dType: DType,
        val id: Int,
        val v: Int)
    data class ObjO(
        val dType: DType,
        val id: Int,
        val pIntRangeA: PIntRange,
        val pIntRangeB: PIntRange,
        val m1: ObjI,
        val m2: ObjI
    )
    fun printAny(v: Any?)  = print(v)
    fun printAnyList(l: List<*>) = l.forEach { printAny(it) }
    @Test
    fun testActiveWindowProducerConsumer() {
        // given list of input li1 and li2, match whenever the vals are equal and place into lo1

        var numCases = 1000
        var li1 = mutableListOf<ObjI>()
        var li2 = mutableListOf<ObjI>()
        var lo1 = mutableListOf<ObjO>()

        // setup
        //for()
    }
    @Test
    fun testSortedValuesConsumeWindowMultirun() {
        repeat(10) {
            print("\n")
            testSortedValuesConsumeWindow()
        }
    }
    @Test
    fun testSortedValuesConsumeWindow() {
        // given a list of T = (id,v), sort by v. then consume each T and
        // track the min id and the max id each consumption
        val sz = 10
        val l1 = mutableListOf<MutableTriple<String, Int, Int>>()
        var ic = 97 // 97 == 'a'
        for(i in 0..sz-1) {
            l1.add(MutableTriple("${(ic+i).toChar()}", null, null))
        }
        U.shuffle(l1)
        for(i in 0..sz-1) {
            l1[i].two = i
        }
        U.shuffle(l1)
        for(i in 0..sz-1) {
            l1[i].three = i
        }
        U.shuffle(l1)


        // suppose two == timestamp. keep track of sorted one (char val) from smallest to largest,
        // and keep the min,max timestamp window for each item removed. use [x,y] index inclusive
        val p = P(0,sz-1)
        var b = false
        // method 1
        if(true) {
            val set = HashSet<Int>()
            val ls = l1.sortedWith(compareBy({it.one}))
            var imin = 0
            var imax = sz-1
            val lr = mutableListOf<Triple<MutableTriple<String, Int, Int>, Int, Int>>()
            for(i in 0..sz-1) {
                assert("${(ic+i).toChar()}" == ls[i].one)
                val idx = ls[i].two!!
                set.add(idx)

                when(idx) {
                    imin -> while(imin in set && imin < imax) imin++
                    imax -> while(imax in set && imax > imin) imax--
                    else -> {}  // do nothing, or use Unit || {}
                }
                lr.add(Triple(ls[i],imin,imax))
                print(
                    "${ls[i].one!!.padEnd(3)} " +
                    "${ls[i].two.toString().padEnd(3)} " +
                    "${ls[i].three.toString().padEnd(3)} " +
                    "min:${imin.toString().padEnd(3)} " +
                    "max:${imax.toString().padEnd(3)} " +
                    "\n"
                )
            }
            b = true
        }
    }
    @Test
    fun testSortComparator() {
        val l1 = listOf(
            Triple("e",5, 30),
            Triple("b",3, 10),
            Triple("d",1, 50),
            Triple("a",2, 40),
            Triple("c",4, 20),
        )
        val l2 = listOf<Triple<String?, Int?, Int?>>(
            Triple("e",5, 30),
            Triple("b",3, 10),
            Triple("d",1, 50),
            Triple("a",2, 40),
            Triple("c",4, 20),
            Triple("e",null,60)
        )
        val cmp1 = Comparator<Triple<String, Int, Int>> { o1, o2 ->
            when {
                o1.two == null || o2.two == null -> throw NullPointerException()
                else -> o1.two.compareTo(o2.two)
            }
        }
        // comparator is fickle, cannot mix T? with T when doing list.sortedWith(cmp)

        val cmp2 = Comparator<Triple<String, Int, Int>> { o1, o2 -> o1.two!!.compareTo(o2.two!!)}
        val cmp3 = Comparator<Triple<String?, Int?, Int?>> { o1, o2 -> o1.two!!.compareTo(o2.two!!)}
        val r1 = l1.sortedWith(cmp1)
        val le = listOf(l1[2],l1[3],l1[1],l1[4],l1[0])
        assert(r1 == le)
        val r2 = l1.sortedWith(cmp2)
        assert(r2 == le)

        var b = false
        try {
            val r = l2.sortedWith(cmp3)
        } catch(npe: NullPointerException) {
            b = true
        }
        assert(b == true)
        return
    }
    @Test
    fun testTriple() {
        val v = Triple("hi",100,1.23)
        assert(v.one == "hi" && v.two == 100 && v.three == 1.23)
    }
}
