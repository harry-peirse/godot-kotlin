package godot

import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlin.reflect.KClass

class WrappedFunction<T : Wrapped>(val type: KClass<T>, val function: Function<Variant?>) {
    operator fun invoke(entity: T, vararg arguments: Variant): Variant? {
        return when(arguments.size - 1) {
            -1, 0 -> (function as Function1<T, Variant?>).invoke(entity)
            1 -> (function as Function2<T, Variant, Variant?>).invoke(entity, arguments[0])
            2 -> (function as Function3<T, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1])
            3 -> (function as Function4<T, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2])
            4 -> (function as Function5<T, Variant, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2], arguments[3])
            5 -> (function as Function6<T, Variant, Variant, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4])
            6 -> (function as Function7<T, Variant, Variant, Variant, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5])
            7 -> (function as Function8<T, Variant, Variant, Variant, Variant, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5], arguments[6])
            8 -> (function as Function9<T, Variant, Variant, Variant, Variant, Variant, Variant, Variant, Variant, Variant?>).invoke(entity, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5], arguments[6], arguments[7])
            else -> throw IllegalStateException("Unsupported number of arguments")
        }
    }
}