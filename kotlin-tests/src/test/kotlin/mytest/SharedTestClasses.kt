package mytest

import java.util.Random

class SharedTestClasses {
}

object U {
    val r = Random()
    fun <T> swap(l: MutableList<T>, i: Int, j: Int) {
        val v = l[i]
        l[i] = l[j]
        l[j] = v
    }
    fun <T: Any> shuffle(l: MutableList<T>) {
        val sz = l.size
        for(i in 0..sz-1) {     // 0..sz == [0..sz]
            val j = r.nextInt(sz) // [0..sz)
            swap(l, i, j)
        }
    }
}

object SingletonInstance {
    var vi1: Int? = null
    val id: Int  = 1
    val listMutable: MutableList<Int> = mutableListOf()
    val listImmutable1: List<Int> = listOf(1,2,3)
    var listImmutable2: List<Int> = listOf(1,2,3)
}

/*
 * data class has automatic equals(), hashCode(), toString(), copy()
 * componentN() functions corresponding to order of declaration
 *
 * - primary constructor has to have at least 1 param
 * - cannot be abstract, open, sealed, inner
 *
 * - only properties defined in constructor are used for equals, hashCode, copy
 */
data class DataClass1(var vi1: Int?, val vi2: Int?) {
    var vi3: Int? = 3
    val vi4: Int? = vi1
}

data class DataClass2(val vi1: Int, val vi2: Int) {
    object DataClassComparator: Comparator<DataClass2> {
        override fun compare(o1: DataClass2, o2: DataClass2): Int =
            o1.vi1.compareTo(o2.vi1)
    }
}

enum class EnumVal {
    Small,
    Medium,
    Large,
    Integer,
    String
}

interface V
class VNum(val v: Int): V
class VStr(val v: String): V

open class ObjectTest(val v: String) {
    open fun getVV(): String {
        val s = "mytest.ObjectTest:$v"
        return s
    }
    /*
    open var y: String? = null
        get() = "mytest.ObjectTest:y:$y"
        }
        fun set(x: String) {
            y = x
        }

     */
}
open class FunctionsClass {
    fun getEnumType(v: Any): EnumVal =
        when(v) {
            is VNum -> EnumVal.Integer
            is VStr -> EnumVal.String
            is Int -> EnumVal.Integer
            is String -> EnumVal.String
            else -> throw IllegalArgumentException("unknown type $v")
        }

    open fun setObjectClass(v: String): ObjectTest = ObjectTest(v)
    open fun getObjectClassValue(v: String): String = ObjectTest(v).getVV()
    open fun add1(x: Int, y: Int): Int = x+y

    fun add2(x: Int, y: Int): Int {
        return x+y
    }
}

open class FunctionsSubclass1: FunctionsClass() {
    override fun add1(x: Int, y: Int): Int = x + x
    override open fun setObjectClass(v: String): ObjectTest {
        // override with object keyword
        return object: ObjectTest(v) {
            override fun getVV(): String {
                val s = "ObjectOverrideTest:$v"
                return s
            }
        }
    }
}

open class SingleFunctionClass {
    open fun getObj(v:String): ObjectTest = ObjectTest("basic:$v")
}

interface SimpleInterface {
    val iv1: Int
    val iv2: Int
    fun foo1()
    fun foo2(): Int = 42
}

class SimpleClassI(val v1: Int, val v2: Int, val v3: Int) : SimpleInterface {
    override val iv1: Int = v1
    override val iv2: Int = v2
    override fun foo1() {
    }
    override fun foo2(): Int {
        var tv2 = super<SimpleInterface>.foo2()
        tv2++
        return tv2
    }
}

class SimpleClassObject(var v1: Int?) {
    fun doubleStringVal(): String = "${v1!!.times(2)}"
    fun add(v1: Int, v2: Int): Int = v1+v2
}

class Object1(id: Int?, sb: StringBuilder) {
    var vi1: Int? = null
        get() = field
        set(v) {
            field = v
        }
    var vi2: Int? = null
    // constructor id var isnt visible in functions
    // so assign it to class var!
    var vid: Int? = id
    val sb: StringBuilder = sb
    fun getString(): String {
        // constructor id var isnt visible here, so cannot use $id
        var vs = "id:$vid,"
        sb.append(vs)
        return vs
    }
    // init block
    init {
        getString()
    }
}

class Object2(id: Int) {
    val id = id
    fun isIdOdd() = id%2 == 1
}
class Person() {
    var nameFirst: String? = "first"
    var nameLast: String? = "last"
    var id: Int? = 1
    fun show() = "($id, $nameFirst, $nameLast)"
}

class SimpleSTLClass(val v1: Int, val v2: Int, val v3: Int)

// these classes are for polymorphism

enum class E_LIFE_TYPE {
    PLANT,
    ANIMAL,
    OTHER
}

enum class E_ANIMAL_TYPE {
    FISH,
    APE,
    BIRD
}

open class TransformString {
    companion object {
        open fun addHi1(s: String, sb: StringBuilder? = null): String = sb?.append("hi $s").toString() ?: "hi $s"
        open fun addHi(s: String, sb: StringBuilder) = sb.append("hi $s")
        open fun addHi(s: String ): String = "hi $s"
        open fun addBye(s: String, sb: StringBuilder) = sb.append("bye $s")
        open fun addBye(s: String): String = "bye $s"
        open fun addMyName(s: String): String = "my name is $s"
    }
}
fun TransformString.Companion.aggregateParallel(s: String): String = "${addHi(s)}, ${addBye(s)}"
fun TransformString.Companion.aggregateSerial(s: String): String = "${addHi(s)},".also { "${addBye(it)}"}

open class Lifeform(open val type: E_LIFE_TYPE, open val id: Int)

open class Animal(
    val animalType: E_ANIMAL_TYPE,
    override val id: Int,
    open val weight: Int):
    Lifeform(E_LIFE_TYPE.ANIMAL, id)

open class AnimalFish(
    override val id: Int,
    override val weight: Int,
    val name: String):
    Animal(E_ANIMAL_TYPE.FISH, id, weight)

open class AnimalApe(
    override val id: Int,
    override val weight: Int,
    val name: String):
    Animal(E_ANIMAL_TYPE.APE, id, weight)

open class AnimalBird(
    override val id: Int,
    override val weight: Int,
    val name: String):
    Animal(E_ANIMAL_TYPE.BIRD, id, weight)
