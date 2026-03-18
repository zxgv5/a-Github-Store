package zed.rainxch.core.presentation.model

data class DiscoveryRepositoryUi(
    val isInstalled: Boolean,
    val isUpdateAvailable: Boolean,
    val isFavourite: Boolean,
    val isStarred: Boolean,
    val repository: GithubRepoSummaryUi,
)
