package otus.homework.customview.data

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.data.model.PayloadItemRaw

class ChartDataRepository {

    fun getExpenseRawList(resources: Resources): List<PayloadItemRaw> {
        val payloadJson = resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }

        return Gson().fromJson(
            payloadJson,
            object : TypeToken<List<PayloadItemRaw>>() {}.type
        )
    }
}
