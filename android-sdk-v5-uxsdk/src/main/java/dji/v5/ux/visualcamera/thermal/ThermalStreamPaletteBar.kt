package dji.v5.ux.visualcamera.thermal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.sdk.keyvalue.value.camera.ThermalAreaMetersureTemperature
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.base.widget.FrameLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.popover.PopoverHelper
import dji.v5.ux.core.util.UnitConversionUtil
import dji.v5.ux.core.util.units.DataStoreUnitPreferenceStorageManagerDJIV5
import dji.v5.ux.core.util.units.UnitsDJIV5
import kotlinx.android.synthetic.main.uxsdk_m3m_stream_palette_bar.view.left_tv
import kotlinx.android.synthetic.main.uxsdk_m3m_stream_palette_bar.view.right_tv
import kotlinx.android.synthetic.main.uxsdk_m3m_stream_palette_bar.view.stream_palette_root_view
import kotlinx.android.synthetic.main.uxsdk_m3t_stream_palette_bar.view.iv_palette_preview

/**
 * Houses the color bar that allows switching thermal palette and shows min-max temperature
 * for the current frame.
 */
class ThermalStreamPaletteBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayoutWidget<Any>(context, attrs), ICameraIndex, View.OnClickListener {

    private val widgetModel by lazy {
        ThermalStreamPaletteBarPanelModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance())
    }
    private var temperatureUnit = UnitsDJIV5.CELSIUS


    override fun getCameraIndex(): ComponentIndexType = widgetModel.getCameraIndex()

    override fun getLensType(): CameraLensType = widgetModel.getLensType()

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        widgetModel.updateCameraSource(cameraIndex, lensType)
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        inflate(context, R.layout.uxsdk_m3t_stream_palette_bar, this)
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            widgetModel.setup()
        }
        temperatureUnit = DataStoreUnitPreferenceStorageManagerDJIV5.getTemperatureUnit()
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            widgetModel.cleanup()
        }
        super.onDetachedFromWindow()
    }

    override fun reactToModelChanges() {
        addReaction(widgetModel.cameraThermalPaletteDataProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                updateCurrentPaletteModelPosition(it)
            }
        )

        addReaction(widgetModel.temperatureProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                updateTemperature(it)
            })

    }

    /**
     * Converts temperature into preferred unit
     *
     * @param temp temperature in celsius
     * @return temperature in preferred unit
     */
    private fun convertTemperature(temp: Double): Double {
        return when (temperatureUnit) {
            UnitsDJIV5.FAHRENHEIT -> {
                UnitConversionUtil.celsiusToFahrenheit(temp.toFloat()).toDouble()
            }
            else -> temp
        }
    }
    private fun updateTemperature(it: ThermalAreaMetersureTemperature) {
        if (it.minAreaTemperature == 0.0 && it.maxAreaTemperature == 0.0) return
        left_tv.text = convertTemperature(it.minAreaTemperature).toInt().toString()
        right_tv.text = convertTemperature(it.maxAreaTemperature).toInt().toString()
    }

    private fun updateCurrentPaletteModelPosition(type: CameraThermalPalette) {
        if (type == CameraThermalPalette.UNKNOWN) {
            return
        }
        val model = widgetModel.thermalPaletteModels.find { it.sourceType == type }
        model?.let {
            iv_palette_preview.setImageResource(it.colorBar)
        }
    }

    override fun onClick(v: View?) {
        val view = ThermalStreamPopoverViewWidget(context)
        view.updateCameraSource(getCameraIndex(), getLensType())
        view.selectIndex = 0
        PopoverHelper.showPopover(stream_palette_root_view, view)
    }
}