package com.haven.evelauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.core.widget.WidgetSlotState
import com.haven.evelauncher.ui.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EveDebugPanel(
    viewModel: HomeViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val widgetState by viewModel.widgetEngine.widgetState.collectAsState()
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(16.dp)
            .padding(top = 40.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("EVE BRAIN DEBUG", style = MaterialTheme.typography.headlineSmall, color = Color.Green)
                Text(
                    "CLOSE", 
                    color = Color.White, 
                    modifier = Modifier.clickable { onClose() },
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                item { DebugSection("WIDGET SLOTS") }
                
                val slots = listOf(widgetState.slot1, widgetState.slot2, widgetState.slot3, widgetState.slot4)
                itemsIndexed(slots) { index, slotState ->
                    DebugSlotItem(index, slotState.state, sdf)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Orchestrator: ACTIVE", color = Color.Cyan)
                    Text("Last Pulse: ${sdf.format(Date())}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun DebugSection(title: String) {
    Text(
        text = title,
        color = Color.Yellow,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun DebugSlotItem(
    index: Int,
    state: WidgetSlotState,
    sdf: SimpleDateFormat
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("SLOT ${index + 1}: ${state::class.simpleName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        
        when (state) {
            is WidgetSlotState.Content -> {
                state.items.forEach { item ->
                    Text("TYPE: ${item.sourceType} | GROUP: ${item.semanticGroupId}", color = Color.Green, fontSize = 11.sp)
                    Text("DATA: ${item.title} | ${item.subtitle}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text("PRIORITY: ${item.priority} | FRESH: ${item.freshnessScore}", color = Color.Cyan, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            is WidgetSlotState.Error -> {
                Text("REASON: ${state.reason}", color = Color.Red, fontSize = 11.sp)
            }
            else -> {
                Text("Waiting for valid candidate...", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
    }
}
