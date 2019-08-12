package godot

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

class WrappedProperty(val property: KMutableProperty1<*, *>,
                      val type: KClass<out Any>) {

    @Suppress("UNCHECKED_CAST")
    fun setter(entity: Wrapped, value: Variant) {
        (property as KMutableProperty1<Wrapped, Any>).set(entity, value.cast(type))
    }

    @Suppress("UNCHECKED_CAST")
    fun getter(entity: Wrapped): Variant? {
        return Variant.from((property as KMutableProperty1<Wrapped, Any>).get(entity))
    }
}