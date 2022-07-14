package mytest

import java.util.concurrent.atomic.AtomicInteger

class SimpleClass1 {
    fun sum(x: Int, y: Int): Int {
        println("sum: x:$x y:$y = ${x+y}")
        return x+y
    }
}

class SimpleClass2(val i1: Int, val s1: String) {
    val pi1: Int = i1
    protected val ps1: String = s1
    private var pi2: Int = 10
    fun getFoo(i:Int): Int = pi2 + i
    fun setFoo(i:Int) = { pi2 = i }
    var pi3: Int? = null
    var vi4DefaultPublic: Int? = null
    var vs5: String ? = s1
        get() {
            return "getter: $field"
        }
        set(v) {
            field = "setter: $v"
        }

}

class SimpleClass3(var i1: Int, val s1: String)

typealias SC1 = SimpleClass1
typealias SC2 = SimpleClass2
typealias SC3 = SimpleClass3

class SimpleClass4(
    var vi1: Int?,
    var vi2: Int?,
    var vs1: String?,
    var vs2: String?
)

typealias SC4 = SimpleClass4

open class SimpleClassBase5(
    open var vi1: Int?,         // open means overrideable by subclass
    val vi2: Int?
) {
    var _vi1: Int? = vi1
    var _vi2: Int? = vi2
}

class SimpleClass5(
    override var vi1: Int?,     // same name as base, so needs override
    var vi2a: Int?,             // not same name, so no override
    var vs1: String?,
    var vs2: String?) :
    SimpleClassBase5(vi1, vi2a)
{
    var _vs1: String? = vs1
    var _vs2: String? = vs2
}

// this class has no default constructor, how to subclass?
open class SimpleClassBase6 {
    var vi1: Int? = null
    var vi2: Int? = null
    var vi3: Int? = null
    //lateinit var vi4: Int // primitive type not allowed
    constructor(vi1: Int?) {
        this.vi1 = vi1
    }
    constructor(vi1: Int?, vi2: Int?) : this(vi1) {
        this.vi2 = vi2
    }
    init {
        vi3 = (vi1 ?: 0) + (vi2 ?: 0)
    }
    fun sum(v:Int = 0): Int = v + (vi1 ?: 0) + (vi2 ?: 0) + (vi3 ?: 0)
}

class SimpleClass6A: SimpleClassBase6 {
    var vi4: Int? = null
    constructor(v1: Int?, v2: Int?, v4: Int?) : super(v1, v2) {
        this.vi4 = v4
    }
}

class SimpleClass6B(v1: Int?, v2: Int?, v4: Int?): SimpleClassBase6(v1, v2) {
    var vi4: Int? = v4
    fun getString(): String = "SC6: v1:$vi1 v2:$vi2 v3:$vi3 v4:$vi4"
}

class SimpleClass7(var v1: Int, var v2: Int = 10, val v3: Int = 20, val v4: Int? = 30)

class SimpleClassWhen(v1: Int) {
    //lateinit var sc6bLI: mytest.SimpleClass6B
    var sc6bLI: SimpleClass6B
    var sc6b: SimpleClass6B? = null
    fun getString(): String = "SCW: ${sc6b.toString()}"
    init {
        when {
            v1 == 1 -> {
                sc6bLI = SimpleClass6B(1,1,1)
                sc6b = SimpleClass6B(1,1,1)
            }
            v1 == 2 -> {
                sc6bLI = SimpleClass6B(2,2,2)
                sc6b = SimpleClass6B(2,2,2)
            }
            else -> {
                sc6bLI = SimpleClass6B(3,3,3)
            }
        }
    }
}

class SimpleClassInnerOuter {
    var vi1: Int? = 1
    class StaticClass {
        var vi1: Int? = 2
    }
    inner class InnerClass {
        var vi1: Int? = 3
        var vi2: Int? = this@SimpleClassInnerOuter.vi1
    }
    fun add(x: Int, y: Int): Int = x+y

    // companion object is 1 per class and is for static stuff
    companion object {
        var vi1: Int? = 5
        fun add(x: Int, y: Int): Int = x+y+1
    }
}

class SimpleClassCompanion1 {
    // only 1 companion allowed per class
    fun add(x: Int, y: Int): Int = x+y
    companion object Companion1 {
        var vi1: Int? = 1
        fun add(x: Int, y: Int): Int = x+y+1
    }
}

class SimpleClassAccessors(var vii1: Int? = 10, val vii2: Int? = 20) {
    var vi1: Int? = vii1
        private set             // but getter is public
    private var vi2: Int? = vii2
    fun getVi2(): Int? = this.vi2
}

class DecoratorClassBase(val vi1: Int? = null, val vi2: Int? = null) {
    fun getX(): Int = 1 + (vi1 ?: 0)  + (vi2 ?: 0)
    fun getY(): Int = 2 + (vi1 ?: 0)  + (vi2 ?: 0)
}

class DecoratorClassSubclass1(val vi1: Int? = null, val vi2: Int? = null) {
    val dcb: DecoratorClassBase

    init {
        dcb = DecoratorClassBase(vi1, vi2)
    }

    fun getX(): Int = dcb.getX()
    fun getY(): Int = (dcb.vi1 ?: 0) + (dcb.vi2 ?: 0)
}



// object is singleton instances
object SingletonInstanceClass1 {
    var vi1: Int? = 10
    var vi2: Int? = null
    fun addInts() = (vi1 ?: 0) + (vi2 ?: 0)
    fun incVi1() {
        //vi1 += 1 // this will throw exception!
    }
    fun setVi1(v: Int) {
        vi1 = v     // this will not throw exception... why?
    }
    val ac = AtomicInteger(0)
    fun incAc() {
        ac.incrementAndGet()
    }
}

open class BaseClass1 {
    var vi1: Int? = 10
    var vi2: Int? = null
    fun addInts() = (vi1 ?: 0) + (vi2 ?: 0)
}

open class BaseClass2(var vci1: Int?) {
    var vi1: Int? = 10
    var vi2: Int? = null
    fun addInts() = (vi1 ?: 0) + (vi2 ?: 0) + (vci1 ?: 0)
}

// object is singleton instances
object SingletonInstanceBaseClass1: BaseClass1() {
    var vi3: Int? = 20
    fun multInts() = (vi1 ?: 1) * (vi2 ?: 1) * (vi3 ?: 1)
}

object SingletonInstanceBaseClass2: BaseClass2(5) {
    var vi3: Int? = 20
    fun multInts() = (vci1 ?: 1) * (vi1 ?: 1) * (vi2 ?: 1) * (vi3 ?: 1)
}

//----------classes to test methods, lambdas, receivers



