package zed.rainxch.githubstore.feature.settings.presentation

import zed.rainxch.githubstore.core.presentation.model.AppTheme

data class SettingsState(
    val selectedThemeColor: AppTheme = AppTheme.OCEAN
)