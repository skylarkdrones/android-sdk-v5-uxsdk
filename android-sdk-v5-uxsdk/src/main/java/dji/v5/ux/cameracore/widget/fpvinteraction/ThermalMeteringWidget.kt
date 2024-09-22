package dji.v5.ux.cameracore.widget.fpvinteraction

import android.content.Context
import android.util.AttributeSet
import android.view.View
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.SchedulerProvider.ui
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.UnitConversionUtil
import dji.v5.ux.core.util.units.DataStoreUnitPreferenceStorageManagerDJIV5
import dji.v5.ux.core.util.units.UnitsDJIV5
import kotlinx.android.synthetic.main.uxsdk_thermal_focus_view.view.temp_tv
import kotlinx.android.synthetic.main.uxsdk_thermal_focus_view.view.thermal_spot_group
import java.text.DecimalFormat

/**
 * Displays a metering target on the screen. Allows spot measurement.
 */
class ThermalMeteringWidget : ConstraintLayoutWidget<Any?>, ICameraIndex {
    //region Fields
    private var widgetModel: ThermalMeteringWidgetModel? = null
    private var temperatureUnit = UnitsDJIV5.CELSIUS

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.uxsdk_thermal_focus_view, this)
        if (!isInEditMode) {
            widgetModel = ThermalMeteringWidgetModel(
                DJISDKModel.getInstance(),
                ObservableInMemoryKeyedStore.getInstance()
            )
        }
    }

    override fun reactToModelChanges() {
        addReaction(widgetModel!!.temperatureProcessor.toFlowable()
            .observeOn(ui())
            .subscribe {
                temp_tv.text = formatTemperature(it)
            }
        )

        addReaction(widgetModel!!.isSBSOnProcessor.toFlowable()
            .observeOn(ui())
            .subscribe {
                thermal_spot_group.visibility = View.GONE
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

    private fun formatTemperature(it: Double): String {
        val convertedTemp = convertTemperature(it)
        val formatter = DecimalFormat("0.#")
        val valueFormatted = formatter.format(convertedTemp)
        return "$valueFormatted${temperatureUnit.formattedStr()}"
    }

    override fun getIdealDimensionRatioString(): String? {
        return null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            widgetModel!!.setup()
        }
        temperatureUnit = DataStoreUnitPreferenceStorageManagerDJIV5.getTemperatureUnit()
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            widgetModel!!.cleanup()
        }
        super.onDetachedFromWindow()
    }

    fun clickEvent(
        x: Float, y: Float,
        parentWidth: Float, parentHeight: Float,
    ) {
        if (!shouldShowTemperature(x, y, parentWidth, parentHeight)) {
            return
        }

        thermal_spot_group.visibility = View.VISIBLE

        var newX = x
        var newY = y
        newX -= width / 2f
        newY -= height / 2f
        setX(newX)
        setY(newY)

        widgetModel!!.setSpotMetersurePoint(
            x.toDouble() / parentWidth,
            y.toDouble() / parentHeight
        )
    }

    /**
     * Determines if an element at coordinates should show temperature based on its position
     * within the parent container.
     *
     * @param x The X coordinate of the element.
     * @param y The Y coordinate of the element.
     * @param parentWidth The width of the parent container.
     * @param parentHeight The height of the parent container.
     * @return true if the element is within the specified area of the parent container.
     */
    private fun shouldShowTemperature(
        x: Float,
        y: Float,
        parentWidth: Float,
        parentHeight: Float
    ): Boolean {
        var allowedHeightPercent = ALLOWED_HEIGHT_PERCENT
        if (widgetModel!!.isSBSOnProcessor.value) {
            if (x > parentWidth / 2) {
                return false
            }
            // the height and width of the feed changes when SBS is toggled
            // height reduces and width increases slightly
            allowedHeightPercent = ALLOWED_HEIGHT_PERCENT_SBS
        }

        // Calculate the boundaries of the central box
        val leftBoundary = (parentWidth - ALLOWED_WIDTH_PERCENT * parentWidth) / 2
        val rightBoundary = (parentWidth + ALLOWED_WIDTH_PERCENT * parentWidth) / 2
        val topBoundary = (parentHeight - allowedHeightPercent * parentHeight) / 2
        val bottomBoundary = (parentHeight + allowedHeightPercent * parentHeight) / 2

        // Check if the point is within the boundaries of the central box
        val isWithinAllowedWidth = x in leftBoundary..rightBoundary
        val isWithinAllowedHeight = y in topBoundary..bottomBoundary

        return isWithinAllowedWidth && isWithinAllowedHeight
    }

    fun enableSpotMetering() {
        widgetModel!!.setMeasureMode()
        thermal_spot_group.visibility = View.VISIBLE
    }

    fun disableSpotMetering() {
        widgetModel!!.disableMeasureMode()
        thermal_spot_group.visibility = View.GONE
    }

    override fun getCameraIndex(): ComponentIndexType {
        return widgetModel!!.getCameraIndex()
    }

    override fun getLensType(): CameraLensType {
        return widgetModel!!.getLensType()
    }

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        widgetModel!!.updateCameraSource(cameraIndex, lensType)
    }

    companion object {
       private const val ALLOWED_HEIGHT_PERCENT = 0.8f
       private const val ALLOWED_HEIGHT_PERCENT_SBS = 0.6f
       private const val ALLOWED_WIDTH_PERCENT = 0.6f
    }
}