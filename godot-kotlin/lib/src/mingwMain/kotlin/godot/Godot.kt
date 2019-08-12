package godot

import godot.internal.*
import kotlinx.cinterop.*
import platform.posix.memset
import kotlin.reflect.KMutableProperty1

const val GDNATIVE_INIT = "godot_gdnative_init"
const val GDNATIVE_TERMINATE = "godot_gdnative_terminate"
const val NATIVESCRIPT_INIT = "godot_nativescript_init"

typealias GDNativeInitOptions = godot_gdnative_init_options
typealias GDNativeTerminateOptions = godot_gdnative_terminate_options
typealias NativescriptHandle = COpaquePointer

internal fun String.toGodotString(): CPointer<godot_string> = memScoped {
    val godotString: CPointer<godot_string> = godot.alloc(godot_string.size)
    godot.api.godot_string_new!!(godotString)
    godot.api.godot_string_parse_utf8!!(godotString, this@toGodotString.cstr.ptr)
    return godotString
}

internal fun CValue<godot_string>.toKotlinString(): String = memScoped {
    val godotCharString = godot.api.godot_string_utf8!!(this@toKotlinString.ptr)
    godot.api.godot_char_string_get_data!!(godotCharString.ptr)!!.toKStringFromUtf8()
}

internal fun _constructor(instance: COpaquePointer?, methodData: COpaquePointer?): COpaquePointer? {
    val godotClass = methodData!!.asStableRef<GodotClass>().get()
    val wrapped = godot.api.godot_alloc!!(_Wrapped.size.toInt())!!.reinterpret<_Wrapped>().pointed
    wrapped._owner = instance
    wrapped._typeTag = godotClass.getTypeTag()
    val newInstance = godotClass.new()
    newInstance._wrapped = wrapped.ptr
    return StableRef.create(newInstance).asCPointer()
}

@Suppress("UNUSED_PARAMETER")
internal fun _destructor(instance: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?) {
    val wrapped = userData?.asStableRef<Wrapped>()?.get()?._wrapped
    godot.api.godot_free!!(wrapped)
}

@Suppress("UNUSED_PARAMETER")
internal fun functionWrapper(godotObject: COpaquePointer?,
                             methodData: COpaquePointer?,
                             userData: COpaquePointer?,
                             numArgs: Int,
                             args: CPointer<CPointerVar<godot_variant>>?
): CValue<godot_variant> {
    val entity = userData!!.asStableRef<Wrapped>().get()
    val wrapper = methodData!!.asStableRef<WrappedFunction>().get()
    val arguments: List<Variant> = (0..numArgs).map { Variant(args!![it]!!) }
    val result: Variant? = wrapper(entity, *arguments.toTypedArray())
    return result?._wrapped?.pointed?.readValue() ?: cValue()
}

internal fun destroyFunctionWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<WrappedFunction>().dispose()
}

@Suppress("UNUSED_PARAMETER")
internal fun getterWrapper(godotObject: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?): CValue<godot_variant> {
    val entity = userData!!.asStableRef<Wrapped>().get()
    val wrapper = methodData!!.asStableRef<WrappedProperty>().get()
    return wrapper.getter(entity)?._wrapped?.pointed?.readValue() ?: cValue()
}

@Suppress("UNUSED_PARAMETER")
internal fun setterWrapper(godotObject: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?, value: CPointer<godot_variant>?) {
    val entity = userData!!.asStableRef<Wrapped>().get()
    val wrapper = methodData!!.asStableRef<WrappedProperty>().get()
    wrapper.setter(entity, Variant(value!!))
}

internal fun destroySetterWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<WrappedProperty>().dispose()
}

internal fun destroyGetterWrapper(methodData: COpaquePointer?) {
    methodData!!.asStableRef<WrappedProperty>().dispose()
}

internal fun wrapperCreate(data: COpaquePointer?, typeTag: COpaquePointer?, instance: COpaquePointer?): COpaquePointer? {
    godot.print("wrapperCreate data: $data, typeTag: $typeTag, instance: $instance")
    val wrapperMemory: CPointer<_Wrapped> = godot.api.godot_alloc!!(_Wrapped.size.toInt())?.reinterpret()
            ?: return null
    wrapperMemory.pointed._owner = instance
    wrapperMemory.pointed._typeTag = typeTag?.reinterpret<UIntVar>()?.pointed?.value!!

    return wrapperMemory
}

internal fun wrapperDestroy(data: COpaquePointer?, wrapper: COpaquePointer?) {
    godot.print("wrapperDestroy data: $data, wrapper: $wrapper")
    if (wrapper != null) godot.api.godot_free!!(wrapper)
}

internal val tagDB = TagDB()

lateinit var api: godot_gdnative_core_api_struct
lateinit var gdnlib: COpaquePointer
lateinit var nativescriptApi: godot_gdnative_ext_nativescript_api_struct
lateinit var nativescript11Api: godot_gdnative_ext_nativescript_1_1_api_struct

fun print(message: Any?) {
    GodotString(message.toString()) {
        api.godot_print!!(_wrapped)
    }
}

