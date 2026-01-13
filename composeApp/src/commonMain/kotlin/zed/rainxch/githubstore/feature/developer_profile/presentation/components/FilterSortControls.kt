package zed.rainxch.githubstore.feature.developer_profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zed.rainxch.githubstore.feature.developer_profile.domain.model.RepoFilterType
import zed.rainxch.githubstore.feature.developer_profile.domain.model.RepoSortType
import zed.rainxch.githubstore.feature.developer_profile.presentation.DeveloperProfileAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilterSortControls(
    currentFilter: RepoFilterType,
    currentSort: RepoSortType,
    searchQuery: String,
    repoCount: Int,
    totalCount: Int,
    onAction: (DeveloperProfileAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                onAction(DeveloperProfileAction.OnSearchQueryChange(query))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search repositories...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { onAction(DeveloperProfileAction.OnSearchQueryChange("")) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = currentFilter.ordinal,
                modifier = Modifier.weight(1f),
                edgePadding = 0.dp,
                divider = {}
            ) {
                RepoFilterType.entries.forEach { filter ->
                    FilterChipTab(
                        selected = currentFilter == filter,
                        onClick = { onAction(DeveloperProfileAction.OnFilterChange(filter)) },
                        label = filter.displayName()
                    )
                }
            }

            SortMenu(
                currentSort = currentSort,
                onSortChange = { sort ->
                    onAction(DeveloperProfileAction.OnSortChange(sort))
                }
            )
        }

        Text(
            text = if (repoCount == totalCount) {
                "$repoCount ${if (repoCount == 1) "repository" else "repositories"}"
            } else {
                "Showing $repoCount of $totalCount repositories"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterChipTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun SortMenu(
    currentSort: RepoSortType,
    onSortChange: (RepoSortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilledIconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RepoSortType.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentSort == sort) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                            }

                            Text(
                                text = sort.displayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentSort == sort) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    },
                    onClick = {
                        onSortChange(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RepoFilterType.displayName(): String {
    return when (this) {
        RepoFilterType.ALL -> "All"
        RepoFilterType.WITH_RELEASES -> "With Releases"
        RepoFilterType.INSTALLED -> "Installed"
        RepoFilterType.FAVORITES -> "Favorites"
    }
}

@Composable
private fun RepoSortType.displayName(): String {
    return when (this) {
        RepoSortType.UPDATED -> "Recently Updated"
        RepoSortType.STARS -> "Most Stars"
        RepoSortType.NAME -> "Name"
    }
}