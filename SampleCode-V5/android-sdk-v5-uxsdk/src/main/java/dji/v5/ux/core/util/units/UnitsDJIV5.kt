package dji.v5.ux.core.util.units

/**
 * These represents all possible units for conversion in UX SDK.
 */
enum class UnitsDJIV5 {
    METRE,
    FEET,
    KILOMETRE_PER_HOUR,
    METRE_PER_SECOND,
    MILES_PER_HOUR,
    CELSIUS,
    FAHRENHEIT,
    UNKNOWN;

    /**
     * Formats the unit value to string representation. Localization is not supported in UX SDK.
     *
     * @param unit The unit value to be formatter
     * @return The formatted string
     */
    fun formattedStr(): String = when (this) {
        KILOMETRE_PER_HOUR -> "km/h"
        METRE_PER_SECOND -> "m/s"
        MILES_PER_HOUR -> "mi/h"
        METRE -> "m"
        FEET -> "ft"
        UNKNOWN -> ""
        CELSIUS -> "°C"
        FAHRENHEIT -> "°F"
    }

    companion object {

        /**
         * Parses the string representation to the unit value.
         *
         * @param value The string representation to be parsed
         * @return The unit value
         */
        fun fromString(value: String): UnitsDJIV5 {
            return try {
                UnitsDJIV5.valueOf(value)
            } catch (e: Exception) {
                UNKNOWN
            }
        }
    }
}

/**
 * This enum is used as key to persist the user preference in DataStore.
 */
enum class UnitSetting {
    HEIGHT, DISTANCE, AREA, SPEED, TEMPERATURE;
}

/**
 * This enum is used as value to persist the user preference in DataStore.
 */
enum class DistanceAndHeightDisplayUnitType {
    METRIC, IMPERIAL;
}
