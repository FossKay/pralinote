package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pralinote.model.HomeModel
import com.example.pralinote.model.HomeworkDoneModel
import com.example.pralinote.model.HomeworkModel
import uniffi.pronote_rs.Homework

@Composable
fun Homework(
    homework: Homework,
    homeworkDoneModel: HomeworkDoneModel = viewModel(),
) {
    val state by homeworkDoneModel.homeworkDone.collectAsStateWithLifecycle()
    val done = state.done[homework.id] ?: homework.done

    val color = getColor(Color(homework.color.toColorInt()), isSystemInDarkTheme())
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                )
            )
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .alpha(if (done) 0.5f else 1f)
            .clickable {
                if (homework.id !in state.loading && homework.id !in state.error) {
                    homeworkDoneModel.setDone(homework.id, !done)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .background(color)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    Box(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            formatSubject(homework.subject.name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    val raw = AnnotatedString.fromHtml(homework.description)
                    val trimmed = raw.subSequence(0, raw.text.trimEnd().length)
                    Text(
                        trimmed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    homework.attachments.forEach { attachment ->
                        AttachmentView(attachment)
                    }
                }
            }
        }
    }

    if (homework.id in state.error) {
        ErrorView(state.error[homework.id]!!)
    }
}

@Composable
fun HomeworkDay(text: String) {
    Text(
        text,
        modifier = Modifier.padding(
            horizontal = 8.dp,
            vertical = 6.dp,
        ).fillMaxWidth(),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
fun HomeHomeworkView(
    paddingValues: PaddingValues,
    homeViewModel: HomeModel = viewModel(),
    homeworkViewModel: HomeworkModel = viewModel(),
) {
    val homeState by homeViewModel.home.collectAsStateWithLifecycle()
    val homeworkState by homeworkViewModel.homework.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (!homeState.isLoading && homeState.home == null && homeState.error == null) {
            homeViewModel.loadHome()
        }
    }

    when {
        homeState.isLoading -> LoadingView()
        homeState.error != null -> ErrorView(homeState.error!!)
        homeState.home != null -> {
            val homeworkList = homeState.home!!.homework + (homeworkState.homework ?: emptyList())

            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                itemsIndexed(homeworkList) { index, homework ->
                    val showTitle =
                        index == 0 || homeworkList[index - 1].dueDate != homework.dueDate
                    if (showTitle) HomeworkDay(
                        prettyDate(getDate(homework.dueDate))
                    )
                    Homework(homework)
                }

                item {
                    when {
                        homeworkState.isLoading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                LoadingView()
                            }
                        }

                        homeworkState.error != null -> ErrorView(homeworkState.error!!)
                        homeworkState.homework != null -> {}
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Button(onClick = {
                                    val lastDate =
                                        if (homeworkList.isEmpty()) getDate(homeState.home!!.date).plusDays(
                                            6L
                                        ) else getDate(
                                            homeworkList.last().dueDate
                                        )
                                    homeworkViewModel.loadHomework(lastDate)
                                }) {
                                    Text("Voir plus")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

