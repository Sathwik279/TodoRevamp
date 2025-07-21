package com.example.todorevamp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todorevamp.repository.AIAgentRepository
import com.example.todorevamp.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestResult(
    val query: String,
    val result: String,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AITestViewModel @Inject constructor(
    private val aiAgentRepository: AIAgentRepository
) : ViewModel() {
    
    private val _testResults = MutableStateFlow<List<TestResult>>(emptyList())
    val testResults = _testResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    fun testAIAgent(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = aiAgentRepository.enhanceNote(query)) {
                is ApiResult.Success -> {
                    addTestResult(
                        TestResult(
                            query = query,
                            result = result.data,
                            isSuccess = true
                        )
                    )
                }
                is ApiResult.Error -> {
                    addTestResult(
                        TestResult(
                            query = query,
                            result = "Error: ${result.message}",
                            isSuccess = false
                        )
                    )
                }
                is ApiResult.Loading -> {
                    // Handle loading state if needed
                }
            }
            
            _isLoading.value = false
        }
    }
    
    private fun addTestResult(result: TestResult) {
        _testResults.value = listOf(result) + _testResults.value
    }
    
    fun clearResults() {
        _testResults.value = emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITestScreen(
    viewModel: AITestViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val testResults by viewModel.testResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Agent Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Input section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Enter your query") },
                    placeholder = { Text("e.g., 'Plan a meeting with the team'") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            if (query.isNotBlank()) {
                                viewModel.testAIAgent(query)
                            }
                        },
                        enabled = !isLoading && query.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Test AI Agent")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.clearResults() },
                        enabled = testResults.isNotEmpty()
                    ) {
                        Text("Clear")
                    }
                }
                
                // Quick test buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            query = "Plan a meeting with the team"
                            viewModel.testAIAgent(query)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Test Meeting", maxLines = 1)
                    }
                    
                    Button(
                        onClick = { 
                            query = "current weather in Vijayawada"
                            viewModel.testAIAgent(query)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Test Weather", maxLines = 1)
                    }
                }
            }
        }
        
        // Results section
        if (testResults.isNotEmpty()) {
            Text(
                text = "Test Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testResults) { result ->
                    TestResultCard(result = result)
                }
            }
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccess) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.isSuccess) "✅ Success" else "❌ Failed",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(result.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "Query: ${result.query}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = result.result,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
