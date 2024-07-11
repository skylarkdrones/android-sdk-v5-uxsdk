package dji.sampleV5.aircraft.pages

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.DataProtectionVm
import dji.sampleV5.aircraft.util.Helper
import dji.v5.utils.common.DiskUtil
import kotlinx.android.synthetic.main.frag_data_protection_page.btn_clear_log
import kotlinx.android.synthetic.main.frag_data_protection_page.btn_export_and_zip_log
import kotlinx.android.synthetic.main.frag_data_protection_page.btn_open_log_path
import kotlinx.android.synthetic.main.frag_data_protection_page.log_path_tv
import kotlinx.android.synthetic.main.frag_data_protection_page.msdk_log_switch
import kotlinx.android.synthetic.main.frag_data_protection_page.product_improvement_switch

class DataProtectionFragment : DJIFragment() {

    private val diagnosticVm: DataProtectionVm by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_data_protection_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product_improvement_switch.isChecked = diagnosticVm.isAgreeToProductImprovement()
        product_improvement_switch.setOnCheckedChangeListener { _: CompoundButton?,
            isChecked: Boolean ->
            diagnosticVm.agreeToProductImprovement(isChecked)
        }

        msdk_log_switch.isChecked = diagnosticVm.isLogEnable()
        msdk_log_switch.setOnCheckedChangeListener { _: CompoundButton?,
            isChecked: Boolean ->
            diagnosticVm.enableLog(isChecked)
        }

        log_path_tv.text = diagnosticVm.logPath()

        btn_open_log_path.setOnClickListener {
            val path = diagnosticVm.logPath()
            if (!path.contains(DiskUtil.SDCARD_ROOT)) {
                return@setOnClickListener
            }
            val uriPath = path.substring(DiskUtil.SDCARD_ROOT.length + 1, path.length - 1).replace("/", "%2f")
            Helper.openFileChooser(uriPath, activity)
        }

        btn_clear_log.setOnClickListener {
            val configDialog = requireContext().let {
                AlertDialog.Builder(it, R.style.Base_ThemeOverlay_AppCompat_Dialog_Alert)
                    .setTitle(R.string.clear_msdk_log)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ad_confirm) { configDialog, _ ->
                        kotlin.run {
                            diagnosticVm.clearLog()
                            configDialog.dismiss()
                        }
                    }
                    .setNegativeButton(R.string.ad_cancel) { configDialog, _ ->
                        kotlin.run {
                            configDialog.dismiss()
                        }
                    }
                    .create()
            }
            configDialog.show()
        }

        btn_export_and_zip_log.setOnClickListener {
            checkPermission()
            diagnosticVm.zipAndExportLog()
        }
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION")
            startActivityForResult(intent, 0)
        }
    }
}
