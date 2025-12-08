package zed.rainxch.githubstore.feature.details.presentation

sealed interface DetailsEvent {
    data class OnOpenRepositoryInApp(val repositoryId: Int) : DetailsEvent
}