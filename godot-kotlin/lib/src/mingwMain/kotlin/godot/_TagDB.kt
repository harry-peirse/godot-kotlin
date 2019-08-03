package godot

import kotlinx.cinterop.*

val _TagDB_parentTo: HashMap<Int, Int> = HashMap()

fun _TagDB_registerGlobalType(name: String, typeTag: Int, baseTypeTag: Int) {
    memScoped {
        Godot_nativescript11Api.godot_nativescript_set_global_type_tag!!(Godot_RegisterState_languageIndex, name.cstr.ptr, alloc<IntVar> { value = typeTag }.ptr)
        _TagDB_registerType(typeTag, baseTypeTag)
    }
}

fun _TagDB_registerType(typeTag: Int, baseTypeTag: Int) {
    if (typeTag != baseTypeTag) _TagDB_parentTo[typeTag] = baseTypeTag
}

fun _TagDB_isTypeKnown(typeTag: Int) = _TagDB_parentTo.containsKey(typeTag)

fun _TagDB_isTypeCompatible(askTag: Int, haveTag: Int): Boolean {
    if (haveTag == 0) return false

    var tag: Int? = haveTag
    while (tag != null) {
        if (tag == askTag) return true
        tag = _TagDB_parentTo[tag]
    }

    return false
}