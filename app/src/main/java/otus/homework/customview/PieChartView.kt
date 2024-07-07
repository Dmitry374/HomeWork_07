package otus.homework.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.sp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val circleRect = RectF()
    private var circleStrokeWidth: Float = 6.dp
    private var circleRadius: Float = 0F
    private var circlePadding: Float = 8.dp

    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F

    private var descriptionTextPain: TextPaint = TextPaint()
    private var amountTextPaint: TextPaint = TextPaint()

    private var textAmount: String = ""
    private var textAmountXNumber: Float = 0F
    private var textAmountYNumber: Float = 0F
    private var textAmountXDescription: Float = 0F
    private var textAmountYDescription: Float = 0F

    private var totalAmount: Int = 0
    private var pieChartColors: List<Int> = listOf()
    private var partsOfCircleList: List<PieChartModel> = listOf()

    private var animationSweepAngle: Int = 0

    private var dataList = listOf<CategoryExpenseModel>()

    var onCategoryClick: (String) -> Unit = {}

    init {
        var textAmountSize: Float = 22.sp
        var textAmountColor: Int = Color.WHITE

        var textDescriptionSize: Float = 14.sp
        var textDescriptionColor: Int = Color.GRAY

        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)

            val colorResId = typeArray.getResourceId(R.styleable.PieChartView_pieChartColors, 0)
            pieChartColors = typeArray.resources.getIntArray(colorResId).toList()

            circleStrokeWidth = typeArray.getDimension(
                R.styleable.PieChartView_pieChartCircleStrokeWidth,
                circleStrokeWidth
            )
            circlePadding = typeArray.getDimension(
                R.styleable.PieChartView_pieChartCirclePadding,
                circlePadding
            )

            textAmountSize = typeArray.getDimension(
                R.styleable.PieChartView_pieChartTextAmountSize,
                textAmountSize
            )
            textAmountColor = typeArray.getColor(
                R.styleable.PieChartView_pieChartTextAmountColor,
                textAmountColor
            )
            textAmount = typeArray.getString(R.styleable.PieChartView_pieChartTextAmount) ?: ""

            textDescriptionSize = typeArray.getDimension(
                R.styleable.PieChartView_pieChartTextDescriptionSize,
                textDescriptionSize
            )
            textDescriptionColor = typeArray.getColor(
                R.styleable.PieChartView_pieChartTextDescriptionColor,
                textDescriptionColor
            )

            typeArray.recycle()
        }

        circlePadding += circleStrokeWidth

        initTextPaint(amountTextPaint, textAmountSize, textAmountColor)
        initTextPaint(descriptionTextPain, textDescriptionSize, textDescriptionColor)

        startAnimation()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = when (wMode) {
            MeasureSpec.UNSPECIFIED -> DEFAULT_VIEW_SIZE.dp.toInt()
            MeasureSpec.EXACTLY -> {
                val size = min(wSize, hSize)
                if (size > DEFAULT_VIEW_SIZE.dp.toInt()) {
                    size
                } else {
                    DEFAULT_VIEW_SIZE.dp.toInt()
                }
            }

            MeasureSpec.AT_MOST -> min(wSize, hSize)

            else -> DEFAULT_VIEW_SIZE.dp.toInt()
        }

        calculateCircleRadius(size, size)

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawCircle(canvas)
        drawText(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val angleTheta =
                atan2((event.y - circleCenterY).toDouble(), (event.x - circleCenterX).toDouble())
            val angle =
                (if (angleTheta > 0) angleTheta else (2 * Math.PI + angleTheta)) * 360 / (2 * Math.PI)

            val length = sqrt((event.x - circleCenterX).pow(2) + (event.y - circleCenterY).pow(2))

            val isInCircleArea =
                circleRadius + (circleStrokeWidth / 2) > length && length > circleRadius - (circleStrokeWidth / 2)

            if (isInCircleArea) {
                partsOfCircleList.find { angle in it.startAtCircleDegree..it.startAtCircleDegree + it.amountOfCircle }
                    ?.let { model ->
                        onCategoryClick.invoke(model.category)
                    }
            }
        }

        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartState(superState, dataList)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val analyticalPieChartState = state as? PieChartState
        super.onRestoreInstanceState(analyticalPieChartState?.superState ?: state)

        dataList = analyticalPieChartState?.dataList ?: mutableListOf()
    }

    fun setData(list: List<CategoryExpenseModel>) {
        this.dataList = list
        calculateCircleParts()
    }

    private fun startAnimation() {
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 1500
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                animationSweepAngle = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }

    private fun drawCircle(canvas: Canvas) {
        for (percent in partsOfCircleList) {
            if (animationSweepAngle > percent.startAtCircleDegree + percent.amountOfCircle) {
                canvas.drawArc(
                    circleRect,
                    percent.startAtCircleDegree,
                    percent.amountOfCircle,
                    false,
                    percent.paint
                )
            } else if (animationSweepAngle > percent.startAtCircleDegree) {
                canvas.drawArc(
                    circleRect,
                    percent.startAtCircleDegree,
                    animationSweepAngle - percent.startAtCircleDegree,
                    false,
                    percent.paint
                )
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        canvas.drawText(
            totalAmount.toString(),
            textAmountXNumber,
            textAmountYNumber,
            amountTextPaint
        )
        canvas.drawText(
            textAmount,
            textAmountXDescription,
            textAmountYDescription,
            descriptionTextPain
        )
    }

    private fun calculateCircleParts() {
        totalAmount = dataList.fold(0) { res, value -> res + value.expenseAmount }

        var startAt = 0F

        partsOfCircleList = dataList.mapIndexed { index, model ->
            var circleAmount = model.expenseAmount * 100 / totalAmount.toFloat()
            circleAmount = if (circleAmount < 0F) 0F else circleAmount

            val resultModel = PieChartModel(
                startAtCircleDegree = calculateStartAtCircleDegree(startAt),
                amountOfCircle = calculateAmountOfCircle(circleAmount),
                paint = getDiagramPaint(index),
                category = model.category
            )
            if (circleAmount != 0F) startAt += circleAmount
            resultModel
        }
    }

    private fun calculateStartAtCircleDegree(percent: Float): Float {
        if (percent < 0 || percent > 100) {
            return 0F
        }

        return 360 * percent / 100
    }

    private fun calculateAmountOfCircle(percent: Float): Float {
        if (percent < 0 || percent > 100) {
            return 100F
        }

        return 360 * percent / 100
    }

    private fun getDiagramPaint(index: Int): Paint {
        return Paint().apply {
            color = pieChartColors[index % pieChartColors.size]
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = circleStrokeWidth
            isDither = true
        }
    }

    private fun initTextPaint(textPaint: TextPaint, textPaintSize: Float, textColor: Int) {
        textPaint.apply {
            color = textColor
            textSize = textPaintSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun calculateCircleRadius(width: Int, height: Int) {
        circleRadius = width.toFloat() / 2 - circlePadding

        with(circleRect) {
            left = circlePadding
            top = circlePadding
            right = circleRadius * 2 + circlePadding
            bottom = height / 2 + circleRadius
        }

        circleCenterX = (circleRadius * 2 + circlePadding * 2) / 2
        circleCenterY = (circleRadius * 2 + circlePadding * 2) / 2

        textAmountYNumber = circleCenterY

        val sizeTextAmountNumber = getWidthOfAmountText(
            totalAmount.toString(),
            amountTextPaint
        )

        textAmountXNumber = circleCenterX - sizeTextAmountNumber.width() / 2
        textAmountXDescription =
            circleCenterX - getWidthOfAmountText(textAmount, descriptionTextPain).width() / 2
        textAmountYDescription = circleCenterY + sizeTextAmountNumber.height()
    }

    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private companion object {
        const val DEFAULT_VIEW_SIZE = 250
    }
}
