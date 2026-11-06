package com.example.pralinote.view

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.example.pralinote.model.AppClient
import com.example.pralinote.ui.theme.PrimaryHex
import kotlinx.coroutines.launch
import uniffi.pronote_rs.GradeValue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun getPrettyTime(time: String): String {
    return time.split(" ")[1].split(":").take(2).joinToString("h").removeSuffix("00")
}

fun gradeToText(grade: GradeValue): String = when (grade) {
    is GradeValue.Value -> grade.v1.toString().removeSuffix(".0").replace(".", ",")
    GradeValue.Absent -> "Absent"
    GradeValue.Excused -> "Excusé"
    GradeValue.NotGraded -> "Pas noté"
    GradeValue.Unfit -> "Inapte"
    GradeValue.NotHandedIn -> "Non remis"
    GradeValue.AbsentZero -> "Absent (0/20)"
    GradeValue.NotHandedInZero -> "Non remis (0/20)"
    GradeValue.Congratulations -> "Félicitations"
    GradeValue.Empty -> ""
    GradeValue.Exception -> "Erreur" // GradeValue::Error in original code :/
}

fun getPraliColor(it: String?): Color {
    return Color((it ?: PrimaryHex).toColorInt())
}

fun formatTeacher(it: String): String {
    return it.split(" ").dropLast(1).joinToString(" ")
}

fun formatSubject(subject: String): String {
    val ignored = setOf("LV1", "LV2", "LP", "LM")

    return subject
        .split("/").joinToString(", ") { part ->
            part.trim()
                .split(" ")
                .filter { it.uppercase() !in ignored }
                .joinToString(" ")
                .lowercase()
                .replace("mathematiques", "mathématiques")
                .replace("francais", "français")
                .replace("education", "éducation")
                .replace("- ", "")
                .replace("& ", " & ")
                .split("/ ").last()
                .replaceFirstChar { it.uppercase() }
        }
}

fun getWeek(date: LocalDate): UByte {
    val days = ChronoUnit.DAYS.between(
        getDate(AppClient.params!!.firstDay),
        date
    )

    return (days / 7 + 1).toUByte()
}

fun getColor(color: Color, isDark: Boolean): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(
        color.toArgb(),
        hsl
    )
    if (isDark) {
        hsl[2] = maxOf(hsl[2], 0.6f)
    } else {
        hsl[2] = minOf(hsl[2], 0.5f)
    }
    return Color(ColorUtils.HSLToColor(hsl))
}

fun isHoliday(date: LocalDate): Boolean {
    val holidays = AppClient.params!!.holidays

    return holidays.any {
        val start = getDate(it.start)
        val end = getDate(it.end)

        date in start..end
    }
}

fun toWorkDate(date: LocalDate): LocalDate {
    val n = AppClient.params!!.weekDays.toInt()
    var result = date

    while (result.dayOfWeek.value > n || isHoliday(result)) {
        result = result.plusDays(1)
    }

    return result
}

fun nextWorkDate(date: LocalDate, hour: Int = 12): LocalDate {
    val n = AppClient.params!!.weekDays.toInt()
    var result = when {
        date.dayOfWeek.value > n -> date.plusDays((8 - date.dayOfWeek.value).toLong())
        hour < 12 -> date
        date.dayOfWeek.value < n -> date.plusDays(1)
        else -> date.plusDays((8 - date.dayOfWeek.value).toLong())
    }

    while (result.dayOfWeek.value > n || isHoliday(result)) {
        result = result.plusDays(1)
    }

    return result
}

fun previousWorkDate(date: LocalDate): LocalDate {
    val n = AppClient.params!!.weekDays.toInt()
    var result = when {
        date.dayOfWeek.value > n -> date.minusDays((date.dayOfWeek.value - n).toLong())
        date.dayOfWeek.value > 1 -> date.minusDays(1)
        else -> date.minusDays((7 - n + 1).toLong())
    }

    while (result.dayOfWeek.value > n || isHoliday(result)) {
        result = result.minusDays(1)
    }

    return result
}

fun prettyDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (ChronoUnit.DAYS.between(today, date)) {
        -1L -> "Hier"
        0L -> "Aujourd'hui"
        1L -> "Demain"
        in 2L..7L -> getDayOfWeek(date)
        else -> "${date.dayOfMonth} ${getMonth(date)}" +
                if (today.year != date.year) " ${date.year}" else ""
    }
}

fun getDate(date: String): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return LocalDate.parse(date, formatter)
}

fun dateToString(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return date.format(formatter)
}

fun getDayOfWeek(date: LocalDate): String {
    return when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lundi"
        DayOfWeek.TUESDAY -> "Mardi"
        DayOfWeek.WEDNESDAY -> "Mercredi"
        DayOfWeek.THURSDAY -> "Jeudi"
        DayOfWeek.FRIDAY -> "Vendredi"
        DayOfWeek.SATURDAY -> "Samedi"
        DayOfWeek.SUNDAY -> "Dimanche"
    }
}

fun getMonth(date: LocalDate): String {
    return when (date.month) {
        Month.JANUARY -> "janvier"
        Month.FEBRUARY -> "février"
        Month.MARCH -> "mars"
        Month.APRIL -> "avril"
        Month.MAY -> "mai"
        Month.JUNE -> "juin"
        Month.JULY -> "juillet"
        Month.AUGUST -> "août"
        Month.SEPTEMBER -> "septembre"
        Month.OCTOBER -> "octobre"
        Month.NOVEMBER -> "novembre"
        Month.DECEMBER -> "décembre"
    }
}

fun getShortMonth(date: LocalDate): String {
    return when (date.month) {
        Month.JANUARY -> "janv"
        Month.FEBRUARY -> "fev"
        Month.MARCH -> "mars"
        Month.APRIL -> "avr"
        Month.MAY -> "mai"
        Month.JUNE -> "juin"
        Month.JULY -> "juil"
        Month.AUGUST -> "août"
        Month.SEPTEMBER -> "sep"
        Month.OCTOBER -> "oct"
        Month.NOVEMBER -> "nov"
        Month.DECEMBER -> "dec"
    }
}

fun shortPeriod(p: String): String {
    return p
        .replace("Trimestre ", "T")
        .replace("Semestre ", "S")
        .replace("Hors période", "HP")
        .replace("Année continue", "ANNEE")
        .replace("Contrôle en cours de formation", "FORM")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
fun ErrorView(
    error: Throwable,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val details = remember(error) {
        buildString {
            appendLine(error.stackTraceToString())
            error.cause?.let {
                appendLine()
                appendLine("Cause:")
                appendLine(it.stackTraceToString())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Une erreur s'est produite",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            IconButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("error", details))
                        )
                    }
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copier l'erreur",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = error.message ?: error::class.simpleName ?: "Erreur inconnue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
