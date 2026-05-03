package zed.rainxch.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import zed.rainxch.core.data.dto.AnnouncementsResponseDto
import zed.rainxch.core.data.mappers.toDomain
import zed.rainxch.core.data.network.BackendApiClient
import zed.rainxch.core.data.services.LocalizationManager
import zed.rainxch.core.domain.getPlatform
import zed.rainxch.core.domain.model.Announcement
import zed.rainxch.core.domain.model.AnnouncementCategory
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.repository.AnnouncementsFeedSnapshot
import zed.rainxch.core.domain.repository.AnnouncementsRepository
import zed.rainxch.core.domain.repository.TweaksRepository
import zed.rainxch.core.domain.system.AppVersionInfo

class AnnouncementsRepositoryImpl(
    private val backendApiClient: BackendApiClient,
    private val tweaksRepository: TweaksRepository,
    private val localizationManager: LocalizationManager,
    private val appVersionInfo: AppVersionInfo,
) : AnnouncementsRepository {
    private val logger = Logger.withTag("AnnouncementsRepository")

    private val json = Json { ignoreUnknownKeys = true }

    private val platformTag: String = when (getPlatform()) {
        Platform.ANDROID -> "ANDROID"
        Platform.WINDOWS, Platform.MACOS, Platform.LINUX -> "DESKTOP"
    }

    override fun observeFeed(): Flow<AnnouncementsFeedSnapshot> =
        combine(
            tweaksRepository.getAnnouncementsCachedPayload(),
            tweaksRepository.getAnnouncementsDismissedIds(),
            tweaksRepository.getAnnouncementsAcknowledgedIds(),
            tweaksRepository.getAnnouncementsMutedCategories(),
            tweaksRepository.getAnnouncementsLastFetchedAt(),
        ) { payload, dismissed, acknowledged, mutedNames, lastFetched ->
            val items = parseAndFilter(payload)
            val mutedCategories = mutedNames.mapNotNull { name ->
                runCatching { AnnouncementCategory.valueOf(name) }.getOrNull()
            }.toSet()
            AnnouncementsFeedSnapshot(
                items = items,
                dismissedIds = dismissed,
                acknowledgedIds = acknowledged,
                mutedCategories = mutedCategories,
                lastFetchedAtMillis = lastFetched,
                lastRefreshFailed = false,
            )
        }

    override suspend fun refresh(): Result<Unit> {
        val result = backendApiClient.getAnnouncements()
        return result.fold(
            onSuccess = { dto ->
                val raw = json.encodeToString(AnnouncementsResponseDto.serializer(), dto)
                tweaksRepository.setAnnouncementsCachedPayload(raw)
                tweaksRepository.setAnnouncementsLastFetchedAt(nowMillis())
                Result.success(Unit)
            },
            onFailure = { error ->
                logger.w("Announcements refresh failed: ${error.message}")
                Result.failure(error)
            },
        )
    }

    override suspend fun dismiss(id: String) {
        try {
            tweaksRepository.addAnnouncementDismissedId(id)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to persist dismissed announcement $id" }
        }
    }

    override suspend fun acknowledge(id: String) {
        try {
            tweaksRepository.addAnnouncementAcknowledgedId(id)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to persist acknowledged announcement $id" }
        }
    }

    override suspend fun setMuted(category: AnnouncementCategory, muted: Boolean) {
        if (!category.isMutable) return
        try {
            tweaksRepository.setAnnouncementCategoryMuted(category.name, muted)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to persist mute toggle for $category" }
        }
    }

    private fun parseAndFilter(payload: String?): List<Announcement> {
        if (payload.isNullOrBlank()) return emptyList()
        val parsed = runCatching {
            json.decodeFromString(AnnouncementsResponseDto.serializer(), payload)
        }.getOrElse {
            logger.w("Failed to parse cached announcements payload: ${it.message}")
            return emptyList()
        }
        val full = localizationManager.getCurrentLanguageCode()
        val primary = localizationManager.getPrimaryLanguageCode()
        val now = nowMillis()
        val versionCode = appVersionInfo.versionCode

        return parsed.items
            .asSequence()
            .map { it.toDomain(fullLocale = full, primaryLocale = primary) }
            .filter { item ->
                val expired = item.expiresAt?.let { isPastIso(it, now) } == true
                val minVc = item.minVersionCode
                val maxVc = item.maxVersionCode
                val versionFloorOk = minVc == null || versionCode >= minVc
                val versionCeilingOk = maxVc == null || versionCode <= maxVc
                val platformOk = item.platforms?.contains(platformTag) ?: true
                !expired && versionFloorOk && versionCeilingOk && platformOk
            }
            .sortedByDescending { it.publishedAt }
            .toList()
    }

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    private fun isPastIso(iso: String, nowMillis: Long): Boolean {
        val parsed = runCatching { Instant.parse(iso).toEpochMilliseconds() }.getOrNull()
        return parsed != null && parsed < nowMillis
    }
}
