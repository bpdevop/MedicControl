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
fun OxygenSaturationWheelPicker(
    modifier: Modifier = Modifier,
    saturationRange: List<Int> = (80..100).toList(),
    pulseRange: List<Int> = (40..150).toList(),
    saturationValue: Int,
    pulseValue: Int,
    onSnappedOxygenSaturation: (saturation: Int, pulse: Int) -> Unit,
) {
    var snappedSaturation by remember { mutableIntStateOf(saturationValue) }
    var snappedPulse by remember { mutableIntStateOf(pulseValue) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row {
            // Oxygen Saturation
            WheelTextPicker(
                size = DpSize(100.dp, 150.dp),
                texts = saturationRange.map { it.toString() },
                rowCount = 3,
                startIndex = saturationRange.indexOf(saturationValue).takeIf { it in saturationRange.indices } ?: 0,
                onScrollFinished = { snappedIndex ->
                    if (snappedIndex in saturationRange.indices) {
                        snappedSaturation = saturationRange[snappedIndex]
                        onSnappedOxygenSaturation(snappedSaturation, snappedPulse)
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
                        onSnappedOxygenSaturation(snappedSaturation, snappedPulse)
                    }
                    return@WheelTextPicker snappedIndex
                }
            )
        }
    }
}
