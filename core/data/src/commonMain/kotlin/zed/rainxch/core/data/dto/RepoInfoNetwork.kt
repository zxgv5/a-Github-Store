package zed.rainxch.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoInfoNetwork(
    @SerialName("stargazers_count") val stars: Int,
    @SerialName("forks_count") val forks: Int,
    @SerialName("open_issues_count") val openIssues: Int,
    val license: LicenseNetwork? = null,
)

@Serializable
data class LicenseNetwork(
    @SerialName("spdx_id") val spdxId: String? = null,
    val name: String? = null,
)
