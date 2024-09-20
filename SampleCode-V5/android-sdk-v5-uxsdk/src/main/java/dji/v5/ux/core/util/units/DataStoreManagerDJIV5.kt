package dji.v5.ux.core.util.units

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object DataStoreManagerDJIV5 {

    private lateinit var prefsDataStore: DataStore<Preferences>

    enum class EndPoints(val endPoint: String) {
        DISTANCE_UNIT_PREFERENCE("distance_unit_preference"),
        HEIGHT_UNIT_PREFERENCE("height_unit_preference"),
        SPEED_UNIT_PREFERENCE("speed_unit_preference"),
        AREA_UNIT_PREFERENCE("area_unit_preference"),
        TEMPERATURE_UNIT_PREFERENCE("temperature_unit_preference"),
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

}

