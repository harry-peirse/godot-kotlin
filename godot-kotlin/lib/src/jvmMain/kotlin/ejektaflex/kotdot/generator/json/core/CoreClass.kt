package ejektaflex.kotdot.generator.json.core

import com.squareup.kotlinpoet.*
import ejektaflex.kotdot.generator.json.reg.CTypeRegistry
import ejektaflex.kotdot.generator.json.reg.CoreClassRegistry

open class CoreClass(val name: String) {

    val ktName: String
        get() = name.substringAfter("godot_").capitalize()

    val methods = mutableListOf<CoreMethod>()

    val normalMethods: List<CoreMethod>
        get() = methods - newClassMethod

    val newClassMethod: CoreMethod
        get() {
            return methods.first { it.name == "${name}_new" }
        }

    val className: ClassName
        get() = ClassName("", name)

    fun generate(): String {
        val file = FileSpec.builder("structure", ktName).apply {
            val newClazz = TypeSpec.classBuilder(ktName).apply {

                addImport("kotlin", "Double", "Boolean")
                addImport("kotlinx.cinterop",
                        "invoke", "nativeHeap", "alloc", "ptr")
                addImport("interop", className.simpleName)
                addImport("", "GDNativeAPI")


                //val value = nativeHeap.alloc<godot_vector2>()
                addProperty(
                        PropertySpec.builder("value", className).apply {
                            initializer("nativeHeap.alloc()")
                        }.build()
                )



                addFunction(newClassMethod.generate(initFunc = true).build())

                for (method in normalMethods) {
                    addFunction(method.generate().build())
                }



            }.build()

            addType(newClazz)
        }.build()


        val output = StringBuilder()

        file.writeTo(output)

        return output.toString()
    }

}