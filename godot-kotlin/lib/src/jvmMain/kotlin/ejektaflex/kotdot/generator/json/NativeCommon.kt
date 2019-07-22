package ejektaflex.kotdot.generator.json

import com.squareup.kotlinpoet.ClassName
import ejektaflex.kotdot.generator.json.structure.GodotClass
import ejektaflex.kotdot.generator.json.structure.GodotMethod
import ejektaflex.kotdot.generator.json.structure.GodotProperty

object NativeCommon {

    val godotVersion = "3.1"
    val language = "en"

    fun propertyUrl(clazz: GodotClass, prop: GodotProperty): String {
        val clazzName = clazz.name.toLowerCase().replace("_", "-")
        val propName = prop.name.replace("_", "-")
        return "https://docs.godotengine.org/$language/$godotVersion/classes/" +
                "class_${clazz.name.toLowerCase()}.html#class-$clazzName-property-$propName"
    }

    fun classUrl(clazz: GodotClass): String {
        val clazzName = clazz.name.toLowerCase().replace("_", "-")
        return "https://docs.godotengine.org/$language/$godotVersion/classes/" +
                "class_${clazz.name.toLowerCase()}.html"
    }

    fun methodUrl(clazz: GodotClass, method: GodotMethod): String {
        val clazzName = clazz.name.toLowerCase().replace("_", "-")
        val methodName = method.name.replace("_", "-")
        return "https://docs.godotengine.org/$language/$godotVersion/classes/" +
                "class_${clazz.name.toLowerCase()}.html#class-$clazzName-method-$methodName"
    }

    private val godot = "structure"
    private val cinterop = "kotlinx.cinterop"

    val variant = ClassName(godot, "Variant")
    val copaque = ClassName(cinterop, "COpaquePointer")
    val cpointer = ClassName(cinterop, "CPointer")
    val copaquevar = ClassName(cinterop, "COpaquePointerVar")
    val godotBind = ClassName(godot, "CPointer<godot_method_bind>")
    val memscope = ClassName(cinterop, "MemScope")

    fun cPointerArray(contentClass: String): ClassName {
        return ClassName(cinterop, "CArrayPointer<$contentClass>")
    }

    fun cPointerArray(className: ClassName): ClassName {
        return ClassName(cinterop, "CArrayPointer<${className.simpleName}>")
    }


    val bindMap = ClassName(godot, "BindMap")
}