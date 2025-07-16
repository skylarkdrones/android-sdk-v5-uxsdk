package dji.v5.ux.remotecontroller.customiztion

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import dji.v5.utils.common.LogUtils
import dji.v5.ux.R
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.databinding.UxsdkWidgetCustomSelectionBinding

class CustomSelectionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<CustomSelectionWidget.ModelState>(context, attrs, defStyleAttr) {

    private lateinit var binding: UxsdkWidgetCustomSelectionBinding

    private var mAdapter: ArrayAdapter<String>? = null

    private var mSelectedPosition = 0
    private var mSelectedListener: OnItemSelectedListener? = null

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkWidgetCustomSelectionBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )
        initSelectionView()
        initListener()
    }

    override fun reactToModelChanges() {
        /* no-op */
    }

    fun setLabel(label: String) {
        binding.textViewLabel.text = label
    }

    fun addOnItemSelectedListener(listener: OnItemSelectedListener?) {
        mSelectedListener = listener
    }

    fun setEntries(entries: List<String?>) {
        mAdapter?.clear()
        mAdapter?.addAll(entries)
    }

    fun select(position: Int) {
        Log.d(TAG, "select: $position")
        if (position >= 0 && position < (mAdapter?.count ?: -1)) {
            binding.spinnerAction.setSelection(position, true)
            mSelectedPosition = position
        }
        invalidate()
    }


    override fun setEnabled(enable: Boolean) {
        super.setEnabled(enable)
        binding.spinnerAction.isEnabled = enable
    }

    private fun initSelectionView() {
        mAdapter = object : ArrayAdapter<String>(context, R.layout.uxsdk_spinner_item_bord) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View? {
                parent.setBackgroundColor(context.getColor(R.color.uxsdk_dropdown_bg))
                val rootView = super.getDropDownView(position, convertView, parent)
                rootView?.let {
                    val view = rootView as TextView
                    view.setCompoundDrawables(null, null, null, null)
                    if (mSelectedPosition == position) {
                        view.setTextColor(
                            ContextCompat.getColor(context, R.color.uxsdk_edit_cell_text_color)
                        )
                    } else {
                        view.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.uxsdk_white,
                            )
                        )
                    }
                }
                return rootView
            }


            private fun checkRightCompoundDrawable(view: TextView?): Boolean {
                return view?.compoundDrawables != null && view.compoundDrawables.size == 4 && view.compoundDrawables[2] != null
            }
        }

        mAdapter?.setDropDownViewResource(R.layout.uxsdk_spinner_item_drop)
    }

    private fun initListener() {
        binding.spinnerAction.isSaveEnabled = false
        binding.spinnerAction.adapter = mAdapter
        binding.spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                LogUtils.i(
                    TAG,
                    "onItemSelected , mSelectedPosition=$mSelectedPosition,position=$position"
                )

                if (mSelectedPosition != position) {
                    binding.spinnerAction.setSelection(position, true)
                    mSelectedListener?.onItemSelected(position)
                    mSelectedPosition = position
                }

            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                LogUtils.e(TAG, "onNothingSelected")
            }

        }
    }

    sealed class ModelState

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    companion object {
        private const val TAG = "CustomSelectionWidget"
    }
}