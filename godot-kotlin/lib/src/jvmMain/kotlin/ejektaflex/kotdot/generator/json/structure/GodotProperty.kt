package ejektaflex.kotdot.generator.json.structure

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import ejektaflex.kotdot.generator.json.NativeCommon
import ejektaflex.kotdot.generator.json.reg.TypeRegistry

data class GodotProperty(
        val name: String,
        val type: String,
        val getter: String,
        val setter: String,
        val index: Int
) {

    lateinit var parentClass: GodotClass

    val isEssentialGetter: Boolean
        get() = getter in parentClass.methods.map { it.name }

    val getterMethod: GodotMethod?
        get() = parentClass.methods.find { it.name == getter }


    val isEssentialSetter: Boolean
        get() = setter in parentClass.methods.map { it.name }

    val setterMethod: GodotMethod?
        get() = parentClass.methods.find { it.name == setter }


    fun generate(document: Boolean = true): PropertySpec.Builder {

        return PropertySpec.builder(name, TypeRegistry.lookup(type)).apply {
            addModifiers(KModifier.OPEN)
            mutable()

            if (document) {
                addKdoc("@see·[property](${NativeCommon.propertyUrl(parentClass, this@GodotProperty)})")
            }

            if (isEssentialGetter) {
                getter(FunSpec.getterBuilder().apply {
                    //addKdoc("\n@see·[getter](${NativeCommon.methodUrl(parentClass, getterMethod!!)})")
                    getterMethod!!.genPtrCall(this)
                }.build()).build()
            }

            if (isEssentialSetter) {
                setter(FunSpec.setterBuilder().apply {
                    //addKdoc("@see·[setter](${NativeCommon.methodUrl(parentClass, setterMethod!!)})")
                    val setterArg = setterMethod!!.arguments.first()
                    addParameter("value", TypeRegistry.lookup(setterArg.type))
                    //addComment("Godot Setter: ${setterMethod!!.name}")
                    setterMethod!!.genPtrCall(this, "value")
                }.build()).build()
            }


        }

    }

}