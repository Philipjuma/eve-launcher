package com.haven.evelauncher.core.widget

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class EveWidgetEngine(
    private val context: Context,
    private val sources: List<EveDataSource>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val orchestrator = EveWidgetOrchestrator()
    
    private val _widgetState = MutableStateFlow(EveHomeWidgetsState())
    val widgetState = _widgetState.asStateFlow()

    private val candidatePool = MutableStateFlow<Map<EveDataSourceType, List<EveWidgetCandidate>>>(emptyMap())

    init {
        // Collect from all sources with individual error handling
        sources.forEach { source ->
            scope.launch {
                source.getCandidates()
                    .catch { e -> 
                        e.printStackTrace()
                        emit(emptyList()) 
                    }
                    .collect { candidates ->
                        candidatePool.update { it + (source.sourceType to candidates) }
                    }
            }
        }

        // Periodic Re-orchestration for Content Rotation (Every 5 mins)
        scope.launch {
            while(true) {
                delay(300000)
                reorchestrate()
            }
        }

        // Orchestrate whenever pool changes, debounced to avoid thrashing
        scope.launch {
            candidatePool
                .debounce(1000) // Slightly longer debounce for peak stability
                .distinctUntilChanged()
                .collect { 
                    reorchestrate()
                }
        }
    }

    private suspend fun reorchestrate() {
        try {
            val snapshot = candidatePool.value
            val allCandidates = snapshot.values.flatten()
            val assignments = orchestrator.orchestrate(allCandidates)
            
            if (assignments.size >= 4) {
                withContext(Dispatchers.Main) {
                    _widgetState.update { 
                        it.copy(
                            slot1 = EveWidgetSlotState(state = assignments[0], lastUpdated = System.currentTimeMillis()),
                            slot2 = EveWidgetSlotState(state = assignments[1], lastUpdated = System.currentTimeMillis()),
                            slot3 = EveWidgetSlotState(state = assignments[2], lastUpdated = System.currentTimeMillis()),
                            slot4 = EveWidgetSlotState(state = assignments[3], lastUpdated = System.currentTimeMillis())
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
