package otus.homework.customview.presentation.linechart.model

import android.os.Parcelable
import android.view.View.BaseSavedState
import otus.homework.customview.presentation.linechart.view.model.ExpenseModel

class LineChartState(
    superSavedState: Parcelable?,
    val dataList: List<ExpenseModel>
) : BaseSavedState(superSavedState)
