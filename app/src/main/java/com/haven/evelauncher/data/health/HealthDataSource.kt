package com.haven.evelauncher.data.health

import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import java.util.Locale

class HealthDataSource(
    private val repository: HealthRepository
) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.HEALTH

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = 
        repository.getTodaySteps().map { steps ->
            listOf(
                EveWidgetCandidate(
                    id = "health_steps",
                    category = EveCategory.PERSONAL,
                    semanticGroupId = "activity_family",
                    deduplicationKey = "steps_today",
                    title = String.format(Locale.getDefault(), "%,d", steps),
                    subtitle = "steps today",
                    icon = EveIcon(emoji = "👟"),
                    priority = 75,
                    relevanceScore = 0.8f,
                    sourceType = EveDataSourceType.HEALTH,
                    isPersonal = true
                )
            )
        }
}
