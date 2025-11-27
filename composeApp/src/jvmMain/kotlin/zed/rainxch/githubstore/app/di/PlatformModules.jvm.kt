package zed.rainxch.githubstore.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import zed.rainxch.githubstore.feature.home.data.repository.getPlatform
import zed.rainxch.githubstore.feature.install.Downloader
import zed.rainxch.githubstore.feature.install.FileLocationsProvider
import zed.rainxch.githubstore.feature.install.Installer
import zed.rainxch.githubstore.feature.details.data.DesktopDownloader
import zed.rainxch.githubstore.feature.details.data.DesktopFileLocationsProvider
import zed.rainxch.githubstore.feature.details.data.DesktopInstaller

actual val platformModule: Module = module {
    single<Downloader> {
        DesktopDownloader(
            http = get(),
            files = get()
        )
    }

    single<Installer> {
        val platform = getPlatform()
        DesktopInstaller(
            files = get(),
            platform = platform.type
        )
    }

    single<FileLocationsProvider> {
        val platform = getPlatform()
        DesktopFileLocationsProvider(platform = platform.type)
    }
}