package foo.bar

fun box(): String {
    return Foo(1).box()
}

class Foo {

    var test: Int

    private constructor() {
        test = 1
    }

    constructor(a: Int) {
        test = 2
    }

    internal constructor(a: String) {
        test = 3
    }

    public constructor(a: Double) {
        test = 4
    }

    public fun box(): String {
        return "OK"
    }

    fun foo(): String {
        return "Hello world"
    }

    protected fun bar(): String {
        return "Hello bar"
    }

    private fun baz(): String {
        return "Hello baz"
    }

    internal fun qux(): String {
        return "Hello qux"
    }
}
