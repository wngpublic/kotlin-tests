@file:JvmName("UtilFuncs")

package myutils.mytest

import java.util.Random
import kotlin.streams.toList

// need to have package name for import. look in SyntaxTest

// functions can be placed outside of class, which declutters

var GCTRVAR = 0

val GCTRVAL = 1

const val GCTRVALCONST = 2

fun echo(msg: String?): String = "toplevel: $msg"
fun greet(msg: String?): String = "toplevel: hello $msg"

// infix functions: must be member function, must have 1 parameter, must not have default value
// used for a f b, instead of a.f(b)

// generic functions: fun <T> f(v: T): ret<T> { .. }

// inline functions: generates code. define inline function, and call it. at the called place,
// compiler replaces it with generated code from definition

val random = Random()
val charsetFull  = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
val charsetFullSpace  = charsetFull + "     "
val charsetShort = "abcdefghijklmnopqrstuvwxyz"
val charsetShortSpace = charsetShort + "     "

fun getCharSeq(numChars: Long, includeSpace: Boolean): String {
    val charset = if(includeSpace) charsetShortSpace else charsetShort
    val v = random
        .ints(numChars,0,charset.length)
        .toList()
        .map{ i -> charset.get(i) }
        .joinToString(separator="")
    return v
}

inline fun <T> T.applyThenReturnUtil1(f: (T) -> Unit): T {
    f(this)
    return this
}
inline fun <T> T.applyThenReturnUtil2(f: T.() -> Unit): T {
    f()
    return this
}
