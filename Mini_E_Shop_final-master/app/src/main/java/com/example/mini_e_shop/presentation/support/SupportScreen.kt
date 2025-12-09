package com.example.mini_e_shop.presentation.support

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mini_e_shop.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Updated data class to support string resources
data class Message(
    val text: String? = null,
    @StringRes val textResId: Int? = null,
    val isFromUser: Boolean,
    val showAvatar: Boolean
) {
    init {
        require(text != null || textResId != null) { "Either text or textResId must be provided" }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: SupportViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            Message(textResId = R.string.support_greeting, isFromUser = false, showAvatar = true),
        )
    }
    val context = LocalContext.current
    val rememberedContext = remember { context }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Launcher to take a picture. The result is a Bitmap.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // TODO: Handle the bitmap image (e.g., display in chat, upload to server)
            Toast.makeText(rememberedContext, rememberedContext.getString(R.string.support_photo_taken), Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher to request permission.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, launch the camera
            cameraLauncher.launch()
        } else {
            // Permission denied
            Toast.makeText(rememberedContext, rememberedContext.getString(R.string.support_camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.support_chat_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToContact) {
                        Icon(Icons.Default.SupportAgent, contentDescription = stringResource(id = R.string.support_contact_icon))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets.statusBars // Sử dụng statusBars để tự động tính toán padding
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) {
                    MessageBubble(message = it)
                }
            }

            if (showEmojiPicker) {
                EmojiPicker {
                    messageText += it
                }
            }

            MessageInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        messages.add(Message(text = messageText, isFromUser = true, showAvatar = false))
                        messageText = ""
                        scope.launch {
                            delay(1000) // Add a 1-second delay
                            messages.add(Message(textResId = R.string.support_feature_in_development, isFromUser = false, showAvatar = true))
                        }
                    }
                },
                onCameraClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(rememberedContext, Manifest.permission.CAMERA) -> cameraLauncher.launch()
                        else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onEmojiClick = { showEmojiPicker = !showEmojiPicker }
            )
        }
    }
}

@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😊", "😂", "❤️", "👍", "🤔", "🙏", "🎉", "😍", "😢", "😡")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(emojis) {
            Text(
                text = it,
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onEmojiSelected(it) }
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser && message.showAvatar) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.support_avatar),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.Bottom)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (!message.isFromUser) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    if (message.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.dp)
                )
                .padding(12.dp)
                .weight(1f, fill = false)
        ) {
            val messageText = message.text ?: stringResource(id = message.textResId!!)
            Text(
                text = messageText,
                color = if (message.isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCameraClick: () -> Unit,
    onEmojiClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEmojiClick) {
            Icon(Icons.Outlined.SentimentSatisfied, contentDescription = stringResource(id = R.string.support_emoji_icon), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        IconButton(onClick = onCameraClick) {
            Icon(Icons.Default.CameraAlt, contentDescription = stringResource(id = R.string.support_attach_image_icon), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }

        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(id = R.string.support_message_placeholder)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        IconButton(onClick = onSendClick) {
            Icon(Icons.Default.Send, contentDescription = stringResource(id = R.string.support_send_icon), tint = MaterialTheme.colorScheme.primary)
        }
    }
}
