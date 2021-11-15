package testpassword.plugins

fun printErr(vararg obj: Any) = System.err.println(obj.toString())

infix operator fun String.minus(removable: String) = this.replace(removable, "")