package zed.rainxch.domain.model

enum class SortBy {
    MostStars,
    MostForks,
    BestMatch,
    RecentlyUpdated,
    RecentlyReleased,
    ;

    /**
     * GitHub's REST `/search/repositories?sort=...` doesn't expose a
     * "by latest release date" axis — only repo-level activity. When the
     * backend isn't reachable and we fall through here, RecentlyReleased
     * borrows `updated` semantics (closest available approximation) so
     * the UI doesn't degrade silently to relevance order.
     */
    fun toGithubSortParam(): String? =
        when (this) {
            MostStars -> "stars"
            MostForks -> "forks"
            BestMatch -> null
            RecentlyUpdated -> "updated"
            RecentlyReleased -> "updated"
        }
}
