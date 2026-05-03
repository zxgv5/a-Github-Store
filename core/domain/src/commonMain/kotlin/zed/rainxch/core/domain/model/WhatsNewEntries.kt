package zed.rainxch.core.domain.model

object WhatsNewEntries {
    val all: List<WhatsNewEntry> = buildList {
        add(
            WhatsNewEntry(
                versionCode = 16,
                versionName = "1.8.1",
                releaseDate = "2026-05-03",
                sections = listOf(
                    WhatsNewSection(
                        type = WhatsNewSectionType.NEW,
                        bullets = listOf(
                            "APK Inspect — peek inside any release before installing.",
                            "Apps screen now groups updates, pending installs, and installed apps.",
                            "What's new sheet — see what changed after every update.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.IMPROVED,
                        bullets = listOf(
                            "Manual rescan surfaces every GitHub-style app on device.",
                            "Tighter auth handling — transient 401s no longer trigger spurious sign-outs.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.FIXED,
                        bullets = listOf(
                            "Multi-source downloads no longer clobber each other's APK file.",
                            "Shizuku-fallback installs no longer flip rows to \"installed\" prematurely.",
                            "Self-update no longer leaves apps stuck on \"Preparing to install\".",
                        ),
                    ),
                ),
            ),
        )
        add(
            WhatsNewEntry(
                versionCode = 15,
                versionName = "1.8.0",
                releaseDate = "2026-05-01",
                sections = listOf(
                    WhatsNewSection(
                        type = WhatsNewSectionType.NEW,
                        bullets = listOf(
                            "GitHub Store backend — auth, search, and discovery work in China.",
                            "Library Imports — auto-detects GitHub apps you already have on device.",
                            "Download Mirror System — multi-source race plus SHA-256 verification.",
                            "Personal Access Token sign-in for networks blocking the OAuth browser flow.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.IMPROVED,
                        bullets = listOf(
                            "Send feedback in-app — bug reports, feature ideas, change requests.",
                            "Pre-release channel chip plus \"switch to stable\" rollback in Details.",
                            "Arch Linux native pkg.tar.zst packages alongside deb, rpm, and AppImage.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.FIXED,
                        bullets = listOf(
                            "Linux Desktop no longer silently wipes settings and library on reboot.",
                            "Variant picking now honored — chosen variant installs without override.",
                            "macOS accessibility crash fixed via Compose Multiplatform 1.10.3.",
                        ),
                    ),
                ),
            ),
        )
    }.sortedByDescending { it.versionCode }

    fun forVersionCode(versionCode: Int): WhatsNewEntry? =
        all.firstOrNull { it.versionCode == versionCode }
}
