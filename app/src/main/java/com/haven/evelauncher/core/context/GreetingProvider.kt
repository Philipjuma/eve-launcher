package com.haven.evelauncher.core.context

import java.util.Calendar

object GreetingProvider {
    private val morningGreetings = listOf(
        "Morning, human.",
        "Good morning.",
        "A fresh start.",
        "Quiet morning.",
        "Early bird.",
        "Beginning again."
    )

    private val afternoonGreetings = listOf(
        "Good afternoon.",
        "Productive day?",
        "Sunlight is high.",
        "Keep going.",
        "Afternoon light.",
        "Halfway there."
    )

    private val eveningGreetings = listOf(
        "Good evening.",
        "Quiet evening.",
        "Winding down.",
        "Evening peace.",
        "The day fades.",
        "Rest awaits."
    )

    private val nightGreetings = listOf(
        "Late night.",
        "Night owl.",
        "Stars are out.",
        "Dreaming soon.",
        "The world sleeps.",
        "Midnight peace."
    )

    private val genericGreetings = listOf(
        "Hey.",
        "Eve is here.",
        "Ready?",
        "Nothing urgent.",
        "Enjoy the peace."
    )

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetings = when (hour) {
            in 5..11 -> morningGreetings
            in 12..17 -> afternoonGreetings
            in 18..21 -> eveningGreetings
            else -> nightGreetings
        }
        
        // Add a bit of randomness to occasionally show a generic one
        return if (Math.random() < 0.8) {
            greetings.random()
        } else {
            genericGreetings.random()
        }
    }
}
