package com.bpdevop.mediccontrol.ui.screens.bloodpressure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.data.model.BloodPressure

@Composable
fun BloodPressureScreen(
    patientId: String,
    onSaveSuccess: () -> Unit,
    onEditBloodPressure: (BloodPressure) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        stringResource(id = R.string.blood_pressure_screen_new_record),
        stringResource(id = R.string.blood_pressure_screen_record_history)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(text = title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        when (selectedTab) {
            0 -> NewBloodPressureScreen(
                patientId = patientId,
                onBloodPressureAdded = onSaveSuccess
            )

            1 -> BloodPressureHistoryScreen(
                patientId = patientId,
                onEditBloodPressure = onEditBloodPressure
            )
        }
    }
}
