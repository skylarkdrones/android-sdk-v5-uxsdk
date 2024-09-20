package dji.v5.ux.visualcamera.thermal

import android.util.Log
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.camera.ThermalAreaMetersureTemperature
import dji.sdk.keyvalue.value.camera.ThermalDisplayMode
import dji.sdk.keyvalue.value.camera.ThermalTemperatureMeasureMode
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.DoubleRect
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.et.create
import dji.v5.et.createCamera
import dji.v5.manager.KeyManager
import dji.v5.ux.core.base.CameraWidgetModel
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ThermalStreamPaletteBarPanelModel(
    djiSdkModel: DJISDKModel,
    keyedStore: ObservableInMemoryKeyedStore,
) : CameraWidgetModel(djiSdkModel, keyedStore) {

    private val streamSourceCameraTypeProcessor =
        DataProcessor.create(CameraVideoStreamSourceType.UNKNOWN)
    val temperatureProcessor: DataProcessor<ThermalAreaMetersureTemperature> =
        DataProcessor.create(ThermalAreaMetersureTemperature())
    private var fetchTemperatureJob: Job? = null
    private var isFetching = false

    val thermalPaletteModels = StreamPanelUtil.THERMAL_PALETTE_MODEL_LIST
    val cameraThermalPaletteDataProcessor: DataProcessor<CameraThermalPalette> =
        DataProcessor.create(CameraThermalPalette.UNKNOWN)
    private val thermalDisplayModeProcessor = DataProcessor.create(ThermalDisplayMode.UNKNOWN)


    override fun inSetup() {
        KeyManager.getInstance().listen(
            CameraKey.KeyThermalPalette.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ), this, object : CommonCallbacks.KeyListener<CameraThermalPalette> {
                override fun onValueChange(p0: CameraThermalPalette?, p1: CameraThermalPalette?) {
                    if (p1 == null) return
                    cameraThermalPaletteDataProcessor.onNext(p1)
                }

            }
        )
        bindDataProcessor(
            CameraKey.KeyCameraVideoStreamSource.create(cameraIndex),
            streamSourceCameraTypeProcessor
        ) {
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
            if (lensType == CameraLensType.CAMERA_LENS_THERMAL) {
                setupTemperatureFetchingJob()
            } else {
                fetchTemperatureJob?.cancel()
            }
        }

        bindDataProcessor(
            CameraKey.KeyThermalDisplayMode.createCamera(
                cameraIndex,
                CameraLensType.CAMERA_LENS_THERMAL
            ), thermalDisplayModeProcessor
        )
    }

    private fun setupTemperatureFetchingJob() {
        Log.d(TAG, "setupTemperatureFetchingJob() called $this")
        if (fetchTemperatureJob != null) return
        try {
            runBlocking {
                fetchTemperatureJob = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        if (!isActive) return@launch
                        if (isFetching) continue
                        isFetching = true
                        setMeasureMode(ThermalTemperatureMeasureMode.REGION)
                        delay(FETCH_TEMPERATURE_INTERVAL_MILLIS)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "inSetup: $e")
        }
    }

    private fun setMeasureMode(mode: ThermalTemperatureMeasureMode) {
        Log.d(TAG, "setMeasureMode() called with: mode = $mode")
        val key = KeyTools.createCameraKey(
            CameraKey.KeyThermalTemperatureMeasureMode,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )

        KeyManager.getInstance().setValue(
            key,
            mode,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    if (mode == ThermalTemperatureMeasureMode.SPOT) return
                    setMetersureArea()
                }

                override fun onFailure(error: IDJIError) {
                    isFetching = false
                    Log.e(TAG, "onFailure: $error")
                }
            })

    }

    private fun setMetersureArea() {
        val pointKey = KeyTools.createCameraKey(
            CameraKey.KeyThermalRegionMetersureArea,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )
        val frame = if (thermalDisplayModeProcessor.value == ThermalDisplayMode.PIP) {
            DoubleRect(0.0, 0.0, 0.5, 0.5) // half frame when SBS is enabled
        } else {
            DoubleRect(0.0, 0.0, 1.0, 1.0) // entire frame
        }
        KeyManager.getInstance().setValue(
            pointKey,
            frame, // region for which temperature has to be measured
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    fetchAreaTemperature()
                }

                override fun onFailure(p0: IDJIError) {
                    isFetching = false
                    Log.e(TAG, "onFailure: $p0")
                }
            }
        )
    }

    private fun fetchAreaTemperature() {
        val tempKey = KeyTools.createCameraKey(
            CameraKey.KeyThermalRegionMetersureTemperature,
            cameraIndex,
            CameraLensType.CAMERA_LENS_THERMAL
        )

        KeyManager.getInstance().getValue(
            tempKey,
            object : CommonCallbacks.CompletionCallbackWithParam<ThermalAreaMetersureTemperature> {
                override fun onSuccess(p0: ThermalAreaMetersureTemperature?) {
                    if (p0 == null) return
                    temperatureProcessor.onNext(p0)
                    isFetching = false
                    setMeasureMode(ThermalTemperatureMeasureMode.SPOT)
                }

                override fun onFailure(p0: IDJIError) {
                    isFetching = false
                    setMeasureMode(ThermalTemperatureMeasureMode.SPOT)
                    Log.e(TAG, "onFailure: $p0")
                }
            })
    }

    override fun inCleanup() {
        fetchTemperatureJob?.cancel()
        fetchTemperatureJob = null
        isFetching = false
    }

    companion object {
        private const val TAG = "ThermalStreamPaletteBar"
        private const val FETCH_TEMPERATURE_INTERVAL_MILLIS = 5000L
    }
}