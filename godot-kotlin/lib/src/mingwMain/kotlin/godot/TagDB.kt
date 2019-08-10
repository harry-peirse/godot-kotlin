package godot

import kotlinx.cinterop.*

@UseExperimental(ExperimentalUnsignedTypes::class)
class TagDB {
    val parentTo: HashMap<UInt, UInt> = HashMap()

    fun registerGlobalType(name: String, typeTag: UInt, baseTypeTag: UInt) {
        memScoped {
            godot.nativescript11Api.godot_nativescript_set_global_type_tag!!(godot.languageIndex, name.cstr.ptr, alloc<UIntVar> { value = typeTag }.ptr)
            registerType(typeTag, baseTypeTag)
        }
    }

    fun registerType(typeTag: UInt, baseTypeTag: UInt) {
        if (typeTag != baseTypeTag) parentTo[typeTag] = baseTypeTag
    }

    fun isTypeKnown(typeTag: UInt) = parentTo.containsKey(typeTag)

    fun isTypeCompatible(askTag: UInt, haveTag: UInt): Boolean {
        if (haveTag == 0u) return false

        var tag: UInt? = haveTag
        while (tag != null) {
            if (tag == askTag) return true
            tag = parentTo[tag]
        }

        return false
    }
}