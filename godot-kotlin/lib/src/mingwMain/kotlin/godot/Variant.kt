package godot

import godot.internal.godot_array
import godot.internal.godot_dictionary
import godot.internal.godot_string
import godot.internal.godot_variant
import kotlinx.cinterop.*

fun CPointer<godot_string>.toKString(): String = memScoped {
    api.godot_char_string_get_data!!(
            api.godot_string_utf8!!(this@toKString).ptr
    )!!.toKStringFromUtf8()
}

fun String.toGString(): CPointer<godot_string> {
    val _string = alloc<godot_string>(godot_string.size)
    api.godot_string_new!!(_string)
    memScoped {
        api.godot_string_parse_utf8!!(_string, this@toGString.cstr.ptr)
    }
    return _string
}

fun CPointer<godot_dictionary>.toKMutableMap(): MutableMap<Variant, Any?> = TODO()
fun MutableMap<Variant, Any?>.toGDictionary(): CPointer<godot_dictionary> = TODO()
fun CPointer<godot_array>.toKArray(): Array<Variant> = TODO()
fun Array<Variant>.toGArray(): CPointer<godot_array> = TODO()

@UseExperimental(ExperimentalUnsignedTypes::class)
class Variant internal constructor(val _raw: CPointer<godot_variant>) : Comparable<Variant> {
    enum class Type {

        NIL,

        // atomic types
        BOOL,
        INT,
        REAL,
        STRING,

        // math types

        VECTOR2, // 5
        RECT2,
        VECTOR3,
        TRANSFORM2D,
        PLANE,
        QUAT, // 10
        RECT3, //sorry naming convention fail :( not like it's used often
        BASIS,
        TRANSFORM,

        // misc types
        COLOR,
        NODE_PATH, // 15
        _RID,
        OBJECT,
        DICTIONARY,
        ARRAY,

        // arrays
        POOL_BYTE_ARRAY, // 20
        POOL_INT_ARRAY,
        POOL_REAL_ARRAY,
        POOL_STRING_ARRAY,
        POOL_VECTOR2_ARRAY,
        POOL_VECTOR3_ARRAY, // 25
        POOL_COLOR_ARRAY,

        VARIANT_MAX

    }

    enum class Operator {

        //comparation
        OP_EQUAL,
        OP_NOT_EQUAL,
        OP_LESS,
        OP_LESS_EQUAL,
        OP_GREATER,
        OP_GREATER_EQUAL,

        //mathematic
        OP_ADD,
        OP_SUBSTRACT,
        OP_MULTIPLY,
        OP_DIVIDE,
        OP_NEGATE,
        OP_POSITIVE,
        OP_MODULE,
        OP_STRING_CONCAT,

        //bitwise
        OP_SHIFT_LEFT,
        OP_SHIFT_RIGHT,
        OP_BIT_AND,
        OP_BIT_OR,
        OP_BIT_XOR,
        OP_BIT_NEGATE,

        //logic
        OP_AND,
        OP_OR,
        OP_XOR,
        OP_NOT,

        //containment
        OP_IN,
        OP_MAX

    }

    internal constructor(_raw: CValue<godot_variant>) : this(_raw.place(alloc(godot_variant.size)))

    constructor() : this(alloc(godot_variant.size)) {
        api.godot_variant_new_nil!!(_raw)
    }

