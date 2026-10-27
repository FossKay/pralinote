package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.HomeModel
import com.example.pralinote.model.TimetableModel
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Lesson
import java.time.LocalDate
import java.time.LocalDateTime

fun getPrettyTime(time: String): String {
    return time.split(" ")[1].split(":").take(2).joinToString("h").removeSuffix("00")
}

@Composable
fun Class(lesson: Lesson, showEnd: Boolean) {
    Box(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 6.dp,
        ),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    getPrettyTime(lesson.start),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showEnd) Text(
                    getPrettyTime(lesson.end),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            when (lesson.id) {
                "free" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Coffee,
                            contentDescription = "Pas de cours"
                        )
                    }
                }

                "lunch" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Restaurant,
                            contentDescription = "Déjeuner"
                        )
                    }
                }

                else -> {
                    val color = getColor(
                        Color(lesson.color.ifEmpty { "#ffffff" }.toColorInt()),
                        isSystemInDarkTheme()
                    )
                    Row(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 8.dp,
                                    bottomStart = 8.dp,

                                    topEnd = 16.dp,
                                    bottomEnd = 16.dp,
                                )
                            )
                            .height(IntrinsicSize.Min)
                            .alpha(if (lesson.cancelled) 0.5f else 1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                        //.background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(8.dp)
                                .background(color)
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        formatSubject(lesson.content.subject.name),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        listOf(
                                            lesson.content.teachers.joinToString(", ") {
                                                it.split(" ").first()
                                            },
                                            lesson.content.group.orEmpty(),
                                            lesson.content.classroom.joinToString(", ") { it.name }
                                        )
                                            .filter { it.isNotBlank() }
                                            .joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    if (lesson.note.isNotEmpty()) {
                                        Spacer(modifier = Modifier.padding(top = 4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp,
                                                ),
                                                text = lesson.note,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                    if (lesson.status.isNotEmpty()) {
                                        Spacer(modifier = Modifier.padding(top = 4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer)
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp,
                                                ),
                                                text = lesson.status,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTimetableView(
    paddingValues: PaddingValues,
    homeViewModel: HomeModel = viewModel(),
    timetableViewModel: TimetableModel = viewModel(),
) {
    val homeState by homeViewModel.home.collectAsStateWithLifecycle()
    val timetableState by timetableViewModel.timetable.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedWeek by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        if (!homeState.isLoading && homeState.home == null && homeState.error == null) {
            homeViewModel.loadHome()
        }
    }

    LaunchedEffect(homeState.home) {
        if (selectedDate == null && homeState.home != null) {
            selectedDate = when {
                homeState.home!!.nextDate == null -> homeState.home!!.date
                LocalDateTime.now().hour > 12 || toWorkDate(LocalDate.now()) != LocalDate.now() -> homeState.home!!.nextDate
                else -> homeState.home!!.date
            }
        }
    }

    LaunchedEffect(selectedDate) {
        if (selectedDate == null || selectedDate == homeState.home?.date || selectedDate == homeState.home?.nextDate) {
            return@LaunchedEffect
        }
        val week = AppClient.instance!!.getWeek(selectedDate!!)
        selectedWeek = week.toInt()
        if (week !in timetableState.timetable && week !in timetableState.loading && week !in timetableState.error) {
            timetableViewModel.loadWeek(week)
        }
    }

    Column(modifier = Modifier.padding(paddingValues)) {
        when (selectedDate) {
            is String -> {
                val localDate = getDate(selectedDate!!)
                DatePicker(
                    date = localDate,
                    onPrevious = {
                        scope.launch {
                            selectedDate = dateToString(previousWorkDate(localDate))
                            selectedWeek = AppClient.instance!!.getWeek(selectedDate!!).toInt()
                        }
                    },
                    onNext = {
                        scope.launch {
                            selectedDate = dateToString(nextWorkDate(localDate))
                            selectedWeek = AppClient.instance!!.getWeek(selectedDate!!).toInt()
                        }
                    },
                )
            }

            else -> {}
        }

        when {
            homeState.isLoading || selectedDate == null -> {
                LoadingView()
            }

            homeState.error != null -> {
                ErrorView(homeState.error!!)
            }

            else -> {
                when (selectedDate!!) {
                    homeState.home?.date -> {
                        Timetable(homeState.home!!.lessons)
                    }

                    homeState.home?.nextDate -> {
                        Timetable(homeState.home!!.nextLessons)
                    }

                    else -> {
                        val week = selectedWeek!!.toUByte()
                        val isLoading = week in timetableState.loading
                        val error = timetableState.error[week]
                        val weekLessons = timetableState.timetable[week]

                        when {
                            isLoading -> LoadingView()
                            error != null -> ErrorView(error)
                            weekLessons != null -> {
                                val weekDay = getDate(selectedDate!!).dayOfWeek.value - 1
                                Timetable(weekLessons[weekDay])
                            }

                            else -> LoadingView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Timetable(
    lessons: List<Lesson>
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        if (lessons.isEmpty()) item {
            Text(
                "Pas de cours",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        itemsIndexed(lessons) { index, lesson ->
            val showEnd = index == lessons.lastIndex ||
                    lessons[index + 1].start != lesson.end
            Class(lesson, showEnd)
        }
    }
}

@Composable
fun DatePicker(
    date: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Outlined.ChevronLeft,
                contentDescription = "Date précédente",
            )
        }
        Text(text = prettyDate(date))
        IconButton(onClick = onNext) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Prochaine date",
            )
        }
    }
}

