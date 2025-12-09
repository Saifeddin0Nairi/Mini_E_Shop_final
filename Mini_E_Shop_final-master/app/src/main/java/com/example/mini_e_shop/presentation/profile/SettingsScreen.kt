package com.example.mini_e_shop.presentation.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mini_e_shop.R
import com.example.mini_e_shop.data.local.entity.UserEntity
import android.app.Activity
import com.example.mini_e_shop.presentation.settings.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    currentUser: UserEntity?, // Current user
    onBack: () -> Unit,
    onNavigateToSupport: () -> Unit // Navigate to Support
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val rememberedContext = remember { context }
    val colors = MaterialTheme.colorScheme
    val surfaceColor = colors.surface
    val mutedTextColor = colors.onSurface.copy(alpha = 0.7f)
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEditInfoDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Update user info from currentUser into ViewModel when screen is created
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.updateUserInfo(currentUser.name, currentUser.email)
        }
    }
    // Listen event to recreate Activity
    LaunchedEffect(Unit) {
        viewModel.recreateActivityEvent.collectLatest {
            (rememberedContext as? Activity)?.recreate()
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = state.languageCode,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                viewModel.changeLanguage(languageCode)
            }
        )
    }

    if (showEditInfoDialog) {
        EditInfoDialog(
            currentName = state.name,
            currentEmail = state.email,
            onDismiss = { showEditInfoDialog = false },
            onSave = { name, email ->
                viewModel.updateUserInfo(name, email)
                showEditInfoDialog = false
                Toast.makeText(
                    rememberedContext,
                    rememberedContext.getString(R.string.update_info_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicyDialog = false })
    }

    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false },
            onNavigateToLiveChat = {
                showHelpDialog = false
                onNavigateToSupport()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.settings),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Account section
            SettingsSection(title = stringResource(id = R.string.account)) {
                SettingsTile(
                    icon = Icons.Outlined.Person,
                    iconColor = Color(0xFF3B82F6), // Blue
                    title = stringResource(id = R.string.personal_information),
                    value = state.name,
                    onClick = { showEditInfoDialog = true }
                )
            }

            // Application section
            SettingsSection(title = stringResource(id = R.string.application)) {
                // Notifications
                SettingsSwitchTile(
                    icon = Icons.Outlined.Notifications,
                    iconColor = Color(0xFFF97316), // Orange
                    title = stringResource(id = R.string.notifications),
                    checked = state.notificationsEnabled,
                    onCheckedChange = {
                        viewModel.toggleNotifications(it)
                        val message =
                            if (it) rememberedContext.getString(R.string.notifications_on_success)
                            else rememberedContext.getString(R.string.notifications_off_success)
                        Toast.makeText(rememberedContext, message, Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                // Language
                SettingsTile(
                    icon = Icons.Outlined.Language,
                    iconColor = Color(0xFF10B981), // Green
                    title = stringResource(id = R.string.language),
                    value = state.language,
                    onClick = { showLanguageDialog = true }
                )
                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                // Dark Mode
                SettingsSwitchTile(
                    icon = Icons.Outlined.DarkMode,
                    iconColor = Color(0xFF8B5CF6), // Purple
                    title = stringResource(id = R.string.dark_mode),
                    checked = state.darkModeEnabled,
                    onCheckedChange = {
                        viewModel.toggleDarkMode(it)
                        val message =
                            if (it) rememberedContext.getString(R.string.dark_mode_on_success)
                            else rememberedContext.getString(R.string.dark_mode_off_success)
                        Toast.makeText(rememberedContext, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Support section
            SettingsSection(title = stringResource(id = R.string.support)) {
                SettingsTile(
                    icon = Icons.Outlined.Security, // Shield
                    iconColor = Color(0xFFEF4444), // Red
                    title = stringResource(id = R.string.privacy_policy),
                    onClick = { showPrivacyPolicyDialog = true }
                )
                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                SettingsTile(
                    icon = Icons.Outlined.HelpOutline,
                    iconColor = Color(0xFF06B6D4), // Cyan
                    title = stringResource(id = R.string.help),
                    onClick = { showHelpDialog = true }
                )
                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                SettingsTile(
                    icon = Icons.Outlined.Info,
                    iconColor = Color(0xFF6B7280), // Gray
                    title = stringResource(id = R.string.about_shopmini),
                    value = "v1.0.0",
                    onClick = { /* TODO */ }
                )
            }

            // Footer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "ShopMini Version 1.0.0",
                    color = mutedTextColor,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun EditInfoDialog(
    currentName: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    val themeColors = MaterialTheme.colorScheme
    val mutedTextColor = themeColors.onSurface.copy(alpha = 0.7f)
    val surfaceColor = themeColors.surface

    DialogWithCloseButton(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Edit information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Your name", fontSize = 14.sp, color = mutedTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = themeColors.outline,
                    focusedBorderColor = themeColors.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Email", fontSize = 14.sp, color = mutedTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = themeColors.outline,
                    focusedBorderColor = themeColors.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = surfaceColor,
                        contentColor = themeColors.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(stringResource(id = R.string.cancel), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onSave(name, email) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(stringResource(id = R.string.save), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    FullScreenDialog(title = "Privacy Policy", onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Privacy commitment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "ShopMini is committed to protecting your personal information and ensuring your privacy is fully respected.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            PolicyItem(
                icon = Icons.Outlined.Description,
                iconColor = Color(0xFF3B82F6),
                title = "Information we collect",
                content = "We only collect necessary information such as your name, email, address, and phone number to process your orders."
            )

            PolicyItem(
                icon = Icons.Outlined.Lock,
                iconColor = Color(0xFF10B981),
                title = "Information security",
                content = "Your information is encrypted and stored securely. We use advanced security measures to protect your data."
            )

            PolicyItem(
                icon = Icons.Outlined.Visibility,
                iconColor = Color(0xFF8B5CF6),
                title = "How we use your information",
                content = "Your information is only used to process orders, deliver products, and provide customer support. We do not share it with third parties."
            )

            PolicyItem(
                icon = Icons.Outlined.Person,
                iconColor = Color(0xFFF97316),
                title = "Your rights",
                content = "You have the right to access, edit, or delete your personal information at any time through the Settings section."
            )
        }
    }
}

@Composable
fun PolicyItem(icon: ImageVector, iconColor: Color, title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    content,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDialog(
    onDismiss: () -> Unit,
    onNavigateToLiveChat: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val muted = colorScheme.onSurface.copy(alpha = 0.7f)
    val surface = colorScheme.surface

    FullScreenDialog(title = "Help", onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Contact us", fontWeight = FontWeight.SemiBold, color = muted)

            ContactItem(
                icon = Icons.Outlined.Phone,
                iconColor = Color(0xFF3B82F6),
                title = "Hotline",
                value = "1900 1234",
                showArrow = false // Hide arrow
            )

            ContactItem(
                icon = Icons.Outlined.Email,
                iconColor = Color(0xFF10B981),
                title = "Email",
                value = "support@shopmini.com",
                showArrow = false // Hide arrow
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToLiveChat) // Click to navigate
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Live Chat", color = muted, fontSize = 12.sp)
                        Text("Chat now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = muted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Working hours",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Monday - Friday: 8:00 - 22:00",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        "Saturday - Sunday: 9:00 - 21:00",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Frequently Asked Questions", fontWeight = FontWeight.SemiBold, color = muted)

            FAQItem(
                question = "How can I track my order?",
                answer = "You can track your orders by going to 'Profile' > 'My orders'. There you will see the status and details of all your orders."
            )
            FAQItem(
                question = "What is the return policy?",
                answer = "We accept returns within 30 days from the date you receive the product. Items must be unused, in original condition, and with full accessories."
            )
            FAQItem(
                question = "How long does delivery take?",
                answer = "Standard delivery time is 3–5 working days. For remote areas, delivery may take an additional 1–2 days."
            )
            FAQItem(
                question = "Can I cancel my order?",
                answer = "You can cancel your order within 24 hours after placing it. After that time, the order will be processed and cannot be cancelled."
            )
            FAQItem(
                question = "Which payment methods are supported?",
                answer = "We support cash on delivery (COD), credit/debit cards, and bank transfers."
            )
            FAQItem(
                question = "How can I update my account information?",
                answer = "Go to 'Profile' > 'Settings' > 'Personal information' to edit your name, email, and other details."
            )
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    showArrow: Boolean = true
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (showArrow) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded } // Click to expand/collapse
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, modifier = Modifier.weight(1f), fontSize = 15.sp)
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Expand/collapse animation
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DialogWithCloseButton(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                content()
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.select_language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                mapOf("vi" to "Vietnamese", "en" to "English").forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == currentLanguageCode),
                            onClick = { onLanguageSelected(code) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsTile(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SettingsSwitchTile(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (checked) stringResource(id = R.string.on) else stringResource(id = R.string.off),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 8.dp)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            ),
            modifier = Modifier.scale(0.8f) // Make switch slightly smaller to fit elegantly
        )
    }
}
