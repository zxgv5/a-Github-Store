package zed.rainxch.githubstore.core.presentation.utils

import java.awt.Desktop
import java.net.URI

actual fun openBrowser(
    url: String,
    onError: (error: String) -> Unit
) {
    val os = System.getProperty("os.name").lowercase()

    try {
        when {
            os.contains("linux") -> {
                val process = Runtime.getRuntime().exec(arrayOf("xdg-open", url))

                Thread.sleep(100)
                if (process.isAlive || process.exitValue() != 0) {
                    throw Exception("xdg-open failed")
                }
            }

            Desktop.isDesktopSupported() && Desktop.getDesktop()
                .isSupported(Desktop.Action.BROWSE) -> {
                Desktop.getDesktop().browse(URI(url))
            }

            else -> {
                error("Cannot open browser automatically. Please visit: $url")
            }
        }
    } catch (e: Exception) {
        println("Failed to open browser: ${e.message}")
        println("Please open this URL manually: $url")
    }
}