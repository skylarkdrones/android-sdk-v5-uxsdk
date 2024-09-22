package dji.v5.ux.visualcamera.thermal

import android.content.Context
import android.util.AttributeSet
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import kotlinx.android.synthetic.main.uxsdk_panel_thermal.view.widget_thermal_display_mode
import kotlinx.android.synthetic.main.uxsdk_panel_thermal.view.widget_thermal_stream_palette_bar

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

    private fun updateContent() {
        widget_thermal_display_mode.updateCameraSource(
            widgetModel.getCameraIndex(),
            widgetModel.getLensType()
        )
        widget_thermal_stream_palette_bar.updateCameraSource(
            widgetModel.getCameraIndex(),
            widgetModel.getLensType()
        )
        val visibility = if (widgetModel.cameraVideoStreamSourceProcessor.value
            == CameraVideoStreamSourceType.INFRARED_CAMERA
        ) VISIBLE else GONE
        widget_thermal_display_mode.visibility = visibility
        widget_thermal_stream_palette_bar.visibility = visibility
        this.visibility = visibility
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        inflate(context, R.layout.uxsdk_panel_thermal, this)
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