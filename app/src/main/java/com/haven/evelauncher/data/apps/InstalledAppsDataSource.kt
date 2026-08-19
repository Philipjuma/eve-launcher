package com.haven.evelauncher.data.apps

import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.platform.apps.LauncherService
import kotlinx.coroutines.flow.*

class InstalledAppsDataSource(
    private val launcherService: LauncherService
) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.INSTALLED_APPS

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        // Emit Orbit (Apps Grid) candidate
        emit(listOf(
            EveWidgetCandidate(
                id = "orbit_grid",
                category = EveCategory.ORBIT,
                semanticGroupId = "apps",
                deduplicationKey = "orbit_grid",
                title = "Orbit",
                subtitle = "Frequently used",
                icon = EveIcon(emoji = "◉"),
                priority = 50,
                sourceType = EveDataSourceType.INSTALLED_APPS
            )
        ))
    }
}
