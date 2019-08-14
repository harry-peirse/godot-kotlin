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

    fun GMethod?.isSuitableGetter(propertyType: String): Boolean = this != null && !hasVarargs && arguments.isEmpty() && (returnType == propertyType || returnTypeIsEnum && propertyType == "int")
    fun GMethod?.isSuitableSetter(propertyType: String): Boolean = this != null && !hasVarargs && returnType == "void" && arguments.size == 1 && arguments[0].type == propertyType

    fun parse(clazz: GClass, content: List<GClass>): PropertySpec? {
        this.clazz = clazz
        val getterMethod = clazz.methodMap[getter]
        val setterMethod = clazz.methodMap[setter]
        if (getterMethod.isSuitableGetter(type) && setterMethod.isSuitableSetter(type)) {

            val type = getterMethod!!.returnType
            setterMethod!!.arguments[0].type = type

            // XXX: Not being returned yet - decided its best for now as getters have misleading behaviour
            // They always return a different reference, not the same one. Fine for a method, but not for a property
            // Keeping this code running in the meantime as it makes the setters and getters type arguments consistent
            PropertySpec.builder(sanitisedName(), getterMethod.sanitisedReturnType.toClassName().parameterized(), KModifier.OPEN)
                    .mutable(true)
                    .getter(parseGetter(getterMethod))
                    .setter(parseSetter(setterMethod))
                    .apply {
                        var c = clazz
                        loop@ while (c.baseClass.isNotBlank() && c.baseClass != "Variant") {
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
        }
        return null
    }

    fun parseGetter(getterMethod: GMethod): FunSpec = FunSpec.getterBuilder()
            .addStatement("return ${getterMethod.sanitisedName}()")
            .build()

    fun parseSetter(setterMethod: GMethod): FunSpec = FunSpec.setterBuilder()
            .addParameter("value", type.toClassName().parameterized())
            .addStatement("return ${setterMethod.sanitisedName}(value)")
            .build()
}