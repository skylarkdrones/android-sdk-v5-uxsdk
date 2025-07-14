package dji.v5.ux.remotecontroller.customiztion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.databinding.UxsdkWidgetCustomSelectionBinding

class CustomSelectionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<CustomSelectionWidget.ModelState>(context, attrs, defStyleAttr) {

    private lateinit var binding: UxsdkWidgetCustomSelectionBinding
    private var listener: CustomSelectionWidgetListener? = null

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkWidgetCustomSelectionBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )
    }

    override fun reactToModelChanges() {
        binding.selectedText.setOnClickListener {
            listener?.onShowDropDown()
        }
    }

    fun setLabel(label: String) {
        binding.label.text = label
    }

    fun setSelectedText(text: String) {
        binding.selectedText.text = text
    }

    fun setSelectionListener(listener: CustomSelectionWidgetListener) {
        this.listener = listener
    }

    sealed class ModelState

    interface CustomSelectionWidgetListener {
        fun onShowDropDown()
    }
}