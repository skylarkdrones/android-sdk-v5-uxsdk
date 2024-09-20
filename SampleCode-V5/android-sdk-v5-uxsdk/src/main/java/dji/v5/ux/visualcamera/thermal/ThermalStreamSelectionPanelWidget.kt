package dji.v5.ux.visualcamera.thermal

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.utils.common.AndUtil
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.R
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.extension.showShortToast
import dji.v5.ux.core.ui.component.PaletteItemDecoration
import dji.v5.ux.visualcamera.ndvi.StreamAdapter
import kotlin.math.roundToInt

/**
 *  Panel that houses the palette list for thermal lens.
 **/
class ThermalStreamSelectionPanelWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<Any>(context, attrs, defStyleAttr), ICameraIndex {

    private val widgetModel by lazy {
        ThermalStreamSelectionPanelWidgetModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance())
    }

    private lateinit var paletteList: RecyclerView
    private var paletteAdapter: StreamAdapter<StreamPanelUtil.ThermalPaletteModel> = StreamAdapter {
        if (widgetModel.isEnableProcessor.value) {
            setCurrentPalettePosition(it)
            widgetModel.setThermalModel(it).subscribe()
        } else {
            showShortToast("Unable to switch thermal lens in current shooting mode")
        }
    }

    override fun getCameraIndex(): ComponentIndexType = widgetModel.getCameraIndex()

    override fun getLensType(): CameraLensType = widgetModel.getLensType()

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        widgetModel.updateCameraSource(cameraIndex, lensType)
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        inflate(context, R.layout.uxsdk_ux_thermal_stream_selection_panel, this)
        val layoutManager: GridLayoutManager = object : GridLayoutManager(getContext(), 3) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        paletteList = findViewById(R.id.palette_index_list)
        paletteList.layoutManager = layoutManager
        paletteList.itemAnimator = null

        val hEdgeSpacing = AndUtil.getDimension(R.dimen.uxsdk_8_dp).roundToInt()
        val vSpacing = AndUtil.getDimension(R.dimen.uxsdk_12_dp).roundToInt()
        val hSpacing = AndUtil.getDimension(R.dimen.uxsdk_4_dp).roundToInt()
        val decoration = PaletteItemDecoration(3, hEdgeSpacing, vSpacing, 0, hSpacing, vSpacing)
        paletteList.addItemDecoration(decoration)
    }

    override fun reactToModelChanges() {
        addReaction(widgetModel.currentPaletteModelProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                setCurrentPalettePosition(it)
            }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            widgetModel.setup()
        }
        paletteList.adapter = paletteAdapter
        setPaletteData(widgetModel.thermalPaletteModels)
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            widgetModel.cleanup()
        }
        super.onDetachedFromWindow()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setPaletteData(models: List<StreamPanelUtil.ThermalPaletteModel>) {
        paletteAdapter.models.clear()
        paletteAdapter.models.addAll(models)
        paletteAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setCurrentPalettePosition(position: StreamPanelUtil.ThermalPaletteModel) {
        paletteAdapter.currentPosition = position
        paletteAdapter.notifyDataSetChanged()
    }

}