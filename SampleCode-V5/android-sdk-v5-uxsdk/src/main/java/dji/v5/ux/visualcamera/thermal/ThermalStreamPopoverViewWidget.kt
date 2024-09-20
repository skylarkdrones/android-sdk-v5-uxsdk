package dji.v5.ux.visualcamera.thermal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.ux.R
import dji.v5.ux.core.base.ICameraIndex
import dji.v5.ux.core.base.widget.FrameLayoutWidget
import dji.v5.ux.core.ui.component.SegmentedButtonGroup
import dji.v5.ux.databinding.UxsdkM3tStreamPalettePopoverViewBinding

/**
 * Popup that houses the list of palettes supported.
 */
class ThermalStreamPopoverViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayoutWidget<Any>(context, attrs, defStyleAttr), ICameraIndex {

    private var cameraIndex = ComponentIndexType.LEFT_OR_MAIN
    private var lensType = CameraLensType.CAMERA_LENS_ZOOM
    private var group: SegmentedButtonGroup? = null
    private lateinit var binding: UxsdkM3tStreamPalettePopoverViewBinding

    var selectIndex = 0
        set(value) {
            group?.check(if (value == 0) R.id.stream_selection else R.id.stream_palette_bar)
            field = value
        }

    override fun getCameraIndex(): ComponentIndexType = cameraIndex

    override fun getLensType(): CameraLensType = lensType

    override fun updateCameraSource(cameraIndex: ComponentIndexType, lensType: CameraLensType) {
        this.cameraIndex = cameraIndex
        this.lensType = lensType

        binding.paletteSelectionPanel.updateCameraSource(cameraIndex, lensType)
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkM3tStreamPalettePopoverViewBinding.inflate(LayoutInflater.from(context), this, true)
        group = findViewById(R.id.segmented_button_group)
        group?.onCheckedChangedListener = SegmentedButtonGroup.OnCheckedListener {
            post {
                if (it == R.id.stream_selection) {
                    binding.paletteSelectionPanel.visibility = VISIBLE
                } else {
                    binding.paletteSelectionPanel.visibility = GONE
                }
            }
        }
        selectIndex = 0
    }

    override fun reactToModelChanges() {
        //do nothing
    }
}