package zed.rainxch.core.domain.model

import zed.rainxch.core.domain.util.VersionMath

/**
 * Single source of truth for "should this release be treated as a
 * pre-release across the app".
 *
 * Combines:
 *  - [GithubRelease.isPrerelease] — the authoritative GitHub API flag.
 *  - [VersionMath.isPreReleaseTag] on [GithubRelease.tagName] — catches
 *    the common case where a maintainer publishes `v2.0.0-rc.1` but
 *    forgets to tick the "This is a pre-release" box. Without the
 *    tag heuristic, an opted-out user would be silently offered that
 *    build as if it were stable.
 *  - [VersionMath.isPreReleaseTag] on [GithubRelease.name] — some
 *    maintainers only put the `beta` marker in the human-readable
 *    release title (e.g. tag=`2.0.0`, name=`2.0.0 (beta)`).
 *
 * Every UI that shows a "Pre-release" badge and every filter that
 * decides whether to surface a release to a given user MUST use this
 * helper, otherwise the flag-vs-tag mismatch surfaces as a silent
 * bug.
 */
fun GithubRelease.isEffectivelyPreRelease(): Boolean =
    isPrerelease ||
        VersionMath.isPreReleaseTag(tagName) ||
        VersionMath.isPreReleaseTag(name)
