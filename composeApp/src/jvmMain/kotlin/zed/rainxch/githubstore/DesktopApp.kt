package zed.rainxch.githubstore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import zed.rainxch.githubstore.app.di.initKoin
import githubstore.composeapp.generated.resources.Res
import githubstore.composeapp.generated.resources.app_icon

fun main(args: Array<String>) = application {
    initKoin()

    val deepLinkUri = args.firstOrNull()

    Window(
        onCloseRequest = ::exitApplication,
        title = "GitHub Store",
        icon = painterResource(Res.drawable.app_icon)
    ) {
        App(deepLinkUri = deepLinkUri)
    }
}
