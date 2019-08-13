package godot

import godot.internal.*
import kotlinx.cinterop.*

const val GDNATIVE_INIT = "godot_gdnative_init"
const val GDNATIVE_TERMINATE = "godot_gdnative_terminate"
const val NATIVESCRIPT_INIT = "godot_nativescript_init"

typealias GDNativeInitOptions = godot_gdnative_init_options
typealias GDNativeTerminateOptions = godot_gdnative_terminate_options
typealias NativescriptHandle = COpaquePointer

lateinit var api: godot_gdnative_core_api_struct
lateinit var gdnlib: CPointer<godot_variant>
lateinit var nativescriptApi: godot_gdnative_ext_nativescript_api_struct
lateinit var nativescript11Api: godot_gdnative_ext_nativescript_1_1_api_struct

fun print(message: Any?) {
    memScoped {
        api.godot_print!!(godot.api.godot_string_chars_to_utf8!!(message.toString().cstr.ptr).ptr)
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
    val variant = Variant(instance!!.reinterpret())
    val obj: Object = variant.toObject(Variant(typeTag!!.reinterpret()).toUInt())
    obj._init()
    return obj._stableRef.asCPointer()
}

@Suppress("UNUSED_PARAMETER")
internal fun wrapperDestroy(data: COpaquePointer?, wrapper: COpaquePointer?) {
    wrapper?.asStableRef<Object>()?.dispose()
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

internal inline fun <reified T : CStructVar> alloc(size: Long): CPointer<T> {
    return godot.api.godot_alloc!!(size.toInt())!!.reinterpret()
}