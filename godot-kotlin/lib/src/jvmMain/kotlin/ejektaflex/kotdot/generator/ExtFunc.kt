package ejektaflex.kotdot.generator

import com.squareup.kotlinpoet.TypeName

val TypeName.simpleName: String
    get() = this.toString().split('.').last()

fun String.toCamelCase(): String {
    return this.split('_').mapIndexed { i, it ->
        if (i != 0) {
            it.capitalize()
        } else {
            it
        }
    } .joinToString("")
}