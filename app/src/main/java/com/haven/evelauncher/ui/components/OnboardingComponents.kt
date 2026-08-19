package com.haven.evelauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.ui.theme.EveTypography

@Composable
fun DockSetupStep(prioritizedApps: List<AppInfo>, onFinish: (List<AppInfo>) -> Unit) {
    val selectedApps = remember { mutableStateListOf<AppInfo>() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
            .padding(top = 32.dp) // Extra top padding to lower it
    ) {
        Text(
            text = "Setup your Dock", 
            style = EveTypography.OnboardingTitle.copy(fontSize = 28.sp),
            color = Color(0xFFA8FFD0)
        )
        Text(
            text = "Pick 4 apps you use most.", 
            style = EveTypography.WidgetSecondary,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prioritizedApps) { app ->
                val isSelected = selectedApps.contains(app)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
                            if (isSelected) selectedApps.remove(app)
                            else if (selectedApps.size < 4) selectedApps.add(app)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(bitmap = app.iconBitmap, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(app.label, style = EveTypography.AppLabel, color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFA8FFD0))
                }
            }
        }
        
        Button(
            onClick = { onFinish(selectedApps.toList()) },
            enabled = selectedApps.size == 4,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f),
                contentColor = Color.White
            )
        ) {
            Text("Finish Setup", style = EveTypography.WidgetSecondary)
        }
    }
}

@Composable
fun PermissionItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title, 
            style = EveTypography.WidgetSecondary.copy(fontWeight = FontWeight.SemiBold), // Using WidgetSecondary
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description, 
            style = EveTypography.Metadata, // Reduced globally
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
