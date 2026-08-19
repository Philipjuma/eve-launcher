package com.haven.evelauncher.data.greeting

import com.haven.evelauncher.core.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.Calendar

class GreetingDataSource(
    private val greetingSelector: EveGreetingSelector
) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.GREETING

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        while (true) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val timeOfDay = when (hour) {
                in 0..4 -> TimeOfDay.NIGHT
                in 5..7 -> TimeOfDay.EARLY_MORNING
                in 8..11 -> TimeOfDay.MORNING
                in 12..16 -> TimeOfDay.AFTERNOON
                in 17..20 -> TimeOfDay.EVENING
                else -> TimeOfDay.LATE_NIGHT
            }
            
            val greeting = greetingSelector.select(timeOfDay)
            emit(listOf(
                EveWidgetCandidate(
                    id = "greeting_candidate",
                    category = EveCategory.WORLD,
                    semanticGroupId = "greeting_family",
                    deduplicationKey = "current_greeting",
                    title = greeting,
                    subtitle = "Haven Engine™",
                    icon = EveIcon(emoji = "✨"),
                    priority = 10,
                    sourceType = EveDataSourceType.GREETING
                )
            ))
            delay(300000)
        }
    }
}
