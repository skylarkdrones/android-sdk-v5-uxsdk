package dji.v5.ux.visualcamera.thermal

import androidx.annotation.DrawableRes
import dji.sdk.keyvalue.value.camera.CameraThermalPalette
import dji.v5.ux.R


object StreamPanelUtil {

    val THERMAL_PALETTE_MODEL_LIST: List<ThermalPaletteModel> by lazy {
        mutableListOf(
            ThermalPaletteModel(CameraThermalPalette.WHITE_HOT, "WhiteHot", R.drawable.uxsdk_thermal_palette_wh, R.drawable.uxsdk_thermal_palette_wh_bar),
            ThermalPaletteModel(CameraThermalPalette.BLACK_HOT, "BlackHot", R.drawable.uxsdk_thermal_palette_black_hot, R.drawable.uxsdk_thermal_palette_black_hot_bar),
            ThermalPaletteModel(CameraThermalPalette.RED_HOT, "Tint", R.drawable.uxsdk_thermal_palette_ink_green_red, R.drawable.uxsdk_thermal_palette_ink_green_red_bar),
            ThermalPaletteModel(CameraThermalPalette.IRONBOW1, "IronRed", R.drawable.uxsdk_thermal_palette_blue_red_yellow, R.drawable.uxsdk_thermal_palette_blue_red_yellow_bar),
            ThermalPaletteModel(CameraThermalPalette.COLOR2, "HotIron", R.drawable.uxsdk_thermal_palette_purple_red_yellow, R.drawable.uxsdk_thermal_palette_purple_red_yellow_bar),
            ThermalPaletteModel(CameraThermalPalette.ICE_FIRE, "Arctic", R.drawable.uxsdk_thermal_palette_arctic, R.drawable.uxsdk_thermal_palette_arctic_bar),
            ThermalPaletteModel(CameraThermalPalette.GREEN_HOT, "Medical", R.drawable.uxsdk_thermal_palette_blue_green_red, R.drawable.uxsdk_thermal_palette_blue_green_red_bar),
            ThermalPaletteModel(CameraThermalPalette.COLOR1, "Fulgurite", R.drawable.uxsdk_thermal_palette_bh, R.drawable.uxsdk_thermal_palette_bh_bar),
            ThermalPaletteModel(CameraThermalPalette.RAINBOW, "Rainbow1", R.drawable.uxsdk_thermal_palette_rainbow1, R.drawable.uxsdk_thermal_palette_rainbow1_bar),
        )
    }

    data class ThermalPaletteModel(
        val sourceType: CameraThermalPalette,
        val name: String,
        @DrawableRes
        val image: Int = -1,
        @DrawableRes
        val colorBar: Int = -1
    )
}