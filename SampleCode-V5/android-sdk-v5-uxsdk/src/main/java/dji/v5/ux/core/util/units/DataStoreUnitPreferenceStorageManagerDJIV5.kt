package dji.v5.ux.core.util.units

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.v5.ux.core.util.units.DataStoreManagerDJIV5.EndPoints

object DataStoreUnitPreferenceStorageManagerDJIV5 {
    private val gson by lazy {
        Gson()
    }

    /**
     * Non blocking to allow accessing from Java. Avoid fetching the units from datastore multiple
     * times. Cache the value locally to avoid race conditions and concurrent access.
     */
    fun get(unitSetting: UnitSetting): String? {
        return try {
            val key = getPreferenceKey(unitSetting)
            val value = DataStoreManagerDJIV5.get(key)
            gson.fromJson(
                value,
                object : TypeToken<String>() {}.type
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets the preference key used to store the preference for the particular [UnitSetting]
     *
     * @param unitSetting represents the type of setting for the units.
     * @return unit which is used to store and retrieve the data from DataStore.
     */
    private fun getPreferenceKey(unitSetting: UnitSetting): String {
        return when (unitSetting) {
            UnitSetting.DISTANCE -> EndPoints.DISTANCE_UNIT_PREFERENCE.endPoint
            UnitSetting.HEIGHT -> EndPoints.HEIGHT_UNIT_PREFERENCE.endPoint
            UnitSetting.SPEED -> EndPoints.SPEED_UNIT_PREFERENCE.endPoint
            UnitSetting.TEMPERATURE -> EndPoints.TEMPERATURE_UNIT_PREFERENCE.endPoint
            UnitSetting.AREA -> EndPoints.AREA_UNIT_PREFERENCE.endPoint
        }
    }

    /**
     * Gets the preferred speed unit or default unit.
     *
     * @return preferred speed unit or default unit - metre per sec
     */
    fun getSpeedUnit(): String {
        return get(UnitSetting.SPEED) ?: UnitsDJIV5.METRE_PER_SECOND.name
    }

    /**
     * Gets the preferred height unit or default unit - metric.
     *
     * @return preferred height unit.
     */
    private fun getHeightUnit(): String {
        return get(UnitSetting.HEIGHT) ?: DistanceAndHeightDisplayUnitType.METRIC.name
    }

    /**
     * Checks if the height unit is metric.
     *
     * @return true if the height unit is metric.
     */
    fun isHeightMetric(): Boolean {
        return getHeightUnit() == DistanceAndHeightDisplayUnitType.METRIC.name
    }

    /**
     * Checks if the distance unit is metric.
     *
     * @return true if the distance unit is metric.
     */
    fun isDistanceMetric(): Boolean {
        return isHeightMetric()
    }

    /**
     * Returns the preferred temperature or default unit - Celsius.
     */
    fun getTemperatureUnit(): UnitsDJIV5 {
        val unitStr = get(UnitSetting.TEMPERATURE)
        val unit = UnitsDJIV5.valueOf(unitStr ?: UnitsDJIV5.CELSIUS.name)
        return unit
    }

}