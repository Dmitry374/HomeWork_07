package otus.homework.customview.presentation.piechart.model

import android.graphics.Paint

data class PieChartModel(
    val startAtCircleDegree: Float,
    val amountOfCircle: Float,
    val paint: Paint,
    val category: String
)
