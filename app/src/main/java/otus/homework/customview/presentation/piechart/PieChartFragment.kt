package otus.homework.customview.presentation.piechart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import otus.homework.customview.R
import otus.homework.customview.data.ChartDataRepository
import otus.homework.customview.data.model.PayloadItemRaw
import otus.homework.customview.presentation.piechart.view.PieChartView
import otus.homework.customview.presentation.piechart.view.model.CategoryExpenseModel

class PieChartFragment : Fragment(R.layout.fragment_pie_chart) {

    private val repository = ChartDataRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChartView = view.findViewById<PieChartView>(R.id.pieChartView)

        pieChartView.apply {
            setData(getExpenseData())
            onCategoryClick = { categoryName ->
                Toast.makeText(requireContext(), categoryName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getExpenseData(): List<CategoryExpenseModel> {
        val expenseRawList = repository.getExpenseRawList(resources)
        return mapToCategoryExpenseModelList(expenseRawList)
    }

    private fun mapToCategoryExpenseModelList(list: List<PayloadItemRaw>): List<CategoryExpenseModel> {
        val expenses = mutableMapOf<String, Int>()
        val dataList = mutableListOf<CategoryExpenseModel>()
        list.forEach { item ->
            if (expenses.keys.contains(item.category)) {
                val categoryAmount = expenses[item.category] ?: 0
                expenses[item.category] = categoryAmount + item.amount
            } else {
                expenses[item.category] = item.amount
            }
        }

        expenses.forEach { (category, expense) ->
            dataList.add(
                CategoryExpenseModel(
                    category = category,
                    expenseAmount = expense
                )
            )
        }

        return dataList
    }

    companion object {
        const val TAG = "PieChartFragment"
    }
}
