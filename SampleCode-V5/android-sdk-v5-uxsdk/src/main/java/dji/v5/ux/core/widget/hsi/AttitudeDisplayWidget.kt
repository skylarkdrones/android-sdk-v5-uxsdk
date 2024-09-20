package dji.v5.ux.core.widget.hsi

import android.content.Context
import android.util.AttributeSet
import kotlin.jvm.JvmOverloads
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.ui.hsi.dashboard.AttitudeDashBoard
import android.widget.TextView
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.value.common.Velocity3D
import dji.sdk.keyvalue.value.rtkmobilestation.RTKTakeoffAltitudeInfo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import io.reactivex.rxjava3.core.ObservableEmitter
import dji.v5.common.utils.GpsUtils
import dji.v5.common.utils.UnitUtils
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import dji.v5.ux.R
import dji.v5.ux.core.base.SchedulerProvider
import dji.v5.ux.core.util.UnitConversionUtil
import dji.v5.ux.core.util.units.DataStoreUnitPreferenceStorageManagerDJIV5.getSpeedUnit
import dji.v5.ux.core.util.units.DataStoreUnitPreferenceStorageManagerDJIV5.isHeightMetric
import dji.v5.ux.core.util.units.UnitsDJIV5
import io.reactivex.rxjava3.core.Observable
import java.util.*

open class AttitudeDisplayWidget @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayoutWidget<Boolean?>(
        context!!, attrs, defStyleAttr) {
    var mAttitudeDashBoard: AttitudeDashBoard? = null
    var mTvAslText: TextView? = null
    var mTvAslValue: TextView? = null
    var mTvVsText: TextView? = null
    var mTvVsValue: TextView? = null

    /**
     * 飞行器相对home点的高度
     */
    private var mAltitude = 0.0

    /**
     * home点的高度
     */
    private var mHomePointAltitude = 0.0

    /**
     * 飞行器垂直速度
     */
    private var mSpeedZ = 0f

    /**
     * 飞行器二维坐标
     */
    private var mDroneLocation: LocationCoordinate2D? = null
    private val mCompositeDisposable = CompositeDisposable()
    private val widgetModel = AttitudeDisplayModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance())

    private var speedUnit = UnitsDJIV5.METRE_PER_SECOND.name
    private var isHeightUnitMetric = isHeightMetric()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            mAttitudeDashBoard?.setModel(widgetModel)
            widgetModel.setup()
        }
        speedUnit = getSpeedUnit()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!isInEditMode) {
            widgetModel.cleanup()
        }
    }

    private fun updateAltitude() {
        mCompositeDisposable.add(Observable.create { emitter: ObservableEmitter<Any?> ->
            val lat = if (mDroneLocation != null) mDroneLocation!!.latitude else Double.NaN
            val lon = if (mDroneLocation != null) mDroneLocation!!.longitude else Double.NaN
            val aslValue = GpsUtils.egm96Altitude(mHomePointAltitude + mAltitude, lat, lon)
            val unitType =
                if (isHeightUnitMetric) UnitUtils.UnitType.METRIC else UnitUtils.UnitType.IMPERIAL
            val value =
                UnitUtils.getValueFromMetricByLength(aslValue.toFloat(), unitType)

            emitter.onNext(value)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { aValue: Any? ->
            val value = String.format(Locale.US, "%06.1f", aValue)
            if (mTvAslValue?.text != value) {
                mTvAslValue?.text = value
            }
        })
    }

    /**
     * Converts the speed to preferred unit.
     *
     * @param speed speed in meters per second
     */
    private fun convertSpeedUnit(speed: Float): Float {
        return when (speedUnit) {
            UnitsDJIV5.MILES_PER_HOUR.name -> {
                UnitConversionUtil.convertMetersPerSecToMilesPerHr(speed)
            }
            UnitsDJIV5.KILOMETRE_PER_HOUR.name -> {
                UnitConversionUtil.convertMetersPerSecToKmPerHr(speed)
            }
            else -> {
                // meters per second
                speed
            }
        }
    }

    private fun updateSpeed() {
        var showSpeedZ = mSpeedZ
        if (!java.lang.Float.isNaN(mSpeedZ) && mSpeedZ != 0f) {
            showSpeedZ = -mSpeedZ
        }
        val value = convertSpeedUnit(showSpeedZ)
        mTvVsValue?.text = String.format(Locale.US, "%03.1f", value)
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        loadLayout(context)
        mAttitudeDashBoard = findViewById(R.id.pfd_attitude_dash_board)
        mTvAslText = findViewById(R.id.pfd_asl_text)
        mTvAslValue = findViewById(R.id.pfd_asl_value)
        mTvVsText = findViewById(R.id.pfd_vs_text)
        mTvVsValue = findViewById(R.id.pfd_vs_value)
    }

    open fun loadLayout(context: Context) {
        inflate(context, R.layout.uxsdk_liveview_pfd_attitude_display_widget, this)
    }

    override fun reactToModelChanges() {
        mCompositeDisposable.add(widgetModel.velocityProcessor.toFlowable().observeOn(SchedulerProvider.ui()).subscribe { velocity3D: Velocity3D ->
            mSpeedZ = velocity3D.z.toFloat()
            updateSpeed()
        })
        //Relative altitude of the aircraft relative to take off location, measured by the barometer, in meters.
        mCompositeDisposable.add(widgetModel.altitudeProcessor.toFlowable().observeOn(SchedulerProvider.ui()).subscribe { altitude: Double ->
            mAltitude = altitude
            updateAltitude()
        })
        //RTK起飞高度信息
        mCompositeDisposable.add(widgetModel.rtkTakeoffAltitudeInfoProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .filter { info: RTKTakeoffAltitudeInfo -> Math.abs(mHomePointAltitude - info.altitude.toFloat()) >= 0.001 }
            .subscribe { rtkTakeoffAltitudeInfo: RTKTakeoffAltitudeInfo ->
                mHomePointAltitude = rtkTakeoffAltitudeInfo.altitude
                updateAltitude()
            })
        mCompositeDisposable.add(widgetModel.aircraftLocationDataProcessor.toFlowable()
            .observeOn(SchedulerProvider.ui())
            .subscribe { locationCoordinate2D: LocationCoordinate2D? ->
            mDroneLocation = locationCoordinate2D
            if (mDroneLocation != null) {
                updateAltitude()
            }
        })
    }

    override fun getIdealDimensionRatioString(): String? {
        return null
    }
}