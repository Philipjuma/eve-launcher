package com.haven.evelauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.data.repository.ClockSize
import com.haven.evelauncher.design.settings.LocalEveSettings
import com.haven.evelauncher.ui.theme.EveTypography
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EveClock(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val settings = LocalEveSettings.current
    val clockColor = if (settings.isClockDynamic) color else Color(settings.personalClockColor)
    val context = LocalContext.current
    
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val is24Hour = DateFormat.is24HourFormat(context)
    val timePattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeFormat = remember(is24Hour) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    
    val calendar = Calendar.getInstance()
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(currentTime))
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(currentTime)).uppercase()
    
    val suffix = getDayOfMonthSuffix(dayOfMonth)
    val dateText = "It is $dayOfWeek, $dayOfMonth$suffix $month"

    val baseStyle = EveTypography.Clock
    val clockStyle = when(settings.clockSize) {
        ClockSize.SMALL -> baseStyle.copy(fontSize = 70.sp, letterSpacing = (-2).sp)
        ClockSize.MEDIUM -> baseStyle.copy(fontSize = 90.sp, letterSpacing = (-3).sp)
        ClockSize.LARGE -> baseStyle
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeFormat.format(Date(currentTime)),
            style = clockStyle,
            color = clockColor,
            modifier = Modifier.clickable {
                try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general clock intent
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                        ?: context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent != null) context.startActivity(intent)
                }
            }
        )
        Text(
            text = dateText,
            style = EveTypography.Metadata.copy(
                fontSize = 15.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = clockColor.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable {
                    try {
                        val uri = Uri.parse("content://com.android.calendar/time/" + System.currentTimeMillis())
                        val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback
                    }
                }
        )
    }
}

private fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) return "th"
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
