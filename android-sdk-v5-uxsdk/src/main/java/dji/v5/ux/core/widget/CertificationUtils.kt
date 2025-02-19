package dji.v5.ux.core.widget

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.v5.ux.core.util.units.DataStoreManagerDJIV5

/**
 * This class acts as a state holder for any flags or variables that maybe constant or set
 * according to our needs.
 */
object CertificationUtils {
    fun isCertificationBuild(): Boolean {
        val flag = DataStoreManagerDJIV5.get(
            DataStoreManagerDJIV5.EndPoints.IS_CERTIFICATION_BUILD.endPoint
        )
        return Gson().fromJson(
            flag,
            object : TypeToken<Boolean>() {}.type
        ) ?: false
    }
}