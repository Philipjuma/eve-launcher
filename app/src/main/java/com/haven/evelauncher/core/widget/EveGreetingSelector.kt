package com.haven.evelauncher.core.widget

import java.util.*

enum class TimeOfDay {
    NIGHT, EARLY_MORNING, MORNING, AFTERNOON, EVENING, LATE_NIGHT
}

class EveGreetingSelector {
    private var lastShown: String? = null
    private val sessionShown = mutableSetOf<String>()

    fun select(timeOfDay: TimeOfDay): String {
        val pool = greetingPool(timeOfDay)
            .filter { it != lastShown }

        val unseen = pool.filter { it !in sessionShown }
        val candidates = unseen.ifEmpty { pool }

        val chosen = candidates.ifEmpty { listOf("") }.random()
        lastShown = chosen
        sessionShown += chosen
        return chosen
    }

    private fun greetingPool(timeOfDay: TimeOfDay): List<String> = when (timeOfDay) {
        TimeOfDay.NIGHT -> listOf(
            "Quiet now", "Still up", "Dark out there", "World's asleep", 
            "Night owl", "3am, huh", "Everyone else is asleep", "Just the screen glow", 
            "This late again", "Half past dark", "Nothing's open right now", 
            "Silence out there", "One more scroll?", "Still going", 
            "The city's quiet", "Insomnia again?", "Almost tomorrow", "Small hours"
        )
        TimeOfDay.EARLY_MORNING -> listOf(
            "Early", "Before the world wakes", "Quiet start", "First light", 
            "Up before it's light out", "Coffee first", "Not a morning person, huh", 
            "Early bird", "The house is still quiet", "Sun's not up yet either"
        )
        TimeOfDay.MORNING -> listOf(
            "Morning", "Day's underway", "Fully caffeinated? no judgment", 
            "Here we go", "Fresh start", "Eyes open, mostly", "New day", 
            "Getting into it", "Second cup?", "Already busy out there"
        )
        TimeOfDay.AFTERNOON -> listOf(
            "Midday", "Halfway there", "Afternoon lull", "Keep going", 
            "Lunch happened. probably.", "Sun's high", "Almost the home stretch", 
            "Still at it", "3pm slump incoming", "The day's middle bit"
        )
        TimeOfDay.EVENING -> listOf(
            "Evening", "Winding down", "Day's nearly done", "Golden hour", 
            "Dinner soon?", "Almost there", "Sun's dropping", "Home stretch", 
            "Quiet evening settling in", "Last stretch of the day"
        )
        TimeOfDay.LATE_NIGHT -> listOf(
            "Getting late", "Wind-down time", "One more thing before bed?", 
            "Day's basically over", "Dark already", "Almost tomorrow", 
            "Quiet hours starting", "The house is settling", "Still awake"
        )
    }
}
