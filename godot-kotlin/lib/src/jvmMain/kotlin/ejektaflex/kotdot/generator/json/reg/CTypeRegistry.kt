package ejektaflex.kotdot.generator.json.reg

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlin.reflect.KType
import kotlin.reflect.full.createType

object CTypeRegistry : SimpleRegistry<String, KType>() {

    init {
        mapOf(
                "godot_int" to Int::class.createType(),
                "godot_real" to Float::class.createType(),
                "void" to Unit::class.createType(),
                "godot_bool" to Boolean::class.createType()
        ).forEach { t, u ->
            delegate[t] = u
        }
    }

    fun lookup(name: String): TypeName {
        return if (name in delegate) {
            delegate[name]!!.asTypeName()
        } else {
            ClassName("", CoreClassRegistry[name]!!.ktName)
        }
    }


}