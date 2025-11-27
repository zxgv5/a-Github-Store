package zed.rainxch.githubstore.core.presentation.utils

expect fun openBrowser(
    url: String,
    onError: (error: String) -> Unit = { },
)
