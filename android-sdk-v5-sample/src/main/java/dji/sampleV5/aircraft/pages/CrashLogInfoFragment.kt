package dji.sampleV5.aircraft.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.MSDKCrashLogVM
import dji.sampleV5.aircraft.util.ToastUtils
import kotlinx.android.synthetic.main.frag_log_info_page.*

/**
 * ClassName : LogInfoFragment
 * Description : 展示最新崩溃日志信息
 * Author : daniel.chen
 * CreateDate : 2022/5/7 2:33 下午
 * Copyright : ©2022 DJI All Rights Reserved.
 */
class CrashLogInfoFragment : DJIFragment() {
    private val logVm: MSDKCrashLogVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_log_info_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logVm.updateLogInfo()
        logVm.logInfo.observe(viewLifecycleOwner) {
            tv_log_info.text = logVm.logInfo.value
        }
        logVm.logMsg.observe(viewLifecycleOwner) {
            ToastUtils.showToast("Msg:$it")
        }
        btn_get_log_info.setOnClickListener {
            logVm.updateLogInfo()
        }
        btn_test_java_crash.setOnClickListener {
            logVm.testJavaCrash(false)
        }
        btn_test_native_crash.setOnClickListener {
            logVm.testNativeCrash(true)
        }
    }
}