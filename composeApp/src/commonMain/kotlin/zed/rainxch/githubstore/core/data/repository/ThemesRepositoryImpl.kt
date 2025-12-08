package zed.rainxch.githubstore.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zed.rainxch.githubstore.core.domain.repository.ThemesRepository
import zed.rainxch.githubstore.core.presentation.model.AppTheme

class ThemesRepositoryImpl(
    private val preferences: DataStore<Preferences>
) : ThemesRepository {
    private val THEME_KEY = stringPreferencesKey("app_theme")


    override fun getThemeColor(): Flow<AppTheme> {
        return preferences.data.map { prefs ->
            val themeName = prefs[THEME_KEY]
            AppTheme.fromName(themeName)
        }
    }

    override suspend fun setThemeColor(theme: AppTheme) {
        preferences.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
    }

}