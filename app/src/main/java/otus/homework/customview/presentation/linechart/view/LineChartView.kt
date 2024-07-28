package otus.homework.customview.presentation.linechart.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.presentation.linechart.model.Expense
import otus.homework.customview.presentation.linechart.model.LineChartState
import otus.homework.customview.presentation.linechart.view.model.ExpenseModel
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.formatToString
import otus.homework.customview.utils.sp
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.log10
import kotlin.math.pow

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataSet: List<ExpenseModel> = emptyList()

    private var maxTime: LocalDate = LocalDate.MIN // Максимальное время
    private var maxAmount: Int = 0 // Максимальная цена

    private var minTime: LocalDate = LocalDate.MIN // Минимальная дата
    private var minAmount: Int = 0 // Минимальная цена

    private var daysBetween: Long = 0L // Расстояние между начальной и конечной датой в днях

    private val path = Path() // Path для отрисовки графика

    private var chartPadding: Float = 15.dp // Внешний отступ

    private var textPadding: Float = 4.dp // расстояние между графиком и текстом
    private var textDatePadding: Float = 8.dp // Расстояние между текстом дат

    private var digitSize = 30.dp // Размер одного разряда для цены

    private var textAmountWidth: Float = 0F // Длина текста цены
    private var textAmountHeight: Float = 0F // Высота текста цены

    private var textDateWidth: Float = 0F // Длина текста даты
    private var textDateHeight: Float = 0F // Высота текста даты

    private var startChartX: Float = 0F // Начало графика по X
    private var startChartY: Float = 0F // Начало графика по Y
    private var endChartX: Float = 0F // Окончание графика по X
    private var endChartY: Float = 0F // Окончание графика по Y

    private var axisXSize: Float = 0F // Длина оси по X
    private var axisYSize: Float = 0F // Длина оси по Y

    private var shiftTextDateX: Float = 0F // Сдвиг текста для даты по X
    private var shiftTextAmountY: Float = 0F // Сдвиг текста для цены по Y

    private val paint = Paint() // Paint для рисования графика
    private val separatorsPaint = Paint() // Paint для горизонтальных разделителей (сетки)
    private val textPaint: TextPaint = TextPaint() // Paint для отображения текста
    private val axisLinePaint = Paint() // Paint для рисования осей

    init {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.LineChartView)

            /* Инициализация кисти для рисования графика */
            val chartColor = typeArray.getColor(
                R.styleable.LineChartView_lineChartColor,
                Color.GREEN
            )

            val chartStrokeWidth = typeArray.getDimension(
                R.styleable.LineChartView_lineChartStrokeWidth,
                4.dp
            )

            paint.apply {
                color = chartColor
                strokeWidth = chartStrokeWidth
                style = Paint.Style.STROKE
                pathEffect = CornerPathEffect(60f)
            }

            /* Инициализация кисти для рисования графика */
            val chartSeparatorColor = typeArray.getColor(
                R.styleable.LineChartView_lineChartSeparatorColor,
                Color.GRAY
            )

            separatorsPaint.apply {
                style = Paint.Style.FILL
                color = chartSeparatorColor
            }

            /* Инициализация кисти для текста суммы трат и даты */
            val chartTextSize = typeArray.getDimension(
                R.styleable.LineChartView_lineChartTextSize,
                12.sp
            )

            val chartTextColor = typeArray.getColor(
                R.styleable.LineChartView_lineChartTextColor,
                Color.GRAY
            )

            textPaint.apply {
                color = chartTextColor
                textSize = chartTextSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            /* Инициализация кисти для рисования осей */
            val chartAxisColor = typeArray.getColor(
                R.styleable.LineChartView_lineChartAxisColor,
                Color.RED
            )

            val chartAxisStrokeWidth = typeArray.getDimension(
                R.styleable.LineChartView_lineChartAxisStrokeWidth,
                2.dp
            )

            axisLinePaint.apply {
                color = chartAxisColor
                strokeWidth = chartAxisStrokeWidth
            }

            typeArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        calculateTextSize()

        when (wMode) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                var width = ((chartPadding * 2) + textAmountWidth + (textPadding * 2)).toInt()

                repeat((0..daysBetween).count()) {
                    width += (textDateWidth + textDatePadding).toInt()
                }

                val digit = log10(maxAmount.toDouble()).toInt()

                val digitNumber = (maxAmount / 10.0.pow(digit.toDouble())) + 1

                var height =
                    chartPadding.toInt() + shiftTextAmountY.toInt() + (digitNumber * digitSize).toInt()

                if (width > wSize) {
                    width = wSize
                }

                if (height > hSize) {
                    height = hSize
                }

                setMeasuredDimension(width, height)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawAxis(canvas)
        drawChart(canvas)
        drawTextAndSeparators(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return LineChartState(superState, dataSet)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val lineChartState = state as? LineChartState
        super.onRestoreInstanceState(lineChartState?.superState ?: state)

        dataSet = lineChartState?.dataList ?: mutableListOf()
    }

    fun setData(dataList: List<Expense>) {
        dataSet = mapToExpenseByDayModels(dataList)

        maxTime = dataSet.maxOf { it.localDate }
        maxAmount = dataSet.maxOf { it.amount }

        minTime = dataSet.minOf { it.localDate }
        minAmount = dataSet.minOf { it.amount }

        daysBetween = ChronoUnit.DAYS.between(minTime, maxTime)

        requestLayout()
    }

    /**
     * Приводим данные к формату количество трат в день
     */
    private fun mapToExpenseByDayModels(list: List<Expense>): List<ExpenseModel> {
        val expenses = mutableMapOf<LocalDate, Int>()
        val dataList = mutableListOf<ExpenseModel>()
        list.forEach { item ->
            if (expenses.keys.contains(item.localDate)) {
                val amountByDate = expenses[item.localDate] ?: 0
                expenses[item.localDate] = amountByDate + item.amount
            } else {
                expenses[item.localDate] = item.amount
            }
        }

        expenses.forEach { (localDate, expense) ->
            dataList.add(
                ExpenseModel(
                    amount = expense,
                    localDate = localDate
                )
            )
        }

        return dataList
    }

    private fun calculateTextSize() {
        val amountTextRectList = mutableListOf<Rect>()
        val dateTextRectList = mutableListOf<Rect>()

        dataSet.forEach { model ->
            amountTextRectList.add(getWidthOfAmountText(model.amount.toString(), textPaint))
            dateTextRectList.add(
                getWidthOfAmountText(
                    model.localDate.formatToString(DATE_PATTERN),
                    textPaint
                )
            )
        }

        textAmountWidth = amountTextRectList.maxOf { it.width() }.toFloat()
        textAmountHeight = amountTextRectList.maxOf { it.height() }.toFloat()

        textDateWidth = dateTextRectList.maxOf { it.width() }.toFloat()
        textDateHeight = dateTextRectList.maxOf { it.height() }.toFloat()

        shiftTextDateX = textDateWidth / 2
        shiftTextAmountY = textAmountHeight / 2
    }

    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun drawAxis(canvas: Canvas) {
        startChartX = chartPadding + textAmountWidth + textPadding
        startChartY = chartPadding + shiftTextAmountY

        axisXSize = width.toFloat() - startChartX - (chartPadding * 2)
        axisYSize = height.toFloat() - startChartY - (chartPadding * 2)

        endChartX = startChartX + axisXSize
        endChartY = startChartY + axisYSize

        canvas.drawLine(startChartX, startChartY, startChartX, endChartY, axisLinePaint)
        canvas.drawLine(startChartX, endChartY, endChartX, endChartY, axisLinePaint)
    }

    private fun drawChart(canvas: Canvas) {
        path.reset()

        var x: Float
        var y: Float

        dataSet.forEachIndexed { index, model ->
            x = startChartX + ((ChronoUnit.DAYS.between(
                model.localDate,
                minTime
            ) * axisXSize) / ChronoUnit.DAYS.between(maxTime, minTime))
            y = endChartY - (((model.amount) * axisYSize) / (maxAmount))

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, paint)
    }

    private fun drawTextAndSeparators(canvas: Canvas) {
        /* Отображение значений цены */
        var x: Float = startChartX - textAmountWidth - textPadding
        var y: Float

        var separatorStartX: Float
        var separatorStartY: Float

        var separatorEndX: Float
        var separatorEndY: Float

        dataSet.forEach { model ->
            y = (endChartY + shiftTextAmountY - (model.amount * axisYSize) / maxAmount)

            canvas.drawText(
                model.amount.toString(),
                x,
                y,
                textPaint
            )

            /* Отрисовка разделителей для цены */
            separatorStartX = x + textAmountWidth + textPadding
            separatorStartY = y - shiftTextAmountY

            separatorEndX = endChartX
            separatorEndY = separatorStartY

            canvas.drawLine(
                separatorStartX,
                separatorStartY,
                separatorEndX,
                separatorEndY,
                separatorsPaint
            )
        }

        /* Отображение значений даты */
        y = endChartY + textDateHeight + textPadding

        var dayLocalDate = minTime
        (0..daysBetween).forEach { index ->
            x = startChartX - shiftTextDateX + (ChronoUnit.DAYS.between(
                dayLocalDate,
                minTime
            ) * axisXSize) / ChronoUnit.DAYS.between(maxTime, minTime)

            canvas.drawText(
                dayLocalDate.formatToString(DATE_PATTERN),
                x,
                y,
                textPaint
            )

            dayLocalDate = dayLocalDate.plusDays(1)

            /* Отрисовка разделителей для даты */
            if (index > 0) {
                separatorStartX = x + shiftTextDateX
                separatorStartY = startChartY

                separatorEndX = separatorStartX
                separatorEndY = endChartY

                canvas.drawLine(
                    separatorStartX,
                    separatorStartY,
                    separatorEndX,
                    separatorEndY,
                    separatorsPaint
                )
            }
        }
    }

    private companion object {
        const val DATE_PATTERN = "dd.MM"
    }
}
