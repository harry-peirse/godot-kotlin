package ejektaflex.kotdot.generator.json.reg

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlin.reflect.KType
import kotlin.reflect.full.createType

object CTypeRegistry : SimpleRegistry<String, TypeName>() {

    init {
        mapOf(
                "void"          to Unit::class.asTypeName()
        ).forEach { t, u ->
            delegate[t] = u
        }
    }

    fun lookup(name: String): TypeName {
        return if (name in delegate) {
            delegate[name]!!
        } else {
            ClassName("godotapi", name)
        }
    }
}