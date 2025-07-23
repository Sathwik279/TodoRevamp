package com.example.todorevamp.ui.todoList

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todorevamp.data.Todo
import com.example.todorevamp.data.TodoRepository
import com.example.todorevamp.goai.GoAiService
import com.example.todorevamp.repository.AuthRepository
import com.example.todorevamp.util.PdfExporter
import com.example.todorevamp.util.Routes
import com.example.todorevamp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val authRepository: AuthRepository,
    private val goAiService: GoAiService,
    @ApplicationContext private val context: Context
): ViewModel(){

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _todos = repository.getTodos()
    val todos = combine(_todos, _searchText) { todoList, searchText ->
        if (searchText.isBlank()) {
            todoList
        } else {
            // Search in title, description, and tags
            todoList.filter { todo ->
                todo.title.contains(searchText, ignoreCase = true) ||
                todo.description.contains(searchText, ignoreCase = true) ||
                todo.tags.contains(searchText, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var deleteTodo: Todo? = null // used to undo a delete

    // Image dialog state
    private val _showImageDialog = MutableStateFlow(false)
    val showImageDialog = _showImageDialog.asStateFlow()
    
    private val _selectedTodoImages = MutableStateFlow<List<String>>(emptyList())
    val selectedTodoImages = _selectedTodoImages.asStateFlow()

    // Get current user info
    fun getCurrentUser() = authRepository.getCurrentUser()

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun isGoAiEnabled(): Boolean = goAiService.isGoAiEnabled()
    
    fun dismissImageDialog() {
        _showImageDialog.value = false
        _selectedTodoImages.value = emptyList()
    }

    fun onEvent(event: TodoListEvent){
        when(event){
            is TodoListEvent.OnTodoClick ->{
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO+"?todoId=${event.todo.id}"))
            }
            is TodoListEvent.OnAddTodoClick->{
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO))
            }
            is TodoListEvent.OnUndoDeleteClick ->{
                // as there a chance of null access using ?
                deleteTodo?.let{todo->
                    viewModelScope.launch{
                        repository.insertTodo(todo)
                    }

                }
            }
            is TodoListEvent.OnDeleteTodoClick->{
                deleteTodo = event.todo // Store the deleted todo for undo
                viewModelScope.launch{
                    repository.deleteTodo(event.todo)
                }
                sendUiEvent(UiEvent.ShowSnackBar(
                    message = "Todo deleted",
                    action = "Undo"
                ))
            }
            is TodoListEvent.OnDoneChange ->{
                viewModelScope.launch{
                    repository.insertTodo(
                        event.todo.copy(
                            isDone = event.isDone // this isDone is parameter of the data class OnDoneChange
                        )
                    )
                }
            }
            is TodoListEvent.OnLogoutClick -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    sendUiEvent(UiEvent.Navigate(Routes.LOGIN))
                }
            }
            is TodoListEvent.OnGoAiToggle -> {
                viewModelScope.launch {
                    val updatedTodo = if (event.todo.isGoAiTagged) {
                        // Remove goAi tagging
                        event.todo.copy(
                            isGoAiTagged = false,
                            enhancementStatus = "none",
                            enhancedContent = null,
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else {
                        // Add goAi tagging
                        event.todo.copy(
                            isGoAiTagged = true,
                            enhancementStatus = "pending",
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    
                    repository.insertTodo(updatedTodo)
                    
                    // Schedule processing if tagged
                    if (!event.todo.isGoAiTagged) {
                        goAiService.scheduleProcessing()
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "Todo tagged for AI enhancement. Processing will start shortly...",
                            action = null
                        ))
                    }
                }
            }
            is TodoListEvent.OnPinToggle -> {
                viewModelScope.launch {
                    val updatedTodo = event.todo.copy(
                        isPinned = !event.todo.isPinned,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.insertTodo(updatedTodo)
                    
                    val message = if (updatedTodo.isPinned) "Todo pinned to top" else "Todo unpinned"
                    sendUiEvent(UiEvent.ShowSnackBar(
                        message = message,
                        action = null
                    ))
                }
            }
            is TodoListEvent.OnShowImages -> {
                val imageList = event.todo.imagePaths.split(",").filter { it.isNotBlank() }
                _selectedTodoImages.value = imageList
                _showImageDialog.value = true
            }
            is TodoListEvent.OnExportToPdf -> {
                viewModelScope.launch {
                    exportTodoToPdf(event.todo)
                }
            }
        }
    }

    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch{
            _uiEvent.send(event)
        }
    }
    
    private suspend fun exportTodoToPdf(todo: Todo) {
        try {
            val pdfExporter = PdfExporter(context)
            val pdfFile = pdfExporter.exportTodoToPdf(todo)
            
            if (pdfFile != null && pdfFile.exists()) {
                // Create share intent
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Todo: ${todo.title}")
                    putExtra(Intent.EXTRA_TEXT, "Here's your todo exported as PDF")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                val chooserIntent = Intent.createChooser(shareIntent, "Share Todo PDF")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
                
                sendUiEvent(UiEvent.ShowSnackBar(
                    message = "PDF exported and ready to share!",
                    action = null
                ))
            } else {
                sendUiEvent(UiEvent.ShowSnackBar(
                    message = "Failed to export PDF. Please try again.",
                    action = null
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendUiEvent(UiEvent.ShowSnackBar(
                message = "Error exporting PDF: ${e.message}",
                action = null
            ))
        }
    }

}