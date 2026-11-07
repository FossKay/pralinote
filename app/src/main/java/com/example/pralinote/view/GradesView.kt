package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.GradesModel
import com.example.pralinote.model.HomeModel
import uniffi.pronote_rs.Grade
import uniffi.pronote_rs.GradeValue
import uniffi.pronote_rs.Period

@Composable
fun GradesView(
    paddingValues: PaddingValues,
    homeViewModel: HomeModel = viewModel(),
    gradesViewModel: GradesModel = viewModel(),
) {
    val homeState by homeViewModel.home.collectAsStateWithLifecycle()
    val state by gradesViewModel.grades.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!homeState.isLoading && homeState.home == null && homeState.error == null) {
            homeViewModel.loadHome()
        }
    }

    when {
        homeState.isLoading -> LoadingView()
        homeState.error != null -> ErrorView(homeState.error!!)
        homeState.home != null -> {
            val homeGrades = homeState.home!!.grades
            val fullGrades = selected?.let {
                state.grades[selected]
            }

            // If user selects a period
            LaunchedEffect(selected) {
                if (selected != null && selected !in state.grades && selected !in state.loading) {
                    gradesViewModel.loadPeriod(selected!!)
                }
            }

            Column(modifier = Modifier.padding(paddingValues)) {
                PeriodPicker(
                    AppClient.userParams!!.student.periods.periods,
                    selected ?: homeState.home?.grades?.period?.id
                ) { p ->
                    selected = p
                }
                val showHome = selected == null || selected == homeState.home!!.grades.period?.id

                when {
                    selected != null && state.error[selected!!] != null -> ErrorView(state.error[selected]!!)
                    !showHome && fullGrades == null -> LoadingView()
                    else -> {
                        GradesList(
                            selectedNull = selected == null,
                            grades = fullGrades?.grades ?: emptyList(),
                            homeGrades = if (showHome) {
                                homeGrades.grades
                            } else {
                                emptyList()
                            },
                            footer = {
                                // "Voir plus" only on the home period
                                if (fullGrades == null) {
                                    when {
                                        selected != null -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                LoadingView()
                                            }
                                        }

                                        else -> {
                                            if (homeState.home!!.grades.period != null) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center,
                                                ) {
                                                    Button(onClick = {
                                                        selected =
                                                            homeState.home!!.grades.period!!.id
                                                    }) {
                                                        Text("Voir plus")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodPicker(periods: List<Period>, selected: String?, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        periods.forEachIndexed { i, p ->
            ToggleButton(
                checked = p.id == selected,
                onCheckedChange = { onSelect(p.id) },
                modifier = Modifier.weight(1f),
                shapes = when (i) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    periods.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(shortPeriod(p.name), maxLines = 1)
            }
        }
    }
}

@Composable
fun GradesList(selectedNull: Boolean, grades: List<Grade>, homeGrades: List<Grade>, footer: @Composable () -> Unit = {}) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if ((grades + homeGrades).isEmpty()) {
            item {
                Text(
                    if (selectedNull) "Pas de notes récentes" else { "Pas de notes" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (homeGrades.isNotEmpty()) {
            if (grades.isNotEmpty()) {
                val newHome = homeGrades.map {
                    grades.firstOrNull { grade ->
                        grade.id == it.id
                    } ?: it
                }
                items(newHome.reversed()) {
                    GradeView(it)
                }
            } else {
                items(homeGrades.reversed()) {
                    GradeView(it)
                }
            }
        } else if (grades.isNotEmpty()) {
            items(grades.reversed()) {
                GradeView(it)
            }
        }

        item { footer() }

        if (homeGrades.isNotEmpty() && grades.isNotEmpty()) {
            val newFull = grades.filter {
                // could also .all(... != ...)
                !homeGrades.any({ homeGrade ->
                    homeGrade.id == it.id
                })
            }

            item {
                Text(
                    "Anciennes notes",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            items(newFull.reversed()) {
                GradeView(it)
            }
        }
    }
}

@Composable
fun GradeView(grade: Grade) {
    val color = getColor(Color(grade.subject.color.toColorInt()), isSystemInDarkTheme())
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .background(color)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val date = getDate(grade.date)
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    verticalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        "${date.dayOfMonth}\n${getShortMonth(date)}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 1.3.em,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatSubject(grade.subject.subject.name),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (grade.description.isNotEmpty()) {
                        Text(
                            grade.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    grade.solution?.let {
                        AttachmentView(grade.solution!!)
                    }
                }
                Row {
                    Text(
                        gradeToText(grade.grade),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (grade.grade is GradeValue.Value && grade.scale != grade.defaultScale) {
                        Text(
                            "/${grade.scale}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}