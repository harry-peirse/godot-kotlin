package godot

import godot.internal.*
import kotlinx.cinterop.*

internal const val PLANE_EQ_DOT_EPSILON = 0.999f
internal const val PLANE_EQ_D_EPSILON = 0.0001f
internal const val CMP_EPSILON = 0.00001f
internal const val CMP_EPSILON2 = (CMP_EPSILON * CMP_EPSILON)
internal const val Math_PI = 3.14159265358979323846f

const val GDNATIVE_INIT = "godot_gdnative_init"
const val GDNATIVE_TERMINATE = "godot_gdnative_terminate"
const val NATIVESCRIPT_INIT = "godot_nativescript_init"

typealias GDNativeInitOptions = godot_gdnative_init_options
typealias GDNativeTerminateOptions = godot_gdnative_terminate_options
typealias NativescriptHandle = COpaquePointer

typealias Point2 = Vector2
typealias Size2 = Vector2

lateinit var api: godot_gdnative_core_api_struct
lateinit var gdnlib: CPointer<godot_variant>
lateinit var nativescriptApi: godot_gdnative_ext_nativescript_api_struct
lateinit var nativescript11Api: godot_gdnative_ext_nativescript_1_1_api_struct

internal fun CPointer<godot_string>.toKString(): String = memScoped {
    api.godot_char_string_get_data!!(
            api.godot_string_utf8!!(this@toKString).ptr
    )!!.toKStringFromUtf8()
}

internal fun String.toGString(scope: AutofreeScope): CPointer<godot_string> {
    val _string: CPointer<godot_string> = scope.alloc<godot_string>().ptr
    api.godot_string_new!!(_string)
    memScoped {
        api.godot_string_parse_utf8!!(_string, this@toGString.cstr.ptr)
    }
    return _string
}

internal fun CPointer<godot_dictionary>.toKMutableMap(): MutableMap<Variant, Variant> = memScoped {
    val keys = godot.api.godot_dictionary_keys!!(this@toKMutableMap).ptr.toKArray()
    val map = mutableMapOf<Variant, Variant>()
    keys.forEach {
        map[it] = Variant(godot.api.godot_dictionary_get!!(this@toKMutableMap, it._raw))
    }
    return map
}

internal fun MutableMap<Variant, Variant>.toGDictionary(scope: AutofreeScope): CPointer<godot_dictionary> {
    val _dictionary: CPointer<godot_dictionary> = scope.alloc<godot_dictionary>().ptr
    godot.api.godot_dictionary_new!!(_dictionary)
    forEach { (key, value) ->
        api.godot_dictionary_set!!(_dictionary, key._raw, Variant.of(value)._raw)
    }
    return _dictionary
}

internal fun CPointer<godot_array>.toKArray(): Array<Variant> = memScoped {
    val size = godot.api.godot_array_size!!(this@toKArray)
    return Array(size) {
        Variant(godot.api.godot_array_get!!(this@toKArray, it))
    }
}

internal fun Array<Variant>.toGArray(scope: AutofreeScope): CPointer<godot_array> {
    val _array: CPointer<godot_array> = scope.alloc<godot_array>().ptr
    godot.api.godot_array_new!!(_array)
    api.godot_array_resize!!(_array, size)
    forEachIndexed { index, it ->
        api.godot_array_set!!(_array, index, it._raw)
    }
    return _array
}

fun String.toNodePath(): NodePath = NodePath(this)

fun print(message: Any?) {
    memScoped {
        api.godot_print!!(message.toString().toGString(this))
    }
}

fun printWarning(description: Any?, function: String = "", file: String = "", line: Int = 0) = memScoped {
    godot.api.godot_print_warning!!(description.toString().cstr.ptr, function.cstr.ptr, file.cstr.ptr, line)
}

fun printError(description: Any?, function: String = "", file: String = "", line: Int = 0) = memScoped {
    godot.api.godot_print_error!!(description.toString().cstr.ptr, function.cstr.ptr, file.cstr.ptr, line)
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun gdNativeInit(options: GDNativeInitOptions) {
    api = if (options.api_struct?.pointed != null) options.api_struct?.pointed!! else {
        println("api is null!")
        throw NullPointerException("api is null!")
    }
    gdnlib = if (options.gd_native_library != null) options.gd_native_library?.reinterpret()!! else {
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

@Suppress("UNUSED_PARAMETER")
internal fun wrapperCreate(data: COpaquePointer?, typeTag: COpaquePointer?, instance: COpaquePointer?): COpaquePointer? {
    val variant = Variant(typeTag!!.reinterpret())
    val producer = tagDB.producers[variant.toUInt()]
    val obj = producer?.invoke()
    obj?._raw = instance!!.reinterpret()
    return obj?._stableRef?.asCPointer()
}

@Suppress("UNUSED_PARAMETER")
internal fun wrapperDestroy(data: COpaquePointer?, wrapper: COpaquePointer?) {
    wrapper?.asStableRef<Object>()?.dispose()
}

fun nativeScriptInit(handle: NativescriptHandle) {
    nativescriptHandle = handle

    languageIndex = nativescript11Api.godot_nativescript_register_instance_binding_data_functions!!(cValue {
        alloc_instance_binding_data = staticCFunction(::wrapperCreate)
        free_instance_binding_data = staticCFunction(::wrapperDestroy)
    })

    _registerTypes()
    _initMethodBindings()
}

internal lateinit var nativescriptHandle: COpaquePointer
internal var languageIndex: Int = -1