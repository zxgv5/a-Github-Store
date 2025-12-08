package zed.rainxch.githubstore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.githubstore.core.presentation.model.AppTheme

interface ThemesRepository {
    fun getThemeColor(): Flow<AppTheme>
    suspend fun setThemeColor(theme: AppTheme)
}