// This is an example, you need to ensure your actual UserPreferences is correct.
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    // Define keys for your preferences
    private object PreferencesKeys {
        val USER_ID = intPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val TOKEN = stringPreferencesKey("token")
        // Add other keys as needed
    }

    val id: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    val email: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EMAIL]
        }

    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TOKEN]
        }


    suspend fun saveUserData(id: Int, username: String, email: String, createdAt: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.TOKEN] = token
            // Save createdAt if you need it in preferences
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}