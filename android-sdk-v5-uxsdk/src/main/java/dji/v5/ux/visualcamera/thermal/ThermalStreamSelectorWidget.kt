package dji.v5.ux.visualcamera.thermal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.base.widget.FrameLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.popover.PopoverHelper
import dji.v5.ux.databinding.UxsdkCameraStatusActionItemContentBinding

class ThermalStreamSelectorWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayoutWidget<Any>(context, attrs, defStyleAttr), ICameraIndex, View.OnClickListener {

    private val widgetModel by lazy {
        ThermalStreamSelectorWidgetModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance())
    }
    private lateinit var binding: UxsdkCameraStatusActionItemContentBinding

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

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkCameraStatusActionItemContentBinding.inflate(LayoutInflater.from(context), this, true)
        setOnClickListener(this)
    }

    override fun reactToModelChanges() {
        addReaction(widgetModel.cameraVideoStreamSourceProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                updateContent()
            }
        )
        addReaction(widgetModel.cameraThermalPaletteProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe {
                updateContent()
            }
        )
    }

    private fun updateContent() {
        binding.tvContent.text = when (widgetModel.cameraVideoStreamSourceProcessor.value) {
            CameraVideoStreamSourceType.INFRARED_CAMERA -> {
                when (widgetModel.cameraThermalPaletteProcessor.value) {
                    CameraThermalPalette.WHITE_HOT -> "WhiteHot"
                    CameraThermalPalette.BLACK_HOT -> "BlackHot"
                    CameraThermalPalette.RED_HOT -> "RedHot"
                    CameraThermalPalette.GREEN_HOT -> "GreenHot"
                    CameraThermalPalette.FUSION -> "Fusion"
                    CameraThermalPalette.RAINBOW -> "Rainbow"
                    CameraThermalPalette.IRONBOW1 -> "IronBow1"
                    CameraThermalPalette.IRONBOW2 -> "IronBow2"
                    CameraThermalPalette.ICE_FIRE -> "IceFire"
                    CameraThermalPalette.SEPIA -> "Sepia"
                    CameraThermalPalette.GLOWBOW -> "GlowBow"
                    CameraThermalPalette.COLOR1 -> "Color1"
                    CameraThermalPalette.COLOR2 -> "Color2"
                    CameraThermalPalette.RAIN -> "Rain"
                    CameraThermalPalette.HOT_SPOT -> "HotSpot"
                    CameraThermalPalette.RAINBOW2 -> "Rainbow2"
                    CameraThermalPalette.GRAY -> "Gray"
                    CameraThermalPalette.METAL -> "Metal"
                    CameraThermalPalette.COLD_SPOT -> "ColdSpot"
                    CameraThermalPalette.UNKNOWN -> "Unknown"
                }
            }
            else -> ""
        }
    }

    override fun onClick(v: View?) {
        openSettingPanel()
    }

    private fun openSettingPanel() {
        val view = ThermalStreamPopoverViewWidget(context)
        view.updateCameraSource(getCameraIndex(),getLensType())
        view.selectIndex = 0
        PopoverHelper.showPopover(binding.streamSelectorRootView, view)
    }

    override fun getCameraIndex(): ComponentIndexType {
        return widgetModel.getCameraIndex()
    }

    override fun getLensType(): CameraLensType {
        return widgetModel.getLensType()
    }

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        widgetModel.updateCameraSource(cameraIndex, lensType)
    }
}