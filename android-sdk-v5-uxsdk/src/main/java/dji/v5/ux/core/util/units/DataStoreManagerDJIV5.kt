package dji.v5.ux.core.util.units

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object DataStoreManagerDJIV5 {

    private const val TAG = "DataStoreManagerDJIV5"

    private lateinit var prefsDataStore: DataStore<Preferences>

    private val gson by lazy {
        Gson()
    }

    enum class EndPoints(val endPoint: String) {
        DISTANCE_UNIT_PREFERENCE("distance_unit_preference"),
        HEIGHT_UNIT_PREFERENCE("height_unit_preference"),
        SPEED_UNIT_PREFERENCE("speed_unit_preference"),
        AREA_UNIT_PREFERENCE("area_unit_preference"),
        TEMPERATURE_UNIT_PREFERENCE("temperature_unit_preference"),
        IS_CERTIFICATION_BUILD("is_certification_build"),
        CERTIFICATION_DATA_PARAMS("certification_data_params"),
        CUSTOM_BUTTON_SETTINGS("custom_button_settings"),
    }

    /**
     * Set the DataStore instance. There can only be one reference of data store at a time.
     * Call immediately when after the Application is created.
     */
    fun setDatastore(datastore: DataStore<Preferences>) {
        prefsDataStore = datastore
    }

    fun get(key: String): String? {
        return try {
            runBlocking {
                prefsDataStore.data.map {
                    it[stringPreferencesKey(key)]
                }.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun set(key: String, data: Any): Boolean {
        try {
            prefsDataStore.edit {
                val jsonData = gson.toJson(data)
                Log.d(TAG, "Writing data $key $jsonData")
                it[stringPreferencesKey(key)] = jsonData
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write data for $key, error: $e")
            return false
        }
        return true
    }

    fun observe(key: String): Flow<String?>? {
        return try {
            prefsDataStore.data.map {
                it[stringPreferencesKey(key)]
            }
        } catch (e: Exception) {
            null
        }
    }

}

