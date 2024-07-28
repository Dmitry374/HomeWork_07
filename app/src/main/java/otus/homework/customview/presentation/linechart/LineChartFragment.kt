package otus.homework.customview.presentation.linechart

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
        val spinnerCategories = view.findViewById<Spinner>(R.id.spinnerCategories)

        val categories = getCategories()

        val adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item).apply {
                addAll(categories)
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        spinnerCategories.apply {
            setAdapter(adapter)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val dataList = getExpenseData(categories[position])
                    lineChartView.setData(dataList)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        val dataList = getExpenseData(categories.first())
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

    private fun getCategories(): List<String> {
        val expenseRawList = repository.getExpenseRawList(resources)
        return expenseRawList.map { it.category }.toSet().toList()
    }

    companion object {
        const val TAG = "LineChartFragment"
    }
}
