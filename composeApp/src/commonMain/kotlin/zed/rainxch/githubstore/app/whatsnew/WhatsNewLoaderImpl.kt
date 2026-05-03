package zed.rainxch.githubstore.app.whatsnew

import kotlinx.serialization.json.Json
import zed.rainxch.core.data.dto.WhatsNewEntryDto
import zed.rainxch.core.data.mappers.toDomain
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.WhatsNewEntry
import zed.rainxch.core.domain.repository.WhatsNewLoader
import zed.rainxch.githubstore.core.presentation.res.Res

class WhatsNewLoaderImpl(
    private val knownVersionCodes: List<Int>,
    logger: GitHubStoreLogger,
) : WhatsNewLoader {
    private val tagged = logger.withTag("WhatsNewLoader")

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadAll(): List<WhatsNewEntry> =
        knownVersionCodes
            .mapNotNull { vc -> loadOrNull(vc) }
            .sortedByDescending { it.versionCode }

    override suspend fun forVersionCode(versionCode: Int): WhatsNewEntry? = loadOrNull(versionCode)

    private suspend fun loadOrNull(versionCode: Int): WhatsNewEntry? {
        val path = "files/whatsnew/$versionCode.json"
        return try {
            val bytes = Res.readBytes(path)
            val text = bytes.decodeToString()
            json.decodeFromString(WhatsNewEntryDto.serializer(), text).toDomain()
        } catch (t: Throwable) {
            tagged.warn("Failed to load what's-new entry at $path: ${t.message}")
            null
        }
    }
}

object KnownWhatsNewVersionCodes {
    val ALL: List<Int> = emptyList()
}
