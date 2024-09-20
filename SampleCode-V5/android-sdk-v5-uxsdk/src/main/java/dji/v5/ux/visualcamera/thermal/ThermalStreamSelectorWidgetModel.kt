package dji.v5.ux.visualcamera.thermal

import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.et.create
import dji.v5.et.createCamera
import dji.v5.ux.core.base.CameraWidgetModel
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor

class ThermalStreamSelectorWidgetModel(
    djiSdkModel: DJISDKModel,
    keyedStore: ObservableInMemoryKeyedStore,
) : CameraWidgetModel(djiSdkModel, keyedStore) {

    val cameraVideoStreamSourceProcessor: DataProcessor<CameraVideoStreamSourceType> = DataProcessor.create(CameraVideoStreamSourceType.UNKNOWN)
    val cameraThermalPaletteProcessor: DataProcessor<CameraThermalPalette> = DataProcessor.create(CameraThermalPalette.UNKNOWN)

    override fun inSetup() {
        bindDataProcessor(
            CameraKey.KeyCameraVideoStreamSource.create(cameraIndex),
            cameraVideoStreamSourceProcessor
        )
        bindDataProcessor(
            CameraKey.KeyThermalPalette.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ),
            cameraThermalPaletteProcessor
        )
    }

    override fun inCleanup() {
        // nothing to clean
    }

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        if (this.cameraIndex == cameraIndex) {
            return
        }
        this.cameraIndex = cameraIndex
        restart()
    }
}