fun printWarning(description: String, function: String, file: String, line: Int) = memScoped {
    godot.api.godot_print_warning!!(description.cstr.ptr, function.cstr.ptr, file.cstr.ptr, line)
}

fun printError(description: String, function: String, file: String, line: Int) = memScoped {
    godot.api.godot_print_error!!(description.cstr.ptr, function.cstr.ptr, file.cstr.ptr, line)
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun gdNativeInit(options: GDNativeInitOptions) {
    api = if (options.api_struct?.pointed != null) options.api_struct?.pointed!! else {
        println("api is null!")
        throw NullPointerException("api is null!")
    }
    gdnlib = if (options.gd_native_library != null) options.gd_native_library!! else {
        println("gdnlib is null!")
        throw NullPointerException("gdnlib is null!")
    }

    for (i in 0 until api.num_extensions.toInt()) {
        when (api.extensions?.get(i)?.pointed?.type) {
            GDNATIVE_API_TYPES.GDNATIVE_EXT_NATIVESCRIPT.value -> {
                val temp = api.extensions?.get(i)?.reinterpret<godot_gdnative_ext_nativescript_api_struct>()?.pointed
                nativescriptApi = if (temp != null) temp else {
                    println("nativescriptApi is null!")
                    throw NullPointerException("nativescriptApi is null!")
                }
                var extension: CPointer<godot_gdnative_api_struct>? = nativescriptApi.next
                while (extension != null) {
                    if (extension.pointed.version.major == 1u && extension.pointed.version.minor == 1u) {
                        nativescript11Api = extension.reinterpret<godot_gdnative_ext_nativescript_1_1_api_struct>().pointed
                    }
                    extension = extension.pointed.next
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
fun gdNativeTerminate(options: GDNativeTerminateOptions) = Unit

@UseExperimental(ExperimentalUnsignedTypes::class)
fun gdnativeProfilingAddData(signature: String, time: ULong) {
    memScoped {
        if (nativescript11Api.godot_nativescript_profiling_add_data != null) {
            nativescript11Api.godot_nativescript_profiling_add_data!!(signature.cstr.ptr, time)
        } else {
            println("nativescript11Api.godot_nativescript_profiling_add_data is null!")
            throw NullPointerException("nativescript11Api.godot_nativescript_profiling_add_data is null!")
        }
    }
}

@Suppress("UNUSED_PARAMETER")
fun nativescriptTerminate(handle: NativescriptHandle) {
    if (nativescript11Api.godot_nativescript_unregister_instance_binding_data_functions != null) {
        nativescript11Api.godot_nativescript_unregister_instance_binding_data_functions!!(languageIndex)
    } else {
        println("nativescript11Api.godot_nativescript_unregister_instance_binding_data_functions is null!")
        throw NullPointerException("nativescript11Api.godot_nativescript_unregister_instance_binding_data_functions is null!")
    }
}

fun nativeScriptInit(handle: NativescriptHandle) {
    nativescriptHandle = handle

    memScoped {
        languageIndex = nativescript11Api.godot_nativescript_register_instance_binding_data_functions!!(cValue {
            alloc_instance_binding_data = staticCFunction(::wrapperCreate)
            free_instance_binding_data = staticCFunction(::wrapperDestroy)
        })

        _registerTypes()
        _initMethodBindings()
    }
}

internal lateinit var nativescriptHandle: COpaquePointer
internal var languageIndex: Int = -1

fun registerClass(clazz: GodotClass) {
    memScoped {
        val create = cValue<godot_instance_create_func> {
            create_func = staticCFunction(::_constructor)
            method_data = StableRef.create(clazz).asCPointer()
        }
        val destroy = cValue<godot_instance_destroy_func> {
            destroy_func = staticCFunction(::_destructor)
            method_data = StableRef.create(clazz).asCPointer()
        }

        print("Registering class ${clazz.getTypeName()} : ${clazz.getBaseTypeName()}")
        tagDB.registerType(clazz.getTypeTag(), clazz.getBaseTypeTag())

        nativescriptApi.godot_nativescript_register_class!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, clazz.getBaseTypeName().cstr.ptr, create, destroy)
        nativescript11Api.godot_nativescript_set_type_tag!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, alloc<UIntVar> { value = clazz.getTypeTag() }.ptr)
        clazz.registerMethods()
    }
}

inline fun <reified T : Wrapped> registerSignal(signalName: String, vararg arguments: Pair<String, godot_variant_type>) {
    registerSignal(T::class.simpleName!!, signalName, *arguments)
}

inline fun <reified T : Wrapped, reified A1 : Any> registerProperty(propertyName: String, property: KMutableProperty1<T, A1>, defaultValue: A1) {
    registerProperty(T::class.simpleName!!, propertyName, defaultValue, WrappedProperty(property, A1::class))
}

inline fun <reified T : Wrapped, reified R> registerMethod(functionName: String, noinline function: Function1<T, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class))
}

inline fun <reified T : Wrapped, reified R, reified A1> registerMethod(functionName: String, noinline function: Function2<T, A1, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2> registerMethod(functionName: String, noinline function: Function3<T, A1, A2, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3> registerMethod(functionName: String, noinline function: Function4<T, A1, A2, A3, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4> registerMethod(functionName: String, noinline function: Function5<T, A1, A2, A3, A4, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5> registerMethod(functionName: String, noinline function: Function6<T, A1, A2, A3, A4, A5, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6> registerMethod(functionName: String, noinline function: Function7<T, A1, A2, A3, A4, A5, A6, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7> registerMethod(functionName: String, noinline function: Function8<T, A1, A2, A3, A4, A5, A6, A7, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8> registerMethod(functionName: String, noinline function: Function9<T, A1, A2, A3, A4, A5, A6, A7, A8, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9> registerMethod(functionName: String, noinline function: Function10<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10> registerMethod(functionName: String, noinline function: Function11<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A10::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11> registerMethod(functionName: String, noinline function: Function12<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12> registerMethod(functionName: String, noinline function: Function13<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class))
}

inline fun <reified T : Wrapped, reified R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12, reified A13> registerMethod(functionName: String, noinline function: Function14<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, R::class, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class, A13::class))
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerMethod(className: String, functionName: String, wrappedFunction: WrappedFunction) {
    godot.print("  $className: registering method   $functionName(${wrappedFunction.argumentTypes.joinToString(", ") { it.simpleName!! }}): ${wrappedFunction.returnType.simpleName}")
    memScoped {
        val methodName = functionName.cstr.ptr
        val method = cValue<godot_instance_method> {
            method_data = StableRef.create(wrappedFunction).asCPointer()
            free_func = staticCFunction(::destroyFunctionWrapper)
            method = staticCFunction(::functionWrapper)
        }
        val attr = cValue<godot_method_attributes> {
            rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
        }

        nativescriptApi.godot_nativescript_register_method!!(nativescriptHandle, className.cstr.ptr, methodName, attr, method)
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerProperty(className: String, propertyName: String, defaultValue: Any?, wrappedProperty: WrappedProperty) {
    godot.print("  $className: registering property $propertyName: ${wrappedProperty.type.simpleName}")
    memScoped {
        val getter = cValue<godot_property_get_func> {
            method_data = StableRef.create(wrappedProperty).asCPointer()
            free_func = staticCFunction(::destroyGetterWrapper)
            get_func = staticCFunction(::getterWrapper)
        }
        val setter = cValue<godot_property_set_func> {
            method_data = StableRef.create(wrappedProperty).asCPointer()
            free_func = staticCFunction(::destroySetterWrapper)
            set_func = staticCFunction(::setterWrapper)
        }

        val variant = Variant.from(defaultValue)

        val attr = cValue<godot_property_attributes> {
            type = (variant?.getType() ?: godot_variant_type.GODOT_VARIANT_TYPE_OBJECT).value.toInt()
            if (variant != null) godot.api.godot_variant_new_copy!!(default_value.ptr, variant._wrapped)
            hint = godot_property_hint.GODOT_PROPERTY_HINT_NONE
            rset_type = GODOT_METHOD_RPC_MODE_DISABLED
            usage = GODOT_PROPERTY_USAGE_DEFAULT
            godot.api.godot_string_parse_utf8!!(hint_string.ptr, "".cstr.ptr)
        }
        nativescriptApi.godot_nativescript_register_property!!(nativescriptHandle, className.cstr.ptr, propertyName.cstr.ptr, attr.ptr, setter, getter)
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerSignal(className: String, signalName: String, vararg arguments: Pair<String, godot_variant_type>) {
    godot.print("  $className: registering signal   $signalName(${arguments.joinToString(", ") { it.first }})")
    memScoped {
        val signal: godot_signal = alloc()
        godot.api.godot_string_parse_utf8!!(signal.name.ptr, signalName.cstr.ptr)
        signal.num_args = arguments.size
        signal.num_default_args = 0

        if (signal.num_args != 0) {
            signal.args = godot.alloc(godot_signal_argument.size * signal.num_args)
            memset(signal.args, 0, (godot_signal_argument.size * signal.num_args).toULong())
        }

        (0 until signal.num_args).forEach { i ->
            val name: String = arguments[i].first
            val _key: CPointer<godot_string> = name.toGodotString()
            godot.api.godot_string_new_copy!!(signal.args!![i].name.ptr, _key)
            signal.args!![i].type = arguments[i].second.value.toInt()
        }

        godot.nativescriptApi.godot_nativescript_register_signal!!(godot.nativescriptHandle, className.cstr.ptr, signal.ptr)

        (0 until signal.num_args).forEach { i ->
            godot.api.godot_string_destroy!!(signal.args!![i].name.ptr)
        }

        if (signal.args != null) {
            godot.api.godot_free!!(signal.args)
        }
    }
}

internal inline fun <reified T : CStructVar> alloc(size: Long): CPointer<T> {
    return godot.api.godot_alloc!!(size.toInt())!!.reinterpret()
}
