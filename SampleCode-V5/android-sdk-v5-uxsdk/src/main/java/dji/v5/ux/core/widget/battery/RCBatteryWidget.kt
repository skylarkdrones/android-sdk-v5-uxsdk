package dji.v5.ux.core.widget.battery

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.base.WidgetSizeDescription
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.extension.getString
import dji.v5.ux.core.panel.listitem.rcbattery.RCBatteryListItemWidgetModel
import io.reactivex.rxjava3.core.Flowable

/**
 * RC Battery Widget - Show transmitter battery.
 */
open class RCBatteryWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayoutWidget<RCBatteryWidget.ModelState>(context, attrs, defStyleAttr) {

    //region Fields
    private val widgetModel by lazy {
        RCBatteryListItemWidgetModel(
            DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance()
        )
    }
    private val batteryValueTextView: TextView = findViewById(R.id.textview_battery_value)

    //endregion

    //region Constructors
    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.uxsdk_widget_rc_battery, this)
    }

    init {
        attrs?.let { initAttributes(context, it) }
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        // noop - not allowing external customization
    }

    //endregion

    override val widgetSizeDescription: WidgetSizeDescription = WidgetSizeDescription(
        WidgetSizeDescription.SizeType.OTHER,
        widthDimension = WidgetSizeDescription.Dimension.WRAP,
        heightDimension = WidgetSizeDescription.Dimension.EXPAND
    )

    //region Lifecycle
    override fun reactToModelChanges() {
        addReaction(widgetModel.productConnection.observeOn(SchedulerProvider.ui()).subscribe {
            widgetStateDataProcessor.onNext(
                ModelState.ProductConnected(it)
            )
        })
        addReaction(widgetModel.rcBatteryState.observeOn(SchedulerProvider.ui())
            .subscribe { this.updateUI(it) })

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

    //endregion

    //region Reactions to model
    private fun updateUI(rcBatteryState: RCBatteryListItemWidgetModel.RCBatteryState) {
        widgetStateDataProcessor.onNext(ModelState.RCBatteryStateUpdated(rcBatteryState))
        when (rcBatteryState) {
            RCBatteryListItemWidgetModel.RCBatteryState.RCDisconnected -> {
                updateBatteryPercentText(null)
            }

            is RCBatteryListItemWidgetModel.RCBatteryState.Normal -> {
                updateBatteryPercentText(rcBatteryState.remainingChargePercent)
            }

            is RCBatteryListItemWidgetModel.RCBatteryState.Low -> {
                updateBatteryPercentText(rcBatteryState.remainingChargePercent)
            }
        }
    }

    private fun updateBatteryPercentText(percent: Int?) {
        val batteryPercentText = if (percent == null) {
            getString(R.string.uxsdk_string_default_value)
        } else {
            getString(R.string.uxsdk_rc_battery_percent, percent)
        }
        batteryValueTextView.text = batteryPercentText
    }

    //endregion

    //region Hooks
    /**
     * Get the [ModelState] updates
     */
    @SuppressWarnings
    override fun getWidgetStateUpdate(): Flowable<ModelState> {
        return super.getWidgetStateUpdate()
    }

    //endregion

    /**
     * Class defines the widget state updates
     */
    sealed class ModelState {
        /**
         * Product connection update
         */
        data class ProductConnected(val isConnected: Boolean) : ModelState()

        /**
         * RC battery State update
         */
        data class RCBatteryStateUpdated(val rcBatteryState: RCBatteryListItemWidgetModel.RCBatteryState) :
            ModelState()
    }

}