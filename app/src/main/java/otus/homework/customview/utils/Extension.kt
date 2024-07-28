package otus.homework.customview.utils

import android.content.res.Resources
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val Int.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

val Int.sp: Float
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity)

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun LocalDate.formatToString(format: String): String {
    return DateTimeFormatter.ofPattern(format).withLocale(Locale.getDefault()).format(this)
}
