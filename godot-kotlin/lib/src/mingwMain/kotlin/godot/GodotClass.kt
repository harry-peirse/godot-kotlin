package godot

import godot.internal.godot_string
import kotlinx.cinterop.cstr
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlin.reflect.KClass

interface GodotClass {
    val type: KClass<out Wrapped>
    val baseType: KClass<out Wrapped>
    fun new(): Wrapped
    fun registerMethods()

    fun getTypeName() = type.simpleName ?: throw IllegalStateException("Missing TypeName")
    @UseExperimental(ExperimentalUnsignedTypes::class)
    fun getTypeTag() = type.simpleName.hashCode().toUInt()

    fun getBaseTypeName() = baseType.simpleName ?: throw IllegalStateException("Missing BaseTypeName")
    @UseExperimental(ExperimentalUnsignedTypes::class)
    fun getBaseTypeTag() = baseType.simpleName.hashCode().toUInt()

    fun _new(): Wrapped {
        try {
            val script = NativeScript.new()
            val gdNative = GDNativeLibrary()
            gdNative._wrapped = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, godot.gdnlib)?.reinterpret()
            script.setLibrary(gdNative)

            memScoped {
                val typeName = godot.alloc<godot_string>(godot_string.size)
                godot.api.godot_string_new!!(typeName)
                godot.api.godot_string_parse_utf8!!(typeName, getTypeName().cstr.ptr)
                script.setClassName(GodotString(typeName))
                val instance = new()
                instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(script.new()._owner)?.reinterpret()

                godot.print("instance wrapped: ${instance._wrapped}")
                godot.print("instance owner: ${instance._owner}")

                return instance
            }
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
            throw e
        }
    }

    fun getFromVariant(a: Variant): Wrapped {
        val instance = new()
        instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(Object.getFromVariant(a)._owner)?.reinterpret()
        return instance
    }
}