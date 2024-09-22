package dji.v5.ux.visualcamera.thermal

import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.v5.et.create
import dji.v5.ux.core.base.CameraWidgetModel
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor

class CameraThermalPanelWidgetModel(
    djiSdkModel: DJISDKModel,
    keyedStore: ObservableInMemoryKeyedStore,
) : CameraWidgetModel(djiSdkModel, keyedStore) {

    val cameraVideoStreamSourceProcessor: DataProcessor<CameraVideoStreamSourceType> =
        DataProcessor.create(
            CameraVideoStreamSourceType.UNKNOWN
        )


    override fun inSetup() {
        bindDataProcessor(
            CameraKey.KeyCameraVideoStreamSource.create(cameraIndex),
            cameraVideoStreamSourceProcessor
        ) {
            cameraVideoStreamSourceProcessor.onNext(it)
        }
    }

    override fun inCleanup() {
        // nothing to clean
    }
}