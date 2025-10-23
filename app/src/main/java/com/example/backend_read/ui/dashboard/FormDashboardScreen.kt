package com.example.backend_read.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.backend_read.data.model.FilterState
import com.example.backend_read.data.model.Submission
import com.example.backend_read.data.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDashboardScreen(viewModel: FormDashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val availableUserIds by viewModel.availableUserIds.collectAsState()
    val availableCropTypes by viewModel.availableCropTypes.collectAsState()
    val availableCropStatuses by viewModel.availableCropStatuses.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Form Submissions") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            FilterControls(
                filterState = filterState,
                availableUserIds = availableUserIds,
                availableCropTypes = availableCropTypes,
                availableCropStatuses = availableCropStatuses,
                onFilterChange = viewModel::onFilterChange,
                onApply = viewModel::applyFilters,
                onClear = viewModel::clearFilters
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is UiState.Success -> SubmissionList(submissions = state.submissions)
                    is UiState.Error -> Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                    is UiState.Empty -> Text(text = "No submissions found.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun FilterControls(
    filterState: FilterState,
    availableUserIds: List<String>,
    availableCropTypes: List<String>,
    availableCropStatuses: List<String>,
    onFilterChange: (FilterState) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MultiSelectDropdown(
                label = "User ID",
                options = availableUserIds,
                selectedOptions = filterState.selectedUserIds,
                onSelectionChange = { onFilterChange(filterState.copy(selectedUserIds = it)) },
                modifier = Modifier.weight(1f)
            )
            MultiSelectDropdown(
                label = "Crop Type",
                options = availableCropTypes,
                selectedOptions = filterState.selectedCropTypes,
                onSelectionChange = { onFilterChange(filterState.copy(selectedCropTypes = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        MultiSelectDropdown(
            label = "Crop Status",
            options = availableCropStatuses,
            selectedOptions = filterState.selectedCropStatus,
            onSelectionChange = { onFilterChange(filterState.copy(selectedCropStatus = it)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filterState.startDate ?: "",
                onValueChange = { onFilterChange(filterState.copy(startDate = it)) },
                label = { Text("Start Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterState.endDate ?: "",
                onValueChange = { onFilterChange(filterState.copy(endDate = it)) },
                label = { Text("End Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                Text("Apply Filters")
            }
            Button(onClick = onClear, modifier = Modifier.weight(1f)) {
                Text("Clear")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(), // This is important
            readOnly = true,
            value = if (selectedOptions.isEmpty()) "All" else selectedOptions.joinToString(),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        val newSelection = if (selectedOptions.contains(option)) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = selectedOptions.contains(option),
                            onCheckedChange = null // Handled by parent onClick
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SubmissionList(submissions: List<Submission>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(submissions) { submission ->
            SubmissionItem(submission = submission)
            HorizontalDivider()
        }
    }
}

@Composable
fun SubmissionItem(submission: Submission) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }, // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (!submission.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = submission.imageUrl,
                    contentDescription = "Submission Photo for ${submission.cultivo}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize() // Animate size changes
            ) {
                Text(text = submission.cultivo, style = MaterialTheme.typography.titleLarge)
                Text(text = "Sown on: ${submission.fechaSiembra}", style = MaterialTheme.typography.bodySmall)
                Text(text = "User ID: ${submission.userId ?: "N/A"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow(label = "Phenological State:", value = submission.estadoFenologico)
                    DetailRow(label = "Foliage Density:", value = submission.densidadFollaje)
                    DetailRow(label = "Foliage Color:", value = submission.colorFollaje)
                    DetailRow(label = "Foliage State:", value = submission.estadoFollaje)

                    Spacer(modifier = Modifier.height(8.dp))

                    submission.humedad?.let { DetailRow(label = "Humidity:", value = "$it%" + (submission.metodoHumedad?.let { " ($it)" } ?: "")) }
                    submission.ph?.let { DetailRow(label = "pH:", value = "$it" + (submission.metodoPh?.let { " ($it)" } ?: "")) }
                    submission.alturaPlanta?.let { DetailRow(label = "Plant Height:", value = "${it}cm" + (submission.metodoAltura?.let { " ($it)" } ?: "")) }

                    Spacer(modifier = Modifier.height(8.dp))

                    submission.observaciones?.let {
                        Text(text = "Observations:", style = MaterialTheme.typography.titleMedium)
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    DetailRow(label = "Location:", value = submission.localizacion)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(140.dp)
            )
            Text(text = value)
        }
    }
}
