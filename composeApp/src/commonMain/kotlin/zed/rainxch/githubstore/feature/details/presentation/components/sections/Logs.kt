package zed.rainxch.githubstore.feature.details.presentation.components.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquefiable
import zed.rainxch.githubstore.feature.details.presentation.DetailsState
import zed.rainxch.githubstore.feature.details.presentation.utils.LocalTopbarLiquidState

fun LazyListScope.logs(state: DetailsState) {
    item {
        val liquidState = LocalTopbarLiquidState.current

        HorizontalDivider()

        Text(
            text = "Install logs",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .liquefiable(liquidState),
            fontWeight = FontWeight.Bold,
        )
    }

    items(state.installLogs) { log ->
        val liquidState = LocalTopbarLiquidState.current

        Text(
            text = "> ${log.result}: ${log.assetName}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontStyle = FontStyle.Italic
            ),
            color = if (log.result.startsWith("Error")) {
                MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.outline,
            modifier = Modifier.liquefiable(liquidState)
        )
    }
}