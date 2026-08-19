package com.haven.evelauncher.core.widget

import kotlinx.coroutines.flow.Flow

interface EveDataSource {
    val sourceType: EveDataSourceType
    fun getCandidates(): Flow<List<EveWidgetCandidate>>
}
