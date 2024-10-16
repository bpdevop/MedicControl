package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.MedicalAppointment
import com.bpdevop.mediccontrol.data.model.VisitType
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.viewmodels.AppointmentViewModel
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    onAppointmentClick: (MedicalAppointment) -> Unit,
) {
    // Estado de la cita del doctor desde el ViewModel
    val doctorAppointmentHistoryState by viewModel.doctorAppointmentHistoryState.collectAsState()

    // Estado para el calendario y el día seleccionado
    var selectedDate by remember { mutableStateOf(Date()) }

    // Estado de scroll para la lista
    val listState = rememberLazyListState()

    // Agrupar citas por fecha
    val groupedAppointments = when (doctorAppointmentHistoryState) {
        is UiState.Success -> {
            val appointments = (doctorAppointmentHistoryState as UiState.Success<List<MedicalAppointment>>).data
            appointments
                .sortedBy { it.date }
                .groupBy { it.date?.formatToString("EEEE, d MMMM") ?: "" }
        }

        else -> emptyMap()
    }

    // Llamada inicial para obtener las citas del día actual
    LaunchedEffect(Unit) {
        viewModel.getDoctorAppointmentHistory(selectedDate)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado del calendario
        CalendarHeader(
            selectedDate = selectedDate,
            onDaySelected = { date ->
                selectedDate = date
                viewModel.getDoctorAppointmentHistory(date) // Filtrar por el día seleccionado
            },
            onMonthChange = { newDate ->
                selectedDate = newDate
                viewModel.getDoctorAppointmentHistory(newDate) // Actualizar el mes también, si es necesario
            }
        )

        // Mostrar el estado de carga o error según el estado de UiState
        Box(modifier = Modifier.fillMaxSize()) {
            when (doctorAppointmentHistoryState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (doctorAppointmentHistoryState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is UiState.Success -> {
                    if (groupedAppointments.isEmpty()) {
                        EmptyAgendaMessage()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            groupedAppointments.forEach { (date, appointmentsForDate) ->
                                stickyHeader {
                                    DateHeader(date = date)
                                }
                                items(appointmentsForDate) { appointment ->
                                    AppointmentItem(
                                        appointment = appointment,
                                        onClick = { onAppointmentClick(appointment) }
                                    )
                                }
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: Date,
    onDaySelected: (Date) -> Unit,
    onMonthChange: (Date) -> Unit,
) {
    val calendar = Calendar.getInstance().apply {
        time = selectedDate
    }
    val selectedDay = calendar[Calendar.DAY_OF_MONTH]
    val selectedMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
    val selectedYear = calendar[Calendar.YEAR]

    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedDay) {
        lazyListState.scrollToItem(maxOf(0, selectedDay - 4))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botones para cambiar de mes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        calendar.add(Calendar.MONTH, -1)
                        onMonthChange(calendar.time)
                    }
            )

            Text(
                text = "$selectedMonth $selectedYear",
                style = MaterialTheme.typography.headlineMedium
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        calendar.add(Calendar.MONTH, 1)
                        onMonthChange(calendar.time)
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Días del mes
        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            items(daysInMonth) { day ->
                val date = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, day + 1)
                    set(Calendar.MONTH, calendar[Calendar.MONTH])
                    set(Calendar.YEAR, calendar[Calendar.YEAR])
                }.time

                val isSelected = selectedDay == day + 1

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { onDaySelected(date) }
                ) {
                    // Día de la semana
                    val dayOfWeek = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, day + 1)
                    }.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())?.uppercase()

                    Text(
                        text = dayOfWeek ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = (day + 1).toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: MedicalAppointment,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        // Nombre del paciente
        Text(
            text = appointment.patientName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fila que muestra el tipo de visita y la hora con iconos
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono del tipo de cita
                Icon(
                    imageVector = when (appointment.visitType) {
                        VisitType.NEW -> Icons.Default.Event
                        VisitType.FOLLOW_UP -> Icons.Default.Refresh
                        VisitType.EMERGENCY -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(
                        id = when (appointment.visitType) {
                            VisitType.NEW -> R.string.appointment_type_new
                            VisitType.FOLLOW_UP -> R.string.appointment_type_follow_up
                            VisitType.EMERGENCY -> R.string.appointment_type_emergency
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = appointment.time?.formatToString("hh:mm a") ?: stringResource(R.string.no_time_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Divider para separar los elementos visualmente
        HorizontalDivider()
    }
}


@Composable
fun EmptyAgendaMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.agenda_screen_no_appointments_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
