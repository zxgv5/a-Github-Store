package zed.rainxch.githubstore.feature.auth.presentation

import zed.rainxch.githubstore.feature.auth.data.DeviceStart

sealed interface AuthenticationAction {
    data class StartLogin(val scope: String) : AuthenticationAction
    data class CopyCode(val start: DeviceStart) : AuthenticationAction
    data class OpenGitHub(val start: DeviceStart) : AuthenticationAction
    data object Logout : AuthenticationAction
    data object MarkLoggedOut : AuthenticationAction
    data object MarkLoggedIn : AuthenticationAction
    data class OnInfo(val message: String) : AuthenticationAction
}