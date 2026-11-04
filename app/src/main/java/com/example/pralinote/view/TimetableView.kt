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
import com.example.pralinote.model.AgendaModel
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.HomeModel
import com.example.pralinote.model.TimetableModel
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Event
import uniffi.pronote_rs.Lesson
import java.time.LocalDate
import java.time.LocalDateTime

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
                            .clip(RoundedCornerShape(8.dp))
                            .height(IntrinsicSize.Min)
                            .alpha(if (lesson.cancelled) 0.5f else 1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.5f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(8.dp)
                                .background(color)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                formatSubject(lesson.content.subject.name),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                (listOf(
                                    lesson.content.teachers.joinToString(", ") {
                                        formatTeacher(it)
                                    },
                                    lesson.content.group.orEmpty(),
                                    lesson.content.classroom.joinToString(", ") { it.name }
                                )
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ") + "\n" + lesson.note).trim(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
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

@Composable
fun HomeTimetableView(
    paddingValues: PaddingValues,
    homeViewModel: HomeModel = viewModel(),
    timetableViewModel: TimetableModel = viewModel(),
    agendaViewModel: AgendaModel = viewModel(),
) {
    val homeState by homeViewModel.home.collectAsStateWithLifecycle()
    val timetableState by timetableViewModel.timetable.collectAsStateWithLifecycle()
    val agendaState by agendaViewModel.agenda.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!homeState.isLoading && homeState.home == null && homeState.error == null) {
            homeViewModel.loadHome()
        }
    }

    LaunchedEffect(homeState.home) {
        val home = homeState.home ?: return@LaunchedEffect

        if (selectedDate == null) {
            val now = LocalDateTime.now()
            selectedDate = when {
                now.hour > 12 && home.nextDate != null -> home.nextDate!!
                else -> home.date
            }
        }
    }

    LaunchedEffect(selectedDate, homeState.home) {
        val dateString = selectedDate ?: return@LaunchedEffect
        val home = homeState.home ?: return@LaunchedEffect

        if (dateString == home.date || dateString == home.nextDate) {
            return@LaunchedEffect
        }

        val date = getDate(dateString)
        val week = getWeek(date)

        if (
            week !in timetableState.timetable &&
            week !in timetableState.loading &&
            week !in timetableState.error
        ) {
            timetableViewModel.loadWeek(week)
        }
    }

    LaunchedEffect(selectedDate, homeState.home, agendaState) {
        if (selectedDate == null || homeState.home == null) {
            return@LaunchedEffect
        }

        val home = homeState.home!!

        if (selectedDate == home.date || selectedDate == home.nextDate) {
            return@LaunchedEffect
        }

        val date = getDate(selectedDate!!)

        if (home.events.isEmpty()) {
            return@LaunchedEffect
        }

        val firstDate = getDate(home.events.first().start.split(" ").first())
        val lastDate = getDate(home.events.last().start.split(" ").first())
        val fetchAgenda = date !in firstDate..lastDate

        if (fetchAgenda && agendaState.agenda == null && agendaState.error == null && !agendaState.isLoading) {
            agendaViewModel.loadAgenda()
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
                            val date = previousWorkDate(localDate)
                            selectedDate = dateToString(date)
                        }
                    },
                    onNext = {
                        scope.launch {
                            val date = nextWorkDate(localDate)
                            selectedDate = dateToString(date)
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
                val agendaLoading = agendaState.isLoading
                val agenda = (agendaState.agenda?.events ?: homeState.home!!.events).filter {
                    getDate(it.start.split(" ").first()) <= getDate(selectedDate!!) &&
                            getDate(selectedDate!!) <= getDate(it.end.split(" ").first())
                }

                when (selectedDate!!) {
                    homeState.home?.date -> {
                        Timetable(homeState.home!!.lessons, agenda, agendaLoading)
                    }

                    homeState.home?.nextDate -> {
                        Timetable(homeState.home!!.nextLessons, agenda, agendaLoading)
                    }

                    else -> {
                        val week = getWeek(getDate(selectedDate!!)) // u8
                        val isLoading = week in timetableState.loading
                        val error = timetableState.error[week]
                        val weekLessons = timetableState.timetable[week]

                        when {
                            isLoading -> LoadingView()
                            error != null -> ErrorView(error)
                            weekLessons != null -> {
                                val weekDay = getDate(selectedDate!!).dayOfWeek.value - 1
                                Timetable(weekLessons[weekDay], agenda, agendaLoading)
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
    lessons: List<Lesson>,
    events: List<Event>,
    eventsLoading: Boolean,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            when {
                eventsLoading -> LoadingView()
                else -> {
                    Column {
                        events.forEach { item ->
                            Event(item)
                        }
                    }
                }
            }
        }
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
fun Event(event: Event) {
    Box(
        modifier = Modifier.padding(
            start = 16.dp,
            bottom = 12.dp,
        ),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            val color = getColor(Color(event.color.toColorInt()), isSystemInDarkTheme())

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(color)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    event.title.trim(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (event.description.trim().isNotEmpty()) {
                    Text(
                        event.description.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
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
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            enabled = date != getDate(AppClient.params!!.firstDay),
            onClick = onPrevious
        ) {
            Icon(
                Icons.Outlined.ChevronLeft,
                contentDescription = "Date précédente",
            )
        }
        Text(
            text = prettyDate(date),
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(
            enabled = date != getDate(AppClient.params!!.lastDay),
            onClick = onNext
        ) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Prochaine date",
            )
        }
    }
}

