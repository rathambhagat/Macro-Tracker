package com.bunkmarte.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bunkmarte.domain.SubjectStatsUi
import com.bunkmarte.domain.WarningLevel
import com.bunkmarte.ui.theme.CardDark
import com.bunkmarte.ui.theme.CriticalRed
import com.bunkmarte.ui.theme.DangerAmber
import com.bunkmarte.ui.theme.DimGray
import com.bunkmarte.ui.theme.NeonPurple
import com.bunkmarte.ui.theme.PureBlack
import com.bunkmarte.ui.theme.SafeGreen
import com.bunkmarte.ui.theme.SubtleGray
import com.bunkmarte.ui.viewmodel.SummaryViewModel

/**
 * Subject Summary screen — dark themed with neon percentage indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    viewModel: SummaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh statistics every time this screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Subject Summary",
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureBlack,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CalendarToday, "Today") },
                    label = { Text("Today") },
                    selected = false,
                    onClick = { navController.popBackStack() },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        indicatorColor = NeonPurple.copy(alpha = 0.12f),
                        unselectedIconColor = SubtleGray,
                        unselectedTextColor = SubtleGray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.BarChart, "Summary") },
                    label = { Text("Summary") },
                    selected = true,
                    onClick = { },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        indicatorColor = NeonPurple.copy(alpha = 0.12f),
                        unselectedIconColor = SubtleGray,
                        unselectedTextColor = SubtleGray
                    )
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(PureBlack),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPurple)
                }
            }

            uiState.subjectStats.all { it.totalClasses == 0 } -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(PureBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No attendance data yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start marking attendance on the daily view",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SubtleGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(PureBlack),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = uiState.subjectStats,
                        key = { it.subject }
                    ) { stats ->
                        SubjectCard(stats = stats)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subject Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubjectCard(stats: SubjectStatsUi) {
    val case1Color = when (stats.warningLevel) {
        WarningLevel.SAFE -> SafeGreen
        WarningLevel.DANGER -> DangerAmber
        WarningLevel.CRITICAL -> CriticalRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header: Subject name + total classes badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stats.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonPurple.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = if (stats.totalClasses == 0) "No data"
                               else "${stats.totalClasses} class${if (stats.totalClasses != 1) "es" else ""}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple
                    )
                }
            }

            if (stats.totalClasses > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── Case 1: Normal (with progress bar) ──
                PercentageRow(
                    label = "Normal",
                    sublabel = "P / (P + A + B)",
                    percentage = stats.case1,
                    color = case1Color,
                    showProgress = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Case 2: With Mass Bunk Penalty ──
                PercentageRow(
                    label = "w/ Mass Bunk",
                    sublabel = "P / (P + A + B + MB)",
                    percentage = stats.case2,
                    color = SubtleGray,
                    showProgress = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Case 3: With Cancelled Bonus ──
                PercentageRow(
                    label = "w/ Cancelled",
                    sublabel = "(P + C) / (P + A + B + C)",
                    percentage = stats.case3,
                    color = SubtleGray,
                    showProgress = false
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DimGray)
                Spacer(modifier = Modifier.height(12.dp))

                // ── Predictor ──
                PredictorText(stats = stats)
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No attendance tracked yet for this subject",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleGray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Percentage Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PercentageRow(
    label: String,
    sublabel: String,
    percentage: Double?,
    color: Color,
    showProgress: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleGray,
                    fontSize = 10.sp
                )
            }
            Text(
                text = percentage?.let { String.format("%.1f%%", it) } ?: "N/A",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (percentage != null) color else SubtleGray
            )
        }

        if (showProgress && percentage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.10f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Predictor Text
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PredictorText(stats: SubjectStatsUi) {
    when {
        stats.classesNeeded == null -> {
            PredictorRow(
                icon = "📋",
                text = "No standard attendance data yet",
                color = SubtleGray
            )
        }
        stats.classesNeeded == 0 -> {
            PredictorRow(
                icon = "✅",
                text = "You're safe! Attendance is above 75%",
                color = SafeGreen
            )
        }
        else -> {
            PredictorRow(
                icon = "⚠\uFE0F",
                text = "Attend ${stats.classesNeeded} more consecutive class${if (stats.classesNeeded != 1) "es" else ""} to reach 75%",
                color = DangerAmber
            )
        }
    }
}

@Composable
private fun PredictorRow(icon: String, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
