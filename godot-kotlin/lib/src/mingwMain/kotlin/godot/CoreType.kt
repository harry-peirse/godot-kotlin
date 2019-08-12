package godot

internal interface CoreType<T : CPointed> {
    internal val _wrapped : CPointer<T>
}