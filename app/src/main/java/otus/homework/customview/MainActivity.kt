package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.utils.toLocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
//
//        pieChartView.apply {
//            setData(getExpenseData())
//            onCategoryClick = { categoryName ->
//                Toast.makeText(this@MainActivity, categoryName, Toast.LENGTH_SHORT).show()
//            }
//        }

        val lineChartView = findViewById<LineChartView>(R.id.lineChartView)

        val dataList = getExpenseData2("Продукты")

        lineChartView.setData(dataList)
    }

    private fun getExpenseData(): List<CategoryExpenseModel> {
        val expenseRawList = getExpenseRawList()
        return mapToCategoryExpenseModelList(expenseRawList)
    }

    private fun getExpenseRawList(): List<PayloadItemRaw> {
        val payloadJson = resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }

        return Gson().fromJson(
            payloadJson,
            object : TypeToken<List<PayloadItemRaw>>() {}.type
        )
    }

    private fun getExpenseData2(category: String): List<Expense> {
        val expenseRawList = getExpenseRawList()
        return expenseRawList.sortedBy { it.time }.filter { it.category == category }.map {
            Expense(
                amount = it.amount,
                localDate = (it.time * 1000).toLocalDate()
            )
        }
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
}