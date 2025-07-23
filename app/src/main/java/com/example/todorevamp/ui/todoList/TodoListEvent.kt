package com.example.todorevamp.ui.todoList

import com.example.todorevamp.data.Todo

sealed class TodoListEvent {

    data class OnTodoClick(val todo: Todo): TodoListEvent()

    data class OnDoneChange(val todo:Todo, val isDone: Boolean): TodoListEvent()

    data class OnDeleteTodoClick(val todo: Todo): TodoListEvent()

    data class OnGoAiToggle(val todo: Todo): TodoListEvent()
    
    data class OnPinToggle(val todo: Todo): TodoListEvent()
    
    data class OnShowImages(val todo: Todo): TodoListEvent()
    
    data class OnExportToPdf(val todo: Todo): TodoListEvent()

    object OnUndoDeleteClick: TodoListEvent()
    object OnAddTodoClick: TodoListEvent()
    object OnLogoutClick: TodoListEvent()


}