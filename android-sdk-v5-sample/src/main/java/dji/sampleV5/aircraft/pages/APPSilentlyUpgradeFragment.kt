package dji.sampleV5.aircraft.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.APPSilentlyUpgradeVM
import dji.sampleV5.aircraft.models.MSDKCrashLogVM
import dji.sampleV5.aircraft.util.ToastUtils
import kotlinx.android.synthetic.main.frag_app_silently_upgrade_page.btn_install_test_app
import kotlinx.android.synthetic.main.frag_app_silently_upgrade_page.btn_silently_upgrade_package
import kotlinx.android.synthetic.main.frag_log_info_page.*
class APPSilentlyUpgradeFragment : DJIFragment() {
    private val vm: APPSilentlyUpgradeVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_app_silently_upgrade_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_silently_upgrade_package.setOnClickListener {
            vm.setAPPSilentlyUpgrade(requireContext())
        }
        btn_install_test_app.setOnClickListener {
            vm.installApkWithOutNotice(requireContext())
        }
    }
}