package dji.v5.ux.visualcamera.thermal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.databinding.UxsdkPanelThermalBinding

open class CameraThermalPanelWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayoutWidget<Any>(context, attrs, defStyleAttr) {

    private val widgetModel by lazy {
        CameraThermalPanelWidgetModel(
            DJISDKModel.getInstance(),
            ObservableInMemoryKeyedStore.getInstance()
        )
    }

    private lateinit var binding:UxsdkPanelThermalBinding

    private fun updateContent() {
        binding.widgetThermalDisplayMode.updateCameraSource(
            widgetModel.getCameraIndex(),
            widgetModel.getLensType()
        )
        binding.widgetThermalStreamPaletteBar.updateCameraSource(
            widgetModel.getCameraIndex(),
            widgetModel.getLensType()
        )
        val visibility = if (widgetModel.cameraVideoStreamSourceProcessor.value
            == CameraVideoStreamSourceType.INFRARED_CAMERA
        ) VISIBLE else GONE
        binding.widgetThermalDisplayMode.visibility = visibility
        binding.widgetThermalStreamPaletteBar.visibility = visibility
        this.visibility = visibility
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkPanelThermalBinding.inflate(LayoutInflater.from(context), this, true)
        if (background == null) {
            setBackgroundResource(R.drawable.uxsdk_background_black_rectangle)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            widgetModel.setup()
        }
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            widgetModel.cleanup()
        }
        super.onDetachedFromWindow()
    }

    override fun reactToModelChanges() {
        addReaction(widgetModel.cameraVideoStreamSourceProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                updateContent()
            }
        )
    }

    override fun getIdealDimensionRatioString(): String? = null
}