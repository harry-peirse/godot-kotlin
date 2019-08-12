package godot

import kotlin.reflect.KClass

class WrappedFunction(val function: Function<*>,
                      val returnType: KClass<*>,
                      vararg val argumentTypes: KClass<*>) {

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(entity: Wrapped, vararg arguments: Variant): Variant? {
        val typedArgs = argumentTypes.mapIndexed { index, it -> arguments[index].cast(it) }
        return Variant.from(
                when (typedArgs.size) {
                    0 -> (function as Function1<Wrapped, *>).invoke(entity)
                    1 -> (function as Function2<Wrapped, Any?, *>).invoke(entity, typedArgs[0])
                    2 -> (function as Function3<Wrapped, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1])
                    3 -> (function as Function4<Wrapped, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2])
                    4 -> (function as Function5<Wrapped, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3])
                    5 -> (function as Function6<Wrapped, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4])
                    6 -> (function as Function7<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5])
                    7 -> (function as Function8<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6])
                    8 -> (function as Function9<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7])
                    9 -> (function as Function10<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8])
                    10 -> (function as Function11<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9])
                    11 -> (function as Function12<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10])
                    12 -> (function as Function13<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10], typedArgs[11])
                    13 -> (function as Function14<Wrapped, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, *>).invoke(entity, typedArgs[0], typedArgs[1], typedArgs[2], typedArgs[3], typedArgs[4], typedArgs[5], typedArgs[6], typedArgs[7], typedArgs[8], typedArgs[9], typedArgs[10], typedArgs[11], typedArgs[12])
                    else -> throw IllegalStateException("Unsupported number of arguments")
                }
        )
    }
}