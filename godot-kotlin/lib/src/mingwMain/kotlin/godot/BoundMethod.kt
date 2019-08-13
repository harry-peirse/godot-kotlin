package godot

import godot.internal.GODOT_METHOD_RPC_MODE_DISABLED
import godot.internal.godot_instance_method
import godot.internal.godot_method_attributes
import godot.internal.godot_variant
import kotlinx.cinterop.*
import kotlin.reflect.KClass

class BoundMethod(val method: Function<*>,
                  val returnType: KClass<*>,
                  vararg val argumentTypes: KClass<*>) {

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(entity: Object, vararg arguments: Variant): Variant? {
        val typedArgs = argumentTypes.mapIndexed { index, it -> arguments[index].cast(it) }
        return Variant.from(
                when (typedArgs.size) {
                    0 -> (method as Function1<Object, *>).invoke(entity)
                    1 -> (method as Function2<Object, Any?, *>).invoke(entity, typedArgs[0])
                    2 -> (method as Function3<Object, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1])
                    3 -> (method as Function4<Object, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2])
                    4 -> (method as Function5<Object, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3])
                    5 -> (method as Function6<Object, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4])
                    6 -> (method as Function7<Object, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5])
                    7 -> (method as Function8<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6])
                    8 -> (method as Function9<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7])
                    9 -> (method as Function10<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8])
                    10 -> (method as Function11<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9])
                    11 -> (method as Function12<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10])
                    12 -> (method as Function13<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10], typedArgs[11])
                    13 -> (method as Function14<Object, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10], typedArgs[11], typedArgs[12])
                    else -> throw IllegalStateException("Unsupported number of arguments")
                }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun methodWrapper(godotObject: COpaquePointer?,
                           methodData: COpaquePointer?,
                           userData: COpaquePointer?,
                           numArgs: Int,
                           args: CPointer<CPointerVar<godot_variant>>?
): CValue<godot_variant> {
    val obj = userData!!.asStableRef<Object>().get()
    val boundMethod = methodData!!.asStableRef<BoundMethod>().get()
    val arguments: List<Variant> = (0..numArgs).map { Variant.from(args!![it]!!) }
    val result: Variant? = boundMethod(obj, *arguments.toTypedArray())
    return result?._variant?.pointed?.readValue() ?: cValue()
}

internal fun destroyFunctionWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<BoundMethod>().dispose()
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerMethod(className: String, methodName: String, boundMethod: BoundMethod) {
    godot.print("  $className: registering method   $methodName(${boundMethod.argumentTypes.joinToString(", ") { it.simpleName!! }}): ${boundMethod.returnType.simpleName}")
    memScoped {
        val method = cValue<godot_instance_method> {
            method_data = StableRef.create(boundMethod).asCPointer()
            free_func = staticCFunction(::destroyFunctionWrapper)
            method = staticCFunction(::methodWrapper)
        }
        val attr = cValue<godot_method_attributes> {
            rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
        }

        nativescriptApi.godot_nativescript_register_method!!(nativescriptHandle, className.cstr.ptr, methodName.cstr.ptr, attr, method)
    }
}


inline fun <reified T : Object, reified R> registerMethod(methodName: String, noinline method: Function1<T, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class))
}

inline fun <reified T : Object, reified R, reified A1> registerMethod(methodName: String, noinline method: Function2<T, A1, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2> registerMethod(methodName: String, noinline method: Function3<T, A1, A2, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3> registerMethod(methodName: String, noinline method: Function4<T, A1, A2, A3, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4> registerMethod(methodName: String, noinline method: Function5<T, A1, A2, A3, A4, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5> registerMethod(methodName: String, noinline method: Function6<T, A1, A2, A3, A4, A5, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6> registerMethod(methodName: String, noinline method: Function7<T, A1, A2, A3, A4, A5, A6, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7> registerMethod(methodName: String, noinline method: Function8<T, A1, A2, A3, A4, A5, A6, A7, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8> registerMethod(methodName: String, noinline method: Function9<T, A1, A2, A3, A4, A5, A6, A7, A8, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9> registerMethod(methodName: String, noinline method: Function10<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10> registerMethod(methodName: String, noinline method: Function11<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A10::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11> registerMethod(methodName: String, noinline method: Function12<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12> registerMethod(methodName: String, noinline method: Function13<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class))
}

inline fun <reified T : Object, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12, reified A13> registerMethod(methodName: String, noinline method: Function14<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R>) {
    registerMethod(T::class.simpleName!!, methodName, BoundMethod(method, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class, A13::class))
}