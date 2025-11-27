package zed.rainxch.githubstore.feature.details.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zed.rainxch.githubstore.feature.home.data.repository.PlatformType
import zed.rainxch.githubstore.feature.install.FileLocationsProvider
import zed.rainxch.githubstore.feature.install.Installer
import java.awt.Desktop
import java.io.File
import java.io.IOException

class DesktopInstaller(
    private val files: FileLocationsProvider,
    private val platform: PlatformType
) : Installer {

    override suspend fun isSupported(extOrMime: String): Boolean {
        val ext = extOrMime.lowercase().removePrefix(".")
        return when (platform) {
            PlatformType.WINDOWS -> ext in listOf("msi", "exe")
            PlatformType.MACOS -> ext in listOf("dmg", "pkg")
            PlatformType.LINUX -> ext in listOf("appimage", "deb", "rpm")
            else -> false
        }
    }

    override suspend fun ensurePermissionsOrThrow(extOrMime: String) {
        val ext = extOrMime.lowercase().removePrefix(".")
        
        when (platform) {
            PlatformType.LINUX -> {
                // For AppImage, we need executable permission
                if (ext == "appimage") {
                    // Permission will be set in install() method
                }
                // For deb/rpm, user needs sudo access (handled at install time)
            }
            PlatformType.MACOS -> {
                // macOS may require user approval for unsigned apps
                // This is handled by the OS at install time
            }
            PlatformType.WINDOWS -> {
                // Windows may show SmartScreen warnings
                // User must approve at install time
            }
            else -> {}
        }
    }

    override suspend fun install(filePath: String, extOrMime: String) = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalStateException("File not found: $filePath")
        }
        
        val ext = extOrMime.lowercase().removePrefix(".")
        
        when (platform) {
            PlatformType.WINDOWS -> installWindows(file, ext)
            PlatformType.MACOS -> installMacOS(file, ext)
            PlatformType.LINUX -> installLinux(file, ext)
            else -> throw UnsupportedOperationException("Installation not supported on $platform")
        }
    }

    private fun installWindows(file: File, ext: String) {
        when (ext) {
            "msi" -> {
                // Launch MSI installer
                val pb = ProcessBuilder("msiexec", "/i", file.absolutePath)
                pb.start()
            }
            "exe" -> {
                // Launch EXE installer directly
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                } else {
                    val pb = ProcessBuilder(file.absolutePath)
                    pb.start()
                }
            }
            else -> throw IllegalArgumentException("Unsupported Windows installer: .$ext")
        }
    }

    private fun installMacOS(file: File, ext: String) {
        when (ext) {
            "dmg" -> {
                // Open DMG file - user will drag to Applications
                val pb = ProcessBuilder("open", file.absolutePath)
                pb.start()
            }
            "pkg" -> {
                // Launch PKG installer
                val pb = ProcessBuilder("open", file.absolutePath)
                pb.start()
            }
            else -> throw IllegalArgumentException("Unsupported macOS installer: .$ext")
        }
    }

    private fun installLinux(file: File, ext: String) {
        when (ext) {
            "appimage" -> {
                // Make executable and run
                files.setExecutableIfNeeded(file.absolutePath)
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                } else {
                    val pb = ProcessBuilder(file.absolutePath)
                    pb.start()
                }
            }
            "deb" -> {
                // Try to use graphical installer or fallback to terminal
                try {
                    // Try gdebi-gtk (graphical)
                    val pb = ProcessBuilder("gdebi-gtk", file.absolutePath)
                    pb.start()
                } catch (e: IOException) {
                    try {
                        // Fallback to xdg-open
                        val pb = ProcessBuilder("xdg-open", file.absolutePath)
                        pb.start()
                    } catch (e2: IOException) {
                        // Last resort: open terminal with dpkg command
                        openTerminalForPackageInstall("dpkg", file.absolutePath)
                    }
                }
            }
            "rpm" -> {
                // Try to use graphical installer or fallback to terminal
                try {
                    // Try xdg-open first
                    val pb = ProcessBuilder("xdg-open", file.absolutePath)
                    pb.start()
                } catch (e: IOException) {
                    // Fallback to terminal
                    openTerminalForPackageInstall("rpm", file.absolutePath)
                }
            }
            else -> throw IllegalArgumentException("Unsupported Linux installer: .$ext")
        }
    }

    private fun openTerminalForPackageInstall(packageManager: String, filePath: String) {
        // Try different terminal emulators
        val terminals = listOf(
            listOf("gnome-terminal", "--", "bash", "-c", "sudo $packageManager -i $filePath; read -p 'Press Enter to close...'"),
            listOf("konsole", "-e", "bash", "-c", "sudo $packageManager -i $filePath; read -p 'Press Enter to close...'"),
            listOf("xterm", "-e", "bash", "-c", "sudo $packageManager -i $filePath; read -p 'Press Enter to close...'")
        )
        
        for (terminalCmd in terminals) {
            try {
                val pb = ProcessBuilder(terminalCmd)
                pb.start()
                return
            } catch (e: IOException) {
                // Try next terminal
            }
        }
        
        throw IOException("Could not find a terminal emulator to run package installation")
    }
}