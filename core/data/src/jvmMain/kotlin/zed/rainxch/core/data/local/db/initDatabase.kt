package zed.rainxch.core.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import zed.rainxch.core.data.local.DesktopAppDataPaths
import java.io.File

fun initDatabase(): AppDatabase {
    // SQLite WAL mode keeps two side files alongside the .db; migrate all three
    // so an upgrade preserves any uncommitted transactions in the WAL.
    DesktopAppDataPaths.migrateFromTmpIfNeeded("github_store.db")
    DesktopAppDataPaths.migrateFromTmpIfNeeded("github_store.db-shm")
    DesktopAppDataPaths.migrateFromTmpIfNeeded("github_store.db-wal")

    val dbFile = File(DesktopAppDataPaths.appDataDir(), "github_store.db")
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
