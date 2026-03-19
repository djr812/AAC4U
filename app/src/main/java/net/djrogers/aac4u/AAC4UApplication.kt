package net.djrogers.aac4u

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.local.database.DatabaseSeeder
import javax.inject.Inject

@HiltAndroidApp
class AAC4UApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Seed database with default data on first launch
        applicationScope.launch {
            databaseSeeder.seedIfEmpty()
        }
    }
}
