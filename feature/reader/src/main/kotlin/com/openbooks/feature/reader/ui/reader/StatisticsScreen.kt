package com.openbooks.feature.reader.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.foundation.layout.Spacer
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.ReadingSession
import com.openbooks.app.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    book: Book,
    sessions: List<ReadingSession>,
    onDismiss: () -> Unit
) {
    val stats = remember(sessions) { calculateStatistics(sessions) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text("Reading Statistics", fontWeight = FontWeight.Bold) },
            navigationIcon = { 
                androidx.compose.material3.IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer
            )
        )
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview stats
            item {
                Text("Overview", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Time",
                        value = formatDuration(stats.totalReadingTime),
                        icon = Icons.Filled.Timer,
                        color = OpenBooksTheme.colorScheme.primary
                    )
                    StatCard(
                        title = "Sessions",
                        value = "${stats.sessionCount}",
                        icon = Icons.Filled.CalendarMonth,
                        color = OpenBooksTheme.colorScheme.secondary
                    )
                    StatCard(
                        title = "Avg Session",
                        value = formatDuration(stats.averageSessionTime),
                        icon = Icons.Filled.Speed,
                        color = OpenBooksTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Progress stats
            item {
                Text("Progress", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Pages Read",
                        value = "${stats.pagesRead}",
                        icon = Icons.Filled.TrendingUp,
                        color = OpenBooksTheme.colorScheme.primary
                    )
                    StatCard(
                        title = "Current Streak",
                        value = "${stats.currentStreak} days",
                        icon = Icons.Filled.LocalFireDepartment,
                        color = OpenBooksTheme.colorScheme.error
                    )
                    StatCard(
                        title = "Completion",
                        value = "${(book.readingProgress * 100).toInt()}%",
                        icon = Icons.Filled.TrendingUp,
                        color = OpenBooksTheme.colorScheme.secondary
                    )
                }
            }
            
            // Reading sessions history
            item {
                Text("Recent Sessions", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(sessions.take(20)) { session ->
                SessionItem(session = session)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.material.icons.filled.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SessionItem(session: ReadingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = OpenBooksTheme.colorScheme.surfaceContainer
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Read ${session.pagesRead} pages",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatDuration(session.durationSeconds)} • ${session.startTime.toString()}",
                    fontSize = 12.sp,
                    color = OpenBooksTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class ReadingStats(
    val totalReadingTime: Long,
    val sessionCount: Int,
    val averageSessionTime: Long,
    val pagesRead: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

fun calculateStatistics(sessions: List<ReadingSession>): ReadingStats {
    if (sessions.isEmpty()) {
        return ReadingStats(0, 0, 0, 0, 0, 0)
    }
    
    val totalTime = sessions.sumOf { it.durationSeconds }
    val totalPages = sessions.sumOf { it.pagesRead }
    val avgTime = if (sessions.isNotEmpty()) totalTime / sessions.size else 0
    
    // Calculate streaks
    val sortedSessions = sessions.sortedByDescending { it.startTime.toEpochMilli() }
    var currentStreak = 0
    var longestStreak = 0
    var tempStreak = 0
    var lastDate: java.time.LocalDate? = null
    
    for (session in sortedSessions) {
        val sessionDate = java.time.Instant.ofEpochMilli(session.startTime.toEpochMilli())
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        
        if (lastDate == null || sessionDate == lastDate.minusDays(1)) {
            tempStreak++
            if (lastDate == null) currentStreak = tempStreak
        } else if (sessionDate != lastDate) {
            tempStreak = 1
        }
        
        longestStreak = maxOf(longestStreak, tempStreak)
        lastDate = sessionDate
    }
    
    return ReadingStats(
        totalReadingTime = totalTime * 1000, // Convert to milliseconds
        sessionCount = sessions.size,
        averageSessionTime = avgTime * 1000,
        pagesRead = totalPages,
        currentStreak = currentStreak,
        longestStreak = longestStreak
    )
}

fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return if (hours > 0) {
        "${hours}h ${minutes % 60}m"
    } else if (minutes > 0) {
        "${minutes}m ${seconds % 60}s"
    } else {
        "${seconds}s"
    }
}