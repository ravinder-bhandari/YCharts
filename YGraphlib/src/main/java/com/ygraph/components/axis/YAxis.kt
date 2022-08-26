package com.ygraph.components.axis

import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ygraph.components.common.extensions.getTextHeight
import com.ygraph.components.common.extensions.getTextWidth

/**
 *
 * YAxis compose method used for drawing yAxis in any given graph.
 * @param modifier : All modifier related property.
 * @param axisData : All data needed to draw Yaxis.
 * @see com.ygraph.components.axis.AxisData Data class to save all params related to Yaxis
 */
@Composable
fun YAxis(modifier: Modifier, axisData: AxisData) {
    with(axisData) {
        var yAxisWidth by remember { mutableStateOf(0.dp) }
        val isRightAligned = yAxisPos == Gravity.RIGHT
        Column(modifier = modifier.clipToBounds()) {
            Canvas(
                modifier = modifier
                    .clipToBounds()
                    .width(yAxisWidth)
                    .background(backgroundColor)
            ) {
                val (yAxisHeight, reqYLabelsQuo, segmentHeight) = getAxisInitValues(
                    axisData,
                    size.height,
                    yBottomPadding.toPx(),
                    yTopPadding.toPx()
                )
                for (index in 0 until reqYLabelsQuo.toInt()) {
                    // Drawing the axis labels
                    yAxisWidth = drawAxisLabel(
                        index,
                        reqYLabelsQuo,
                        axisData,
                        yAxisWidth,
                        isRightAligned,
                        yAxisHeight,
                        segmentHeight
                    )
                    drawAxisLineWithPointers(
                        axisData,
                        index,
                        reqYLabelsQuo,
                        isRightAligned,
                        yAxisWidth,
                        yAxisHeight,
                        segmentHeight
                    )
                }
            }
        }
    }
}

fun getAxisInitValues(
    axisData: AxisData,
    canvasHeight: Float,
    bottomPadding: Float,
    topPadding: Float
): Triple<Float, Float, Float> = with(axisData) {
    val yAxisHeight = canvasHeight - bottomPadding
    var yMax = yMaxValue
    var reqYLabelsQuo =
        (yMaxValue / yStepValue) + 1 // Added one since it starts from 0
    val reqYLabelsRem = yMaxValue.rem(yStepValue)
    if (reqYLabelsRem > 0f) {
        reqYLabelsQuo += 1
        yMax = (yMaxValue - reqYLabelsRem) + yStepValue
    }
    // Minus the top padding to avoid cropping at the top
    val segmentHeight = (yAxisHeight - topPadding) / yMax
    Triple(yAxisHeight, reqYLabelsQuo, segmentHeight)
}


private fun DrawScope.drawAxisLineWithPointers(
    axisData: AxisData,
    i: Int,
    reqYLabelsQuo: Float,
    isRightAligned: Boolean,
    yAxisWidth: Dp,
    yAxisHeight: Float,
    segmentHeight: Float
) {
    with(axisData) {
        if (axisConfig.isAxisLineRequired) {
            // Draw line only until reqYLabelsQuo -1 else will be a extra line at top with no label
            if (i != (reqYLabelsQuo.toInt() - 1)) {
                //Draw Yaxis line
                drawLine(
                    start = Offset(
                        x = if (isRightAligned) 0.dp.toPx() else yAxisWidth.toPx(),
                        y = yAxisHeight - (segmentHeight * (i * yStepValue))
                    ),
                    end = Offset(
                        x = if (isRightAligned) 0.dp.toPx() else yAxisWidth.toPx(),
                        y = yAxisHeight - (segmentHeight * ((i + 1) * yStepValue))
                    ),
                    color = axisLineColor, strokeWidth = axisLineThickness.toPx()
                )
            }

            //Draw pointer lines on Yaxis
            drawLine(
                start = Offset(
                    x = if (isRightAligned) 0.dp.toPx() else {
                        yAxisWidth.toPx() - indicatorLineWidth.toPx()
                    },
                    y = yAxisHeight - (segmentHeight * (i * yStepValue))
                ),
                end = Offset(
                    x = if (isRightAligned) indicatorLineWidth.toPx() else yAxisWidth.toPx(),
                    y = yAxisHeight - (segmentHeight * (i * yStepValue))
                ),
                color = axisLineColor, strokeWidth = axisLineThickness.toPx()
            )
        }
    }
}


private fun DrawScope.drawAxisLabel(
    index: Int,
    reqYLabelsQuo: Float,
    axisData: AxisData,
    yAxisWidth: Dp,
    isRightAligned: Boolean,
    yAxisHeight: Float,
    segmentHeight: Float
): Dp = with(axisData) {
    var calculatedYAxisWidth = yAxisWidth
    val yAxisTextPaint = TextPaint().apply {
        textSize = axisLabelFontSize.toPx()
        color = axisLabelColor.toArgb()
        textAlign = if (isRightAligned) Paint.Align.RIGHT else Paint.Align.LEFT
        typeface = axisData.typeface
    }
    if (index != reqYLabelsQuo.toInt()) {
        val yAxisLabel = yLabelData(index)
        val measuredWidth = yAxisLabel.getTextWidth(yAxisTextPaint)
        val height: Int = yAxisLabel.getTextHeight(yAxisTextPaint)
        if (measuredWidth > calculatedYAxisWidth.toPx()) {
            val width =
                if (axisConfig.shouldEllipsizeAxisLabel) {
                    axisConfig.minTextWidthToEllipsize
                } else measuredWidth.toDp()
            calculatedYAxisWidth =
                width + yLabelAndAxisLinePadding + yAxisOffset
        }
        val ellipsizedText = TextUtils.ellipsize(
            yAxisLabel,
            yAxisTextPaint,
            axisConfig.minTextWidthToEllipsize.toPx(),
            axisConfig.ellipsizeAt
        )
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                if (axisConfig.shouldEllipsizeAxisLabel) ellipsizedText.toString() else yAxisLabel,
                if (isRightAligned) calculatedYAxisWidth.toPx() - yLabelAndAxisLinePadding.toPx() else {
                    yLabelAndAxisLinePadding.toPx()
                },
                yAxisHeight + height / 2 - ((segmentHeight * (index * yStepValue))),
                yAxisTextPaint
            )
        }
    }
    return calculatedYAxisWidth
}

@Preview(showBackground = true)
@Composable
fun YAxisPreview() {
    val axisData = AxisData.Builder()
        .yMaxValue(800f)
        .yStepValue(100f)
        .yBottomPadding(10.dp)
        .yAxisPos(Gravity.LEFT)
        .axisLabelFontSize(14.sp)
        .yLabelData { index -> index.toString() }
        .build()
    YAxis(modifier = Modifier.height(300.dp), axisData = axisData)
}
