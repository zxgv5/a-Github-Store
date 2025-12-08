package zed.rainxch.githubstore.app.navigation

import kotlinx.serialization.Serializable

sealed interface GithubStoreGraph {
    @Serializable
    data object HomeScreen : GithubStoreGraph

    @Serializable
    data object SearchScreen : GithubStoreGraph

    @Serializable
    data object AuthenticationScreen : GithubStoreGraph

    @Serializable
    data class DetailsScreen(
        val repositoryId: Int
    ) : GithubStoreGraph

    @Serializable
    data object SettingsScreen : GithubStoreGraph
}