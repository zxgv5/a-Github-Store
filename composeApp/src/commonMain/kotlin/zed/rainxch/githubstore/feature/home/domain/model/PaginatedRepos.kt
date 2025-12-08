package zed.rainxch.githubstore.feature.home.domain.model

import zed.rainxch.githubstore.core.domain.model.GithubRepoSummary

data class PaginatedRepos(
    val repos: List<GithubRepoSummary>,
    val hasMore: Boolean,
    val nextPageIndex: Int,
    val totalCount: Int? = null
)