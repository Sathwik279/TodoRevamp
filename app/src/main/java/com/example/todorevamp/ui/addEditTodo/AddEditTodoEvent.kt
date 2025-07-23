package com.example.todorevamp.ui.addEditTodo

sealed class AddEditTodoEvent {
    data class OnTitleChange(val title: String): AddEditTodoEvent()
    data class OnDescriptionChange(val description: String): AddEditTodoEvent()
    data class OnTagsChange(val tags: String): AddEditTodoEvent()
    data class OnAddImage(val imagePath: String): AddEditTodoEvent()
    data class OnRemoveImage(val imagePath: String): AddEditTodoEvent()
    object OnSaveTodoClick: AddEditTodoEvent()
    object OnSelectImages: AddEditTodoEvent()
    object OnMergeAiContent: AddEditTodoEvent()
}