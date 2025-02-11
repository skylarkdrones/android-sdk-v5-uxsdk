package dji.v5.ux.flight.flightparam;


import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.sdk.keyvalue.value.flightcontroller.FailsafeAction;
import dji.v5.ux.R;
import dji.v5.ux.accessory.DescSpinnerCell;
import dji.v5.ux.core.base.DJISDKModel;
import dji.v5.ux.core.base.SchedulerProvider;
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget;
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore;
import dji.v5.ux.core.widget.setting.SettingPanelState;

public class LostActionWidget extends ConstraintLayoutWidget<Object> {

    LostActionWidgetModel widgetModel = new LostActionWidgetModel(DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance());
    DescSpinnerCell spinnerCell;

    public LostActionWidget(@NonNull Context context) {
        super(context);
        setupLostSignalAction();
    }

    public LostActionWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupLostSignalAction();
    }

    public LostActionWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupLostSignalAction();
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.uxsdk_widget_flight_lost_action, this);
        spinnerCell = findViewById(R.id.setting_menu_aircraft_failSafe);
        spinnerCell.addOnItemSelectedListener(position -> {
            FailsafeAction value = FailsafeAction.find(position);
            widgetModel.setLostAction(value).subscribe();
        });
    }

    private void setupLostSignalAction() {
        if (SettingPanelState.INSTANCE.isCertificationBuild()) {
            widgetModel.setLostAction(FailsafeAction.GOHOME).subscribe();
            spinnerCell.setEnabled(false);
        }
    }

    @Override
    protected void reactToModelChanges() {
        addReaction(widgetModel.getLostActionFlowable().observeOn(SchedulerProvider.ui()).subscribe(this::updateSelection));
    }

    private void updateSelection(FailsafeAction failsafeAction) {
        spinnerCell.select(failsafeAction.value());
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


}
