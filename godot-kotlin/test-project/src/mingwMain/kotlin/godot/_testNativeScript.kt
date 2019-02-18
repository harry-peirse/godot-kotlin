package godot

/**
NATIVESCRIPT attempt 1

XXX: This doesn't work yet, cannot pass struct by value in callbacks, therefore registering types with godot.NativeScript won't work
*/
fun test() {



//    memScoped {
//        val nativeScript = nativeScript ?: throw IllegalStateException("Attempted to access the Godot NativeScript API but it was not initialized")
//
//        fun simple_constructor(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
//            val user_data: COpaquePointer? = gdNative!!.api.godot_alloc!!("World from godot.GDNative!".cstr.size)
//            val p: CPointer<ByteVar> = user_data!!.reinterpret()
//            strcpy(p, "World from godot.GDNative!")
//
//            return user_data
//        }
//
//        fun create(): CValue<godot_instance_create_func> {
//            return cValue {
//                create_func = staticCFunction(::simple_constructor)
//            }
//        }
//
//        fun simple_destructor(instance: COpaquePointer?, method_data: COpaquePointer?, user_data: COpaquePointer?) {
//            gdNative!!.api.godot_free!!(user_data)
//        }
//
//        fun destroy(): CValue<godot_instance_destroy_func> {
//            return cValue {
//                destroy_func = staticCFunction(::simple_destructor)
//            }
//        }
//
//        fun getData(godot_object: COpaquePointer?,
//                    method_data: COpaquePointer?,
//                    user_data: COpaquePointer?,
//                    num_args: Int,
//                    args: CPointer<CPointerVar<godot_variant>>?
//        ): CValue<godot_variant> {
//            memScoped {
//                val data: CPointer<godot_string> = alloc<godot_string>().ptr
//                val ret: CValue<godot_variant> = cValue()
//
//                val userData: CPointer<ByteVar> = user_data!!.reinterpret()
//
//                gdNative!!.api.godot_string_new!!(data)
//                gdNative!!.api.godot_string_parse_utf8!!(data, userData)
//                gdNative!!.api.godot_variant_new_string!!(ret.ptr, data)
//                gdNative!!.api.godot_string_destroy!!(data)
//
//                return ret
//            }
//        }
//
//        nativeScript.api.godot_nativescript_register_class!!(nativeScript.handle, "SIMPLE".cstr.ptr, "Reference".cstr.ptr, create(), destroy())
//
//        val get_data: CValue<godot_instance_method> = cValue {
//            method = staticCFunction(::getData)
//
//        }
//
//        val attributes: CValue<godot_method_attributes> = cValue {
//            rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
//        }
//
//        nativeScript.api.godot_nativescript_register_method!!(nativeScript.handle, "SIMPLE".cstr.ptr, "get_data".cstr.ptr, attributes, get_data)
//    }
}