package otus.homework.customview.presentation.piechart.view.model

import android.os.Parcelable
import android.view.View.BaseSavedState

class PieChartState(
    superSavedState: Parcelable?,
    val dataList: List<CategoryExpenseModel>
) : BaseSavedState(superSavedState)
