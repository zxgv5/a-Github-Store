package zed.rainxch.core.data.services.installer

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import zed.rainxch.core.data.services.dhizuku.DhizukuServiceManager
import zed.rainxch.core.data.services.dhizuku.IDhizukuInstallerService
import zed.rainxch.core.data.services.dhizuku.model.DhizukuStatus
import zed.rainxch.core.data.services.shizuku.IShizukuInstallerService
import zed.rainxch.core.data.services.shizuku.ShizukuServiceManager
import zed.rainxch.core.data.services.shizuku.model.ShizukuStatus
import zed.rainxch.core.domain.model.GithubAsset
import zed.rainxch.core.domain.model.InstallerType
import zed.rainxch.core.domain.model.SystemArchitecture
import zed.rainxch.core.domain.repository.TweaksRepository
import zed.rainxch.core.domain.system.InstallOutcome
import zed.rainxch.core.domain.system.Installer
import zed.rainxch.core.domain.system.InstallerInfoExtractor

class SilentInstallerDispatcher(
    private val androidInstaller: Installer,
    private val shizukuServiceManager: ShizukuServiceManager,
    private val dhizukuServiceManager: DhizukuServiceManager,
    private val tweaksRepository: TweaksRepository,
) : Installer {
    companion object {
        private const val TAG = "SilentInstaller"
    }

    @Volatile
    private var cachedInstallerType: InstallerType = InstallerType.DEFAULT

    fun observeInstallerPreference(scope: CoroutineScope) {
        scope.launch {
            tweaksRepository.getInstallerType().collect { type ->
                cachedInstallerType = type
                Logger.d(TAG) { "Installer type changed to: $type" }
            }
        }
    }

    override suspend fun isSupported(extOrMime: String): Boolean = androidInstaller.isSupported(extOrMime)

    override fun isAssetInstallable(assetName: String): Boolean = androidInstaller.isAssetInstallable(assetName)

    override fun choosePrimaryAsset(assets: List<GithubAsset>): GithubAsset? = androidInstaller.choosePrimaryAsset(assets)

    override fun detectSystemArchitecture(): SystemArchitecture = androidInstaller.detectSystemArchitecture()

    override fun isObtainiumInstalled(): Boolean = androidInstaller.isObtainiumInstalled()

    override fun openInObtainium(
        repoOwner: String,
        repoName: String,
        onOpenInstaller: () -> Unit,
    ) = androidInstaller.openInObtainium(repoOwner, repoName, onOpenInstaller)

    override fun isAppManagerInstalled(): Boolean = androidInstaller.isAppManagerInstalled()

    override fun openInAppManager(
        filePath: String,
        onOpenInstaller: () -> Unit,
    ) = androidInstaller.openInAppManager(filePath, onOpenInstaller)

    override fun getApkInfoExtractor(): InstallerInfoExtractor = androidInstaller.getApkInfoExtractor()

    override fun openApp(packageName: String): Boolean = androidInstaller.openApp(packageName)

    override fun openWithExternalInstaller(filePath: String) = androidInstaller.openWithExternalInstaller(filePath)

    override suspend fun ensurePermissionsOrThrow(extOrMime: String) {
        Logger.d(TAG) {
            "ensurePermissionsOrThrow() — extOrMime=$extOrMime, cachedType=$cachedInstallerType"
        }
        if (resolveActiveBackend() != Backend.DEFAULT) {
            Logger.d(TAG) { "Silent backend active — skipping unknown sources permission check" }
            return
        }
        androidInstaller.ensurePermissionsOrThrow(extOrMime)
    }

    override suspend fun install(
        filePath: String,
        extOrMime: String,
    ): InstallOutcome {
        Logger.d(TAG) { "install() called — filePath=$filePath, extOrMime=$extOrMime, cached=$cachedInstallerType" }

        when (resolveActiveBackend()) {
            Backend.SHIZUKU -> {
                Logger.d(TAG) { "Routing install through Shizuku" }
                val outcome = tryShizukuInstall(filePath)
                if (outcome != null) return outcome
            }
            Backend.DHIZUKU -> {
                Logger.d(TAG) { "Routing install through Dhizuku" }
                val outcome = tryDhizukuInstall(filePath)
                if (outcome != null) return outcome
            }
            Backend.DEFAULT -> {
                Logger.d(TAG) { "No silent backend active — using standard installer" }
            }
        }

        Logger.d(TAG) { "Falling back to standard AndroidInstaller for: $filePath" }
        androidInstaller.ensurePermissionsOrThrow(extOrMime)
        return androidInstaller.install(filePath, extOrMime)
    }

    override fun uninstall(packageName: String) {
        Logger.d(TAG) { "uninstall() called — packageName=$packageName, cached=$cachedInstallerType" }

        when (resolveActiveBackend()) {
            Backend.SHIZUKU -> {
                Thread {
                    try {
                        val service: IShizukuInstallerService? = runBlocking { shizukuServiceManager.getService() }
                        if (service == null || service.uninstallPackage(packageName) != 0) {
                            Logger.w(TAG) { "Shizuku uninstall failed, falling back" }
                            androidInstaller.uninstall(packageName)
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG) { "Shizuku uninstall exception, falling back: ${e.message}" }
                        androidInstaller.uninstall(packageName)
                    }
                }.start()
            }
            Backend.DHIZUKU -> {
                Thread {
                    try {
                        val service: IDhizukuInstallerService? = runBlocking { dhizukuServiceManager.getService() }
                        if (service == null || service.uninstallPackage(packageName) != 0) {
                            Logger.w(TAG) { "Dhizuku uninstall failed, falling back" }
                            androidInstaller.uninstall(packageName)
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG) { "Dhizuku uninstall exception, falling back: ${e.message}" }
                        androidInstaller.uninstall(packageName)
                    }
                }.start()
            }
            Backend.DEFAULT -> {
                androidInstaller.uninstall(packageName)
            }
        }
    }

    private suspend fun tryShizukuInstall(filePath: String): InstallOutcome? = try {
        val service = shizukuServiceManager.getService()
        if (service == null) {
            Logger.w(TAG) { "Shizuku service is null, will fall back" }
            null
        } else {
            val result = withContext(Dispatchers.IO) {
                val file = java.io.File(filePath)
                val pfd = android.os.ParcelFileDescriptor.open(
                    file,
                    android.os.ParcelFileDescriptor.MODE_READ_ONLY,
                )
                pfd.use { service.installPackage(it, file.length()) }
            }
            if (result == 0) InstallOutcome.COMPLETED else null
        }
    } catch (e: Exception) {
        Logger.e(TAG) { "Shizuku install exception, falling back: ${e.javaClass.simpleName}: ${e.message}" }
        null
    }

    private suspend fun tryDhizukuInstall(filePath: String): InstallOutcome? = try {
        val service = dhizukuServiceManager.getService()
        if (service == null) {
            Logger.w(TAG) { "Dhizuku service is null, will fall back" }
            null
        } else {
            val result = withContext(Dispatchers.IO) {
                val file = java.io.File(filePath)
                val pfd = android.os.ParcelFileDescriptor.open(
                    file,
                    android.os.ParcelFileDescriptor.MODE_READ_ONLY,
                )
                pfd.use { service.installPackage(it, file.length()) }
            }
            if (result == 0) InstallOutcome.COMPLETED else null
        }
    } catch (e: Exception) {
        Logger.e(TAG) { "Dhizuku install exception, falling back: ${e.javaClass.simpleName}: ${e.message}" }
        null
    }

    private fun resolveActiveBackend(): Backend = when (cachedInstallerType) {
        InstallerType.SHIZUKU -> {
            shizukuServiceManager.refreshStatus()
            if (shizukuServiceManager.status.value == ShizukuStatus.READY) Backend.SHIZUKU else Backend.DEFAULT
        }
        InstallerType.DHIZUKU -> {
            dhizukuServiceManager.refreshStatus()
            if (dhizukuServiceManager.status.value == DhizukuStatus.READY) Backend.DHIZUKU else Backend.DEFAULT
        }
        InstallerType.DEFAULT -> Backend.DEFAULT
    }

    private enum class Backend { DEFAULT, SHIZUKU, DHIZUKU }
}
