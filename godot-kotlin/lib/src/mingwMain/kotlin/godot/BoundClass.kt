package godot

import godot.internal.godot_instance_create_func
import godot.internal.godot_instance_destroy_func
import godot.internal.godot_variant
import kotlinx.cinterop.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

@UseExperimental(ExperimentalUnsignedTypes::class)
class BoundClass<T : S, S : Object>(val type: KClass<T>, val baseType: KClass<S>, val producer: () -> T, val binder: Binder.() -> Unit) {
    val typeName: String
        get() = type.simpleName!!
    val baseTypeName: String
        get() = baseType.simpleName!!

    fun new(): T {
        val script = NativeScript.new()
        val gdNative = GDNativeLibrary.getFromVariant(gdnlib)
        script.setLibrary(gdNative)

        memScoped {
            script.setClassName(typeName)
            return getFromVariant(Variant(script.new())._raw)
        }
    }

    fun bind() {
        Binder(typeName).binder()
    }

    @Suppress("UNCHECKED_CAST")
    fun getFromVariant(_variant: CPointer<godot_variant>): T {
        return nativescriptApi.godot_nativescript_get_userdata!!(_variant)?.asStableRef<Object>()?.get()!! as T
    }

    private var stableRef: StableRef<BoundClass<T, S>>? = null

    fun asStableRef(): StableRef<BoundClass<T, S>>? {
        if (stableRef == null) {
            stableRef = StableRef.create(this)
        }
        return stableRef
    }
}

internal fun constructor(instance: COpaquePointer?, methodData: COpaquePointer?): COpaquePointer? {
    val variant = Variant(instance!!.reinterpret())
    val boundClass = methodData?.asStableRef<BoundClass<*, *>>()?.get()!!
    val obj = boundClass.producer()
    obj._raw = variant._raw
    return obj._stableRef.asCPointer()
}

inline fun <reified T : S, S : Object> registerClass(noinline producer: () -> T, baseType: KClass<S>, noinline binder: Binder.() -> Unit) {
    registerClass(BoundClass(T::class, baseType, producer, binder))
}

class Binder(val type: String) {
    inline fun <T : Object, reified R> method(name: String, noinline method: Function1<T, R>) {
        registerMethod(type, name, BoundMethod(method, R::class))
    }

    inline fun <T : Object, reified R, reified A1 : Any> method(name: String, noinline method: Function2<T, A1, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2> method(name: String, noinline method: Function3<T, A1, A2, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3> method(name: String, noinline method: Function4<T, A1, A2, A3, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4> method(name: String, noinline method: Function5<T, A1, A2, A3, A4, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5> method(name: String, noinline method: Function6<T, A1, A2, A3, A4, A5, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6> method(name: String, noinline method: Function7<T, A1, A2, A3, A4, A5, A6, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7> method(name: String, noinline method: Function8<T, A1, A2, A3, A4, A5, A6, A7, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8> method(name: String, noinline method: Function9<T, A1, A2, A3, A4, A5, A6, A7, A8, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9> method(name: String, noinline method: Function10<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10> method(name: String, noinline method: Function11<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A10::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11> method(name: String, noinline method: Function12<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12> method(name: String, noinline method: Function13<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class))
    }

    inline fun <T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12, reified A13> method(name: String, noinline method: Function14<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R>) {
        registerMethod(type, name, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class, A13::class))
    }

    inline fun <T : Object, reified A1 : Any> property(name: String, property: KMutableProperty1<T, A1>, defaultValue: A1) {
        registerProperty(type, name, defaultValue, BoundProperty(property, A1::class))
    }

    fun signal(name: String, vararg arguments: Pair<String, Variant.Type>) {
        registerSignal(type, name, *arguments)
    }
}

fun registerClass(boundClass: BoundClass<*, *>) {
    memScoped {
        val create = cValue<godot_instance_create_func> {
            create_func = staticCFunction(::constructor)
            method_data = boundClass.asStableRef()?.asCPointer()
        }
        val destroy = cValue<godot_instance_destroy_func>()

        print("Registering class ${boundClass.typeName} : ${boundClass.baseTypeName}")

        nativescriptApi.godot_nativescript_register_class!!(nativescriptHandle, boundClass.typeName.cstr.ptr, boundClass.baseTypeName.cstr.ptr, create, destroy)
        nativescript11Api.godot_nativescript_set_type_tag!!(nativescriptHandle, boundClass.typeName.cstr.ptr, Variant(boundClass.type.tag())._raw)
        tagDB.registerType(boundClass.type, boundClass.baseType, boundClass.producer)
        boundClass.bind()
    }
}