package godot

import godot.internal.godot_signal
import godot.internal.godot_signal_argument
import kotlinx.cinterop.*
import platform.posix.memset

inline fun <reified T : Object> registerSignal(signalName: String, vararg arguments: Pair<String, Variant.Type>) {
    registerSignal(T::class.simpleName!!, signalName, *arguments)
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun registerSignal(className: String, signalName: String, vararg arguments: Pair<String, Variant.Type>) {
    godot.print("  $className: registering signal   $signalName(${arguments.joinToString(", ") { it.first }})")
    memScoped {
        val signal: godot_signal = alloc()
        godot.api.godot_string_parse_utf8!!(signal.name.ptr, signalName.cstr.ptr)
        signal.num_args = arguments.size
        signal.num_default_args = 0

        if (signal.num_args != 0) {
            signal.args = godotAlloc(godot_signal_argument.size * signal.num_args)
            memset(signal.args, 0, (godot_signal_argument.size * signal.num_args).toULong())
        }

        (0 until signal.num_args).forEach { i ->
            val name = arguments[i].first.toGString(this)
            godot.api.godot_string_new_copy!!(signal.args!![i].name.ptr, name)
            signal.args!![i].type = arguments[i].second.ordinal
        }

        godot.nativescriptApi.godot_nativescript_register_signal!!(nativescriptHandle, className.cstr.ptr, signal.ptr)

        (0 until signal.num_args).forEach { i ->
            godot.api.godot_string_destroy!!(signal.args!![i].name.ptr)
        }
    }
}