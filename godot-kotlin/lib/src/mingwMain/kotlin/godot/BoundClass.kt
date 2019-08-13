package godot

import godot.internal.godot_instance_create_func
import godot.internal.godot_instance_destroy_func
import godot.internal.godot_variant
import kotlinx.cinterop.*
import kotlin.reflect.KClass

class ClassBinder

class BoundClass<T : S, S : Object>(val type: KClass<T>, val baseType: KClass<S>, val producer: () -> T, val binder: ClassBinder.() -> Unit) {
    val typeName: String
        get() = type.simpleName!!
    val baseTypeName: String
        get() = baseType.simpleName!!

    fun new(): T {
        val script = NativeScript.new()
        val gdNative = GDNativeLibrary.getFromVariant(godot.gdnlib)
        script.setLibrary(gdNative)

        memScoped {
            script.setClassName(GodotString(typeName))
            return getFromVariant(script.new()._variant)
        }
    }

    fun getFromVariant(_variant: CPointer<godot_variant>): T {
        return godot.nativescriptApi.godot_nativescript_get_userdata!!(_variant)?.asStableRef<Object>()?.get()!! as T
    }

    private var stableRef: StableRef<BoundClass<T, S>>? = null

    fun asStableRef(): StableRef<BoundClass<T, S>>? {
        if (stableRef == null) {
            stableRef = StableRef.create(this)
        }
        return stableRef
    }
}

internal fun _constructor(instance: COpaquePointer?, methodData: COpaquePointer?): COpaquePointer? {
    val _variant = instance?.reinterpret<godot_variant>()!!
    val boundClass = methodData?.asStableRef<BoundClass<*, *>>()?.get()!!
    val obj = Variant.create(_variant, boundClass)
    return obj.asStableRef()?.asCPointer()
}

fun registerClass(boundClass: BoundClass<*, *>) {
    memScoped {
        val create = cValue<godot_instance_create_func> {
            create_func = staticCFunction(::_constructor)
            method_data = boundClass.asStableRef()?.asCPointer()
        }
        val destroy = cValue<godot_instance_destroy_func>()

        print("Registering class ${boundClass.typeName} : ${boundClass.baseTypeName}")

        nativescriptApi.godot_nativescript_register_class!!(nativescriptHandle, boundClass.typeName.cstr.ptr, boundClass.baseTypeName.cstr.ptr, create, destroy)
//        nativescript11Api.godot_nativescript_set_type_tag!!(nativescriptHandle, boundClass.typeName.cstr.ptr, alloc<UIntVar> { value = clazz.getTypeTag() }.ptr)
        boundClass.binder(ClassBinder())
    }
}