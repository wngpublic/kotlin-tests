package mytest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.json.JSONObject

class JSONTest {
    val objectMapper: ObjectMapper
    val jacksonObjectMapper: ObjectMapper
    init {
        objectMapper = ObjectMapper()
        jacksonObjectMapper = jacksonObjectMapper()
    }

    // Reification is the process of taking an abstract thing and creating a concrete thing.
    // a generic type definition and one or more generic type args (abstract thing) are combined
    // to create a new generic type (concrete thing)
    // eg take List<T> and Int to produce List<T>

    inline fun <reified L : Any, reified R : Any> isSubClassOf(): Boolean = R::class.java.isAssignableFrom(L::class.java)

    inline fun <reified L : Any, reified R : Any> isSuperClassOf(): Boolean = L::class.java.isAssignableFrom(R::class.java)

    inline fun <reified T> ObjectMapper.readValue(s: String): T = this.readValue(s, object : TypeReference<T>() {})

    @Test
    fun testJSON() {
        var l = listOf("abc","def","ghi")
        var rs = l.toString()
        var ra: Array<String> = l.toTypedArray()
        var rj = jacksonObjectMapper.writeValueAsString(l)
        var rl = jacksonObjectMapper.readValue<List<String>>(rj)
        var rl2 = jacksonObjectMapper.readValue<List<String>>("[]")
        var rj2 = jacksonObjectMapper.writeValueAsString(emptyList<String>())
        return
    }
    @Test
    fun testJSONJacksonSerdes() {
        var tja1 = TJObjA("hi", 100)
        var tja2: TJObjA?
        var m1: Map<String,Int> = mapOf("a" to 1,"b" to 2, "c" to 3)
        var m2: Map<String,Int>?
        var tjb1 = TJObjB("hi", 100, listOf("one", "two", "three"), m1)
        var tjb2: TJObjB?
        var res1: String
        var res2: String
        var json1: JSONObject
        var json2: JSONObject?
        var json3: JSONObject?

        res1 = objectMapper.writeValueAsString(tja1)
        res2 = jacksonObjectMapper.writeValueAsString(tja1)
        assert(res1 == """{"s":"hi","i":100}""")
        assert(res2 == """{"s":"hi","i":100}""")
        tja2 = jacksonObjectMapper.readValue(res1)
        assert(tja1.equals(tja2))

        res1 = objectMapper.writeValueAsString(m1)
        m2 = objectMapper.readValue(res1, object: TypeReference<Map<String, Int>>(){})
        assert(res1 == """{"a":1,"b":2,"c":3}""")
        assert(m1.equals(m2))

        // based on above inline function reified ObjectMapper.readValue
        m2 = objectMapper.readValue(res1)
        assert(res1 == """{"a":1,"b":2,"c":3}""")
        assert(m1.equals(m2))


        // m2 = jacksonObjectMapper.readValue(res1, Map<String,Int>?::class.java)
        // m2 = jacksonObjectMapper.readValue(res1, Map<String,Int>::class.java)

        res1 = objectMapper.writeValueAsString(tjb1)
        res2 = jacksonObjectMapper.writeValueAsString(tjb1)
        assert(res1 == """{"s":"hi","i":100,"li":["one","two","three"],"ms":{"a":1,"b":2,"c":3}}""")
        assert(res1 == res2)
        tjb2 = jacksonObjectMapper.readValue(res1)
        assert(tjb1.equals(tjb2))

        tjb1 = TJObjB(s = "hi", li = listOf("one", "two", "three"))
        res1 = jacksonObjectMapper.writeValueAsString(tjb1)
        assert(res1 == """{"s":"hi","i":null,"li":["one","two","three"],"ms":null}""")
        tjb2 = jacksonObjectMapper.readValue(res1)
        assert(tjb1.equals(tjb2))

        json1 = JSONObject(tja1)
        res1 = json1.toString()
        json2 = JSONObject(res1)
        assert(res1 == """{"s":"hi","i":100}""")
        assert(!json1.equals(json2))
        assert(json1.similar(json2))

        json1 = JSONObject(tjb1)
        res1 = json1.toString()
        json2 = JSONObject(res1)
        assert(res1 == """{"s":"hi","li":["one","two","three"]}""")
        assert(res1 != """{"s":"hi","i":null,"li":["one","two","three"],"ms":null}""")
        assert(!json1.equals(json2))
        assert(json1.similar(json2))

        tjb1 = TJObjB("hi", 100, listOf("one", "two", "three"), m1)
        json1 = JSONObject(tjb1)
        res1 = json1.toString()
        json2 = JSONObject(res1)
        assert(!json1.equals(json2))
        assert(json1.similar(json2))
        assert(res1 == """{"s":"hi","ms":{"a":1,"b":2,"c":3},"i":100,"li":["one","two","three"]}""")

        json3 = JSONObject()

        json3.put("s", "hi")
        json3.put("i",100)
        json3.put("li", listOf("one","two","three"))
        json3.put("ms", m1)

        assert(json1.similar(json3))
        res1 = json3.toString()
        assert(res1 == """{"s":"hi","ms":{"a":1,"b":2,"c":3},"i":100,"li":["one","two","three"]}""")

        json2 = JSONObject(res1)
        assert(json2.similar(json3))


        res1 = """
            {
                "s":"hi",
                "ms":{"a":1,"b":2,"c":3},
                "i":100,
                "li":["one","two","three"]
            }
            """
        json2 = JSONObject(res1)
        assert(json2.similar(json3))
    }
    @Test
    fun testJacksonMapper() {
        var js1 = """
            {
                "s1": "string1",
                "s2": "string2",
                "tjEnumStr": "TYPEC"
            }
        """.trimIndent()
        var tjtypea: TJTypeA = objectMapper.readValue(js1)
        var tjtypeb: TJTypeB = tjtypea.toTJTypeB()

        js1 = """
            {
                "s1": "string1",
                "s2": "string2",
                "s3": "extra3",
                "tjEnumStr": "TYPEC"
            }
        """.trimIndent()
        tjtypea = objectMapper.readValue(js1)
        tjtypeb = tjtypea.toTJTypeB()

        var js2 = """
            {
                "s1": "string1",
                "s2": "string2",
                "s3": "extra3",
                "tjEnum": "TYPEC"
            }
        """.trimIndent()
        tjtypeb = objectMapper.readValue(js2)

        js1 = """
            {
                "s1": "string1",
                "s2": "string2",
                "s3": "extra3"
            }
        """.trimIndent()
        tjtypea = objectMapper.readValue(js1)
        tjtypeb = tjtypea.toTJTypeB()
        return
    }
    @Test
    fun testJacksonKotlin() {
        var k = KotlinModule()
        // k = KotlinModule.Builder()
        //     .withReflectionCacheSize(reflectionCacheSize)
        //     .configure(KotlinFeature.NullToEmptyCollection, nullToEmptyCollection)
        //     .configure(KotlinFeature.NullToEmptyMap, nullToEmptyMap)
        //     .configure(KotlinFeature.NullIsSameAsDefault, nullIsSameAsDefault)
        //     .configure(KotlinFeature.SingletonSupport, singletonSupport)
        //     .configure(KotlinFeature.StrictNullChecks, strictNullChecks)
        //     .build()
        k = KotlinModule.Builder().build()
        return
    }

}

