package godot

import godot.internal.godot_signal
import godot.internal.godot_signal_argument
import godot.internal.godot_string
import godot.internal.godot_variant_type
import kotlinx.cinterop.*
import platform.posix.memset

inline fun <reified T : BoundClass> registerSignal(signalName: String, vararg arguments: Pair<String, godot_variant_type>) {
    registerSignal(T::class.simpleName!!, signalName, *arguments)
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