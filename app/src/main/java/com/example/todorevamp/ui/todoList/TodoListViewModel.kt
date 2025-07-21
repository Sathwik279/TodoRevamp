package com.example.todorevamp.ui.todoList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todorevamp.data.Todo
import com.example.todorevamp.data.TodoRepository
import com.example.todorevamp.goai.GoAiService
import com.example.todorevamp.repository.AuthRepository
import com.example.todorevamp.util.Routes
import com.example.todorevamp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val authRepository: AuthRepository,
    private val goAiService: GoAiService
): ViewModel(){

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _todos = repository.getTodos()
    val todos = combine(_todos, _searchText) { todoList, searchText ->
        if (searchText.isBlank()) {
            todoList
        } else {
            todoList.filter { todo ->
                todo.title.contains(searchText, ignoreCase = true) ||
                todo.description.contains(searchText, ignoreCase = true)
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

    // Get current user info
    fun getCurrentUser() = authRepository.getCurrentUser()

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun isGoAiEnabled(): Boolean = goAiService.isGoAiEnabled()

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
        }
    }

    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch{
            _uiEvent.send(event)
        }
    }

}