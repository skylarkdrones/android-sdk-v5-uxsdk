package dji.v5.ux.core.widget

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.v5.ux.core.model.DataParams
import dji.v5.ux.core.util.units.DataStoreManagerDJIV5

/**
 * This class acts as a state holder for any flags or variables that maybe constant or set
 * according to our needs.
 */
object CertificationUtils {

    private const val TAG = "CertificationUtils"

    // Limit in meters
    private const val DEFAULT_MAX_DISTANCE_LIMIT = 1000.0
    private const val DEFAULT_MAX_ALTITUDE_LIMIT = 120.0

    fun isCertificationBuild(): Boolean {
        val flag = DataStoreManagerDJIV5.get(
            DataStoreManagerDJIV5.EndPoints.IS_CERTIFICATION_BUILD.endPoint
        )
        try {
            return Gson().fromJson(
                flag,
                object : TypeToken<Boolean>() {}.type
            ) ?: false
        } catch (e: Exception) {
            Log.d(TAG, "isCertificationBuild: fetch failed, reason: ${e.message}")
            return false
        }
    }

    fun getMaxDistanceLimit(): Int {
        return try {
            (getDataParams()?.distance?.max ?: DEFAULT_MAX_DISTANCE_LIMIT).toInt()
        } catch (e: Exception) {
            Log.d(TAG, "CertificationDataPrams distance: fetch failed, reason: ${e.message}")
            DEFAULT_MAX_DISTANCE_LIMIT.toInt()
        }
    }

    fun getMaxAltitudeLimit(): Int {
        return try {
            (getDataParams()?.altitude?.max ?: DEFAULT_MAX_ALTITUDE_LIMIT).toInt()
        } catch (e: Exception) {
            Log.d(TAG, "CertificationDataPrams altitude: fetch failed, reason: ${e.message}")
            DEFAULT_MAX_ALTITUDE_LIMIT.toInt()
        }
    }

    private fun getDataParams(): DataParams? {
        val data = DataStoreManagerDJIV5.get(
            DataStoreManagerDJIV5.EndPoints.CERTIFICATION_DATA_PARAMS.endPoint
        )
        return Gson().fromJson<DataParams>(
            data,
            object : TypeToken<DataParams>() {}.type
        )
    }
}