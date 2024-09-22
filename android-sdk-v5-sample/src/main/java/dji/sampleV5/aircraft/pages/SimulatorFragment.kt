package dji.sampleV5.aircraft.pages

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.data.QuickTestConfig
import dji.sampleV5.aircraft.models.SimulatorVM
import dji.sampleV5.aircraft.util.Helper
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.aircraft.simulator.*
import dji.v5.manager.areacode.AreaCodeManager
import dji.v5.utils.common.LogUtils
import kotlinx.android.synthetic.main.frag_simulator_page.*

/**
 * @author feel.feng
 * @time 2022/01/26 11:22 上午
 * @description:
 */
class SimulatorFragment : DJIFragment() {
    private val simulatorVM: SimulatorVM by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_simulator_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        btn_enable_simulator.setOnClickListener {
            enableSimulator()
        }

        btn_disable_simulator.setOnClickListener {
            disableSimulator(null)
        }

        btn_set_areacode.setOnClickListener {
            updateAreaCode()
        }

        simulatorVM.simulatorStateSb.observe(viewLifecycleOwner) {
            simulator_state_info_tv?.apply {
                text = it
                setTextColor(if (simulatorVM.isSimulatorOn()) Color.BLACK else Color.RED)
            }
        }

        btn_quick_simulator_area.setOnClickListener {
            initPopupNumberPicker(Helper.makeList(simulatorVM.quickInfo)) {
                updateView(simulatorVM.quickInfo[indexChosen[0]])
                if (simulatorVM.isSimulatorOn()) {
                    disableSimulator(object : CommonCallbacks.CompletionCallback {
                        override fun onSuccess() {
                            enableSimulator()
                        }

                        override fun onFailure(error: IDJIError) {
                            enableSimulator()
                        }
                    })
                } else {
                    enableSimulator()
                }
                updateAreaCode()
                resetIndex()
            }
        }
    }

    private fun enableSimulator() {
        val coordinate2D = LocationCoordinate2D(simulator_lat_et.text.toString().toDouble(), simulator_lng_et.text.toString().toDouble())
        val data = InitializationSettings.createInstance(coordinate2D, simulator_gps_num_et.text.toString().toInt())
        simulatorVM.enableSimulator(data, object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                ToastUtils.showToast("start Success")
                mainHandler.post {
                    simulator_state_info_tv?.setTextColor(Color.BLACK)
                }
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showToast("start Failed" + error.description())
            }
        })
    }

    private fun disableSimulator(callbacks: CommonCallbacks.CompletionCallback?) {
        simulatorVM.disableSimulator(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                ToastUtils.showToast("disable Success")
                mainHandler.post { simulator_state_info_tv?.setTextColor(Color.RED) }
                callbacks?.onSuccess()
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showToast("close Failed" + error.description())
                callbacks?.onFailure(error)
            }
        })
    }

    private fun updateAreaCode() {
        val areCode = areacode_et.text.toString()
        LogUtils.d(tag, "areCode:$areCode")
        val idjiError = AreaCodeManager.getInstance().updateAreaCode(areCode)
        if (idjiError == null) {
            ToastUtils.showToast("Success")
        } else {
            ToastUtils.showToast(idjiError.toString())
        }
    }

    private fun updateView(info: QuickTestConfig.SimulatorArea) {
        simulator_lat_et.setText(info.location.latitude.toString())
        simulator_lng_et.setText(info.location.longitude.toString())
        areacode_et.setText(info.areaCode.value())
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}