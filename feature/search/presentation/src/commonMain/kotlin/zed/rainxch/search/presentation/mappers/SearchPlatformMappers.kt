package zed.rainxch.search.presentation.mappers

import zed.rainxch.domain.model.SearchPlatform
import zed.rainxch.domain.model.SearchPlatform.*
import zed.rainxch.search.presentation.model.SearchPlatformUi

fun SearchPlatformUi.toDomain() : SearchPlatform {
    return when (this) {
        SearchPlatformUi.All -> All
        SearchPlatformUi.Android -> Android
        SearchPlatformUi.Windows -> Windows
        SearchPlatformUi.Macos -> Macos
        SearchPlatformUi.Linux -> Linux
    }
}
