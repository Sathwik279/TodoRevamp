package com.example.todorevamp.ui.todoList

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.todorevamp.R
import com.example.todorevamp.data.Todo
import com.example.todorevamp.util.Routes
import com.example.todorevamp.util.UiEvent
import kotlinx.coroutines.launch

@Composable
fun TodoListScreen(
    onNavigate: (UiEvent.Navigate)->Unit,
    viewModel: TodoListViewModel = hiltViewModel()
){
    val todos = viewModel.todos.collectAsState()
    val searchText = viewModel.searchText.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect{event->
            when(event){
                is UiEvent.ShowSnackBar->{
                    // Launch snackbar in separate coroutine to not block UI
                    scope.launch {
                        val duration = if (event.action != null) {
                            // For actions like "Undo", keep it visible longer
                            SnackbarDuration.Long
                        } else {
                            // For informational messages, use short duration
                            SnackbarDuration.Short
                        }
                        
                        val result = scaffoldState.snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.action,
                            duration = duration
                        )
                        if(result == SnackbarResult.ActionPerformed){
                            viewModel.onEvent(TodoListEvent.OnUndoDeleteClick)
                        }
                    }
                }
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }

    }

    androidx.compose.material.ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = viewModel.getCurrentUser(),
                onLogoutClick = { 
                    scope.launch { drawerState.close() }
                    viewModel.onEvent(TodoListEvent.OnLogoutClick) 
                },
                onSettingsClick = { 
                    scope.launch { drawerState.close() }
                    /* TODO: Implement settings */ 
                },
                onBuyMeCoffeeClick = { 
                    scope.launch { drawerState.close() }
                    /* TODO: Implement buy me coffee */ 
                },
                onAITestClick = {
                    scope.launch { drawerState.close() }
                    onNavigate(UiEvent.Navigate(Routes.AI_TEST))
                }
            )
        }
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Todo Revamp") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        // Profile Picture
                        val currentUser = viewModel.getCurrentUser()
                        currentUser?.photoUrl?.let { photoUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    },
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            // Fallback icon if no profile picture
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    viewModel.onEvent(TodoListEvent.OnAddTodoClick)
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            },
            snackbarHost = { hostState ->
                SnackbarHost(
                    hostState = hostState,
                    modifier = Modifier.padding(bottom = 80.dp)
                )
            }
        ){
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { viewModel.updateSearchText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search todos...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                )
            )
            
            // Todo List
            if (todos.value.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchText.value.isNotEmpty()) 
                                "No todos found for \"${searchText.value}\"" 
                            else "No todos yet. Create your first todo!",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ){
                    items(todos.value){todo->
                        TodoItem(
                            todo = todo,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.fillMaxWidth(),
                            isGoAiEnabled = viewModel.isGoAiEnabled()
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun DrawerContent(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBuyMeCoffeeClick: () -> Unit,
    onAITestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with user info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            currentUser?.photoUrl?.let { photoUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = currentUser?.displayName ?: "User",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Divider(modifier = Modifier.padding(bottom = 16.dp))
        
        // App Info
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.app_version),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Menu Items
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            text = stringResource(R.string.settings),
            onClick = onSettingsClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DrawerMenuItem(
            icon = Icons.Default.Search,  // Using Search icon for AI test
            text = "AI Agent Test",
            onClick = onAITestClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DrawerMenuItem(
            icon = Icons.Default.Coffee,
            text = stringResource(R.string.buy_me_coffee),
            onClick = onBuyMeCoffeeClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout at bottom
        Divider(modifier = Modifier.padding(bottom = 16.dp))
        
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Logout",
            onClick = onLogoutClick,
            textColor = MaterialTheme.colors.error
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = textColor
        )
    }
}