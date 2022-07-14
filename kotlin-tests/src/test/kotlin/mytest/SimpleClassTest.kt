package mytest

import kotlin.test.Test
import org.json.JSONObject

// cannot be imported
//import kotlin.native.concurrent.AtomicInt

class SimpleClassTest {

    @Test
    fun testSimpleClass() {
        var sc = SimpleClass7(1,2,3)
        sc.v1 = 2
        sc.v2 = 3
        //sc.v3 = 4     // v3 is val, cannot be reassigned
        assert(sc.v1 == 2)
        sc = SimpleClass7(1, v4=40)
        assert(sc.v2 == 10 && sc.v3 == 20 && sc.v4 == 40)
    }

    @Test
    fun testSimpleClassGetterSetter() {
        val sc = SimpleClass2(1,"hi")
        var res: String?
        res = sc.vs5
        assert(res    == "getter: hi")
        assert(sc.vs5 == "getter: hi")
        sc.vs5 = "hi"
        res = sc.vs5
        assert(res    == "getter: setter: hi")
        assert(sc.vs5 == "getter: setter: hi")
    }

    @Test
    fun testSum() {
        var simpleClass = SimpleClass1() // not new SimpleClass()
        var vsum = simpleClass.sum(10,20)
        println("vsum is $vsum")
    }

    @Test
    fun testSimpleClassConstructors() {
        var sc2 = SimpleClass2(10,"hello")
        val vsc2pi1 = sc2.pi1
        assert(sc2.pi3 == null)
        sc2.pi3 = 10
        var sc3 = SimpleClass3(10, "hello")
        val vsc3i1 = sc3.i1
        assert(vsc2pi1 == 10 && vsc2pi1 == vsc3i1)
    }

    @Test
    fun testFunctionsClass() {
        val f = FunctionsClass()
        val vnum: VNum = VNum(10)
        val vstr: VStr = VStr("10")
        assert(f.getEnumType(vnum) == EnumVal.Integer)
        assert(f.getEnumType(vstr) == EnumVal.String)
        assert(f.getEnumType(10) == EnumVal.Integer)
    }

    @Test
    fun testClassInstances() {
        var scb61 = SimpleClassBase6(1)
        var scb62 = SimpleClassBase6(1,2)
        var sc6a = SimpleClass6A(1,2,3)
        var sc6b = SimpleClass6B(1,2,3)
        val scwLazy: SimpleClassWhen by lazy {
            SimpleClassWhen(1)
        }
        val scw1 = SimpleClassWhen(1)
        val scw2 = SimpleClassWhen(2)
        val scw3 = SimpleClassWhen(3)
        assert(scw1.sc6b?.vi1 == 1)
        assert(scw1.sc6b!!.vi1 == 1)
        assert(scw2.sc6b?.vi1 == 2)
        assert(scw2.sc6b!!.vi1 == 2)
        assert(scw3.sc6b?.vi1 ?: 10 == 10)
        assert(scw3.sc6b?.vi1 == null)
        assert(scwLazy.sc6b?.vi1 == 1)
    }

