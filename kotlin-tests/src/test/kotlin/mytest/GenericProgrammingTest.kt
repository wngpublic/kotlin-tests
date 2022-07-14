package mytest

class GenericProgrammingTest {
    inline fun<T> Array<out T>.forEach(action: (T) -> Unit): Unit {
        for(e in this) action(e)
    }
}