class StandardLibsTest {
    /*
     * maxBy()
     */
    @Test
    fun testFoo() {

    }
    @Test
    fun testKotlinXJson() {
        // added to plugins: kotlin("plugin.serialization") version "1.6.10"
        val pt = Json.decodeFromString<Point>("""{"y": 1, "x": 2}""")
        val str = Json.encodeToString(pt)

        val ilist = Json.decodeFromString<List<Int>>("[-1, -2]")
        val ptlist = Json.decodeFromString<List<Point>>(
            """[{"x": 3, "y": 4}, {"x": 5, "y": 6}]"""
        )
    }
}


// adding NON_EMPTY will fail some tests which assume empty fields have default null
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TJObjA(val s: String? = null, val i: Int? = null)
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TJObjB(val s: String? = null, val i: Int? = null, val li: List<String>? = null, val ms: Map<String,Int>? = null)

enum class TJEnum {
    TYPEA,
    TYPEB,
    TYPEC,
    NONE
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TJTypeA(
    @JsonProperty("s1") val s1: String?,
    @JsonProperty("s2") val s2: String?,
    @JsonProperty("tjEnumStr") val tjEnumStr: String?)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TJTypeB(
    @JsonProperty("s1") val s1: String?,
    @JsonProperty("s2") val s2: String?,
    @JsonProperty("tjEnum") val tjEnum: TJEnum?)

fun TJTypeA.toTJTypeB(): TJTypeB = TJTypeB(s1, s2, TJEnum.valueOf(tjEnumStr ?: "NONE"))
fun TJTypeB.toTJTypeA(): TJTypeA = TJTypeA(s1, s2, tjEnum.toString())


@Serializable
data class Point(val x: Int, val y: Int)
