package dji.v5.ux.core.widget

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.v5.ux.core.model.FirmwareDataConfig
import dji.v5.ux.core.util.units.DataStoreManagerDJIV5
import kotlinx.coroutines.flow.map

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

    fun observeDataParams(): LiveData<FirmwareDataConfig?> {
        return try {
            DataStoreManagerDJIV5.observe(
                DataStoreManagerDJIV5.EndPoints.CERTIFICATION_DATA_PARAMS.endPoint
            )?.map { data ->
                Gson().fromJson<FirmwareDataConfig>(
                    data,
                    object : TypeToken<FirmwareDataConfig>() {}.type
                )
            }?.asLiveData() ?: liveData { emit(null) }
        } catch (e: Exception) {
            liveData { emit(null) }
        }
    }
}