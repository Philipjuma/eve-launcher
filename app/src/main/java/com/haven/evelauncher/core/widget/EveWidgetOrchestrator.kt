package com.haven.evelauncher.core.widget

class EveWidgetOrchestrator {
    
    private val displayHistory = mutableMapOf<Int, DisplayInfo>()
    private val lockDurationMs = 120000L // 2 minutes

    data class DisplayInfo(
        val candidateId: String,
        val sourceType: EveDataSourceType,
        val displayedAt: Long
    )

    /**
     * Orchestrates the 4 physical glass slots globally.
     * Prevents duplicates and semantically overlapping content.
     */
    fun orchestrate(
        candidates: List<EveWidgetCandidate>
    ): List<WidgetSlotState> {
        val uniqueCandidates = deduplicate(candidates)
        val ranked = uniqueCandidates.sortedByDescending { it.calculateTotalScore() }

        val result = mutableListOf<WidgetSlotState>(
            WidgetSlotState.Empty, WidgetSlotState.Empty, 
            WidgetSlotState.Empty, WidgetSlotState.Empty
        )
        
        val usedIds = mutableSetOf<String>()
        val usedSemanticGroups = mutableSetOf<String>()
        val usedCategories = mutableSetOf<EveCategory>()
        val currentTime = System.currentTimeMillis()

        // 1. Check for Locked Content (e.g., Notifications)
        for (i in 0 until 4) {
            val info = displayHistory[i]
            if (info != null && (currentTime - info.displayedAt) < lockDurationMs) {
                // If it's a notification, keep it locked
                if (info.sourceType == EveDataSourceType.NOTIFICATION) {
                    val lockedCandidate = ranked.find { it.id == info.candidateId }
                    if (lockedCandidate != null) {
                        result[i] = WidgetSlotState.Content(listOf(lockedCandidate))
                        markUsed(lockedCandidate, usedIds, usedSemanticGroups, usedCategories)
                    }
                }
            }
        }

        // 2. SLOT 4 is reserved for ORBIT (Apps) if not locked
        if (result[3] is WidgetSlotState.Empty) {
            val orbitCandidate = ranked.find { it.category == EveCategory.ORBIT }
            if (orbitCandidate != null) {
                result[3] = WidgetSlotState.Content(listOf(orbitCandidate))
                markUsed(orbitCandidate, usedIds, usedSemanticGroups, usedCategories)
                updateHistory(3, orbitCandidate, currentTime)
            }
        }

        // 3. Fill remaining slots (1, 2, 3)
        for (i in 0 until 3) {
            if (result[i] !is WidgetSlotState.Empty) continue

            val best = ranked.filter { candidate ->
                !usedIds.contains(candidate.id) &&
                !usedSemanticGroups.contains(candidate.semanticGroupId) &&
                !usedCategories.contains(candidate.category)
            }.firstOrNull()

            if (best != null) {
                result[i] = WidgetSlotState.Content(listOf(best))
                markUsed(best, usedIds, usedSemanticGroups, usedCategories)
                updateHistory(i, best, currentTime)
            }
        }

        // 4. FALLBACK: Fill empty slots
        for (i in 0 until 4) {
            if (result[i] is WidgetSlotState.Empty) {
                val fallback = ranked.filter { candidate ->
                    !usedIds.contains(candidate.id) && !usedSemanticGroups.contains(candidate.semanticGroupId)
                }.firstOrNull()
                
                if (fallback != null) {
                    result[i] = WidgetSlotState.Content(listOf(fallback))
                    markUsed(fallback, usedIds, usedSemanticGroups, usedCategories)
                    updateHistory(i, fallback, currentTime)
                }
            }
        }

        return result
    }

    private fun markUsed(
        c: EveWidgetCandidate,
        ids: MutableSet<String>,
        groups: MutableSet<String>,
        categories: MutableSet<EveCategory>
    ) {
        ids.add(c.id)
        groups.add(c.semanticGroupId)
        categories.add(c.category)
    }

    private fun updateHistory(slot: Int, c: EveWidgetCandidate, time: Long) {
        displayHistory[slot] = DisplayInfo(c.id, c.sourceType, time)
    }

    private fun deduplicate(candidates: List<EveWidgetCandidate>): List<EveWidgetCandidate> {
        val seenKeys = mutableSetOf<String>()
        return candidates.filter { 
            if (seenKeys.contains(it.deduplicationKey)) false
            else {
                seenKeys.add(it.deduplicationKey)
                true
            }
        }
    }

    private fun EveWidgetCandidate.calculateTotalScore(): Float {
        val currentTime = System.currentTimeMillis()
        val ageMinutes = (currentTime - createdAt) / 60000f
        val freshness = (1.0f - (ageMinutes / 60f)).coerceIn(0.1f, 1f)

        return (relevanceScore * 0.4f) + 
               (freshness * 0.3f) + 
               (urgencyScore * 0.2f) + 
               (noveltyScore * 0.1f) + 
               (priority / 100f)
    }
}
