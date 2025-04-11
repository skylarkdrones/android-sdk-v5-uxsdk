package dji.v5.ux.flight.flightparam;


import static dji.sdk.keyvalue.value.flightcontroller.GoHomePathMode.HEIGHT_FIXED;
import static dji.sdk.keyvalue.value.flightcontroller.GoHomePathMode.UNKNOWN;
import static dji.v5.ux.core.base.SchedulerProvider.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import dji.sdk.keyvalue.value.flightcontroller.GoHomePathMode;
import dji.v5.ux.R;
import dji.v5.ux.core.base.DJISDKModel;
import dji.v5.ux.core.base.EditorCell;
import dji.v5.ux.core.base.SwitcherCell;
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget;
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore;
import dji.v5.ux.core.util.ViewUtil;
import dji.v5.ux.core.widget.CertificationUtils;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class DistanceLimitWidget extends ConstraintLayoutWidget<Object> implements EditorCell.OnValueChangedListener {

    private DistanceLimitWidgetModel widgetModel = new DistanceLimitWidgetModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance());

    private EditorCell mGoHomeEditCell;

    private EditorCell mMaxHeightEditCell;
    private EditorCell mMaxRadiusEditorCell;
    private SwitcherCell mMaxRadiusCell;
    private TextView mRTHTipTv;
    private int currMaxHeight = 500;
    private static final int ALARM_HEIGHT = 120;
    private static final int CONFIRM_ALARM_HEIGHT = 500;

    private static final int MIN_ALTITUDE = 20;
    private int MAX_ALTITUDE = 120;
    private static final int MIN_DISTANCE = 20;
    private int MAX_DISTANCE = 1000;

    public DistanceLimitWidget(@NonNull Context context) {
        super(context);
    }

    public DistanceLimitWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DistanceLimitWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setupLimits(int maxDistance, int maxAltitude) {
        setupLimitAndBuffer(maxDistance, maxAltitude);
        restrictDistanceLimitCell();
        setupDistanceLimit();
        setupAltitudeLimit();
        setupRTHLimit();
    }

    private void observeLimits() {
        if (CertificationUtils.INSTANCE.isCertificationBuild()) {
            CertificationUtils.INSTANCE.observeDataParams(
                    (LifecycleOwner) getContext(),
                    (maxDistance, maxAltitude) -> {
                        setupLimits(
                                maxDistance,
                                maxAltitude
                        );

                        // Update the cell values with existing to trigger checks & be within new
                        // limit.
                        updateHeightLimit(mMaxHeightEditCell.getValue());
                        updateDistanceLimit(mMaxRadiusEditorCell.getValue());
                        updateGoHomeHeight(mGoHomeEditCell.getValue());

                        return null;
                    });
        }
    }

    private void setupLimitAndBuffer(int maxDistance, int maxAltitude) {
        MAX_ALTITUDE = maxAltitude;
        MAX_DISTANCE = maxDistance;
    }

    /**
     * The distance switch cell should be checked but disabled, in order to prevent users disabling
     * distance limit.
     */
    private void restrictDistanceLimitCell() {
        mMaxRadiusCell.setChecked(true);
        mMaxRadiusCell.setEnabled(false);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.uxsdk_setting_menu_limit_distance_set_layout, this);
        mGoHomeEditCell = findViewById(R.id.setting_menu_aircraft_goHomeAttitude);
        mGoHomeEditCell.setOnValueChangedListener(this);
        mMaxHeightEditCell = findViewById(R.id.setting_menu_aircraft_maxHeight);
        mMaxHeightEditCell.setOnValueChangedListener(this);
        mMaxRadiusEditorCell = findViewById(R.id.setting_menu_aircraft_maxRadius);
        mMaxRadiusEditorCell.setOnValueChangedListener(this);
        mMaxRadiusCell = findViewById(R.id.setting_menu_aircraft_maxRadius_switch);
        mRTHTipTv = findViewById(R.id.setting_menu_aircraft_go_home_mode_desc);

        mMaxRadiusCell.setOnCheckedChangedListener((cell, isChecked) -> {
            widgetModel.setDistanceLimitEnabled(isChecked).subscribe();
            if(isChecked) {
                mMaxRadiusEditorCell.setVisibility(VISIBLE);
            } else  {
                mMaxRadiusEditorCell.setVisibility(GONE);
            }
        });
    }

    private void setupAltitudeLimit() {
        setMaxHeight(MAX_ALTITUDE);

        mMaxHeightEditCell.setMinValue(MIN_ALTITUDE);
        mMaxHeightEditCell.setMaxValue(MAX_ALTITUDE);
        mMaxHeightEditCell.setTips(MIN_ALTITUDE + "~" + MAX_ALTITUDE + "m");
    }

    private void setupRTHLimit() {
        mGoHomeEditCell.setMinValue(MIN_ALTITUDE);
        mGoHomeEditCell.setMaxValue(MAX_ALTITUDE);
        mGoHomeEditCell.setTips(MIN_ALTITUDE + "~" + MAX_ALTITUDE + "m");
    }

    private void setupDistanceLimit() {
        mMaxRadiusEditorCell.setMinValue(MIN_DISTANCE);
        mMaxRadiusEditorCell.setMaxValue(MAX_DISTANCE);
        mMaxRadiusEditorCell.setTips(MIN_DISTANCE + "~" + MAX_DISTANCE + "m");
    }

    public void setMaxHeight(int maxHeight) {
        this.currMaxHeight = maxHeight;
    }

    @Override
    protected void reactToModelChanges() {
        addReaction(widgetModel.getGoHomeHeight().observeOn(ui()).subscribe(this::updateGoHomeHeight));
        addReaction(widgetModel.getHomeLimitHeight().observeOn(ui()).subscribe(this::updateHeightLimit));
        addReaction(widgetModel.getDistanceLimit().observeOn(ui()).subscribe(this::updateDistanceLimit));
        addReaction(widgetModel.getDistanceLimitEnabled().observeOn(ui()).subscribe(this::updateDistanceLimitEnable));
        addReaction(widgetModel.getGoHomePathMode().observeOn(ui()).subscribe(this::updateGoHomeMode));

        // Setup certification build limits
        observeLimits();
    }

    private void updateGoHomeMode(GoHomePathMode goHomePathMode) {
        // 别的飞机可能没有智能返航功能，如果是UNKNOWN的话，认为是旧的设定高度返航模式，提示设定高度返航的文案
        if (goHomePathMode == HEIGHT_FIXED || goHomePathMode == UNKNOWN) {
            mRTHTipTv.setText(R.string.uxsdk_setting_menu_flyc_smart_rth_set_altitude);
        } else {
            mRTHTipTv.setText(R.string.uxsdk_setting_menu_flyc_smart_rth_smart_altitude);
        }
    }

    private void updateDistanceLimitEnable(Boolean isChecked) {
        mMaxRadiusCell.setChecked(isChecked);
        if(isChecked) {
            mMaxRadiusEditorCell.setVisibility(VISIBLE);
        } else  {
            mMaxRadiusEditorCell.setVisibility(GONE);
        }
    }

    private void updateDistanceLimit(Integer integer) {
        if (CertificationUtils.INSTANCE.isCertificationBuild()) {
            mMaxRadiusEditorCell.setValue(Math.min(MAX_DISTANCE, integer));
        } else {
            mMaxRadiusEditorCell.setValue(integer);
        }
        // This check ensures the initial value set on drone is caught and reset accordingly
        if (CertificationUtils.INSTANCE.isCertificationBuild() && integer > MAX_DISTANCE) {
            widgetModel.setDistanceLimit(MAX_DISTANCE).observeOn(ui()).subscribe(getFinishObserve());
        }
    }

    private void updateHeightLimit(Integer integer) {
        if (CertificationUtils.INSTANCE.isCertificationBuild()) {
            currMaxHeight = Math.min(MAX_ALTITUDE, integer);
        } else {
            currMaxHeight = integer;
        }
        mMaxHeightEditCell.setValue(currMaxHeight);
        // This check ensures the initial value set on drone is caught and reset accordingly
        if (CertificationUtils.INSTANCE.isCertificationBuild() && integer > MAX_ALTITUDE) {
            widgetModel.setHeightLimit(MAX_ALTITUDE).observeOn(ui()).subscribe(getFinishObserve());
        }
    }

    private void updateGoHomeHeight(Integer integer) {
        mGoHomeEditCell.setValue(integer);
        // This check ensures the initial value set on drone is caught and reset accordingly
        if (CertificationUtils.INSTANCE.isCertificationBuild() && integer > MAX_ALTITUDE) {
            widgetModel.setGoHomeHeight(MAX_ALTITUDE).observeOn(ui()).subscribe(getFinishObserve());
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            widgetModel.setup();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            widgetModel.cleanup();
        }
    }


    @Override
    public void onValueChanged(EditorCell cell, int inputValue, int validValue) {
        if (inputValue > cell.getMaxValue() ||  inputValue < cell.getMinValue()) {
           ViewUtil.showToast(getContext() , R.string.uxsdk_setting_menu_setting_fail , Toast.LENGTH_SHORT);
           return;
        }
        if (cell.getId() == R.id.setting_menu_aircraft_goHomeAttitude) {
            if (inputValue > currMaxHeight) {
                ViewUtil.showToast(getContext() , R.string.uxsdk_setting_menu_flyc_gohome_altitude_limit , Toast.LENGTH_SHORT);
                return;
            }
            widgetModel.setGoHomeHeight( inputValue).observeOn(ui()).subscribe(getFinishObserve());
        } else if(cell.getId() == R.id.setting_menu_aircraft_maxHeight) {
            checkMaxFlightHeight(inputValue);
            widgetModel.setHeightLimit( inputValue).observeOn(ui()).subscribe(getFinishObserve());
        }  else if(cell.getId() == R.id.setting_menu_aircraft_maxRadius){
            widgetModel.setDistanceLimit(inputValue).observeOn(ui()).subscribe(getFinishObserve());
        }

    }

    private void checkMaxFlightHeight(int inputValue) {
        if(inputValue <= CONFIRM_ALARM_HEIGHT  && inputValue > ALARM_HEIGHT){
            ViewUtil.showToast(getContext() , getContext().getString(R.string.uxsdk_setting_menu_flyc_limit_high_notice) , Toast.LENGTH_LONG);
        }else if (inputValue > CONFIRM_ALARM_HEIGHT){
            ViewUtil.showToast(getContext() , getContext().getString(R.string.uxsdk_setting_menu_flyc_limit_high_notice_above_500) , Toast.LENGTH_LONG);
        }


    }

    private CompletableObserver getFinishObserve(){
       return  new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //do nothing
            }

            @Override
            public void onComplete() {
                ViewUtil.showToast(getContext(), R.string.uxsdk_setting_menu_setting_success , Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                //add log
            }
        };
    }

}
