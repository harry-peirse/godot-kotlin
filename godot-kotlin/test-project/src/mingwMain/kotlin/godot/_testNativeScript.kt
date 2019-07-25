package godot

import godotapi.*
import kotlinx.cinterop.*
import platform.posix.strcpy

@CName("godot_get_data")
fun getData(godot_object: COpaquePointer?,
            method_data: COpaquePointer?,
            user_data: COpaquePointer?,
            num_args: Int,
            args: CPointer<CPointerVar<godot_variant>>?
): CValue<godot_variant> {

    val data: CPointer<godot_string> = api.c.godot_alloc!!(godot_string.size.toInt())!!.reinterpret()
    val userData: CPointer<ByteVar> = user_data!!.reinterpret()
    api.c.godot_string_new!!(data)
    api.c.godot_string_parse_utf8!!(data, userData)

    val ret: CPointer<godot_variant> = api.c.godot_alloc!!(godot_variant.size.toInt())!!.reinterpret()
    api.c.godot_variant_new_string!!(ret, data)
    api.c.godot_string_destroy!!(data)

    return ret[0].readValue()
}

fun test() {

    val nativeScript = nativeScript
            ?: throw IllegalStateException("Attempted to access the Godot NativeScript API but it was not initialized")

    fun simple_constructor(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
        val user_data: COpaquePointer? = api.c.godot_alloc!!("World from GDNative!".cstr.size)
        val p: CPointer<ByteVar> = user_data!!.reinterpret()
        strcpy(p, "World from GDNative!")

        return user_data
    }

    fun create(): CValue<godot_instance_create_func> {
        return cValue {
            create_func = staticCFunction(::simple_constructor)
        }
    }

    fun simple_destructor(instance: COpaquePointer?, method_data: COpaquePointer?, user_data: COpaquePointer?) {
        api.c.godot_free!!(user_data)
    }

    fun destroy(): CValue<godot_instance_destroy_func> {
        return cValue {
            destroy_func = staticCFunction(::simple_destructor)
        }
    }

    val get_data: CValue<godot_instance_method> = cValue {
        method = staticCFunction(::getData)
    }

    val attributes: CValue<godot_method_attributes> = cValue {
        rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
    }

    memScoped {
        nativeScript.c.godot_nativescript_register_class!!(nativeScript.handle, "SIMPLE".cstr.ptr, "Reference".cstr.ptr, create(), destroy())
        nativeScript.c.godot_nativescript_register_method!!(nativeScript.handle, "SIMPLE".cstr.ptr, "get_data".cstr.ptr, attributes, get_data)
    }
}