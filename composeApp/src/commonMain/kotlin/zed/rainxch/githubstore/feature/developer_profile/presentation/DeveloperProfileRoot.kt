package zed.rainxch.githubstore.feature.developer_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.githubstore.feature.developer_profile.domain.model.DeveloperProfile
import zed.rainxch.githubstore.feature.developer_profile.domain.model.RepoFilterType
import zed.rainxch.githubstore.feature.developer_profile.presentation.components.DeveloperRepoItem
import zed.rainxch.githubstore.feature.developer_profile.presentation.components.FilterSortControls

@Composable
fun DeveloperProfileRoot(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (repoId: Long) -> Unit,
    viewModel: DeveloperProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    DeveloperProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                DeveloperProfileAction.OnNavigateBackClick -> onNavigateBack()
                is DeveloperProfileAction.OnRepositoryClick -> onNavigateToDetails(action.repoId)
                is DeveloperProfileAction.OnOpenLink -> {
                    uriHandler.openUri(action.url)
                }
                else -> viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperProfileScreen(
    state: DeveloperProfileState,
    onAction: (DeveloperProfileAction) -> Unit,
) {
    Scaffold(
        topBar = {
            DevProfileTopbar(
                username = state.username,
                onAction = onAction
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorMessage != null && state.profile == null -> {
                    ErrorContent(
                        message = state.errorMessage,
                        onRetry = { onAction(DeveloperProfileAction.OnRetry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.profile != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ProfileInfoCard(
                                profile = state.profile,
                                onAction = onAction
                            )
                        }

                        item {
                            StatsRow(profile = state.profile)
                        }

                        item {
                            FilterSortControls(
                                currentFilter = state.currentFilter,
                                currentSort = state.currentSort,
                                searchQuery = state.searchQuery,
                                repoCount = state.filteredRepositories.size,
                                totalCount = state.repositories.size,
                                onAction = onAction
                            )
                        }

                        if (state.isLoadingRepos) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularWavyProgressIndicator()
                                }
                            }
                        } else if (state.filteredRepositories.isEmpty()) {
                            item {
                                EmptyReposContent(
                                    filter = state.currentFilter,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        } else {
                            items(
                                items = state.filteredRepositories,
                                key = { it.id }
                            ) { repo ->
                                DeveloperRepoItem(
                                    repository = repo,
                                    onItemClick = {
                                        onAction(DeveloperProfileAction.OnRepositoryClick(repo.id))
                                    },
                                    onToggleFavorite = {
                                        onAction(DeveloperProfileAction.OnToggleFavorite(repo))
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }

            if (state.errorMessage != null && state.profile != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { onAction(DeveloperProfileAction.OnRetry) }) {
                            Text("Retry")
                        }
                    },
                    dismissAction = {
                        IconButton(onClick = { onAction(DeveloperProfileAction.OnDismissError) }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                ) {
                    Text(state.errorMessage)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileInfoCard(
    profile: DeveloperProfile,
    onAction: (DeveloperProfileAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                CoilImage(
                    imageModel = { profile.avatarUrl },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    imageOptions = ImageOptions(
                        contentDescription = "${profile.login}'s avatar",
                        contentScale = ContentScale.Crop
                    ),
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name ?: profile.login,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "@${profile.login}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    profile.bio?.let { bio ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.company?.let { company ->
                    InfoChip(
                        icon = Icons.Default.Business,
                        text = company
                    )
                }

                profile.location?.let { location ->
                    InfoChip(
                        icon = Icons.Default.LocationOn,
                        text = location
                    )
                }

                profile.blog?.takeIf { it.isNotBlank() }?.let { blog ->
                    val displayUrl = blog.removePrefix("https://").removePrefix("http://")
                    AssistChip(
                        onClick = {
                            val url = if (!blog.startsWith("http")) "https://$blog" else blog
                            onAction(DeveloperProfileAction.OnOpenLink(url))
                        },
                        label = {
                            Text(
                                text = displayUrl,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                profile.twitterUsername?.let { twitter ->
                    AssistChip(
                        onClick = {
                            onAction(DeveloperProfileAction.OnOpenLink("https://twitter.com/$twitter"))
                        },
                        label = {
                            Text(
                                text = "@$twitter",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(profile: DeveloperProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Repositories",
            value = profile.publicRepos.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Followers",
            value = formatCount(profile.followers),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Following",
            value = formatCount(profile.following),
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyReposContent(
    filter: RepoFilterType,
    modifier: Modifier = Modifier
) {
    val message = when (filter) {
        RepoFilterType.ALL -> "No repositories found"
        RepoFilterType.WITH_RELEASES -> "No repositories with installable releases"
        RepoFilterType.INSTALLED -> "No installed repositories"
        RepoFilterType.FAVORITES -> "No favorite repositories"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DevProfileTopbar(
    username: String,
    onAction: (DeveloperProfileAction) -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = { onAction(DeveloperProfileAction.OnNavigateBackClick) }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = username,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}