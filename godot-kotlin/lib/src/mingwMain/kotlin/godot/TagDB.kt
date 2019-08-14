package godot

import kotlinx.cinterop.cstr
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlin.reflect.KClass

internal val tagDB = TagDB()

@UseExperimental(ExperimentalUnsignedTypes::class)
fun KClass<out Object>.tag(): UInt {
    return simpleName.hashCode().toUInt()
}

@UseExperimental(ExperimentalUnsignedTypes::class)
internal class TagDB {
    val parents = HashMap<UInt, UInt>()
    val types = HashMap<UInt, KClass<out Object>>()
    val producers = HashMap<UInt, () -> Object>()

    fun registerType(type: KClass<out Object>, baseType: KClass<out Object>, producer: () -> Object) {
        if (type != baseType) {
            parents[type.tag()] = baseType.tag()
        }
        types[type.tag()] = type
        producers[baseType.tag()] = producer
    }

    fun registerGlobalType(name: String, type: KClass<out Object>, baseType: KClass<out Object>, producer: () -> Object) = memScoped {
        godot.nativescript11Api.godot_nativescript_set_global_type_tag!!(godot.languageIndex, name.cstr.ptr, Variant(type.tag())._raw)
        registerType(type, baseType, producer)
    }
}