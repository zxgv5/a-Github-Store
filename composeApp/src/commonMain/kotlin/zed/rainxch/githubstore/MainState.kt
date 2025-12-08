package zed.rainxch.githubstore

import zed.rainxch.githubstore.core.presentation.model.AppTheme

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val currentColorTheme: AppTheme = AppTheme.OCEAN,
)
