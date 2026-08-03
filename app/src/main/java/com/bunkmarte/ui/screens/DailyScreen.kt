package com.bunkmarte.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bunkmarte.data.model.AttendanceStatus
import com.bunkmarte.ui.theme.CardDark
import com.bunkmarte.ui.theme.CardDarkVariant
import com.bunkmarte.ui.theme.DeepViolet
import com.bunkmarte.ui.theme.DimGray
import com.bunkmarte.ui.theme.NeonIndigo
import com.bunkmarte.ui.theme.NeonPurple
import com.bunkmarte.ui.theme.PureBlack
import com.bunkmarte.ui.theme.SubtleGray
import com.bunkmarte.ui.theme.statusColor
import com.bunkmarte.ui.viewmodel.DailyViewModel
import com.bunkmarte.ui.viewmodel.SlotUiState

/**
 * Home screen — dark themed daily class schedule with neon attendance controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    navController: NavController,
    viewModel: DailyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BunkMarte",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                        Text(
                            text = uiState.displayDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = SubtleGray
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = NeonPurple.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 16.dp)
                    )
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
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.BarChart, "Summary") },
                    label = { Text("Summary") },
                    selected = false,
                    onClick = { navController.navigate("summary") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(PureBlack)
        ) {
            // Day selector row
            DaySelector(
                selectedDay = uiState.selectedDay,
                onDaySelected = { viewModel.selectDay(it) }
            )

            if (uiState.slots.isEmpty()) {
                EmptyDayMessage(isWeekend = uiState.isWeekend)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(
                        items = uiState.slots,
                        key = { _, slot -> "${slot.startTime}-${slot.endTime}" }
                    ) { index, slot ->
                        ClassCard(
                            slot = slot,
                            allSubjects = uiState.allSubjects,
                            isExamDay = uiState.isExamDay,
                            onStatusSelected = { status ->
                                viewModel.setStatus(index, status)
                            },
                            onOverrideSubject = { subject ->
                                viewModel.overrideSubject(index, subject)
                            }
                        )
                    }
                }
            }
        }
    }

    // Exam Day confirmation dialog
    if (uiState.showExamDayDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExamDayDialog() },
            containerColor = CardDark,
            icon = {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = NeonIndigo
                )
            },
            title = {
                Text(
                    "Mark as Exam Day?",
                    color = Color.White
                )
            },
            text = {
                Text(
                    "This will mark ALL classes today as Exam Day. " +
                    "No attendance will be recorded. You can undo this " +
                    "by selecting a different status for any slot.",
                    color = SubtleGray
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmExamDay() }) {
                    Text("Confirm", color = NeonIndigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExamDayDialog() }) {
                    Text("Cancel", color = SubtleGray)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Day Selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DaySelector(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val shortDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDay == day
            FilterChip(
                selected = isSelected,
                onClick = { onDaySelected(day) },
                label = {
                    Text(
                        text = shortDays[index],
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold
                                     else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonPurple,
                    selectedLabelColor = PureBlack,
                    containerColor = CardDark,
                    labelColor = SubtleGray
                ),
                border = if (!isSelected) BorderStroke(1.dp, DimGray) else null
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyDayMessage(isWeekend: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isWeekend) "It's the weekend!" else "No classes today!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isWeekend) "Select a weekday above to view or mark attendance"
                       else "Enjoy your free time",
                style = MaterialTheme.typography.bodyMedium,
                color = SubtleGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Class Card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClassCard(
    slot: SlotUiState,
    allSubjects: List<String>,
    isExamDay: Boolean,
    onStatusSelected: (AttendanceStatus) -> Unit,
    onOverrideSubject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExamDay) CardDarkVariant.copy(alpha = 0.5f)
                             else CardDark
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExamDay) NeonIndigo.copy(alpha = 0.3f)
                    else NeonPurple.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Row 1: Time range + duration badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = NeonPurple.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${slot.startTime} — ${slot.endTime}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SubtleGray
                    )
                }
                if (slot.slotCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonPurple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${slot.slotCount} hrs",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Row 2: Subject name + override button ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.effectiveSubject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (slot.isOverridden) {
                        Text(
                            text = "↳ was ${slot.originalSubject}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple.copy(alpha = 0.8f)
                        )
                    }
                }

                OverrideDropdown(
                    allSubjects = allSubjects,
                    currentSubject = slot.effectiveSubject,
                    enabled = !isExamDay,
                    onSubjectSelected = onOverrideSubject
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Row 3: Status buttons ──
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = slot.status == status
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) status.statusColor.copy(alpha = 0.2f)
                                      else Color.Transparent,
                        animationSpec = tween(durationMillis = 250),
                        label = "chipBg"
                    )

                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusSelected(status) },
                        label = {
                            Text(
                                text = status.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold
                                             else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipBg,
                            selectedLabelColor = status.statusColor,
                            containerColor = Color.Transparent,
                            labelColor = SubtleGray
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) status.statusColor.copy(alpha = 0.6f)
                                    else DimGray
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Override Dropdown
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverrideDropdown(
    allSubjects: List<String>,
    currentSubject: String,
    enabled: Boolean,
    onSubjectSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Icon(
                Icons.Outlined.SwapHoriz,
                contentDescription = "Override Subject",
                tint = if (enabled) NeonPurple
                       else SubtleGray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardDarkVariant)
        ) {
            // Header
            Text(
                text = "Override Subject",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = NeonPurple
            )
            HorizontalDivider(color = DimGray)

            // Subject options
            allSubjects.forEach { subject ->
                val isCurrent = subject == currentSubject
                DropdownMenuItem(
                    text = {
                        Text(
                            text = subject,
                            fontWeight = if (isCurrent) FontWeight.Bold
                                         else FontWeight.Normal,
                            color = if (isCurrent) Color.White else SubtleGray
                        )
                    },
                    onClick = {
                        onSubjectSelected(subject)
                        expanded = false
                    },
                    trailingIcon = if (isCurrent) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = NeonPurple
                            )
                        }
                    } else null
                )
            }
        }
    }
}
