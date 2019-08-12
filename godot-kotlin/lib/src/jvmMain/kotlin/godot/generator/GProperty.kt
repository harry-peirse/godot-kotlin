package godot.generator

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

data class GProperty(val name: String,
                     val type: String,
                     val getter: String,
                     val setter: String,
                     val index: Int) {

    lateinit var clazz: GClass

    fun sanitisedName() = name.sanitisedName()

    fun GMethod?.isSuitableGetter(propertyType: String): Boolean = this != null && !hasVarargs && arguments.isEmpty() && (returnType == propertyType || returnTypeIsEnum() && propertyType == "int")
    fun GMethod?.isSuitableSetter(propertyType: String): Boolean = this != null && !hasVarargs && returnType == "void" && arguments.size == 1 && arguments[0].type == propertyType

    fun parse(clazz: GClass, content: List<GClass>): PropertySpec? {
        this.clazz = clazz
        val getterMethod = clazz.methodMap[getter]
        val setterMethod = clazz.methodMap[setter]
        return if (getterMethod.isSuitableGetter(type) && setterMethod.isSuitableSetter(type)) {

            val type = getterMethod!!.returnType
            setterMethod!!.arguments[0].type = type

            PropertySpec.builder(sanitisedName(), getterMethod.sanitisedReturnType().toClassName(), KModifier.OPEN)
                    .mutable(true)
                    .getter(parseGetter(getterMethod))
                    .setter(parseSetter(setterMethod))
                    .apply {
                        var c = clazz
                        loop@ while (c.baseClass.isNotBlank()) {
                            val bc = content.find { it.name == c.baseClass }!!
                            val m = bc.properties.find { it.name == name && it.type == type }
                            if (m != null) {
                                modifiers.remove(KModifier.OPEN)
                                addModifiers(KModifier.OVERRIDE)
                                break@loop
                            }
                            c = bc
                        }
                    }
                    .build()
        } else null
    }

    fun parseGetter(getterMethod: GMethod): FunSpec = FunSpec.getterBuilder()
            .addStatement("return ${getterMethod.sanitisedName()}()")
            .build()

    fun parseSetter(setterMethod: GMethod): FunSpec = FunSpec.setterBuilder()
            .addParameter("value", type.toClassName())
            .addStatement("return ${setterMethod.sanitisedName()}(value)")
            .build()
}