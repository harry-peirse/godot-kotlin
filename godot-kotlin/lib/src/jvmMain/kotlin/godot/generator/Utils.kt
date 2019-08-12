package godot.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.UInt
import kotlin.Unit

val Int = Int::class.asClassName()
@UseExperimental(ExperimentalUnsignedTypes::class)
val UInt = UInt::class.asClassName()
val Float = Float::class.asClassName()
val Boolean = Boolean::class.asClassName()
val Unit = Unit::class.asClassName()

val UseExperimental = ClassName("kotlin", "UseExperimental")
val ThreadLocal = ClassName("kotlin.native", "ThreadLocal")
val HashMap = ClassName("kotlin.collections", "HashMap")
val CPointed = ClassName("kotlinx.cinterop", "CPointed")
val CPointer = ClassName("kotlinx.cinterop", "CPointer")
val COpaquePointer = ClassName("kotlinx.cinterop", "COpaquePointer")
val COpaquePointerVar = ClassName("kotlinx.cinterop", "COpaquePointerVar")
val GodotMethodBind = ClassName(INTERNAL_PACKAGE, "godot_method_bind")
val GodotVariant = ClassName(INTERNAL_PACKAGE, "godot_variant")
val Variant = ClassName(PACKAGE, "Variant")
val CoreType = ClassName(PACKAGE, "CoreType").parameterizedBy(WildcardTypeName.producerOf(ClassName("kotlinx.cinterop", "CPointed")))
val CPointer_COpaquePointerVar = CPointer.parameterizedBy(COpaquePointerVar)
val CPointer_CPointerVar_GodotVariant = CPointer.parameterizedBy(COpaquePointerVar.parameterizedBy(GodotVariant))
val CPointer_GodotMethodBind = CPointer.parameterizedBy(GodotMethodBind)

val UseExperimentalUnsignedTypes = AnnotationSpec.builder(UseExperimental).addMember("ExperimentalUnsignedTypes::class").build()

fun ClassName.isPrimitiveType(): Boolean = packageName == "kotlin"
fun ClassName.isCoreType(): Boolean = (packageName == PACKAGE || packageName == INTERNAL_PACKAGE) && simpleName.isCoreType()
fun ClassName.isCoreEnumType(): Boolean = (packageName == INTERNAL_PACKAGE) && simpleName.isCoreType()
fun ClassName.isEnumType(): Boolean = (packageName == PACKAGE) && !simpleName.isCoreType() && simpleName.contains(".")
fun ClassName.isUnit(): Boolean = this == godot.generator.Unit
fun ClassName.toVarType(): ClassName = if (this.isPrimitiveType()) ClassName("kotlinx.cinterop", "${simpleName}Var") else this
fun ClassName.manageCoreType(): TypeName = if (packageName == PACKAGE && simpleName == "CoreType") this.parameterizedBy(WildcardTypeName.producerOf(CPointed)) else this

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
    "String" -> "GodotString"
    "Array" -> "GodotArray"
    "Dictionary" -> "GodotDictionary"
    "Error" -> "godot_error"
    "PoolRealArray" -> "PoolFloatArray"
    "Variant.Operator" -> "godot_variant_operator"
    "Variant.Type" -> "godot_variant_type"
    "Vector3.Axis" -> "godot_vector3_axis"
    else -> this
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun String.toClassName(): ClassName = when (this) {
    "Int" -> godot.generator.Int
    "UInt" -> godot.generator.UInt
    "Float" -> godot.generator.Float
    "Boolean" -> godot.generator.Boolean
    "Unit" -> godot.generator.Unit
    "godot_error", "godot_variant_operator", "godot_variant_type", "godot_vector3_axis" -> ClassName(INTERNAL_PACKAGE, this)
    else -> ClassName(PACKAGE, this)
}

fun String.sanitisedType() = removePrefix("enum.").replace("::", ".").typeOverride()
fun String.sanitisedName() = removePrefix("enum.").replace("::", "").toCamelCase().escape()

fun String.isPrimitiveType(): Boolean = toClassName().isPrimitiveType()

fun String.isGeneratedClassType(): Boolean = !this.isCoreType() && !this.isPrimitiveType()

fun String.isCoreType(): Boolean = when (this) {
    "AABB",
    "Array",
    "Basis",
    "Color",
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
    "GodotString",
    "GodotArray",
    "GodotDictionary",
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