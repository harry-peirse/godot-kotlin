package ejektaflex.kotdot.generator.json.structure

import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.*
import ejektaflex.kotdot.generator.json.NativeCommon

data class GodotClass(

        val name: String = "UNDEF_NAME",

        @SerializedName("base_class")
        val baseClass: String = "GodotAny",

        val singleton: Boolean = true,

        val instantiable: Boolean = true,

        @SerializedName("is_reference")
        val isReference: Boolean = true,

        val constants: Map<String, Int> = mapOf(),

        val methods: MutableList<GodotMethod>,

        val properties: MutableList<GodotProperty>,

        var baseClassGodot: GodotClass? = null

) {

    /*
    Returns a list of all superclasses, including the current class
     */
    val superclasses: List<GodotClass>
        get() {
            return listOf(this) + if (baseClassGodot == null) {
                listOf()
            } else {
                baseClassGodot!!.superclasses
            }
        }

    val essentialMethods: List<GodotMethod>
        get() {
            return methods.filter { method ->
                method.name !in properties.map { it.getter }
                        && method.name !in properties.map { it.setter }
            }
        }

    fun generate(document: Boolean = true): String {


        val file = FileSpec.builder("godot", name).apply {
            val newClazz = TypeSpec.classBuilder(name).apply {

                addImport("kotlinx.cinterop", "BooleanVar", "ByteVar", "COpaquePointerVar", "memScoped", "allocArray")
                addImport("godotapi", "godot_method_bind_ptrcall")

                // KDoc

                if (document) {
                    addKdoc("@see·" + NativeCommon.classUrl(this@GodotClass))
                }

                // Header / Primary Constructor

                addModifiers(KModifier.OPEN)

                baseClassGodot?.let {
                    superclass(ClassName("godot", it.name))
                }

                /*
                if (instantiable) {
                    addFunction(
                        FunSpec.constructorBuilder()
                            .callSuperConstructor("\"$name\"")
                            .build()
                    )
                } else {
                    addFunction(
                        FunSpec.constructorBuilder()
                            .addModifiers(KModifier.PRIVATE)
                            .callSuperConstructor("\"\"")
                            .build()
                    )
                }
                 */

                addFunction(
                    FunSpec.constructorBuilder()
                        .addModifiers(KModifier.INTERNAL)
                        .addParameter("memory", NativeCommon.copaque)
                        .callSuperConstructor("memory")
                        .build()
                )

                /*
                addFunction(
                    FunSpec.constructorBuilder()
                        .addModifiers(KModifier.INTERNAL)
                        .addParameter("name", String::class)
                        .callSuperConstructor("name")
                        .build()
                )
                 */

                // Companion Object

                val companion = TypeSpec.companionObjectBuilder()



                // Body


                // Add all essential methods to class
                for (essMethod in essentialMethods) {
                    addFunction(
                            essMethod.generate(document).build()
                    )
                }


                // Add binding properties to Companion object
                for (method in methods) {
                    companion.addProperty(
                            PropertySpec.builder(
                                    "bind_${method.name}",
                                    NativeCommon.godotBind
                            ).apply {
                                addModifiers(KModifier.PRIVATE)
                                initializer("BindMap[\"${method.bindingName}\"]")
                            }.build()
                    )
                }

                // Add all properties to class
                for (property in properties) {
                    addProperty(
                            property.generate().build()
                    )
                }

                // Add Companion Object
                addType(companion.build())


                // Constants (should be done but get in the way a lot, so are commented out for now)

                /*
                for (constant in constants) {
                    addProperty(
                        PropertySpec.builder(constant.key, Long::class, KModifier.CONST).initializer(constant.value.toString()).build()
                    )
                }
                 */




            }.build()

            addType(newClazz)
        }.build()


        val output = StringBuilder()

        file.writeTo(output)

        return output.toString()

    }

}