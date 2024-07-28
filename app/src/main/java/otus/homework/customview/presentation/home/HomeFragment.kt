package otus.homework.customview.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import otus.homework.customview.R
import otus.homework.customview.presentation.linechart.LineChartFragment
import otus.homework.customview.presentation.piechart.PieChartFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonPieChartView = view.findViewById<Button>(R.id.buttonPieChartView)
        val buttonLineChartView = view.findViewById<Button>(R.id.buttonLineChartView)

        buttonPieChartView.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<PieChartFragment>(R.id.fragment_container_view)
                addToBackStack(PieChartFragment.TAG)
            }
        }

        buttonLineChartView.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LineChartFragment>(R.id.fragment_container_view)
                addToBackStack(LineChartFragment.TAG)
            }
        }
    }
}
