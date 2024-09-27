package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.bpdevop.mediccontrol.core.wheelpicker.WheelTextPicker

@Composable
fun BloodPressureWheelPicker(
    modifier: Modifier = Modifier,
    systolicRange: List<Int> = (90..180).toList(),
    diastolicRange: List<Int> = (60..120).toList(),
    pulseRange: List<Int> = (50..150).toList(),
    systolicValue: Int,
    diastolicValue: Int,
    pulseValue: Int,
    onSnappedBloodPressure: (systolic: Int, diastolic: Int, pulse: Int) -> Unit,
) {
    var snappedSystolic by remember { mutableIntStateOf(systolicValue) }
    var snappedDiastolic by remember { mutableIntStateOf(diastolicValue) }
    var snappedPulse by remember { mutableIntStateOf(pulseValue) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row {
            // Systolic
            WheelTextPicker(
                size = DpSize(100.dp, 150.dp),
                texts = systolicRange.map { it.toString() },
                rowCount = 3,
                startIndex = systolicRange.indexOf(systolicValue).takeIf { it in systolicRange.indices } ?: 0,
                onScrollFinished = { snappedIndex ->
                    if (snappedIndex in systolicRange.indices) {
                        snappedSystolic = systolicRange[snappedIndex]
                        onSnappedBloodPressure(snappedSystolic, snappedDiastolic, snappedPulse)
                    }
                    return@WheelTextPicker snappedIndex
                }
            )
            // Diastolic
            WheelTextPicker(
                size = DpSize(100.dp, 150.dp),
                texts = diastolicRange.map { it.toString() },
                rowCount = 3,
                startIndex = diastolicRange.indexOf(diastolicValue).takeIf { it in diastolicRange.indices } ?: 0,
                onScrollFinished = { snappedIndex ->
                    if (snappedIndex in diastolicRange.indices) {
                        snappedDiastolic = diastolicRange[snappedIndex]
                        onSnappedBloodPressure(snappedSystolic, snappedDiastolic, snappedPulse)
                    }
                    return@WheelTextPicker snappedIndex
                }
            )
            // Pulse
            WheelTextPicker(
                size = DpSize(100.dp, 150.dp),
                texts = pulseRange.map { it.toString() },
                rowCount = 3,
                startIndex = pulseRange.indexOf(pulseValue).takeIf { it in pulseRange.indices } ?: 0,
                onScrollFinished = { snappedIndex ->
                    if (snappedIndex in pulseRange.indices) {
                        snappedPulse = pulseRange[snappedIndex]
                        onSnappedBloodPressure(snappedSystolic, snappedDiastolic, snappedPulse)
                    }
                    return@WheelTextPicker snappedIndex
                }
            )
        }
    }
}