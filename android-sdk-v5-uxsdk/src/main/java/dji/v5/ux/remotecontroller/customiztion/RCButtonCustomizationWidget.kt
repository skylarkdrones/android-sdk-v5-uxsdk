package dji.v5.ux.remotecontroller.customiztion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import dji.v5.ux.R
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.databinding.UxsdkWidgetSettingRcButtonCustomizeBinding

class RCButtonCustomizationWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<RCButtonCustomizationWidget.ModelState>(context, attrs, defStyleAttr) {

    private lateinit var binding: UxsdkWidgetSettingRcButtonCustomizeBinding
    private var listener: ButtonCustomizationListener? = null

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkWidgetSettingRcButtonCustomizeBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )
    }

    override fun reactToModelChanges() {
        initButtonCustomizationView()
    }

    fun setButtonCustomizationListener(listener: ButtonCustomizationListener) {
        this.listener = listener
    }

    private fun initButtonCustomizationView() {
        // Button C1
        binding.buttonC1Selection.setLabel(context.getString(R.string.uxsdk_setting_ui_label_button_c1))
        binding.buttonC1Selection.setSelectedText(ButtonAction.ZOOM_IN.desc) // todo to be fetched from the data store
        binding.buttonC1Selection.setSelectionListener(object: CustomSelectionWidget.CustomSelectionWidgetListener {
            override fun onShowDropDown() {
                listener?.onShowDropDown(ButtonKey.KEY_1)
            }
        })

        // Button C2
        binding.buttonC2Selection.setLabel(context.getString(R.string.uxsdk_setting_ui_label_button_c2))
        binding.buttonC2Selection.setSelectedText(ButtonAction.ZOOM_OUT.desc) // todo to be fetched from the data store
        binding.buttonC2Selection.setSelectionListener(object: CustomSelectionWidget.CustomSelectionWidgetListener {
            override fun onShowDropDown() {
                listener?.onShowDropDown(ButtonKey.KEY_2)
            }
        })


        // Button C3
        binding.buttonC3Selection.setLabel(context.getString(R.string.uxsdk_setting_ui_label_button_c3))
        binding.buttonC3Selection.setSelectedText(ButtonAction.ZOOM_IN.desc) // todo to be fetched from the data store
        binding.buttonC3Selection.setSelectionListener(object: CustomSelectionWidget.CustomSelectionWidgetListener {
            override fun onShowDropDown() {
                listener?.onShowDropDown(ButtonKey.KEY_3)
            }
        })

    }

    sealed class ModelState

    interface ButtonCustomizationListener {
        fun onShowDropDown(key: ButtonKey)
    }
}