    @Test
    fun testClassInnerOuter() {
        var scio1 = SimpleClassInnerOuter()

        // not allowed: mytest.SimpleClassInnerOuter.InnerClass()
        var ic1 = scio1.InnerClass()
        assert(ic1.vi1 == 3)

        // not allowed: scio1.StaticClass()
        var sc1 = SimpleClassInnerOuter.StaticClass()
        sc1.vi1 = 1
        var sc2 = SimpleClassInnerOuter.StaticClass()
        sc2.vi1 = 2
        assert(sc1.vi1 == 1 && sc2.vi1 == 2)

        var resi: Int? = null

        // static companion object version is called
        resi = SimpleClassInnerOuter.add(1,2)
        assert(resi == 4)

        resi = SimpleClassInnerOuter.add(1,2)
        assert(resi == 4)

        // instance version is called
        resi = scio1.add(1,2)
        assert(resi == 3)

        assert(SimpleClassInnerOuter.vi1 == 5)
        assert(scio1.vi1 == 1)

        var scc1 = SimpleClassCompanion1()
        // add or labeled call both work
        assert(SimpleClassCompanion1.add(1,2) == 4)
        assert(SimpleClassCompanion1.Companion1.add(1,2) == 4)
        assert(scc1.add(1,2) == 3)
    }
    @Test
    fun testClassAccessors() {
        var sca = SimpleClassAccessors(vii2 = 25)
        var res: Int?
        res = sca.vi1
        assert(res == 10)
        // setter is private
        // sca.vi1 = 1

        // getter and setter are private
        // res = sca.vi2
        res = sca.getVi2()
        assert(res == 25)

        // vii1 is not accessible
        // res = sca.vii1

        res = sca.vii2
        assert(res == 25)
    }
    @Test
    fun testSingleton() {
        SingletonInstance.vi1 = 10
        var res: Int?
        res = SingletonInstance.listImmutable1.find { it % 2 == 0 }
        assert(res == 2)
        assert(SingletonInstance.listImmutable1 == listOf(1,2,3))
        SingletonInstance.listMutable.add(1)
        assert(SingletonInstance.listMutable == listOf(1))
        SingletonInstance.listImmutable2 += 4   // this is because it's a var, not val
        assert(SingletonInstance.listImmutable2 == listOf(1,2,3,4))
    }
    @Test
    fun testDataClass() {
        val dc11 = DataClass1(1, 2)
        dc11.vi3 = 33
        val dc12 = DataClass1(1, 2)
        dc12.vi3 = 34
        val dc13 = DataClass1(2, 3)

        assert(dc11.equals(dc12))
        assert(!dc11.equals(dc13))

        val dc14 = dc11.copy(vi1=2, vi2=3)
        assert(dc14.vi3 == 3 && dc11.vi3 == 33)
        assert(dc13.equals(dc14) && dc14.equals(dc13))

        val dc15 = dc11.copy()
        assert(dc11.equals(dc15) && dc15.vi3 == 3)

        val dc21 = DataClass2(10, 10)
        val dc22 = DataClass2(20, 20)
        val dc23 = DataClass2(20, 30)
        val dc24 = DataClass2(30, 40)

        assert(DataClass2.DataClassComparator.compare(dc22, dc21) == 1)
        assert(DataClass2.DataClassComparator.compare(dc22, dc23) == 0)
        assert(DataClass2.DataClassComparator.compare(dc22, dc24) == -1)
    }
    @Test
    fun testSingleton2() {
        assert(SingletonInstanceClass1.addInts() == 10)
        SingletonInstanceClass1.vi2 = 15
        assert(SingletonInstanceClass1.addInts() == 25)
        SingletonInstanceClass1.setVi1(2)
        assert(SingletonInstanceClass1.vi1 == 2)
    }
    @Test
    fun testFunctionsSubclass1() {
        var f = FunctionsClass()
        var fs = FunctionsSubclass1()
        var o: ObjectTest
        o = f.setObjectClass("hello")
        assert(o.getVV() == "mytest.ObjectTest:hello")
        o = fs.setObjectClass("hello")
        assert(o.getVV() == "ObjectOverrideTest:hello")
        o = fs.setObjectClass("bye")
        assert(o.getVV() == "ObjectOverrideTest:bye")

        f = object: FunctionsClass() {
            override open fun setObjectClass(v: String): ObjectTest {
                return object: ObjectTest(v) {
                    override fun getVV(): String {
                        val s = "local:$v"
                        return s
                    }
                }
            }
        }

        o = f.setObjectClass("hello")
        assert(o.getVV() == "local:hello")

        var sfc = SingleFunctionClass()
        o = sfc.getObj("hello")
        assert(o.getVV() == "mytest.ObjectTest:basic:hello")
        sfc = object: SingleFunctionClass() {
            override fun getObj(v:String): ObjectTest {
                return ObjectTest("blank:$v")
            }
        }
    }
    @Test
    fun testPolymorphism() {
        var lf1: Lifeform = AnimalFish(1, 1, "fish")
        var lf2: Lifeform = AnimalApe(2, 2, "ape")
        var lf3: Lifeform = AnimalBird(3, 3, "bird")

        var lf4: AnimalBird = lf3 as AnimalBird
        var lf5 = lf3 as AnimalBird

        // this will throw exception
        //var lf6: mytest.AnimalApe = lf3 as mytest.AnimalApe

    }
    @Test
    fun testReturnWhenType() {
        fun convertJSON(v: Any?): JSONObject? =
            when(v) {
                is SC1 -> JSONObject(v)
                is SC2 -> JSONObject(v)
                else -> null
            }
        var l = listOf(
            SC2(1,"hi"),
            SC3(2,"2"),
            SimpleClass4(1, 2, "aa", "bb"),
            SC2(1,"x")
        )
        var r = mutableListOf<JSONObject?>()
        l.stream().forEach {
            r.add(convertJSON(it))
        }

        var v = r.get(0)
        assert(v != null)
        v = r.get(1)
        assert(v == null)
        v = r.get(2)
        assert(v == null)
        v = r.get(3)
        assert(v != null)
        return

    }
}

abstract class CEntity<T>(id: T?)
abstract class CIdEntity(entity: CEntity<Int>, id: Int): CEntity<Int>(id)
abstract class CBase<out E: CIdEntity>()
class CSingletonBase()


// val variables must be assigned at declaration, except for lateinit
// so call the others as var
// lateinit is used when not setting at declaration, but guaranteed
// to be set by time of use
// lazy is when var is not initialized until used
// lateinit cannot be used with val, only with var
// if variables are mutable, var, use lateinit
// lazy can be used only for val variables
class FluentBuilder1(val id: Int) {
    // lateinit cannot be on ? types
    // lateinit cannot be on primitive types Int
    //lateinit var v1: Int

    val lazyVar: String by lazy {
        "some val that takes a long time to do or later computed"
    }
}
