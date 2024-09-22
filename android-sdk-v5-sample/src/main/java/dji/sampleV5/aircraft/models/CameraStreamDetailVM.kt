package dji.sampleV5.aircraft.models

import android.view.Surface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.value.camera.CameraType
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.et.create
import dji.v5.et.listen
import dji.v5.et.set
import dji.v5.manager.KeyManager
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.manager.interfaces.ICameraStreamManager.FrameFormat
import dji.v5.manager.interfaces.ICameraStreamManager.ScaleType
import dji.v5.utils.common.ContextUtil
import dji.v5.utils.common.DJIExecutor
import dji.v5.utils.common.DateUtils
import dji.v5.utils.common.DiskUtil
import dji.v5.utils.common.LogUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

const val TAG = "CameraStreamDetailFragmentVM"

class CameraStreamDetailVM : DJIViewModel() {

    private val _availableLensListData = MutableLiveData<List<CameraVideoStreamSourceType>>(ArrayList())
    private val _currentLensData = MutableLiveData(CameraVideoStreamSourceType.DEFAULT_CAMERA)
    private val _cameraName = MutableLiveData("Unknown")
    private var cameraIndex = ComponentIndexType.UNKNOWN
    private var streamFile: File? = null
    private var streamFileOutputStream: FileOutputStream? = null

    private val streamListener = ICameraStreamManager.ReceiveStreamListener { data, offset, length, info ->
        if (streamFile == null) {
            val fileName = "[${cameraIndex.name}]${DateUtils.getSystemTime()}.${info.mimeType.name.lowercase(Locale.ROOT)}"
            ToastUtils.showToast("begin to save,$fileName")
            streamFile = File(LogUtils.getLogPath(), fileName)
            streamFileOutputStream = FileOutputStream(streamFile)
            return@ReceiveStreamListener
        }
        DJIExecutor.getExecutor().execute {
            try {
                streamFileOutputStream?.write(data, offset, length)
            } catch (e: Exception) {
                //do nothing
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        setCameraIndex(ComponentIndexType.UNKNOWN)
        doStopDownloadStreamToLocal()
    }

    fun setCameraIndex(cameraIndex: ComponentIndexType) {
        KeyManager.getInstance().cancelListen(this)
        this.cameraIndex = cameraIndex
        listenCameraType()
        listenAvailableLens()
        listenCurrentLens()
    }

    private fun listenCameraType() {
        _cameraName.postValue("")
        if (cameraIndex == ComponentIndexType.UNKNOWN) {
            return
        }
        if (cameraIndex == ComponentIndexType.FPV) {
            _cameraName.postValue(ComponentIndexType.FPV.name)
            return
        }
        CameraKey.KeyCameraType.create(cameraIndex).listen(this) {
            var result = it;
            if (result == null) {
                result = CameraType.NOT_SUPPORTED
            }
            _cameraName.postValue(result.toString())
        }
    }

    private fun listenAvailableLens() {
        _availableLensListData.postValue(arrayListOf())
        if (cameraIndex == ComponentIndexType.UNKNOWN) {
            return
        }
        CameraKey.KeyCameraVideoStreamSourceRange.create(cameraIndex).listen(this) {
            val list: List<CameraVideoStreamSourceType> = it ?: arrayListOf()
            _availableLensListData.postValue(list)
        }
    }

    private fun listenCurrentLens() {
        _currentLensData.postValue(CameraVideoStreamSourceType.DEFAULT_CAMERA)
        if (cameraIndex == ComponentIndexType.UNKNOWN) {
            return
        }
        CameraKey.KeyCameraVideoStreamSource.create(cameraIndex).listen(this) {
            if (it != null) {
                _currentLensData.postValue(it)
            } else {
                _currentLensData.postValue(CameraVideoStreamSourceType.DEFAULT_CAMERA)
            }
        }
    }


    fun changeCameraLens(lensType: CameraVideoStreamSourceType) {
        CameraKey.KeyCameraVideoStreamSource.create(cameraIndex).set(lensType)
    }

    fun putCameraStreamSurface(
        surface: Surface,
        width: Int,
        height: Int,
        scaleType: ScaleType
    ) {
        MediaDataCenter.getInstance().cameraStreamManager.putCameraStreamSurface(cameraIndex, surface, width, height, scaleType)
    }

    fun removeCameraStreamSurface(surface: Surface) {
        MediaDataCenter.getInstance().cameraStreamManager.removeCameraStreamSurface(surface)
    }

    fun downloadYUVImageToLocal(format: FrameFormat, formatName: String) {
        MediaDataCenter.getInstance().cameraStreamManager.addFrameListener(
            cameraIndex,
            format,
            object : ICameraStreamManager.CameraFrameListener {
                override fun onFrame(frameData: ByteArray, offset: Int, length: Int, width: Int, height: Int, format: FrameFormat) {
                    try {
                        val dirs = File(LogUtils.getLogPath() + "STREAM_PIC")
                        if (!dirs.exists()) {
                            dirs.mkdirs()
                        }
                        val fileName = "[${cameraIndex.name}][$width x $height]${DateUtils.getSystemTime()}.${formatName}"
                        val file = File(dirs.absolutePath, fileName)
                        FileOutputStream(file).use { stream ->
                            stream.write(frameData, offset, length)
                            stream.flush()
                            stream.close()
                            ToastUtils.showToast("Save to : ${file.path}")
                        }
                        LogUtils.i(TAG, "Save to : ${file.path}")
                    } catch (e: Exception) {
                        ToastUtils.showToast("Save fail : $e")
                    }
                    // Because only one frame needs to be saved, you need to call removeOnFrameListener here
                    // If you need to read frame data for a long time, you can choose to actually call remove OnFrameListener according to your needs
                    MediaDataCenter.getInstance().cameraStreamManager.removeFrameListener(this)
                }

            })
    }

    fun beginDownloadStreamToLocal() {
        if (streamFile != null) {
            ToastUtils.showToast("Pls stop first.")
            return
        }
        MediaDataCenter.getInstance().cameraStreamManager.addReceiveStreamListener(cameraIndex, streamListener)
    }

    fun stopDownloadStreamToLocal() {
        if (streamFile == null) {
            ToastUtils.showToast("Pls begin first.")
            return
        }
        ToastUtils.showToast("stop to save,${streamFile?.name}")
        doStopDownloadStreamToLocal()
    }

    private fun doStopDownloadStreamToLocal(){
        MediaDataCenter.getInstance().cameraStreamManager.removeReceiveStreamListener(streamListener)
        try {
            streamFileOutputStream?.flush()
            streamFileOutputStream?.close()
            streamFileOutputStream = null
            streamFile = null
        } catch (e: Exception) {
            //do nothing
        }
    }

    val availableLensListData: LiveData<List<CameraVideoStreamSourceType>>
        get() = _availableLensListData

    val currentLensData: LiveData<CameraVideoStreamSourceType>
        get() = _currentLensData

    val cameraName: LiveData<String>
        get() = _cameraName

}