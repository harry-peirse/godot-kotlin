package godot.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

val _Int = ClassName("kotlin", "Int")
val _UInt = ClassName("kotlin", "UInt")
val _Float = ClassName("kotlin", "Float")
val _Boolean = ClassName("kotlin", "Boolean")
val _Unit = ClassName("kotlin", "Unit")

val UseExperimental = ClassName("kotlin", "UseExperimental")
val ThreadLocal = ClassName("kotlin.native", "ThreadLocal")
val HashMap = ClassName("kotlin.collections", "HashMap")
val CPointer = ClassName("kotlinx.cinterop", "CPointer")
val CPointerVar = ClassName("kotlinx.cinterop", "CPointerVar")
val CPointerVarOf = ClassName("kotlinx.cinterop", "CPointerVarOf")
val StableRef = ClassName("kotlinx.cinterop", "StableRef")
val Object = ClassName(PACKAGE, "Object")
val StableRef_Object = StableRef.parameterizedBy(Object)
val COpaquePointer = ClassName("kotlinx.cinterop", "COpaquePointer")
val COpaquePointerVar = ClassName("kotlinx.cinterop", "COpaquePointerVar")
val GodotMethodBind = ClassName(INTERNAL_PACKAGE, "godot_method_bind")
val GodotVariant = ClassName(INTERNAL_PACKAGE, "godot_variant")
val Variant = ClassName(PACKAGE, "Variant")
val CPointer_GodotVariant = CPointer.parameterizedBy(GodotVariant)
val CPointer_COpaquePointerVar = CPointer.parameterizedBy(COpaquePointerVar)
val CPointer_CPointerVar_GodotVariant = CPointer.parameterizedBy(CPointerVar.parameterizedBy(GodotVariant))
val CPointer_GodotMethodBind = CPointer.parameterizedBy(GodotMethodBind)
val CFunction = ClassName("kotlinx.cinterop", "CFunction")
val CFunction_CPointer_GodotVariant = CFunction.parameterizedBy(LambdaTypeName.get(null, emptyList(), CPointer_GodotVariant.copy(true)))

val String = ClassName("kotlin", "String")
val Array = ClassName("kotlin", "Array")
val MutableMap = ClassName("kotlin.collections", "MutableMap")

val Array_Variant = Array.parameterizedBy(Variant)
val MutableMap_Variant_Any = MutableMap.parameterizedBy(Variant, Variant)

val UseExperimentalUnsignedTypes = AnnotationSpec.builder(UseExperimental).addMember("ExperimentalUnsignedTypes::class").build()

fun ClassName.isPrimitiveType(): Boolean = this in listOf(_Int, _UInt, _Float, _Boolean, _Unit)
fun ClassName.isCoreType(): Boolean = (packageName == PACKAGE || packageName == INTERNAL_PACKAGE) && simpleName.isCoreType()
fun ClassName.isEnumType(): Boolean = (packageName == PACKAGE) && simpleName.contains(".")
fun ClassName.isUnit(): Boolean = this == _Unit
fun ClassName.toVarType(): ClassName = when {
    isPrimitiveType() -> ClassName("kotlinx.cinterop", "${simpleName}Var")
    this == String -> ClassName("kotlinx.cinterop", "ByteVar")
    else -> this
}

fun ClassName.parameterized(): TypeName = when (this) {
    Array -> Array_Variant
    MutableMap -> MutableMap_Variant_Any
    else -> this
}

fun String.toCamelCase() = (if (startsWith("_")) "_" else "") + split("_").joinToString("") { it.capitalize() }.decapitalize()

fun String.escape(): String = when (this) {
    "class" -> "_class"
    "object" -> "_object"
    "api" -> "_api"
    "interface" -> "_interface"
    "in" -> "_in"
    "var" -> "_var"
    "args" -> "arguments"
    "toString" -> "toGodotString"
    else -> this
}

fun String.typeOverride(): String = when (this) {
    "int" -> "Int"
    "float", "real" -> "Float"
    "bool" -> "Boolean"
    "void" -> "Unit"
    "Error" -> "godot_error"
    "PoolRealArray" -> "PoolFloatArray"
    else -> this
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun String.toClassName(): ClassName = when (this) {
    "Int" -> _Int
    "UInt" -> _UInt
    "Float" -> _Float
    "Boolean" -> _Boolean
    "Unit" -> _Unit
    "godot_error" -> ClassName(INTERNAL_PACKAGE, this)
    "String" -> String
    "Array" -> Array
    "Dictionary" -> MutableMap
    else -> ClassName(PACKAGE, this)
}

fun String.sanitisedType() = removePrefix("enum.").replace("::", ".").typeOverride()

fun String.sanitisedName() = removePrefix("enum.").replace("::", "").toCamelCase().escape()

fun String.isPrimitiveType(): Boolean = toClassName().isPrimitiveType()

fun String.isClassType(): Boolean = !this.isCoreType() && !this.isPrimitiveType()

fun String.isCoreType(): Boolean = when (this) {
    "AABB",
    "Array",
    "Basis",
    "Color",
    "Dictionary",
    "NodePath",
    "Plane",
    "PoolByteArray",
    "PoolIntArray",
    "PoolFloatArray",
    "PoolStringArray",
    "PoolVector2Array",
    "PoolVector3Array",
    "PoolColorArray",
    "Quat",
    "Rect2",
    "RID",
    "String",
    "Transform",
    "Transform2D",
    "Variant",
    "Vector2",
    "Vector3",
    "godot_error",
    "godot_vector3_axis",
    "godot_variant_operator",
    "godot_variant_type" -> true
    else -> false
}