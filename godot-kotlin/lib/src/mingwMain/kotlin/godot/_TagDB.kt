package godot

import kotlinx.cinterop.*

@UseExperimental(ExperimentalUnsignedTypes::class)
object _TagDB {

    val parentTo: HashMap<Int, Int> = HashMap()

    fun registerGlobalType(name: String, typeTag: Int, baseTypeTag: Int) {
        memScoped {
            Godot.nativescript11Api.godot_nativescript_set_global_type_tag!!(Godot._RegisterState.languageIndex, name.cstr.ptr, alloc<IntVar> { value = typeTag }.ptr)
            registerType(typeTag, baseTypeTag)
        }
    }

    fun registerType(typeTag: Int, baseTypeTag: Int) {
        if (typeTag != baseTypeTag) parentTo[typeTag] = baseTypeTag
    }

    fun isTypeKnown(typeTag: Int) = parentTo.containsKey(typeTag)

    fun isTypeCompatible(askTag: Int, haveTag: Int): Boolean {
        if (haveTag == 0) return false

        var tag: Int? = haveTag
        while (tag != null) {
            if (tag == askTag) return true
            tag = parentTo[tag]
        }

        return false
    }

}