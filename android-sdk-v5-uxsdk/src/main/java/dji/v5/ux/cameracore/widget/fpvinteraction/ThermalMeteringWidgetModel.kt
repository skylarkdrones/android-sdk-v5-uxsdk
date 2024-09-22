package dji.v5.ux.cameracore.widget.fpvinteraction

import android.util.Log
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.camera.ThermalDisplayMode
import dji.sdk.keyvalue.value.camera.ThermalTemperatureMeasureMode
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.common.DoublePoint2D
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.et.create
import dji.v5.et.createCamera
import dji.v5.manager.KeyManager
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.WidgetModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor

class ThermalMeteringWidgetModel(
    djiSdkModel: DJISDKModel,
    uxKeyManager: ObservableInMemoryKeyedStore,
) : WidgetModel(djiSdkModel, uxKeyManager), ICameraIndex {

    private var cameraIndex = ComponentIndexType.LEFT_OR_MAIN
    private var lensType = CameraLensType.CAMERA_LENS_THERMAL

    val temperatureProcessor: DataProcessor<Double> = DataProcessor.create(0.0)
    private val streamSourceCameraTypeProcessor = DataProcessor.create(CameraVideoStreamSourceType.UNKNOWN)

    val isSBSOnProcessor = DataProcessor.create(false)
    private val thermalDisplayModeProcessor = DataProcessor.create(ThermalDisplayMode.UNKNOWN)

    override fun inSetup() {
        bindDataProcessor(CameraKey.KeyCameraVideoStreamSource.create(cameraIndex), streamSourceCameraTypeProcessor) {
            lensType = when (it) {
                CameraVideoStreamSourceType.WIDE_CAMERA -> CameraLensType.CAMERA_LENS_WIDE
                CameraVideoStreamSourceType.ZOOM_CAMERA -> CameraLensType.CAMERA_LENS_ZOOM
                CameraVideoStreamSourceType.INFRARED_CAMERA -> CameraLensType.CAMERA_LENS_THERMAL
                CameraVideoStreamSourceType.NDVI_CAMERA -> CameraLensType.CAMERA_LENS_MS_NDVI
                CameraVideoStreamSourceType.MS_G_CAMERA -> CameraLensType.CAMERA_LENS_MS_G
                CameraVideoStreamSourceType.MS_R_CAMERA -> CameraLensType.CAMERA_LENS_MS_R
                CameraVideoStreamSourceType.MS_RE_CAMERA -> CameraLensType.CAMERA_LENS_MS_RE
                CameraVideoStreamSourceType.MS_NIR_CAMERA -> CameraLensType.CAMERA_LENS_MS_NIR
                CameraVideoStreamSourceType.RGB_CAMERA -> CameraLensType.CAMERA_LENS_RGB
                else -> CameraLensType.CAMERA_LENS_DEFAULT
            }
        }

        bindDataProcessor(
            CameraKey.KeyThermalDisplayMode.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ), thermalDisplayModeProcessor
        ) {
            isSBSOnProcessor.onNext(it == ThermalDisplayMode.PIP)
        }

        val tempKey = KeyTools.createCameraKey(
            CameraKey.KeyThermalSpotMetersureTemperature,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )

        bindDataProcessor(tempKey, temperatureProcessor) {
            temperatureProcessor.onNext(it)
        }
    }

    fun setMeasureMode() {
        val key = KeyTools.createCameraKey(
            CameraKey.KeyThermalTemperatureMeasureMode,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )

        KeyManager.getInstance().setValue(
            key,
            ThermalTemperatureMeasureMode.SPOT,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    /* no-op */
                }

                override fun onFailure(error: IDJIError) {
                    /* no-op */
                }
            })
    }

    fun disableMeasureMode() {
        val key = KeyTools.createCameraKey(
            CameraKey.KeyThermalTemperatureMeasureMode,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )

        KeyManager.getInstance().setValue(
            key,
            ThermalTemperatureMeasureMode.DISABLED,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    /* no-op */
                }

                override fun onFailure(error: IDJIError) {
                    /* no-op */
                }
            })
    }

    fun setSpotMetersurePoint(x: Double, y: Double) {
        setMeasureMode()
        val pointkey = KeyTools.createCameraKey(
            CameraKey.KeyThermalSpotMetersurePoint,
            cameraIndex,
            lensType
        )
        KeyManager.getInstance().setValue(
            pointkey,
            DoublePoint2D(x, y),
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    /* no-op */
                }

                override fun onFailure(error: IDJIError) {
                    /* no-op */
                }
            })
    }

    override fun inCleanup() {
        //do nothing
    }

    override fun getCameraIndex(): ComponentIndexType {
        return cameraIndex
    }

    override fun getLensType(): CameraLensType {
        return lensType
    }

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        this.cameraIndex = cameraIndex
        this.lensType = lensType
        restart()
    }
}
