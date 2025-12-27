package zed.rainxch.githubstore.core.presentation.utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface TextUi {
    data class Static(
        val text: String
    ) : TextUi

    data class Dynamic(
        val resource: StringResource
    ) : TextUi

    @Composable
    fun asString(): String {
        return when (this) {
            is Static -> this@TextUi.text
            is Dynamic -> stringResource(this@TextUi.resource)
        }
    }
}