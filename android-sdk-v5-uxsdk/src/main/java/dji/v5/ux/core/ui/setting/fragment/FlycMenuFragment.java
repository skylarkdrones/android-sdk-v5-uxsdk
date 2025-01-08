package dji.v5.ux.core.ui.setting.fragment;

import static dji.v5.ux.core.ui.setting.ui.SettingMenuFragment.ARG_MAX_DISTANCE;
import static dji.v5.ux.core.ui.setting.ui.SettingMenuFragment.ARG_MIN_DISTANCE;
import static dji.v5.ux.core.ui.setting.ui.SettingMenuFragment.DEFAULT_MAX_DISTANCE;
import static dji.v5.ux.core.ui.setting.ui.SettingMenuFragment.DEFAULT_MIN_DISTANCE;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.v5.utils.common.ContextUtil;
import dji.v5.utils.common.StringUtils;
import dji.v5.ux.R;
import dji.v5.ux.core.base.EditorCell;
import dji.v5.ux.core.base.SwitcherCell;
import dji.v5.ux.core.base.TextCell;
import dji.v5.ux.core.ui.setting.ui.MenuFragment;
import dji.v5.ux.flight.flightparam.DistanceLimitWidget;

/**
 * Description :
 *
 * @author: Byte.Cai
 * date : 2022/11/21
 * <p>
 * Copyright (c) 2022, DJI All Rights Reserved.
 */
public class FlycMenuFragment extends MenuFragment {
    private int minDistance;
    private int maxDistance;

    @Override
    protected String getPreferencesTitle() {
        return StringUtils.getResStr(ContextUtil.getContext(), R.string.uxsdk_setting_menu_title_flyc);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.uxsdk_setting_menu_aircraft_layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        minDistance = args.getInt(ARG_MIN_DISTANCE, DEFAULT_MIN_DISTANCE);
        maxDistance = args.getInt(ARG_MAX_DISTANCE, DEFAULT_MAX_DISTANCE);

        TextCell textCell = view.findViewById(R.id.setting_flyc_sensors_state);
        textCell.setOnClickListener(view1 -> {
            SensorsMenuFragment fragment = new SensorsMenuFragment();
            addFragment(getFragmentManager(), fragment, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();

        if (view == null) {
            return;
        }

        DistanceLimitWidget distanceLimitWidget = view.findViewById(R.id.layoutDistanceWidget);
        if (distanceLimitWidget != null) {
            distanceLimitWidget.setMaxRadius(maxDistance);
            EditorCell maxDistanceCell = distanceLimitWidget.findViewById(
                    R.id.setting_menu_aircraft_maxRadius
            );

            maxDistanceCell.setMinValue(minDistance);
            maxDistanceCell.setMaxValue(maxDistance);
            maxDistanceCell.setTips(minDistance + "~" + maxDistance + "m");
        }
    }
}
