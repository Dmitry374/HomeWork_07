package otus.homework.customview.presentation.linechart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import otus.homework.customview.R
import otus.homework.customview.data.ChartDataRepository
import otus.homework.customview.presentation.linechart.model.Expense
import otus.homework.customview.presentation.linechart.view.LineChartView
import otus.homework.customview.utils.toLocalDate

class LineChartFragment : Fragment(R.layout.fragment_line_chart) {

    private val repository = ChartDataRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineChartView = view.findViewById<LineChartView>(R.id.lineChartView)

        val dataList = getExpenseData("Продукты")

        lineChartView.setData(dataList)
    }

    private fun getExpenseData(category: String): List<Expense> {
        val expenseRawList = repository.getExpenseRawList(resources)
        return expenseRawList.sortedBy { it.time }.filter { it.category == category }.map {
            Expense(
                amount = it.amount,
                localDate = (it.time * 1000).toLocalDate()
            )
        }
    }

    companion object {
        const val TAG = "LineChartFragment"
    }
}
