package godot

import godot.internal._Wrapped
import godot.internal.godot_instance_create_func
import godot.internal.godot_instance_destroy_func
import godot.internal.godot_string
import kotlinx.cinterop.*
import kotlin.reflect.KClass

class ClassBinder {

}

class BoundClass<T : S, S : Object>(val type: KClass<T>, val baseType: KClass<S>, val producer: () -> T, val binder: ClassBinder.() -> Unit) {
    val typeName: String
        get() = type.simpleName!!
    val baseTypeName: String
        get() = baseType.simpleName!!

    fun _new(): T {
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
                val instance = producer()
                instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(script.new()._owner)?.reinterpret()

                return instance
            }
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
            throw e
        }
    }

    fun getFromVariant(a: Variant): BoundClass {
        val instance = new()
        instance._wrapped = godot.nativescriptApi.godot_nativescript_get_userdata!!(Object.getFromVariant(a)._owner)?.reinterpret()
        return instance
    }
}

internal fun _constructor(instance: COpaquePointer?, methodData: COpaquePointer?): COpaquePointer? {
    val godotClass = methodData!!.asStableRef<GodotClass>().get()
    val wrapped = godot.api.godot_alloc!!(_Wrapped.size.toInt())!!.reinterpret<_Wrapped>().pointed
    wrapped._owner = instance
    wrapped._typeTag = godotClass.getTypeTag()
    val newInstance = godotClass.new()
    newInstance._wrapped = wrapped.ptr
    return StableRef.create(newInstance).asCPointer()
}

@Suppress("UNUSED_PARAMETER")
internal fun _destructor(instance: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?) {
    val wrapped = userData?.asStableRef<BoundClass>()?.get()?._wrapped
    godot.api.godot_free!!(wrapped)
}

fun registerClass(clazz: GodotClass) {
    memScoped {
        val create = cValue<godot_instance_create_func> {
            create_func = staticCFunction(::_constructor)
            method_data = StableRef.create(clazz).asCPointer()
        }
        val destroy = cValue<godot_instance_destroy_func> {
            destroy_func = staticCFunction(::_destructor)
            method_data = StableRef.create(clazz).asCPointer()
        }

        print("Registering class ${clazz.getTypeName()} : ${clazz.getBaseTypeName()}")

        nativescriptApi.godot_nativescript_register_class!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, clazz.getBaseTypeName().cstr.ptr, create, destroy)
        nativescript11Api.godot_nativescript_set_type_tag!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, alloc<UIntVar> { value = clazz.getTypeTag() }.ptr)
        clazz.registerMethods()
    }
}