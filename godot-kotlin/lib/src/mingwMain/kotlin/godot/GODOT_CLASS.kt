package godot

import kotlin.reflect.KClass
import kotlinx.cinterop.*

interface GODOT_CLASS<TYPE : BASE_TYPE, BASE_TYPE : Wrapped> {
    val type: KClass<TYPE>
    val baseType: KClass<BASE_TYPE>
    fun _new(): TYPE
    fun registerMethods()

    fun getTypeName() = type.simpleName ?: throw IllegalStateException("Missing TypeName")
    fun getTypeTag() = type.simpleName.hashCode()
    fun getBaseTypeName() = baseType.simpleName ?: throw IllegalStateException("Missing BaseTypeName")
    fun getBaseTypeTag() = baseType.simpleName.hashCode()

    fun new(): TYPE {
        try {
            val script = NativeScript.new()
            val gdNative = GDNativeLibrary()
            gdNative._wrapped = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, godot.gdnlib)?.reinterpret()
            script.setLibrary(gdNative)

            memScoped {
                val typeName: CPointer<GString> = godot.api.godot_alloc!!(GString.size.toInt())!!.reinterpret()
                godot.api.godot_string_new!!(typeName)
                godot.api.godot_string_parse_utf8!!(typeName, getTypeName().cstr.ptr)
                script.setClassName(typeName.pointed)
                val instance: TYPE = _new()
                instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(script.new()._wrapped?.pointed?._owner)?.reinterpret()

                godot.print("instance wrapped: ${instance._wrapped}")
                godot.print("instance owner: ${instance._wrapped?.pointed?._owner}")

                return instance
            }
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
            throw e
        }
    }

    fun getFromVariant(a: CPointer<Variant>): TYPE {
        val instance: TYPE = _new()
        instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(Object.getFromVariant(a)._wrapped?.pointed?._owner)?.reinterpret()
        return instance
    }

    fun registerMethod(functionName: String, function: GodotFunctionCall) {
        godot.registerMethod(this, functionName, function)
    }
}