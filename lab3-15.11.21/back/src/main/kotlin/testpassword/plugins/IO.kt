package testpassword.plugins

fun printErr(vararg obj: Any): Unit = obj.forEach { System.err.println(it.toString()) }

infix operator fun String.minus(removable: String): String = this.replace(removable, "")

fun String.toIntOr(option: Int): Int = toIntOrNull(radix = 10) ?: option