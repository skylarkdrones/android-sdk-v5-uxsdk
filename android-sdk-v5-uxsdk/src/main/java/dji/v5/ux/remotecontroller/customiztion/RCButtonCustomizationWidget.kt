package dji.v5.ux.remotecontroller.customiztion

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.v5.ux.R
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.util.units.DataStoreManagerDJIV5
import dji.v5.ux.databinding.UxsdkWidgetSettingRcButtonCustomizeBinding
import kotlinx.coroutines.runBlocking

class RCButtonCustomizationWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<RCButtonCustomizationWidget.ModelState>(context, attrs, defStyleAttr) {

    private lateinit var binding: UxsdkWidgetSettingRcButtonCustomizeBinding

    private var actions: List<CustomButtonAction> = listOf()
    private var actionValues: List<String> = listOf()
    private lateinit var buttonSettings: Map<String, CustomButtonAction>

    private val gson by lazy {
        Gson()
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkWidgetSettingRcButtonCustomizeBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )
    }

    override fun reactToModelChanges() {
        buttonSettings = getButtonSettingsMap()
        initButtonActions()
        initButtonCustomizationView()
    }

    private fun setSelectedAction(key: CustomButtonKey, action: CustomButtonAction) {
        runBlocking {
            buttonSettings = getButtonSettingsMap()
            val mutableSettingsMap = buttonSettings.toMutableMap()
            mutableSettingsMap["${CustomButtonProfile.PROFILE_1}_$key"] = action
            val result = DataStoreManagerDJIV5.set(
                DataStoreManagerDJIV5.EndPoints.CUSTOM_BUTTON_SETTINGS.endPoint,
                gson.toJson(mutableSettingsMap),
            )
            if (result) {
                Toast.makeText(
                    context,
                    context.getString(R.string.uxsdk_setting_button_action_set_success),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.uxsdk_setting_button_action_set_failure),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun getSelectedAction(key: CustomButtonKey): CustomButtonAction {
        Log.d(TAG, "Settings Map: $buttonSettings")
        return buttonSettings["${CustomButtonProfile.PROFILE_1}_$key"]
            ?: CustomButtonAction.UNDEFINED
    }

    private fun getButtonSettingsMap(): Map<String, CustomButtonAction> {
        try {
            val mapType = object : TypeToken<Map<String, CustomButtonAction>>() {}.type
            val data = DataStoreManagerDJIV5.get(
                DataStoreManagerDJIV5.EndPoints.CUSTOM_BUTTON_SETTINGS.endPoint
            )
            val innerJson = gson.fromJson(data, String::class.java)
            Log.d(TAG, "Data from DataStore: $innerJson")
            return gson.fromJson(innerJson, mapType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get button settings data, error: $e")
            return mapOf()
        }
    }

    private fun initButtonCustomizationView() {
        val shouldShowCButtons = isDjiRcPro() || isDjiRcPlus2()
        val shouldShowLRButtons = isDjiRcPlus2()

        setCButtonVisibility(shouldShowCButtons)
        setLRButtonVisibility(shouldShowLRButtons)
        updateDividerAnchor(shouldShowCButtons, shouldShowLRButtons)

        if (shouldShowCButtons) {
            initCButtonView()
        }
        if (shouldShowLRButtons) {
            initLRButtonView()
        }
        init5DButtonView()
    }

    private fun updateDividerAnchor(shouldShowCButtons: Boolean, shouldShowLRButtons: Boolean) {
        val anchorId = when {
            shouldShowLRButtons -> R.id.buttonR3Selection
            shouldShowCButtons -> R.id.buttonC3Selection
            else -> ConstraintLayout.LayoutParams.PARENT_ID
        }
        val layoutParams = binding.divider.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToBottom = if (anchorId == ConstraintLayout.LayoutParams.PARENT_ID) {
            ConstraintLayout.LayoutParams.UNSET
        } else {
            anchorId
        }
        layoutParams.topToTop = if (anchorId == ConstraintLayout.LayoutParams.PARENT_ID) {
            anchorId
        } else {
            ConstraintLayout.LayoutParams.UNSET
        }
        binding.divider.layoutParams = layoutParams
        binding.divider.requestLayout()
        binding.divider.invalidate()
    }

    private fun initCButtonView() {
        initSelection(
            binding.buttonC1Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_c1),
            CustomButtonKey.KEY_C1,
        )
        initSelection(
            binding.buttonC2Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_c2),
            CustomButtonKey.KEY_C2,
        )
        initSelection(
            binding.buttonC3Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_c3),
            CustomButtonKey.KEY_C3,
        )
    }

    private fun initLRButtonView() {
        initSelection(
            binding.buttonL1Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_l1),
            CustomButtonKey.KEY_L1,
        )
        initSelection(
            binding.buttonL2Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_l2),
            CustomButtonKey.KEY_L2,
        )
        initSelection(
            binding.buttonL3Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_l3),
            CustomButtonKey.KEY_L3,
        )
        initSelection(
            binding.buttonR1Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_r1),
            CustomButtonKey.KEY_R1,
        )
        initSelection(
            binding.buttonR2Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_r2),
            CustomButtonKey.KEY_R2,
        )
        initSelection(
            binding.buttonR3Selection,
            context.getString(R.string.uxsdk_setting_ui_label_button_r3),
            CustomButtonKey.KEY_R3,
        )
    }

    private fun init5DButtonView() {
        initSelection(
            binding.button5DUPSelection,
            context.getString(R.string.uxsdk_setting_ui_label_button_up),
            CustomButtonKey.KEY_5D_UP,
        )
        initSelection(
            binding.button5DDownSelection,
            context.getString(R.string.uxsdk_setting_ui_label_button_down),
            CustomButtonKey.KEY_5D_DOWN,
        )
        initSelection(
            binding.button5DLeftSelection,
            context.getString(R.string.uxsdk_setting_ui_label_button_left),
            CustomButtonKey.KEY_5D_LEFT,
        )
        initSelection(
            binding.button5DRightSelection,
            context.getString(R.string.uxsdk_setting_ui_label_button_right),
            CustomButtonKey.KEY_5D_RIGHT,
        )
    }

    private fun initSelection(
        selectionWidget: CustomSelectionWidget,
        label: String,
        key: CustomButtonKey,
    ) {
        selectionWidget.setLabel(label)
        selectionWidget.setEntries(actionValues)
        selectionWidget.select(actions.indexOf(getSelectedAction(key)))
        selectionWidget.addOnItemSelectedListener(
            object : CustomSelectionWidget.OnItemSelectedListener {
                override fun onItemSelected(position: Int) {
                    setSelectedAction(key, actions[position])
                }
            },
        )
    }

    private fun setCButtonVisibility(isVisible: Boolean) {
        binding.cButtonGroup.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setLRButtonVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.leftButtonGroup.visibility = visibility
        binding.rightButtonGroup.visibility = visibility
    }

    private fun isDjiRcPro(): Boolean {
        return Build.MODEL.equals(DJI_RC_PRO_MODEL, ignoreCase = true)
    }

    private fun isDjiRcPlus2(): Boolean {
        return Build.MODEL.equals(DJI_RC_PLUS_2_MODEL, ignoreCase = true)
    }

    private fun initButtonActions() {
        actions = CustomButtonAction.values().toList()
        actionValues = actions.map { it.desc }
    }

    sealed class ModelState

    companion object {
        private const val TAG = "RCButtonCustomizationWidget"
        private const val DJI_RC_PRO_MODEL = "DJI RC Pro"
        private const val DJI_RC_PLUS_2_MODEL = "DJI RC Plus 2"
    }
}
