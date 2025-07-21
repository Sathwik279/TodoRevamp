package com.example.todorevamp.ui.todoList

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todorevamp.data.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todo: Todo,
    onEvent: (TodoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    isGoAiEnabled: Boolean = true
) {
    // Animation states
    val contentAlpha by animateFloatAsState(
        targetValue = if (todo.isDone) 0.5f else 1.0f,
        animationSpec = tween(300)
    )
    
    val cardColor by animateColorAsState(
        targetValue = if (todo.isDone) 
            Color(0xFFFDF2F8)  // Light pinkish for completed
        else Color(0xFFFFFAFD),  // Very light pinkish for active
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x25FF69B4),
                spotColor = Color(0x35FF69B4)
            )
            .clickable { onEvent(TodoListEvent.OnTodoClick(todo)) },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp  // We're using shadow instead
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onEvent(TodoListEvent.OnDoneChange(todo, !todo.isDone))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (todo.isDone) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = "Not completed",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Content column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                ) {
                    // Title row with goAi badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 21.sp,  // Increased from 16sp by 30%
                                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (todo.isDone) 
                                Color(0xFFCC8A9B)  // Muted pinkish for completed
                            else Color(0xFF1A1A1A),  // Dark text for readability
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // goAi badge and status
                        if (todo.isGoAiTagged) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (todo.enhancementStatus) {
                                    "pending" -> Color(0xFFFFF0F5)      // Light pink
                                    "processing" -> Color(0xFFFFE4E9)   // Soft pink
                                    "completed" -> Color(0xFFE8F8F0)    // Light green-pink
                                    "error" -> Color(0xFFFFE8E8)        // Light red-pink
                                    else -> Color(0xFFFDF2F8)           // Very light pink
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Psychology,
                                        contentDescription = "goAi",
                                        tint = when (todo.enhancementStatus) {
                                            "pending" -> Color(0xFFE91E63)   // Pink
                                            "processing" -> Color(0xFFAD1457) // Deep pink
                                            "completed" -> Color(0xFF4CAF50)  // Green
                                            "error" -> Color(0xFFE53935)     // Red
                                            else -> Color(0xFFBC477B)        // Muted pink
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (todo.enhancementStatus) {
                                            "pending" -> "AI"
                                            "processing" -> "AI"
                                            "completed" -> "AI+"
                                            "error" -> "AI!"
                                            else -> "AI"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (todo.enhancementStatus) {
                                            "pending" -> Color(0xFFE91E63)   // Pink
                                            "processing" -> Color(0xFFAD1457) // Deep pink
                                            "completed" -> Color(0xFF4CAF50)  // Green
                                            "error" -> Color(0xFFE53935)     // Red
                                            else -> Color(0xFFBC477B)        // Muted pink
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    if (todo.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 18.sp,  // Increased from 14sp by 30%
                                lineHeight = 23.sp,  // Increased from 18sp by 30%
                                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (todo.isDone) 
                                Color(0xFFCC8A9B)  // Muted pinkish for completed
                            else Color(0xFF616161),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Action buttons column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // goAi toggle button
                    if (isGoAiEnabled) {
                        IconButton(
                            onClick = { onEvent(TodoListEvent.OnGoAiToggle(todo)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (todo.isGoAiTagged) 
                                    Icons.Filled.Psychology 
                                else 
                                    Icons.Outlined.Psychology,
                                contentDescription = if (todo.isGoAiTagged) "Remove goAi" else "Add goAi",
                                tint = if (todo.isGoAiTagged) Color(0xFFE91E63) else Color(0xFFCC8A9B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { onEvent(TodoListEvent.OnDeleteTodoClick(todo)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFCC8A9B),  // Soft pinkish for delete
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}