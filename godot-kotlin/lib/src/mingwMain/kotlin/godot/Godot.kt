package godot

import godot.internal.*
import kotlinx.cinterop.*

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

internal fun _destructor(instance: COpaquePointer?, methodData: COpaquePointer?, userData: COpaquePointer?) {
    val godotClass = methodData!!.asStableRef<GodotClass>().get()
    val wrapped = userData?.asStableRef<Wrapped>()?.get()?._wrapped
    godot.api.godot_free!!(wrapped)
}

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

fun print(message: Any?) = memScoped {
    val string = message.toString()
    val data: CPointer<godot_string> = api.godot_alloc!!(string.length)!!.reinterpret()
    api.godot_string_new!!(data)
    api.godot_string_parse_utf8!!(data, string.cstr.ptr)
    api.godot_print!!(data)
    api.godot_string_destroy!!(data)
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

        print("registering class ${clazz.getTypeName()} : ${clazz.getBaseTypeName()}, with tag ${clazz.getTypeTag()} : ${clazz.getBaseTypeTag()}")
        tagDB.registerType(clazz.getTypeTag(), clazz.getBaseTypeTag())

        nativescriptApi.godot_nativescript_register_class!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, clazz.getBaseTypeName().cstr.ptr, create, destroy)
        nativescript11Api.godot_nativescript_set_type_tag!!(nativescriptHandle, clazz.getTypeName().cstr.ptr, alloc<UIntVar> { value = clazz.getTypeTag() }.ptr)
        clazz.registerMethods()
    }
}

inline fun <reified T : Wrapped> registerMethod(functionName: String, noinline function: Function1<T, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function))
}

inline fun <reified T : Wrapped, reified A1> registerMethod(functionName: String, noinline function: Function2<T, A1, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2> registerMethod(functionName: String, noinline function: Function3<T, A1, A2, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3> registerMethod(functionName: String, noinline function: Function4<T, A1, A2, A3, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4> registerMethod(functionName: String, noinline function: Function5<T, A1, A2, A3, A4, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5> registerMethod(functionName: String, noinline function: Function6<T, A1, A2, A3, A4, A5, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6> registerMethod(functionName: String, noinline function: Function7<T, A1, A2, A3, A4, A5, A6, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7> registerMethod(functionName: String, noinline function: Function8<T, A1, A2, A3, A4, A5, A6, A7, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8> registerMethod(functionName: String, noinline function: Function9<T, A1, A2, A3, A4, A5, A6, A7, A8, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9> registerMethod(functionName: String, noinline function: Function10<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10> registerMethod(functionName: String, noinline function: Function11<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A10::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11> registerMethod(functionName: String, noinline function: Function12<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12> registerMethod(functionName: String, noinline function: Function13<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class))
}

inline fun <reified T : Wrapped, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10, reified A11, reified A12, reified A13> registerMethod(functionName: String, noinline function: Function14<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, *>) {
    registerMethod(T::class.simpleName!!, functionName, WrappedFunction(function, A1::class, A2::class, A3::class, A4::class, A5::class, A6::class, A7::class, A8::class, A9::class, A10::class, A11::class, A12::class, A13::class))
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerMethod(className: String, functionName: String, wrappedFunction: WrappedFunction) {
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

internal inline fun <reified T : CStructVar> alloc(size: Long): CPointer<T> {
    return godot.api.godot_alloc!!(size.toInt())!!.reinterpret()
}
