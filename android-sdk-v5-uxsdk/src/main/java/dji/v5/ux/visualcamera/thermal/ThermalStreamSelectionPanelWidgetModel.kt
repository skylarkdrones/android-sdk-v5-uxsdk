package dji.v5.ux.visualcamera.thermal

import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.v5.et.create
import dji.v5.et.createCamera
import dji.v5.ux.core.base.CameraWidgetModel
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor
import io.reactivex.rxjava3.core.Completable

class ThermalStreamSelectionPanelWidgetModel(
    djiSdkModel: DJISDKModel,
    keyedStore: ObservableInMemoryKeyedStore,
) : CameraWidgetModel(djiSdkModel, keyedStore) {

    val cameraVideoStreamSourceProcessor: DataProcessor<CameraVideoStreamSourceType> =
        DataProcessor.create(CameraVideoStreamSourceType.UNKNOWN)

    val thermalPaletteModels = StreamPanelUtil.THERMAL_PALETTE_MODEL_LIST
    private val cameraThermalPaletteDataProcessor: DataProcessor<CameraThermalPalette> =
        DataProcessor.create(CameraThermalPalette.UNKNOWN)
    val currentPaletteModelProcessor: DataProcessor<StreamPanelUtil.ThermalPaletteModel> =
        DataProcessor.create(
            (StreamPanelUtil.ThermalPaletteModel(
                CameraThermalPalette.UNKNOWN,
                "UNKNOWN",
                -1
            ))
        )
    private val isShootingContinuousPhotosProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val isShootingVisionPanoramaPhotoProcessor: DataProcessor<Boolean> = DataProcessor.create(false)

    val isEnableProcessor: DataProcessor<Boolean> = DataProcessor.create(false)

    override fun inSetup() {
        bindDataProcessor(
            CameraKey.KeyCameraVideoStreamSource.create(cameraIndex),
            cameraVideoStreamSourceProcessor
        ) {
        }
        bindDataProcessor(
            CameraKey.KeyThermalPalette.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ), cameraThermalPaletteDataProcessor
        ) {
            updateCurrentPaletteModelPosition(it)
        }
        bindDataProcessor(
            CameraKey.KeyCameraShootingContinuousPhotos.create(cameraIndex),
            isShootingContinuousPhotosProcessor
        ) {
            updateEnable()
        }
        bindDataProcessor(
            CameraKey.KeyIsShootingPhotoPanorama.create(cameraIndex),
            isShootingVisionPanoramaPhotoProcessor
        ) {
            updateEnable()
        }
    }

    private fun updateCurrentPaletteModelPosition(type: CameraThermalPalette) {
        if (type == CameraThermalPalette.UNKNOWN) {
            currentPaletteModelProcessor.onNext(
                (StreamPanelUtil.ThermalPaletteModel(
                    CameraThermalPalette.UNKNOWN,
                    "UNKNOWN",
                    -1
                ))
            )
            return
        }
        val model = thermalPaletteModels.find { it.sourceType == type }
        model?.let {
            currentPaletteModelProcessor.onNext(it)
        }
    }

    fun setThermalModel(model: StreamPanelUtil.ThermalPaletteModel): Completable {
        if (model.sourceType == cameraThermalPaletteDataProcessor.value) {
            return Completable.complete()
        }
        return djiSdkModel.setValue(
            CameraKey.KeyThermalPalette.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ), model.sourceType
        )
    }

    private fun updateEnable() {
        isEnableProcessor.onNext(!isShootingContinuousPhotosProcessor.value && !isShootingVisionPanoramaPhotoProcessor.value)
    }

    override fun inCleanup() {
        //do nothing
    }
}