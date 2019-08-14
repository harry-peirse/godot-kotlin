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
        val typedArgs = argumentTypes.mapIndexed { index, it -> arguments[index].to(it) }
        val result = when (typedArgs.size) {
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
            else -> null
        }!!
        return if (result != Unit) Variant.of(result) else null
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
    val arguments: List<Variant> = (0 until numArgs).map { Variant(args!![it]!!) }
    val result: Variant? = boundMethod(obj, *arguments.toTypedArray())
    val ret = result?._raw?.pointed?.readValue() ?: cValue()
    return ret
}

internal fun destroyMethodWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<BoundMethod>().dispose()
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerMethod(className: String, methodName: String, boundMethod: BoundMethod) {
    godot.print("  $className: registering method   $methodName(${boundMethod.argumentTypes.joinToString(", ") { it.simpleName!! }}): ${boundMethod.returnType.simpleName}")
    memScoped {
        val method = cValue<godot_instance_method> {
            method_data = StableRef.create(boundMethod).asCPointer()
            free_func = staticCFunction(::destroyMethodWrapper)
            method = staticCFunction(::methodWrapper)
        }
        val attr = cValue<godot_method_attributes> {
            rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
        }

        nativescriptApi.godot_nativescript_register_method!!(nativescriptHandle, className.cstr.ptr, methodName.cstr.ptr, attr, method)
    }
}