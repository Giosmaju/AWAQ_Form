package com.example.backend_read.ui.dashboard

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Form Submissions") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            FilterControls(
                filterState = filterState,
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
    onFilterChange: (FilterState) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = filterState.selectedUserId ?: "",
            onValueChange = { onFilterChange(filterState.copy(selectedUserId = it)) },
            label = { Text("User ID") },
            modifier = Modifier.fillMaxWidth()
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filterState.cropType ?: "",
                onValueChange = { onFilterChange(filterState.copy(cropType = it)) },
                label = { Text("Crop Type") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterState.cropStatus ?: "",
                onValueChange = { onFilterChange(filterState.copy(cropStatus = it)) },
                label = { Text("Crop Status") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = submission.cultivo, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Sown on: ${submission.fechaSiembra}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                submission.observaciones?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text = "User ID: ${submission.userId ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
