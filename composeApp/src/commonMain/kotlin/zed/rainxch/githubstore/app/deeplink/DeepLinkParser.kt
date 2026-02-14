package zed.rainxch.githubstore.app.deeplink

sealed interface DeepLinkDestination {
    data class Repository(val owner: String, val repo: String) : DeepLinkDestination
    data object None : DeepLinkDestination
}

object DeepLinkParser {

    private val githubExcludedPaths = setOf(
        "settings", "notifications", "explore", "marketplace",
        "login", "signup", "join", "new", "organizations",
        "topics", "trending", "collections", "sponsors",
        "features", "security", "pricing", "enterprise",
        "about", "customer-stories", "readme", "team"
    )

    /**
     * Parses a URI string into a [DeepLinkDestination].
     *
     * Supported formats:
     * - `https://github.com/{owner}/{repo}`
     * - `https://github.com/{owner}/{repo}/...` (any sub-path, extra segments ignored)
     * - `https://github-store.org/app/{owner}/{repo}`
     * - `githubstore://repo/{owner}/{repo}`
     */
    fun parse(uri: String): DeepLinkDestination {
        val trimmed = uri.trim()

        // Handle githubstore://repo/{owner}/{repo}
        if (trimmed.startsWith("githubstore://repo/")) {
            val path = trimmed.removePrefix("githubstore://repo/")
            return parseOwnerRepo(path)
        }

        // Handle https://github.com/{owner}/{repo}
        val githubPattern = Regex("^https?://github\\.com/([^/]+)/([^/?#]+)")
        githubPattern.find(trimmed)?.let { match ->
            val owner = match.groupValues[1]
            val repo = match.groupValues[2]
            if (isValidOwnerRepo(owner, repo)) {
                return DeepLinkDestination.Repository(owner, repo)
            }
        }

        // Handle https://github-store.org/app/{owner}/{repo}
        val storePattern = Regex("^https?://github-store\\.org/app/([^/]+)/([^/?#]+)")
        storePattern.find(trimmed)?.let { match ->
            val owner = match.groupValues[1]
            val repo = match.groupValues[2]
            if (owner.isNotEmpty() && repo.isNotEmpty()) {
                return DeepLinkDestination.Repository(owner, repo)
            }
        }

        return DeepLinkDestination.None
    }

    private fun parseOwnerRepo(path: String): DeepLinkDestination {
        val segments = path.split("/").filter { it.isNotEmpty() }
        if (segments.size >= 2 && segments[0].isNotEmpty() && segments[1].isNotEmpty()) {
            return DeepLinkDestination.Repository(segments[0], segments[1])
        }
        return DeepLinkDestination.None
    }

    private fun isValidOwnerRepo(owner: String, repo: String): Boolean {
        return owner.isNotEmpty() && repo.isNotEmpty()
                && owner.lowercase() !in githubExcludedPaths
    }
}
