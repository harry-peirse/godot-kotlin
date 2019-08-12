package godot

import godot.internal.godot_dictionary
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotDictionary : CoreType<godot_dictionary> {
    internal constructor (_wrapped: CPointer<godot_dictionary>) : super(_wrapped)
    internal constructor(value: CValue<godot_dictionary>) : super(value.place(godot.alloc(godot_dictionary.size)))
    internal constructor() : this(godot.alloc(godot_dictionary.size))
}