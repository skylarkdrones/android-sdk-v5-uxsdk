package dji.v5.ux.remotecontroller.customiztion

/**
 * Represents different types of custom button actions
 */
enum class ButtonAction(val desc: String) {
    GIMBAL_DOWN("Gimbal Down"),
    GIMBAL_RECENTER("Gimbal Recenter"),
    GIMBAL_RECENTER_DOWN("Gimbal Recenter/Down"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    UNDEFINED("Undefined"),
}