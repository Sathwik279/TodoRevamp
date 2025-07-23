package com.example.todorevamp.ui.addEditTodo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todorevamp.data.Todo
import com.example.todorevamp.data.TodoRepository
import com.example.todorevamp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){
    // these are the guys which are visible to the user
    var todo by mutableStateOf<Todo?>(null)
        private set
    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var tags by mutableStateOf("")
        private set
    var imagePaths by mutableStateOf(listOf<String>())
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init{
        val todoId = savedStateHandle.get<Int>("todoId")
        if(todoId!=-1){
            viewModelScope.launch{
                repository.getTodoById(todoId!!)?.let{
                    todo->
                    title = todo.title
                    description = todo.description ?: ""
                    tags = todo.tags
                    imagePaths = if (todo.imagePaths.isNotBlank()) {
                        todo.imagePaths.split(",").filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }

                    this@AddEditTodoViewModel.todo = todo

                }
            }
        }
    }
    fun  onEvent(event: AddEditTodoEvent){
        when(event){
            is AddEditTodoEvent.OnTitleChange ->{
                title = event.title
            }
            is AddEditTodoEvent.OnDescriptionChange ->{
                description = event.description
            }
            is AddEditTodoEvent.OnTagsChange -> {
                tags = event.tags
            }
            is AddEditTodoEvent.OnAddImage -> {
                imagePaths = imagePaths + event.imagePath
            }
            is AddEditTodoEvent.OnRemoveImage -> {
                imagePaths = imagePaths.filter { it != event.imagePath }
            }
            is AddEditTodoEvent.OnSelectImages -> {
                // This will be handled by the UI directly with image picker
                // No action needed here as the UI will call OnAddImage when images are selected
            }
            is AddEditTodoEvent.OnMergeAiContent -> {
                todo?.let { currentTodo ->
                    if (currentTodo.enhancedContent != null && currentTodo.enhancementStatus == "completed") {
                        // Merge AI content into description
                        val mergedDescription = if (description.isBlank()) {
                            currentTodo.enhancedContent
                        } else {
                            "$description\n\n--- AI Enhanced Content ---\n${currentTodo.enhancedContent}"
                        }
                        description = mergedDescription
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "AI content merged successfully!",
                            action = null
                        ))
                    } else {
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "No AI content available to merge",
                            action = null
                        ))
                    }
                }
            }
            is AddEditTodoEvent.OnSaveTodoClick ->{
                viewModelScope.launch{
                    if(title.isBlank()){
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "The title can't be empty"
                        ))
                        return@launch
                    }
                    
                    repository.insertTodo(
                        Todo(
                            title = title,
                            description = description,
                            isDone = todo?.isDone ?: false,
                            isPinned = todo?.isPinned ?: false,
                            tags = tags,
                            imagePaths = imagePaths.joinToString(","),
                            isGoAiTagged = todo?.isGoAiTagged ?: false,
                            enhancementStatus = todo?.enhancementStatus ?: "none",
                            enhancedContent = todo?.enhancedContent,
                            lastUpdated = System.currentTimeMillis(),
                            id = todo?.id
                        )
                    )
                    sendUiEvent(UiEvent.PopBackStack)
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