    constructor(value: Variant) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_copy!!(_raw, value._raw)
    }

    constructor(value: Boolean) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_bool!!(_raw, value)
    }

    constructor(value: Long) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_int!!(_raw, value)
    }

    constructor(value: Int) : this(value.toLong())
    constructor(value: Short) : this(value.toLong())
    constructor(value: Char) : this(value.toLong())

    constructor(value: Double) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_real!!(_raw, value)
    }

    constructor(value: Float) : this(value.toDouble())

    constructor(value: ULong) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_uint!!(_raw, value)
    }

    constructor(value: UInt) : this(value.toULong())
    constructor(value: UShort) : this(value.toULong())

    constructor(value: String) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_string!!(_raw, value.toGString())
    }

    constructor(value: Vector2) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_vector2!!(_raw, value._raw)
    }

    constructor(value: Rect2) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_rect2!!(_raw, value._raw)
    }

    constructor(value: Vector3) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_vector3!!(_raw, value._raw)
    }

    constructor(value: Plane) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_plane!!(_raw, value._raw)
    }

    constructor(value: AABB) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_aabb!!(_raw, value._raw)
    }

    constructor(value: Quat) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_quat!!(_raw, value._raw)
    }

    constructor(value: Basis) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_basis!!(_raw, value._raw)
    }

    constructor(value: Transform2D) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_transform2d!!(_raw, value._raw)
    }

    constructor(value: Transform) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_transform!!(_raw, value._raw)
    }

    constructor(value: Color) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_color!!(_raw, value._raw)
    }

    constructor(value: NodePath) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_node_path!!(_raw, value._raw)
    }

    constructor(value: RID) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_rid!!(_raw, value._raw)
    }

    constructor(value: Object) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_object!!(_raw, value._raw)
    }

    constructor(value: MutableMap<Variant, Any?>) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_dictionary!!(_raw, value.toGDictionary())
    }

    constructor(value: Array<Variant>) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_array!!(_raw, value.toGArray())
    }

    constructor(value: PoolByteArray) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_byte_array!!(_raw, value._raw)
    }

    constructor(value: PoolIntArray) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_int_array!!(_raw, value._raw)
    }

    constructor(value: PoolFloatArray) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_real_array!!(_raw, value._raw)
    }

    constructor(value: PoolStringArray) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_string_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector2Array) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_vector2_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector3Array) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_vector3_array!!(_raw, value._raw)
    }

    constructor(value: PoolColorArray) : this(alloc(godot_variant.size)) {
        api.godot_variant_new_pool_color_array!!(_raw, value._raw)
    }

    fun toBoolean(): Boolean {
        return api.godot_variant_booleanize!!(_raw)
    }

    fun toLong(): Long {
        return api.godot_variant_as_int!!(_raw)
    }

    fun toInt(): Int = toLong().toInt()
    fun toShort(): Short = toLong().toShort()

    fun toULong(): ULong {
        return api.godot_variant_as_uint!!(_raw)
    }

    fun toUInt(): UInt = toULong().toUInt()
    fun toUShort(): UShort = toULong().toUShort()

    fun toDouble(): Double {
        return api.godot_variant_as_real!!(_raw)
    }

    fun toFloat(): Float = toDouble().toFloat()

    override fun toString(): String = memScoped {
        return api.godot_variant_as_string!!(_raw).ptr.toKString()
    }

    fun toVector2(): Vector2 {
        return Vector2(api.godot_variant_as_vector2!!(_raw))
    }

    fun toRect2(): Rect2 {
        return Rect2(api.godot_variant_as_rect2!!(_raw))
    }

    fun toVector3(): Vector3 {
        return Vector3(api.godot_variant_as_vector3!!(_raw))
    }

    fun toPlane(): Plane {
        return Plane(api.godot_variant_as_plane!!(_raw))
    }

    fun toAABB(): AABB {
        return AABB(api.godot_variant_as_aabb!!(_raw))
    }

    fun toQuat(): Quat {
        return Quat(api.godot_variant_as_quat!!(_raw))
    }

    fun toBasis(): Basis {
        return Basis(api.godot_variant_as_basis!!(_raw))
    }

    fun toTransform(): Transform {
        return Transform(api.godot_variant_as_transform!!(_raw))
    }

    fun toTransform2D(): Transform2D {
        return Transform2D(api.godot_variant_as_transform2d!!(_raw))
    }

    fun toColor(): Color {
        return Color(api.godot_variant_as_color!!(_raw))
    }

    fun toNodePath(): NodePath {
        return NodePath(api.godot_variant_as_node_path!!(_raw))
    }

    fun toRID(): RID {
        return RID(api.godot_variant_as_rid!!(_raw))
    }

    inline fun <reified T : Object> toObject(): T {
        api.godot_variant_as_object!!(_raw)
        val result = when(T::class) {
            ARVRAnchor::class -> ARVRAnchor.getFromVariant(_raw)
            ARVRCamera::class -> ARVRCamera.getFromVariant(_raw)
            ARVRController::class -> ARVRController.getFromVariant(_raw)
            ARVRInterface::class -> ARVRInterface.getFromVariant(_raw)
            ARVRInterfaceGDNative::class -> ARVRInterfaceGDNative.getFromVariant(_raw)
            ARVROrigin::class -> ARVROrigin.getFromVariant(_raw)
            ARVRPositionalTracker::class -> ARVRPositionalTracker.getFromVariant(_raw)
            ARVRServer::class -> ARVRServer.getFromVariant(_raw)
            AStar::class -> AStar.getFromVariant(_raw)
            AcceptDialog::class -> AcceptDialog.getFromVariant(_raw)
            AnimatedSprite3D::class -> AnimatedSprite3D.getFromVariant(_raw)
            AnimatedSprite::class -> AnimatedSprite.getFromVariant(_raw)
            AnimatedTexture::class -> AnimatedTexture.getFromVariant(_raw)
            Animation::class -> Animation.getFromVariant(_raw)
            AnimationNode::class -> AnimationNode.getFromVariant(_raw)
            AnimationNodeAdd2::class -> AnimationNodeAdd2.getFromVariant(_raw)
            AnimationNodeAdd3::class -> AnimationNodeAdd3.getFromVariant(_raw)
            AnimationNodeAnimation::class -> AnimationNodeAnimation.getFromVariant(_raw)
            AnimationNodeBlend2::class -> AnimationNodeBlend2.getFromVariant(_raw)
            AnimationNodeBlend3::class -> AnimationNodeBlend3.getFromVariant(_raw)
            AnimationNodeBlendSpace1D::class -> AnimationNodeBlendSpace1D.getFromVariant(_raw)
            AnimationNodeBlendSpace2D::class -> AnimationNodeBlendSpace2D.getFromVariant(_raw)
            AnimationNodeBlendTree::class -> AnimationNodeBlendTree.getFromVariant(_raw)
            AnimationNodeOneShot::class -> AnimationNodeOneShot.getFromVariant(_raw)
            AnimationNodeOutput::class -> AnimationNodeOutput.getFromVariant(_raw)
            AnimationNodeStateMachine::class -> AnimationNodeStateMachine.getFromVariant(_raw)
            AnimationNodeStateMachinePlayback::class -> AnimationNodeStateMachinePlayback.getFromVariant(_raw)
            AnimationNodeStateMachineTransition::class -> AnimationNodeStateMachineTransition.getFromVariant(_raw)
            AnimationNodeTimeScale::class -> AnimationNodeTimeScale.getFromVariant(_raw)
            AnimationNodeTimeSeek::class -> AnimationNodeTimeSeek.getFromVariant(_raw)
            AnimationNodeTransition::class -> AnimationNodeTransition.getFromVariant(_raw)
            AnimationPlayer::class -> AnimationPlayer.getFromVariant(_raw)
            AnimationRootNode::class -> AnimationRootNode.getFromVariant(_raw)
            AnimationTrackEditPlugin::class -> AnimationTrackEditPlugin.getFromVariant(_raw)
            AnimationTree::class -> AnimationTree.getFromVariant(_raw)
            AnimationTreePlayer::class -> AnimationTreePlayer.getFromVariant(_raw)
            Area2D::class -> Area2D.getFromVariant(_raw)
            Area::class -> Area.getFromVariant(_raw)
            ArrayMesh::class -> ArrayMesh.getFromVariant(_raw)
            AtlasTexture::class -> AtlasTexture.getFromVariant(_raw)
            AudioBusLayout::class -> AudioBusLayout.getFromVariant(_raw)
            AudioEffect::class -> AudioEffect.getFromVariant(_raw)
            AudioEffectAmplify::class -> AudioEffectAmplify.getFromVariant(_raw)
            AudioEffectBandLimitFilter::class -> AudioEffectBandLimitFilter.getFromVariant(_raw)
            AudioEffectBandPassFilter::class -> AudioEffectBandPassFilter.getFromVariant(_raw)
            AudioEffectChorus::class -> AudioEffectChorus.getFromVariant(_raw)
            AudioEffectCompressor::class -> AudioEffectCompressor.getFromVariant(_raw)
            AudioEffectDelay::class -> AudioEffectDelay.getFromVariant(_raw)
            AudioEffectDistortion::class -> AudioEffectDistortion.getFromVariant(_raw)
            AudioEffectEQ10::class -> AudioEffectEQ10.getFromVariant(_raw)
            AudioEffectEQ21::class -> AudioEffectEQ21.getFromVariant(_raw)
            AudioEffectEQ6::class -> AudioEffectEQ6.getFromVariant(_raw)
            AudioEffectEQ::class -> AudioEffectEQ.getFromVariant(_raw)
            AudioEffectFilter::class -> AudioEffectFilter.getFromVariant(_raw)
            AudioEffectHighPassFilter::class -> AudioEffectHighPassFilter.getFromVariant(_raw)
            AudioEffectHighShelfFilter::class -> AudioEffectHighShelfFilter.getFromVariant(_raw)
            AudioEffectInstance::class -> AudioEffectInstance.getFromVariant(_raw)
            AudioEffectLimiter::class -> AudioEffectLimiter.getFromVariant(_raw)
            AudioEffectLowPassFilter::class -> AudioEffectLowPassFilter.getFromVariant(_raw)
            AudioEffectLowShelfFilter::class -> AudioEffectLowShelfFilter.getFromVariant(_raw)
            AudioEffectNotchFilter::class -> AudioEffectNotchFilter.getFromVariant(_raw)
            AudioEffectPanner::class -> AudioEffectPanner.getFromVariant(_raw)
            AudioEffectPhaser::class -> AudioEffectPhaser.getFromVariant(_raw)
            AudioEffectPitchShift::class -> AudioEffectPitchShift.getFromVariant(_raw)
            AudioEffectRecord::class -> AudioEffectRecord.getFromVariant(_raw)
            AudioEffectReverb::class -> AudioEffectReverb.getFromVariant(_raw)
            AudioEffectSpectrumAnalyzer::class -> AudioEffectSpectrumAnalyzer.getFromVariant(_raw)
            AudioEffectSpectrumAnalyzerInstance::class -> AudioEffectSpectrumAnalyzerInstance.getFromVariant(_raw)
            AudioEffectStereoEnhance::class -> AudioEffectStereoEnhance.getFromVariant(_raw)
            AudioServer::class -> AudioServer.getFromVariant(_raw)
            AudioStream::class -> AudioStream.getFromVariant(_raw)
            AudioStreamGenerator::class -> AudioStreamGenerator.getFromVariant(_raw)
            AudioStreamGeneratorPlayback::class -> AudioStreamGeneratorPlayback.getFromVariant(_raw)
            AudioStreamMicrophone::class -> AudioStreamMicrophone.getFromVariant(_raw)
            AudioStreamOGGVorbis::class -> AudioStreamOGGVorbis.getFromVariant(_raw)
            AudioStreamPlayback::class -> AudioStreamPlayback.getFromVariant(_raw)
            AudioStreamPlaybackResampled::class -> AudioStreamPlaybackResampled.getFromVariant(_raw)
            AudioStreamPlayer2D::class -> AudioStreamPlayer2D.getFromVariant(_raw)
            AudioStreamPlayer3D::class -> AudioStreamPlayer3D.getFromVariant(_raw)
            AudioStreamPlayer::class -> AudioStreamPlayer.getFromVariant(_raw)
            AudioStreamRandomPitch::class -> AudioStreamRandomPitch.getFromVariant(_raw)
            AudioStreamSample::class -> AudioStreamSample.getFromVariant(_raw)
            BackBufferCopy::class -> BackBufferCopy.getFromVariant(_raw)
            BakedLightmap::class -> BakedLightmap.getFromVariant(_raw)
            BakedLightmapData::class -> BakedLightmapData.getFromVariant(_raw)
            BaseButton::class -> BaseButton.getFromVariant(_raw)
            BitMap::class -> BitMap.getFromVariant(_raw)
            BitmapFont::class -> BitmapFont.getFromVariant(_raw)
            Bone2D::class -> Bone2D.getFromVariant(_raw)
            BoneAttachment::class -> BoneAttachment.getFromVariant(_raw)
            BoxContainer::class -> BoxContainer.getFromVariant(_raw)
            BoxShape::class -> BoxShape.getFromVariant(_raw)
            BulletPhysicsDirectBodyState::class -> BulletPhysicsDirectBodyState.getFromVariant(_raw)
            BulletPhysicsServer::class -> BulletPhysicsServer.getFromVariant(_raw)
            Button::class -> Button.getFromVariant(_raw)
            ButtonGroup::class -> ButtonGroup.getFromVariant(_raw)
            CPUParticles2D::class -> CPUParticles2D.getFromVariant(_raw)
            CPUParticles::class -> CPUParticles.getFromVariant(_raw)
            CSGBox::class -> CSGBox.getFromVariant(_raw)
            CSGCombiner::class -> CSGCombiner.getFromVariant(_raw)
            CSGCylinder::class -> CSGCylinder.getFromVariant(_raw)
            CSGMesh::class -> CSGMesh.getFromVariant(_raw)
            CSGPolygon::class -> CSGPolygon.getFromVariant(_raw)
            CSGPrimitive::class -> CSGPrimitive.getFromVariant(_raw)
            CSGShape::class -> CSGShape.getFromVariant(_raw)
            CSGSphere::class -> CSGSphere.getFromVariant(_raw)
            CSGTorus::class -> CSGTorus.getFromVariant(_raw)
            Camera2D::class -> Camera2D.getFromVariant(_raw)
            Camera::class -> Camera.getFromVariant(_raw)
            CameraFeed::class -> CameraFeed.getFromVariant(_raw)
            CameraServer::class -> CameraServer.getFromVariant(_raw)
            CameraTexture::class -> CameraTexture.getFromVariant(_raw)
            CanvasItem::class -> CanvasItem.getFromVariant(_raw)
            CanvasItemMaterial::class -> CanvasItemMaterial.getFromVariant(_raw)
            CanvasLayer::class -> CanvasLayer.getFromVariant(_raw)
            CanvasModulate::class -> CanvasModulate.getFromVariant(_raw)
            CapsuleMesh::class -> CapsuleMesh.getFromVariant(_raw)
            CapsuleShape2D::class -> CapsuleShape2D.getFromVariant(_raw)
            CapsuleShape::class -> CapsuleShape.getFromVariant(_raw)
            CenterContainer::class -> CenterContainer.getFromVariant(_raw)
            CheckBox::class -> CheckBox.getFromVariant(_raw)
            CheckButton::class -> CheckButton.getFromVariant(_raw)
            CircleShape2D::class -> CircleShape2D.getFromVariant(_raw)
            ClippedCamera::class -> ClippedCamera.getFromVariant(_raw)
            CollisionObject2D::class -> CollisionObject2D.getFromVariant(_raw)
            CollisionObject::class -> CollisionObject.getFromVariant(_raw)
            CollisionPolygon2D::class -> CollisionPolygon2D.getFromVariant(_raw)
            CollisionPolygon::class -> CollisionPolygon.getFromVariant(_raw)
            CollisionShape2D::class -> CollisionShape2D.getFromVariant(_raw)
            CollisionShape::class -> CollisionShape.getFromVariant(_raw)
            ColorPicker::class -> ColorPicker.getFromVariant(_raw)
            ColorPickerButton::class -> ColorPickerButton.getFromVariant(_raw)
            ColorRect::class -> ColorRect.getFromVariant(_raw)
            ConcavePolygonShape2D::class -> ConcavePolygonShape2D.getFromVariant(_raw)
            ConcavePolygonShape::class -> ConcavePolygonShape.getFromVariant(_raw)
            ConeTwistJoint::class -> ConeTwistJoint.getFromVariant(_raw)
            ConfigFile::class -> ConfigFile.getFromVariant(_raw)
            ConfirmationDialog::class -> ConfirmationDialog.getFromVariant(_raw)
            Container::class -> Container.getFromVariant(_raw)
            Control::class -> Control.getFromVariant(_raw)
            ConvexPolygonShape2D::class -> ConvexPolygonShape2D.getFromVariant(_raw)
            ConvexPolygonShape::class -> ConvexPolygonShape.getFromVariant(_raw)
            CubeMap::class -> CubeMap.getFromVariant(_raw)
            CubeMesh::class -> CubeMesh.getFromVariant(_raw)
            Curve2D::class -> Curve2D.getFromVariant(_raw)
            Curve3D::class -> Curve3D.getFromVariant(_raw)
            Curve::class -> Curve.getFromVariant(_raw)
            CurveTexture::class -> CurveTexture.getFromVariant(_raw)
            CylinderMesh::class -> CylinderMesh.getFromVariant(_raw)
            CylinderShape::class -> CylinderShape.getFromVariant(_raw)
            DampedSpringJoint2D::class -> DampedSpringJoint2D.getFromVariant(_raw)
            DirectionalLight::class -> DirectionalLight.getFromVariant(_raw)
            DynamicFont::class -> DynamicFont.getFromVariant(_raw)
            DynamicFontData::class -> DynamicFontData.getFromVariant(_raw)
            EditorExportPlugin::class -> EditorExportPlugin.getFromVariant(_raw)
            EditorFeatureProfile::class -> EditorFeatureProfile.getFromVariant(_raw)
            EditorFileDialog::class -> EditorFileDialog.getFromVariant(_raw)
            EditorFileSystem::class -> EditorFileSystem.getFromVariant(_raw)
            EditorFileSystemDirectory::class -> EditorFileSystemDirectory.getFromVariant(_raw)
            EditorImportPlugin::class -> EditorImportPlugin.getFromVariant(_raw)
            EditorInspector::class -> EditorInspector.getFromVariant(_raw)
            EditorInspectorPlugin::class -> EditorInspectorPlugin.getFromVariant(_raw)
            EditorInterface::class -> EditorInterface.getFromVariant(_raw)
            EditorNavigationMeshGenerator::class -> EditorNavigationMeshGenerator.getFromVariant(_raw)
            EditorPlugin::class -> EditorPlugin.getFromVariant(_raw)
            EditorProperty::class -> EditorProperty.getFromVariant(_raw)
            EditorResourceConversionPlugin::class -> EditorResourceConversionPlugin.getFromVariant(_raw)
            EditorResourcePreview::class -> EditorResourcePreview.getFromVariant(_raw)
            EditorResourcePreviewGenerator::class -> EditorResourcePreviewGenerator.getFromVariant(_raw)
            EditorSceneImporter::class -> EditorSceneImporter.getFromVariant(_raw)
            EditorSceneImporterAssimp::class -> EditorSceneImporterAssimp.getFromVariant(_raw)
            EditorScenePostImport::class -> EditorScenePostImport.getFromVariant(_raw)
            EditorScript::class -> EditorScript.getFromVariant(_raw)
            EditorSelection::class -> EditorSelection.getFromVariant(_raw)
            EditorSettings::class -> EditorSettings.getFromVariant(_raw)
            EditorSpatialGizmo::class -> EditorSpatialGizmo.getFromVariant(_raw)
            EditorSpatialGizmoPlugin::class -> EditorSpatialGizmoPlugin.getFromVariant(_raw)
            EncodedObjectAsID::class -> EncodedObjectAsID.getFromVariant(_raw)
            Environment::class -> Environment.getFromVariant(_raw)
            Expression::class -> Expression.getFromVariant(_raw)
            FileDialog::class -> FileDialog.getFromVariant(_raw)
            Font::class -> Font.getFromVariant(_raw)
            FuncRef::class -> FuncRef.getFromVariant(_raw)
            GDNative::class -> GDNative.getFromVariant(_raw)
            GDNativeLibrary::class -> GDNativeLibrary.getFromVariant(_raw)
            GDNativeLibraryResourceLoader::class -> GDNativeLibraryResourceLoader.getFromVariant(_raw)
            GDNativeLibraryResourceSaver::class -> GDNativeLibraryResourceSaver.getFromVariant(_raw)
            GDScript::class -> GDScript.getFromVariant(_raw)
            GDScriptFunctionState::class -> GDScriptFunctionState.getFromVariant(_raw)
            GIProbe::class -> GIProbe.getFromVariant(_raw)
            GIProbeData::class -> GIProbeData.getFromVariant(_raw)
            Generic6DOFJoint::class -> Generic6DOFJoint.getFromVariant(_raw)
            GeometryInstance::class -> GeometryInstance.getFromVariant(_raw)
            Gradient::class -> Gradient.getFromVariant(_raw)
            GradientTexture::class -> GradientTexture.getFromVariant(_raw)
            GraphEdit::class -> GraphEdit.getFromVariant(_raw)
            GraphNode::class -> GraphNode.getFromVariant(_raw)
            GridContainer::class -> GridContainer.getFromVariant(_raw)
            GridMap::class -> GridMap.getFromVariant(_raw)
            GrooveJoint2D::class -> GrooveJoint2D.getFromVariant(_raw)
            HBoxContainer::class -> HBoxContainer.getFromVariant(_raw)
            HScrollBar::class -> HScrollBar.getFromVariant(_raw)
            HSeparator::class -> HSeparator.getFromVariant(_raw)
            HSlider::class -> HSlider.getFromVariant(_raw)
            HSplitContainer::class -> HSplitContainer.getFromVariant(_raw)
            HTTPClient::class -> HTTPClient.getFromVariant(_raw)
            HTTPRequest::class -> HTTPRequest.getFromVariant(_raw)
            HeightMapShape::class -> HeightMapShape.getFromVariant(_raw)
            HingeJoint::class -> HingeJoint.getFromVariant(_raw)
            IP::class -> IP.getFromVariant(_raw)
            IP_Unix::class -> IP_Unix.getFromVariant(_raw)
            Image::class -> Image.getFromVariant(_raw)
            ImageTexture::class -> ImageTexture.getFromVariant(_raw)
            ImmediateGeometry::class -> ImmediateGeometry.getFromVariant(_raw)
            Input::class -> Input.getFromVariant(_raw)
            InputDefault::class -> InputDefault.getFromVariant(_raw)
            InputEvent::class -> InputEvent.getFromVariant(_raw)
            InputEventAction::class -> InputEventAction.getFromVariant(_raw)
            InputEventGesture::class -> InputEventGesture.getFromVariant(_raw)
            InputEventJoypadButton::class -> InputEventJoypadButton.getFromVariant(_raw)
            InputEventJoypadMotion::class -> InputEventJoypadMotion.getFromVariant(_raw)
            InputEventKey::class -> InputEventKey.getFromVariant(_raw)
            InputEventMIDI::class -> InputEventMIDI.getFromVariant(_raw)
            InputEventMagnifyGesture::class -> InputEventMagnifyGesture.getFromVariant(_raw)
            InputEventMouse::class -> InputEventMouse.getFromVariant(_raw)
            InputEventMouseButton::class -> InputEventMouseButton.getFromVariant(_raw)
            InputEventMouseMotion::class -> InputEventMouseMotion.getFromVariant(_raw)
            InputEventPanGesture::class -> InputEventPanGesture.getFromVariant(_raw)
            InputEventScreenDrag::class -> InputEventScreenDrag.getFromVariant(_raw)
            InputEventScreenTouch::class -> InputEventScreenTouch.getFromVariant(_raw)
            InputEventWithModifiers::class -> InputEventWithModifiers.getFromVariant(_raw)
            InputMap::class -> InputMap.getFromVariant(_raw)
            InstancePlaceholder::class -> InstancePlaceholder.getFromVariant(_raw)
            InterpolatedCamera::class -> InterpolatedCamera.getFromVariant(_raw)
            ItemList::class -> ItemList.getFromVariant(_raw)
            JSONParseResult::class -> JSONParseResult.getFromVariant(_raw)
            JavaScript::class -> JavaScript.getFromVariant(_raw)
            Joint2D::class -> Joint2D.getFromVariant(_raw)
            Joint::class -> Joint.getFromVariant(_raw)
            KinematicBody2D::class -> KinematicBody2D.getFromVariant(_raw)
            KinematicBody::class -> KinematicBody.getFromVariant(_raw)
            KinematicCollision2D::class -> KinematicCollision2D.getFromVariant(_raw)
            KinematicCollision::class -> KinematicCollision.getFromVariant(_raw)
            Label::class -> Label.getFromVariant(_raw)
            LargeTexture::class -> LargeTexture.getFromVariant(_raw)
            Light2D::class -> Light2D.getFromVariant(_raw)
            Light::class -> Light.getFromVariant(_raw)
            LightOccluder2D::class -> LightOccluder2D.getFromVariant(_raw)
            Line2D::class -> Line2D.getFromVariant(_raw)
            LineEdit::class -> LineEdit.getFromVariant(_raw)
            LineShape2D::class -> LineShape2D.getFromVariant(_raw)
            LinkButton::class -> LinkButton.getFromVariant(_raw)
            Listener::class -> Listener.getFromVariant(_raw)
            MainLoop::class -> MainLoop.getFromVariant(_raw)
            MarginContainer::class -> MarginContainer.getFromVariant(_raw)
            Material::class -> Material.getFromVariant(_raw)
            MenuButton::class -> MenuButton.getFromVariant(_raw)
            Mesh::class -> Mesh.getFromVariant(_raw)
            MeshDataTool::class -> MeshDataTool.getFromVariant(_raw)
            MeshInstance2D::class -> MeshInstance2D.getFromVariant(_raw)
            MeshInstance::class -> MeshInstance.getFromVariant(_raw)
            MeshLibrary::class -> MeshLibrary.getFromVariant(_raw)
            MeshTexture::class -> MeshTexture.getFromVariant(_raw)
            MobileVRInterface::class -> MobileVRInterface.getFromVariant(_raw)
            MultiMesh::class -> MultiMesh.getFromVariant(_raw)
            MultiMeshInstance2D::class -> MultiMeshInstance2D.getFromVariant(_raw)
            MultiMeshInstance::class -> MultiMeshInstance.getFromVariant(_raw)
            MultiplayerAPI::class -> MultiplayerAPI.getFromVariant(_raw)
            MultiplayerPeerGDNative::class -> MultiplayerPeerGDNative.getFromVariant(_raw)
            NativeScript::class -> NativeScript.getFromVariant(_raw)
            Navigation2D::class -> Navigation2D.getFromVariant(_raw)
            Navigation::class -> Navigation.getFromVariant(_raw)
            NavigationMesh::class -> NavigationMesh.getFromVariant(_raw)
            NavigationMeshInstance::class -> NavigationMeshInstance.getFromVariant(_raw)
            NavigationPolygon::class -> NavigationPolygon.getFromVariant(_raw)
            NavigationPolygonInstance::class -> NavigationPolygonInstance.getFromVariant(_raw)
            NetworkedMultiplayerENet::class -> NetworkedMultiplayerENet.getFromVariant(_raw)
            NetworkedMultiplayerPeer::class -> NetworkedMultiplayerPeer.getFromVariant(_raw)
            NinePatchRect::class -> NinePatchRect.getFromVariant(_raw)
            Node2D::class -> Node2D.getFromVariant(_raw)
            Node::class -> Node.getFromVariant(_raw)
            NoiseTexture::class -> NoiseTexture.getFromVariant(_raw)
            Object::class -> Object.getFromVariant(_raw)
            OccluderPolygon2D::class -> OccluderPolygon2D.getFromVariant(_raw)
            OmniLight::class -> OmniLight.getFromVariant(_raw)
            OpenSimplexNoise::class -> OpenSimplexNoise.getFromVariant(_raw)
            OptionButton::class -> OptionButton.getFromVariant(_raw)
            PCKPacker::class -> PCKPacker.getFromVariant(_raw)
            PHashTranslation::class -> PHashTranslation.getFromVariant(_raw)
            PackedDataContainer::class -> PackedDataContainer.getFromVariant(_raw)
            PackedDataContainerRef::class -> PackedDataContainerRef.getFromVariant(_raw)
            PackedScene::class -> PackedScene.getFromVariant(_raw)
            PacketPeer::class -> PacketPeer.getFromVariant(_raw)
            PacketPeerGDNative::class -> PacketPeerGDNative.getFromVariant(_raw)
            PacketPeerStream::class -> PacketPeerStream.getFromVariant(_raw)
            PacketPeerUDP::class -> PacketPeerUDP.getFromVariant(_raw)
            Panel::class -> Panel.getFromVariant(_raw)
            PanelContainer::class -> PanelContainer.getFromVariant(_raw)
            PanoramaSky::class -> PanoramaSky.getFromVariant(_raw)
            ParallaxBackground::class -> ParallaxBackground.getFromVariant(_raw)
            ParallaxLayer::class -> ParallaxLayer.getFromVariant(_raw)
            Particles2D::class -> Particles2D.getFromVariant(_raw)
            Particles::class -> Particles.getFromVariant(_raw)
            ParticlesMaterial::class -> ParticlesMaterial.getFromVariant(_raw)
            Path2D::class -> Path2D.getFromVariant(_raw)
            Path::class -> Path.getFromVariant(_raw)
            PathFollow2D::class -> PathFollow2D.getFromVariant(_raw)
            PathFollow::class -> PathFollow.getFromVariant(_raw)
            Performance::class -> Performance.getFromVariant(_raw)
            PhysicalBone::class -> PhysicalBone.getFromVariant(_raw)
            Physics2DDirectBodyState::class -> Physics2DDirectBodyState.getFromVariant(_raw)
            Physics2DDirectBodyStateSW::class -> Physics2DDirectBodyStateSW.getFromVariant(_raw)
            Physics2DDirectSpaceState::class -> Physics2DDirectSpaceState.getFromVariant(_raw)
            Physics2DServer::class -> Physics2DServer.getFromVariant(_raw)
            Physics2DServerSW::class -> Physics2DServerSW.getFromVariant(_raw)
            Physics2DShapeQueryParameters::class -> Physics2DShapeQueryParameters.getFromVariant(_raw)
            Physics2DShapeQueryResult::class -> Physics2DShapeQueryResult.getFromVariant(_raw)
            Physics2DTestMotionResult::class -> Physics2DTestMotionResult.getFromVariant(_raw)
            PhysicsBody2D::class -> PhysicsBody2D.getFromVariant(_raw)
            PhysicsBody::class -> PhysicsBody.getFromVariant(_raw)
            PhysicsDirectBodyState::class -> PhysicsDirectBodyState.getFromVariant(_raw)
            PhysicsDirectSpaceState::class -> PhysicsDirectSpaceState.getFromVariant(_raw)
            PhysicsMaterial::class -> PhysicsMaterial.getFromVariant(_raw)
            PhysicsServer::class -> PhysicsServer.getFromVariant(_raw)
            PhysicsShapeQueryParameters::class -> PhysicsShapeQueryParameters.getFromVariant(_raw)
            PhysicsShapeQueryResult::class -> PhysicsShapeQueryResult.getFromVariant(_raw)
            PinJoint2D::class -> PinJoint2D.getFromVariant(_raw)
            PinJoint::class -> PinJoint.getFromVariant(_raw)
            PlaneMesh::class -> PlaneMesh.getFromVariant(_raw)
            PlaneShape::class -> PlaneShape.getFromVariant(_raw)
            PluginScript::class -> PluginScript.getFromVariant(_raw)
            Polygon2D::class -> Polygon2D.getFromVariant(_raw)
            PolygonPathFinder::class -> PolygonPathFinder.getFromVariant(_raw)
            Popup::class -> Popup.getFromVariant(_raw)
            PopupDialog::class -> PopupDialog.getFromVariant(_raw)
            PopupMenu::class -> PopupMenu.getFromVariant(_raw)
            PopupPanel::class -> PopupPanel.getFromVariant(_raw)
            Position2D::class -> Position2D.getFromVariant(_raw)
            Position3D::class -> Position3D.getFromVariant(_raw)
            PrimitiveMesh::class -> PrimitiveMesh.getFromVariant(_raw)
            PrismMesh::class -> PrismMesh.getFromVariant(_raw)
            ProceduralSky::class -> ProceduralSky.getFromVariant(_raw)
            ProgressBar::class -> ProgressBar.getFromVariant(_raw)
            ProjectSettings::class -> ProjectSettings.getFromVariant(_raw)
            ProximityGroup::class -> ProximityGroup.getFromVariant(_raw)
            ProxyTexture::class -> ProxyTexture.getFromVariant(_raw)
            QuadMesh::class -> QuadMesh.getFromVariant(_raw)
            RandomNumberGenerator::class -> RandomNumberGenerator.getFromVariant(_raw)
            Range::class -> Range.getFromVariant(_raw)
            RayCast2D::class -> RayCast2D.getFromVariant(_raw)
            RayCast::class -> RayCast.getFromVariant(_raw)
            RayShape2D::class -> RayShape2D.getFromVariant(_raw)
            RayShape::class -> RayShape.getFromVariant(_raw)
            RectangleShape2D::class -> RectangleShape2D.getFromVariant(_raw)
            Reference::class -> Reference.getFromVariant(_raw)
            ReferenceRect::class -> ReferenceRect.getFromVariant(_raw)
            ReflectionProbe::class -> ReflectionProbe.getFromVariant(_raw)
            RegEx::class -> RegEx.getFromVariant(_raw)
            RegExMatch::class -> RegExMatch.getFromVariant(_raw)
            RemoteTransform2D::class -> RemoteTransform2D.getFromVariant(_raw)
            RemoteTransform::class -> RemoteTransform.getFromVariant(_raw)
            Resource::class -> Resource.getFromVariant(_raw)
            ResourceFormatDDS::class -> ResourceFormatDDS.getFromVariant(_raw)
            ResourceFormatImporter::class -> ResourceFormatImporter.getFromVariant(_raw)
            ResourceFormatLoader::class -> ResourceFormatLoader.getFromVariant(_raw)
            ResourceFormatLoaderBMFont::class -> ResourceFormatLoaderBMFont.getFromVariant(_raw)
            ResourceFormatLoaderBinary::class -> ResourceFormatLoaderBinary.getFromVariant(_raw)
            ResourceFormatLoaderDynamicFont::class -> ResourceFormatLoaderDynamicFont.getFromVariant(_raw)
            ResourceFormatLoaderGDScript::class -> ResourceFormatLoaderGDScript.getFromVariant(_raw)
            ResourceFormatLoaderImage::class -> ResourceFormatLoaderImage.getFromVariant(_raw)
            ResourceFormatLoaderNativeScript::class -> ResourceFormatLoaderNativeScript.getFromVariant(_raw)
            ResourceFormatLoaderShader::class -> ResourceFormatLoaderShader.getFromVariant(_raw)
            ResourceFormatLoaderStreamTexture::class -> ResourceFormatLoaderStreamTexture.getFromVariant(_raw)
            ResourceFormatLoaderText::class -> ResourceFormatLoaderText.getFromVariant(_raw)
            ResourceFormatLoaderTextureLayered::class -> ResourceFormatLoaderTextureLayered.getFromVariant(_raw)
            ResourceFormatLoaderTheora::class -> ResourceFormatLoaderTheora.getFromVariant(_raw)
            ResourceFormatLoaderVideoStreamGDNative::class -> ResourceFormatLoaderVideoStreamGDNative.getFromVariant(_raw)
            ResourceFormatLoaderWebm::class -> ResourceFormatLoaderWebm.getFromVariant(_raw)
            ResourceFormatPKM::class -> ResourceFormatPKM.getFromVariant(_raw)
            ResourceFormatPVR::class -> ResourceFormatPVR.getFromVariant(_raw)
            ResourceFormatSaver::class -> ResourceFormatSaver.getFromVariant(_raw)
            ResourceFormatSaverBinary::class -> ResourceFormatSaverBinary.getFromVariant(_raw)
            ResourceFormatSaverGDScript::class -> ResourceFormatSaverGDScript.getFromVariant(_raw)
            ResourceFormatSaverNativeScript::class -> ResourceFormatSaverNativeScript.getFromVariant(_raw)
            ResourceFormatSaverShader::class -> ResourceFormatSaverShader.getFromVariant(_raw)
            ResourceFormatSaverText::class -> ResourceFormatSaverText.getFromVariant(_raw)
            ResourceImporter::class -> ResourceImporter.getFromVariant(_raw)
            ResourceImporterOGGVorbis::class -> ResourceImporterOGGVorbis.getFromVariant(_raw)
            ResourceInteractiveLoader::class -> ResourceInteractiveLoader.getFromVariant(_raw)
            ResourcePreloader::class -> ResourcePreloader.getFromVariant(_raw)
            ResourceSaverPNG::class -> ResourceSaverPNG.getFromVariant(_raw)
            RichTextLabel::class -> RichTextLabel.getFromVariant(_raw)
            RigidBody2D::class -> RigidBody2D.getFromVariant(_raw)
            RigidBody::class -> RigidBody.getFromVariant(_raw)
            RootMotionView::class -> RootMotionView.getFromVariant(_raw)
            SceneState::class -> SceneState.getFromVariant(_raw)
            SceneTree::class -> SceneTree.getFromVariant(_raw)
            SceneTreeTimer::class -> SceneTreeTimer.getFromVariant(_raw)
            Script::class -> Script.getFromVariant(_raw)
            ScriptCreateDialog::class -> ScriptCreateDialog.getFromVariant(_raw)
            ScriptEditor::class -> ScriptEditor.getFromVariant(_raw)
            ScrollBar::class -> ScrollBar.getFromVariant(_raw)
            ScrollContainer::class -> ScrollContainer.getFromVariant(_raw)
            SegmentShape2D::class -> SegmentShape2D.getFromVariant(_raw)
            Separator::class -> Separator.getFromVariant(_raw)
            Shader::class -> Shader.getFromVariant(_raw)
            ShaderMaterial::class -> ShaderMaterial.getFromVariant(_raw)
            Shape2D::class -> Shape2D.getFromVariant(_raw)
            Shape::class -> Shape.getFromVariant(_raw)
            ShortCut::class -> ShortCut.getFromVariant(_raw)
            Skeleton2D::class -> Skeleton2D.getFromVariant(_raw)
            Skeleton::class -> Skeleton.getFromVariant(_raw)
            SkeletonIK::class -> SkeletonIK.getFromVariant(_raw)
            Sky::class -> Sky.getFromVariant(_raw)
            Slider::class -> Slider.getFromVariant(_raw)
            SliderJoint::class -> SliderJoint.getFromVariant(_raw)
            SoftBody::class -> SoftBody.getFromVariant(_raw)
            Spatial::class -> Spatial.getFromVariant(_raw)
            SpatialGizmo::class -> SpatialGizmo.getFromVariant(_raw)
            SpatialMaterial::class -> SpatialMaterial.getFromVariant(_raw)
            SpatialVelocityTracker::class -> SpatialVelocityTracker.getFromVariant(_raw)
            SphereMesh::class -> SphereMesh.getFromVariant(_raw)
            SphereShape::class -> SphereShape.getFromVariant(_raw)
            SpinBox::class -> SpinBox.getFromVariant(_raw)
            SplitContainer::class -> SplitContainer.getFromVariant(_raw)
            SpotLight::class -> SpotLight.getFromVariant(_raw)
            SpringArm::class -> SpringArm.getFromVariant(_raw)
            Sprite3D::class -> Sprite3D.getFromVariant(_raw)
            Sprite::class -> Sprite.getFromVariant(_raw)
            SpriteBase3D::class -> SpriteBase3D.getFromVariant(_raw)
            SpriteFrames::class -> SpriteFrames.getFromVariant(_raw)
            StaticBody2D::class -> StaticBody2D.getFromVariant(_raw)
            StaticBody::class -> StaticBody.getFromVariant(_raw)
            StreamPeer::class -> StreamPeer.getFromVariant(_raw)
            StreamPeerBuffer::class -> StreamPeerBuffer.getFromVariant(_raw)
            StreamPeerGDNative::class -> StreamPeerGDNative.getFromVariant(_raw)
            StreamPeerSSL::class -> StreamPeerSSL.getFromVariant(_raw)
            StreamPeerTCP::class -> StreamPeerTCP.getFromVariant(_raw)
            StreamTexture::class -> StreamTexture.getFromVariant(_raw)
            StyleBox::class -> StyleBox.getFromVariant(_raw)
            StyleBoxEmpty::class -> StyleBoxEmpty.getFromVariant(_raw)
            StyleBoxFlat::class -> StyleBoxFlat.getFromVariant(_raw)
            StyleBoxLine::class -> StyleBoxLine.getFromVariant(_raw)
            StyleBoxTexture::class -> StyleBoxTexture.getFromVariant(_raw)
            SurfaceTool::class -> SurfaceTool.getFromVariant(_raw)
            TCP_Server::class -> TCP_Server.getFromVariant(_raw)
            TabContainer::class -> TabContainer.getFromVariant(_raw)
            Tabs::class -> Tabs.getFromVariant(_raw)
            TextEdit::class -> TextEdit.getFromVariant(_raw)
            TextFile::class -> TextFile.getFromVariant(_raw)
            Texture3D::class -> Texture3D.getFromVariant(_raw)
            Texture::class -> Texture.getFromVariant(_raw)
            TextureArray::class -> TextureArray.getFromVariant(_raw)
            TextureButton::class -> TextureButton.getFromVariant(_raw)
            TextureLayered::class -> TextureLayered.getFromVariant(_raw)
            TextureProgress::class -> TextureProgress.getFromVariant(_raw)
            TextureRect::class -> TextureRect.getFromVariant(_raw)
            Theme::class -> Theme.getFromVariant(_raw)
            TileMap::class -> TileMap.getFromVariant(_raw)
            TileSet::class -> TileSet.getFromVariant(_raw)
            Timer::class -> Timer.getFromVariant(_raw)
            ToolButton::class -> ToolButton.getFromVariant(_raw)
            TouchScreenButton::class -> TouchScreenButton.getFromVariant(_raw)
            Translation::class -> Translation.getFromVariant(_raw)
            TranslationLoaderPO::class -> TranslationLoaderPO.getFromVariant(_raw)
            TranslationServer::class -> TranslationServer.getFromVariant(_raw)
            Tree::class -> Tree.getFromVariant(_raw)
            TreeItem::class -> TreeItem.getFromVariant(_raw)
            TriangleMesh::class -> TriangleMesh.getFromVariant(_raw)
            Tween::class -> Tween.getFromVariant(_raw)
            UPNP::class -> UPNP.getFromVariant(_raw)
            UPNPDevice::class -> UPNPDevice.getFromVariant(_raw)
            UndoRedo::class -> UndoRedo.getFromVariant(_raw)
            VBoxContainer::class -> VBoxContainer.getFromVariant(_raw)
            VScrollBar::class -> VScrollBar.getFromVariant(_raw)
            VSeparator::class -> VSeparator.getFromVariant(_raw)
            VSlider::class -> VSlider.getFromVariant(_raw)
            VSplitContainer::class -> VSplitContainer.getFromVariant(_raw)
            VehicleBody::class -> VehicleBody.getFromVariant(_raw)
            VehicleWheel::class -> VehicleWheel.getFromVariant(_raw)
            VideoPlayer::class -> VideoPlayer.getFromVariant(_raw)
            VideoStream::class -> VideoStream.getFromVariant(_raw)
            VideoStreamGDNative::class -> VideoStreamGDNative.getFromVariant(_raw)
            VideoStreamTheora::class -> VideoStreamTheora.getFromVariant(_raw)
            VideoStreamWebm::class -> VideoStreamWebm.getFromVariant(_raw)
            Viewport::class -> Viewport.getFromVariant(_raw)
            ViewportContainer::class -> ViewportContainer.getFromVariant(_raw)
            ViewportTexture::class -> ViewportTexture.getFromVariant(_raw)
            VisibilityEnabler2D::class -> VisibilityEnabler2D.getFromVariant(_raw)
            VisibilityEnabler::class -> VisibilityEnabler.getFromVariant(_raw)
            VisibilityNotifier2D::class -> VisibilityNotifier2D.getFromVariant(_raw)
            VisibilityNotifier::class -> VisibilityNotifier.getFromVariant(_raw)
            VisualInstance::class -> VisualInstance.getFromVariant(_raw)
            VisualScript::class -> VisualScript.getFromVariant(_raw)
            VisualScriptBasicTypeConstant::class -> VisualScriptBasicTypeConstant.getFromVariant(_raw)
            VisualScriptBuiltinFunc::class -> VisualScriptBuiltinFunc.getFromVariant(_raw)
            VisualScriptClassConstant::class -> VisualScriptClassConstant.getFromVariant(_raw)
            VisualScriptComment::class -> VisualScriptComment.getFromVariant(_raw)
            VisualScriptCondition::class -> VisualScriptCondition.getFromVariant(_raw)
            VisualScriptConstant::class -> VisualScriptConstant.getFromVariant(_raw)
            VisualScriptConstructor::class -> VisualScriptConstructor.getFromVariant(_raw)
            VisualScriptCustomNode::class -> VisualScriptCustomNode.getFromVariant(_raw)
            VisualScriptDeconstruct::class -> VisualScriptDeconstruct.getFromVariant(_raw)
            VisualScriptEmitSignal::class -> VisualScriptEmitSignal.getFromVariant(_raw)
            VisualScriptEngineSingleton::class -> VisualScriptEngineSingleton.getFromVariant(_raw)
            VisualScriptExpression::class -> VisualScriptExpression.getFromVariant(_raw)
            VisualScriptFunction::class -> VisualScriptFunction.getFromVariant(_raw)
            VisualScriptFunctionCall::class -> VisualScriptFunctionCall.getFromVariant(_raw)
            VisualScriptFunctionState::class -> VisualScriptFunctionState.getFromVariant(_raw)
            VisualScriptGlobalConstant::class -> VisualScriptGlobalConstant.getFromVariant(_raw)
            VisualScriptIndexGet::class -> VisualScriptIndexGet.getFromVariant(_raw)
            VisualScriptIndexSet::class -> VisualScriptIndexSet.getFromVariant(_raw)
            VisualScriptInputAction::class -> VisualScriptInputAction.getFromVariant(_raw)
            VisualScriptIterator::class -> VisualScriptIterator.getFromVariant(_raw)
            VisualScriptLocalVar::class -> VisualScriptLocalVar.getFromVariant(_raw)
            VisualScriptLocalVarSet::class -> VisualScriptLocalVarSet.getFromVariant(_raw)
            VisualScriptMathConstant::class -> VisualScriptMathConstant.getFromVariant(_raw)
            VisualScriptNode::class -> VisualScriptNode.getFromVariant(_raw)
            VisualScriptOperator::class -> VisualScriptOperator.getFromVariant(_raw)
            VisualScriptPreload::class -> VisualScriptPreload.getFromVariant(_raw)
            VisualScriptPropertyGet::class -> VisualScriptPropertyGet.getFromVariant(_raw)
            VisualScriptPropertySet::class -> VisualScriptPropertySet.getFromVariant(_raw)
            VisualScriptResourcePath::class -> VisualScriptResourcePath.getFromVariant(_raw)
            VisualScriptReturn::class -> VisualScriptReturn.getFromVariant(_raw)
            VisualScriptSceneNode::class -> VisualScriptSceneNode.getFromVariant(_raw)
            VisualScriptSceneTree::class -> VisualScriptSceneTree.getFromVariant(_raw)
            VisualScriptSelect::class -> VisualScriptSelect.getFromVariant(_raw)
            VisualScriptSelf::class -> VisualScriptSelf.getFromVariant(_raw)
            VisualScriptSequence::class -> VisualScriptSequence.getFromVariant(_raw)
            VisualScriptSubCall::class -> VisualScriptSubCall.getFromVariant(_raw)
            VisualScriptSwitch::class -> VisualScriptSwitch.getFromVariant(_raw)
            VisualScriptTypeCast::class -> VisualScriptTypeCast.getFromVariant(_raw)
            VisualScriptVariableGet::class -> VisualScriptVariableGet.getFromVariant(_raw)
            VisualScriptVariableSet::class -> VisualScriptVariableSet.getFromVariant(_raw)
            VisualScriptWhile::class -> VisualScriptWhile.getFromVariant(_raw)
            VisualScriptYield::class -> VisualScriptYield.getFromVariant(_raw)
            VisualScriptYieldSignal::class -> VisualScriptYieldSignal.getFromVariant(_raw)
            VisualServer::class -> VisualServer.getFromVariant(_raw)
            VisualShader::class -> VisualShader.getFromVariant(_raw)
            VisualShaderNode::class -> VisualShaderNode.getFromVariant(_raw)
            VisualShaderNodeBooleanConstant::class -> VisualShaderNodeBooleanConstant.getFromVariant(_raw)
            VisualShaderNodeBooleanUniform::class -> VisualShaderNodeBooleanUniform.getFromVariant(_raw)
            VisualShaderNodeColorConstant::class -> VisualShaderNodeColorConstant.getFromVariant(_raw)
            VisualShaderNodeColorFunc::class -> VisualShaderNodeColorFunc.getFromVariant(_raw)
            VisualShaderNodeColorOp::class -> VisualShaderNodeColorOp.getFromVariant(_raw)
            VisualShaderNodeColorUniform::class -> VisualShaderNodeColorUniform.getFromVariant(_raw)
            VisualShaderNodeCubeMap::class -> VisualShaderNodeCubeMap.getFromVariant(_raw)
            VisualShaderNodeCubeMapUniform::class -> VisualShaderNodeCubeMapUniform.getFromVariant(_raw)
            VisualShaderNodeDeterminant::class -> VisualShaderNodeDeterminant.getFromVariant(_raw)
            VisualShaderNodeDotProduct::class -> VisualShaderNodeDotProduct.getFromVariant(_raw)
            VisualShaderNodeExpression::class -> VisualShaderNodeExpression.getFromVariant(_raw)
            VisualShaderNodeFaceForward::class -> VisualShaderNodeFaceForward.getFromVariant(_raw)
            VisualShaderNodeFresnel::class -> VisualShaderNodeFresnel.getFromVariant(_raw)
            VisualShaderNodeGroupBase::class -> VisualShaderNodeGroupBase.getFromVariant(_raw)
            VisualShaderNodeIf::class -> VisualShaderNodeIf.getFromVariant(_raw)
            VisualShaderNodeInput::class -> VisualShaderNodeInput.getFromVariant(_raw)
            VisualShaderNodeOuterProduct::class -> VisualShaderNodeOuterProduct.getFromVariant(_raw)
            VisualShaderNodeOutput::class -> VisualShaderNodeOutput.getFromVariant(_raw)
            VisualShaderNodeScalarClamp::class -> VisualShaderNodeScalarClamp.getFromVariant(_raw)
            VisualShaderNodeScalarConstant::class -> VisualShaderNodeScalarConstant.getFromVariant(_raw)
            VisualShaderNodeScalarDerivativeFunc::class -> VisualShaderNodeScalarDerivativeFunc.getFromVariant(_raw)
            VisualShaderNodeScalarFunc::class -> VisualShaderNodeScalarFunc.getFromVariant(_raw)
            VisualShaderNodeScalarInterp::class -> VisualShaderNodeScalarInterp.getFromVariant(_raw)
            VisualShaderNodeScalarOp::class -> VisualShaderNodeScalarOp.getFromVariant(_raw)
            VisualShaderNodeScalarSmoothStep::class -> VisualShaderNodeScalarSmoothStep.getFromVariant(_raw)
            VisualShaderNodeScalarUniform::class -> VisualShaderNodeScalarUniform.getFromVariant(_raw)
            VisualShaderNodeSwitch::class -> VisualShaderNodeSwitch.getFromVariant(_raw)
            VisualShaderNodeTexture::class -> VisualShaderNodeTexture.getFromVariant(_raw)
            VisualShaderNodeTextureUniform::class -> VisualShaderNodeTextureUniform.getFromVariant(_raw)
            VisualShaderNodeTransformCompose::class -> VisualShaderNodeTransformCompose.getFromVariant(_raw)
            VisualShaderNodeTransformConstant::class -> VisualShaderNodeTransformConstant.getFromVariant(_raw)
            VisualShaderNodeTransformDecompose::class -> VisualShaderNodeTransformDecompose.getFromVariant(_raw)
            VisualShaderNodeTransformFunc::class -> VisualShaderNodeTransformFunc.getFromVariant(_raw)
            VisualShaderNodeTransformMult::class -> VisualShaderNodeTransformMult.getFromVariant(_raw)
            VisualShaderNodeTransformUniform::class -> VisualShaderNodeTransformUniform.getFromVariant(_raw)
            VisualShaderNodeTransformVecMult::class -> VisualShaderNodeTransformVecMult.getFromVariant(_raw)
            VisualShaderNodeUniform::class -> VisualShaderNodeUniform.getFromVariant(_raw)
            VisualShaderNodeVec3Constant::class -> VisualShaderNodeVec3Constant.getFromVariant(_raw)
            VisualShaderNodeVec3Uniform::class -> VisualShaderNodeVec3Uniform.getFromVariant(_raw)
            VisualShaderNodeVectorClamp::class -> VisualShaderNodeVectorClamp.getFromVariant(_raw)
            VisualShaderNodeVectorCompose::class -> VisualShaderNodeVectorCompose.getFromVariant(_raw)
            VisualShaderNodeVectorDecompose::class -> VisualShaderNodeVectorDecompose.getFromVariant(_raw)
            VisualShaderNodeVectorDerivativeFunc::class -> VisualShaderNodeVectorDerivativeFunc.getFromVariant(_raw)
            VisualShaderNodeVectorDistance::class -> VisualShaderNodeVectorDistance.getFromVariant(_raw)
            VisualShaderNodeVectorFunc::class -> VisualShaderNodeVectorFunc.getFromVariant(_raw)
            VisualShaderNodeVectorInterp::class -> VisualShaderNodeVectorInterp.getFromVariant(_raw)
            VisualShaderNodeVectorLen::class -> VisualShaderNodeVectorLen.getFromVariant(_raw)
            VisualShaderNodeVectorOp::class -> VisualShaderNodeVectorOp.getFromVariant(_raw)
            VisualShaderNodeVectorRefract::class -> VisualShaderNodeVectorRefract.getFromVariant(_raw)
            VisualShaderNodeVectorScalarSmoothStep::class -> VisualShaderNodeVectorScalarSmoothStep.getFromVariant(_raw)
            VisualShaderNodeVectorScalarStep::class -> VisualShaderNodeVectorScalarStep.getFromVariant(_raw)
            VisualShaderNodeVectorSmoothStep::class -> VisualShaderNodeVectorSmoothStep.getFromVariant(_raw)
            WeakRef::class -> WeakRef.getFromVariant(_raw)
            WebRTCDataChannel::class -> WebRTCDataChannel.getFromVariant(_raw)
            WebRTCDataChannelGDNative::class -> WebRTCDataChannelGDNative.getFromVariant(_raw)
            WebRTCMultiplayer::class -> WebRTCMultiplayer.getFromVariant(_raw)
            WebRTCPeerConnection::class -> WebRTCPeerConnection.getFromVariant(_raw)
            WebRTCPeerConnectionGDNative::class -> WebRTCPeerConnectionGDNative.getFromVariant(_raw)
            WebSocketClient::class -> WebSocketClient.getFromVariant(_raw)
            WebSocketMultiplayerPeer::class -> WebSocketMultiplayerPeer.getFromVariant(_raw)
            WebSocketPeer::class -> WebSocketPeer.getFromVariant(_raw)
            WebSocketServer::class -> WebSocketServer.getFromVariant(_raw)
            WindowDialog::class -> WindowDialog.getFromVariant(_raw)
            World2D::class -> World2D.getFromVariant(_raw)
            World::class -> World.getFromVariant(_raw)
            WorldEnvironment::class -> WorldEnvironment.getFromVariant(_raw)
            XMLParser::class -> XMLParser.getFromVariant(_raw)
            YSort::class -> YSort.getFromVariant(_raw)
            _ClassDB::class -> _ClassDB.getFromVariant(_raw)
            _Directory::class -> _Directory.getFromVariant(_raw)
            _Engine::class -> _Engine.getFromVariant(_raw)
            _File::class -> _File.getFromVariant(_raw)
            _Geometry::class -> _Geometry.getFromVariant(_raw)
            _JSON::class -> _JSON.getFromVariant(_raw)
            _Marshalls::class -> _Marshalls.getFromVariant(_raw)
            _Mutex::class -> _Mutex.getFromVariant(_raw)
            _OS::class -> _OS.getFromVariant(_raw)
            _ResourceLoader::class -> _ResourceLoader.getFromVariant(_raw)
            _ResourceSaver::class -> _ResourceSaver.getFromVariant(_raw)
            _Semaphore::class -> _Semaphore.getFromVariant(_raw)
            _Thread::class -> _Thread.getFromVariant(_raw)
            _VisualScriptEditor::class -> _VisualScriptEditor.getFromVariant(_raw)
            else -> throw UnsupportedOperationException()
        }
        return result as T
    }

    fun toMutableMap(): MutableMap<Variant, Any?> = memScoped {
        return api.godot_variant_as_dictionary!!(_raw).ptr.toKMutableMap()
    }

    fun toArray(): Array<Variant> = memScoped {
        return api.godot_variant_as_array!!(_raw).ptr.toKArray()
    }

    fun toPoolByteArray(): PoolByteArray {
        return PoolByteArray(api.godot_variant_as_pool_byte_array!!(_raw))
    }

    fun toPoolIntArray(): PoolIntArray {
        return PoolIntArray(api.godot_variant_as_pool_int_array!!(_raw))
    }

    fun toPoolFloatArray(): PoolFloatArray {
        return PoolFloatArray(api.godot_variant_as_pool_real_array!!(_raw))
    }

    fun toPoolStringArray(): PoolStringArray {
        return PoolStringArray(api.godot_variant_as_pool_string_array!!(_raw))
    }

    fun toPoolVector2Array(): PoolVector2Array {
        return PoolVector2Array(api.godot_variant_as_pool_vector2_array!!(_raw))
    }

    fun toPoolVector3Array(): PoolVector3Array {
        return PoolVector3Array(api.godot_variant_as_pool_vector3_array!!(_raw))
    }

    fun toPoolColorArray(): PoolColorArray {
        return PoolColorArray(api.godot_variant_as_pool_color_array!!(_raw))
    }

    fun getType(): Type {
        return Type.values()[godot.api.godot_variant_get_type!!(_raw).ordinal]
    }

    fun call(method: String, args: Variant, argCount: Int): Variant {
        return Variant(api.godot_variant_call!!(_raw, method.toGString(), args._raw.reinterpret(), argCount, null))
    }

    fun hasMethod(method: String): Boolean {
        return api.godot_variant_has_method!!(_raw, method.toGString())
    }

    override fun hashCode(): Int {
        return _raw.pointed.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Variant)
            api.godot_variant_operator_equal!!(_raw, other._raw)
        else false
    }

    override operator fun compareTo(other: Variant): Int {
        return if (equals(other)) 0
        else if (api.godot_variant_operator_less!!(_raw, other._raw)) -1
        else 1
    }

    fun destroy() {
        api.godot_variant_destroy!!(_raw)
    }
}