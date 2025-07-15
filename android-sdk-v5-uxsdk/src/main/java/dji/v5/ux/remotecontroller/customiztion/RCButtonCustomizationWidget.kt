package dji.v5.ux.remotecontroller.customiztion

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
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

    private var actions: List<ButtonAction> = listOf()
    private lateinit var buttonSettings: Map<String, ButtonAction>

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

    private fun setSelectedAction(key: ButtonKey, action: ButtonAction) {
        runBlocking {
            val mutableSettingsMap = buttonSettings.toMutableMap()
            mutableSettingsMap["${ButtonProfile.PROFILE_1}_$key"] = action
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

    private fun getSelectedAction(key: ButtonKey): ButtonAction {
        Log.d(TAG, "Settings Map: $buttonSettings")
        return buttonSettings["${ButtonProfile.PROFILE_1}_$key"] ?: ButtonAction.UNDEFINED
    }

    private fun getButtonSettingsMap(): Map<String, ButtonAction> {
        try {
            val mapType = object : TypeToken<Map<String, ButtonAction>>() {}.type
            val data = DataStoreManagerDJIV5.get(
                DataStoreManagerDJIV5.EndPoints.CUSTOM_BUTTON_SETTINGS.endPoint
            )
            val innerJson = Gson().fromJson(data, String::class.java)
            Log.d(TAG, "Data from DataStore: $innerJson")
            return gson.fromJson(innerJson, mapType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get button settings data, error: $e")
            return mapOf()
        }
    }

    private fun initButtonCustomizationView() {
        // Button C1
        binding.buttonC1Selection.setLabel(
            context.getString(R.string.uxsdk_setting_ui_label_button_c1)
        )
        binding.buttonC1Selection.setEntries(actions.map { it.desc })
        binding.buttonC1Selection.select(actions.indexOf(getSelectedAction(ButtonKey.KEY_1)))
        binding.buttonC1Selection.addOnItemSelectedListener(
            object: CustomSelectionWidget.OnItemSelectedListener{
                override fun onItemSelected(position: Int) {
                    setSelectedAction(ButtonKey.KEY_1, actions[position])
                }
            }
        )

        // Button C2
        binding.buttonC2Selection.setLabel(
            context.getString(R.string.uxsdk_setting_ui_label_button_c2)
        )
        binding.buttonC2Selection.setEntries(actions.map { it.desc })
        binding.buttonC2Selection.select(actions.indexOf(getSelectedAction(ButtonKey.KEY_2)))
        binding.buttonC2Selection.addOnItemSelectedListener(
            object: CustomSelectionWidget.OnItemSelectedListener{
                override fun onItemSelected(position: Int) {
                    setSelectedAction(ButtonKey.KEY_2, actions[position])
                }
            }
        )

        // Button C3
        binding.buttonC3Selection.setLabel(
            context.getString(R.string.uxsdk_setting_ui_label_button_c3)
        )
        binding.buttonC3Selection.setEntries(actions.map { it.desc })
        binding.buttonC3Selection.select(actions.indexOf(getSelectedAction(ButtonKey.KEY_3)))
        binding.buttonC3Selection.addOnItemSelectedListener(
            object: CustomSelectionWidget.OnItemSelectedListener{
                override fun onItemSelected(position: Int) {
                    setSelectedAction(ButtonKey.KEY_3, actions[position])
                }
            }
        )
    }

    private fun initButtonActions() {
        actions = ButtonAction.values().toList()
    }

    sealed class ModelState

    companion object {
        private const val TAG = "RCButtonCustomizationWidget"
    }
}