package dji.v5.ux.core.ui.setting.fragment;

import dji.v5.utils.common.ContextUtil;
import dji.v5.utils.common.StringUtils;
import dji.v5.ux.R;
import dji.v5.ux.core.ui.setting.ui.MenuFragment;

public class RcButtonCustomizationFragment extends MenuFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.uxsdk_fragment_rc_button_customization;
    }

    @Override
    protected String getPreferencesTitle() {
        return StringUtils.getResStr(
                ContextUtil.getContext(),
                R.string.uxsdk_setting_ui_customize_rc_buttons
        );
    }
}
