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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.PushPin
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
            Color(0xFFF8F8FF).copy(alpha = 0.7f)  // Light lavender for completed
        else Color(0xFFFFFDF7),  // Warm white for active todos
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x40FF6B9D),  // Vibrant pink shadow
                spotColor = Color(0x60FF6B9D)      // Stronger pink spot shadow
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
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()  // Make height responsive to content
        ) {
            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
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
                            tint = Color(0xFF4CAF50),  // Vibrant green for completed
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = "Not completed",
                            tint = Color(0xFF9C27B0),  // Vibrant purple for unchecked
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content column - now takes full width without action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()  // Take full available width
                        .alpha(contentAlpha)
                ) {
                    // Title row with pin and goAi badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Pin icon (if pinned)
                        if (todo.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = Color(0xFFFF9800), // Orange for pin
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (todo.isDone)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // goAi badge and status
                        if (todo.isGoAiTagged) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (todo.enhancementStatus) {
                                    "pending" -> Color(0xFFFFF3E0)      // Warm amber
                                    "processing" -> Color(0xFFE3F2FD)   // Cool blue
                                    "completed" -> Color(0xFFE8F5E8)    // Fresh green
                                    "error" -> Color(0xFFFFEBEE)        // Soft red
                                    else -> Color(0xFFF3E5F5)           // Light purple
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = when (todo.enhancementStatus) {
                                        "pending" -> Color(0xFFFF9800)
                                        "processing" -> Color(0xFF2196F3)
                                        "completed" -> Color(0xFF4CAF50)
                                        "error" -> Color(0xFFF44336)
                                        else -> Color(0xFF9C27B0)
                                    }
                                ),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Psychology,
                                        contentDescription = "goAi",
                                        tint = when (todo.enhancementStatus) {
                                            "pending" -> Color(0xFFFF9800)      // Vibrant amber
                                            "processing" -> Color(0xFF2196F3)   // Vibrant blue
                                            "completed" -> Color(0xFF4CAF50)    // Vibrant green
                                            "error" -> Color(0xFFF44336)        // Vibrant red
                                            else -> Color(0xFF9C27B0)           // Vibrant purple
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (todo.enhancementStatus) {
                                            "pending" -> "AI"
                                            "processing" -> "AI⏳"
                                            "completed" -> "AI✓"
                                            "error" -> "AI⚠"
                                            else -> "AI"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = when (todo.enhancementStatus) {
                                            "pending" -> Color(0xFFFF9800)      // Vibrant amber
                                            "processing" -> Color(0xFF2196F3)   // Vibrant blue
                                            "completed" -> Color(0xFF4CAF50)    // Vibrant green
                                            "error" -> Color(0xFFF44336)        // Vibrant red
                                            else -> Color(0xFF9C27B0)           // Vibrant purple
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (todo.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))  // Reduced spacing
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (todo.isDone)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Tags display
                    if (todo.tags.isNotBlank()) {
                        val tagList = todo.tags.split(",").filter { it.isNotBlank() }
                        if (tagList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))  // Reduced spacing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tagList.take(3).forEach { tag -> // Show max 3 tags
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFE1F5FE), // Light blue
                                        border = BorderStroke(
                                            width = 0.5.dp,
                                            color = Color(0xFF0288D1) // Blue
                                        )
                                    ) {
                                        Text(
                                            text = "#${tag.trim()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp
                                            ),
                                            color = Color(0xFF0277BD), // Dark blue
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (tagList.size > 3) {
                                    Text(
                                        text = "+${tagList.size - 3}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp
                                        ),
                                        color = Color(0xFF0277BD).copy(alpha = 0.7f),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Enhanced content section - Expandable
                    if (todo.enhancedContent != null && todo.enhancementStatus == "completed") {
                        var isExpanded by remember { mutableStateOf(false) }
                        
                        Spacer(modifier = Modifier.height(6.dp))  // Reduced spacing
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E8),  // Light green background
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF4CAF50).copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Psychology,
                                            contentDescription = "AI Enhancement",
                                            tint = Color(0xFF4CAF50),  // Vibrant green
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "✨ Enhanced with AI",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF2E7D32)  // Darker green for text
                                        )
                                    }
                                    
                                    // Expand/Collapse indicator
                                    Text(
                                        text = if (isExpanded) "▼" else "▶",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(if (isExpanded) 8.dp else 4.dp))
                                
                                Text(
                                    text = todo.enhancedContent,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    color = Color(0xFF1B5E20),  // Dark green for content
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                                )
                                
                                if (!isExpanded && todo.enhancedContent.length > 100) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap to expand full AI response",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Action buttons row at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin toggle button
                IconButton(
                    onClick = { onEvent(TodoListEvent.OnPinToggle(todo)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (todo.isPinned) 
                            Icons.Filled.PushPin 
                        else 
                            Icons.Outlined.PushPin,
                        contentDescription = if (todo.isPinned) "Unpin" else "Pin",
                        tint = if (todo.isPinned) 
                            Color(0xFFFF9800)  // Orange for pinned
                        else Color(0xFF9E9E9E),  // Gray for unpinned
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Images button (show only if images exist)
                if (todo.imagePaths.isNotBlank()) {
                    val imageCount = todo.imagePaths.split(",").filter { it.isNotBlank() }.size
                    Box {
                        IconButton(
                            onClick = { onEvent(TodoListEvent.OnShowImages(todo)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Show Images",
                                tint = Color(0xFF2196F3),  // Blue for images
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Image count badge
                        if (imageCount > 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF5722), // Orange red
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = imageCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.wrapContentSize(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                
                // goAi toggle button
                if (isGoAiEnabled) {
                    IconButton(
                        onClick = { onEvent(TodoListEvent.OnGoAiToggle(todo)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (todo.isGoAiTagged) 
                                Icons.Filled.Psychology 
                            else 
                                Icons.Outlined.Psychology,
                            contentDescription = if (todo.isGoAiTagged) "Remove goAi" else "Add goAi",
                            tint = if (todo.isGoAiTagged) 
                                Color(0xFFE91E63)  // Vibrant pink for tagged
                            else Color(0xFF9C27B0),  // Purple for untagged
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // PDF Export button
                IconButton(
                    onClick = { onEvent(TodoListEvent.OnExportToPdf(todo)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export to PDF",
                        tint = Color(0xFF673AB7),  // Deep purple for PDF export
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = { onEvent(TodoListEvent.OnDeleteTodoClick(todo)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),  // Vibrant red for delete
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}