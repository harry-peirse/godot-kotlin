package godot

import godot.internal.*
import kotlinx.cinterop.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

class BoundProperty(val property: KMutableProperty1<out Object, *>,
                    val type: KClass<out Any>) {

    @Suppress("UNCHECKED_CAST")
    fun setter(entity: Object, value: Variant) {
        (property as KMutableProperty1<Object, Any>).set(entity, value.to(type))
    }

    @Suppress("UNCHECKED_CAST")
    fun getter(entity: Object): Variant? {
        return Variant.of((property as KMutableProperty1<Object, Any>).get(entity))
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getterWrapper(godotObject: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?): CValue<godot_variant> {
    godot.print("getterWrapper")
    val obj = userData!!.asStableRef<Object>().get()
    val wrapper = methodData!!.asStableRef<BoundProperty>().get()
    return wrapper.getter(obj)?._raw?.pointed?.readValue() ?: cValue()
}

@Suppress("UNUSED_PARAMETER")
internal fun setterWrapper(godotObject: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?, value: CPointer<godot_variant>?) {
    godot.print("setterWrapper")
    val obj = userData!!.asStableRef<Object>().get()
    val wrapper = methodData!!.asStableRef<BoundProperty>().get()
    wrapper.setter(obj, Variant(value!!))
}

internal fun destroySetterWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<BoundProperty>().dispose()
}

internal fun destroyGetterWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<BoundProperty>().dispose()
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerProperty(className: String, propertyName: String, defaultValue: Any?, boundProperty: BoundProperty) {
    godot.print("  $className: registering property $propertyName: ${boundProperty.type.simpleName}")
    memScoped {
        val getter = cValue<godot_property_get_func> {
            method_data = StableRef.create(boundProperty).asCPointer()
            free_func = staticCFunction(::destroyGetterWrapper)
            get_func = staticCFunction(::getterWrapper)
        }
        val setter = cValue<godot_property_set_func> {
            method_data = StableRef.create(boundProperty).asCPointer()
            free_func = staticCFunction(::destroySetterWrapper)
            set_func = staticCFunction(::setterWrapper)
        }

        val variant = Variant.of(defaultValue)

        val attr = cValue<godot_property_attributes> {
            type = (variant?.getType() ?: Variant.Type.NIL).ordinal
            if (variant != null) godot.api.godot_variant_new_copy!!(default_value.ptr, variant._raw)
            hint = godot_property_hint.GODOT_PROPERTY_HINT_NONE
            rset_type = GODOT_METHOD_RPC_MODE_DISABLED
            usage = GODOT_PROPERTY_USAGE_DEFAULT
            godot.api.godot_string_parse_utf8!!(hint_string.ptr, "".cstr.ptr)
        }
        nativescriptApi.godot_nativescript_register_property!!(nativescriptHandle, className.cstr.ptr, propertyName.cstr.ptr, attr.ptr, setter, getter)
    }
}

inline fun <reified T : Object, reified A1 : Any> registerProperty(propertyName: String, property: KMutableProperty1<T, A1>, defaultValue: A1) {
    registerProperty(T::class.simpleName!!, propertyName, defaultValue, BoundProperty(property, A1::class))
}
