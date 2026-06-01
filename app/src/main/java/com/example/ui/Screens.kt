package com.example.ui

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.data.JournalEntry
import com.example.data.MoodLog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// --- ROOT ROUTING & NAVIGATION BAR SETUP ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalHealthAppContent(viewModel: MentalHealthViewModel) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    var currentRoute by remember { mutableStateOf("home") }

    if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val items = listOf(
                        Triple("home", "Dashboard", Icons.Default.Home),
                        Triple("journal", "Journals", Icons.Default.DateRange),
                        Triple("chat", "AI Chat", Icons.Default.Send),
                        Triple("toolkit", "Coping", Icons.Default.Favorite),
                        Triple("profile", "Profile", Icons.Default.Person)
                    )
                    items.forEach { (route, label, icon) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            modifier = Modifier.testTag("nav_btn_$route"),
                            onClick = { currentRoute = route },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val routeList = listOf("home", "journal", "chat", "toolkit", "profile")
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = {
                        val initialIndex = routeList.indexOf(initialState)
                        val targetIndex = routeList.indexOf(targetState)
                        if (targetIndex > initialIndex) {
                            (slideInHorizontally { width -> width / 5 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(slideOutHorizontally { width -> -width / 5 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                        } else {
                            (slideInHorizontally { width -> -width / 5 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(slideOutHorizontally { width -> width / 5 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                        }
                    },
                    label = "screen_transition"
                ) { route ->
                    when (route) {
                        "home" -> HomeScreen(viewModel = viewModel, onNavigate = { currentRoute = it })
                        "journal" -> JournalScreen(viewModel = viewModel)
                        "chat" -> ChatScreen(viewModel = viewModel)
                        "toolkit" -> ToolkitScreen(viewModel = viewModel)
                        "profile" -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// --- AUTHENTICATION SCREEN ---

@Composable
fun AuthScreen(viewModel: MentalHealthViewModel) {
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    var isLoginTab by remember { mutableStateOf(true) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSlateBlue, MutedNavy)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .gradientBorder(
                    colors = listOf(CalmingTeal, SoothingIndigo),
                    cornerRadius = 24.dp
                )
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MutedNavy.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heart icon represent logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(CalmingTeal.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "App Logo",
                        tint = CalmingTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoginTab) "Welcome Back" else "Begin Your Journey",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CaringIvory,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isLoginTab) "Step into a quiet, secure space." else "Take your first secure step to mental clarity.",
                    fontSize = 13.sp,
                    color = CaringIvory.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Tab Switcher with animated color states
                val loginTabBgColor by animateColorAsState(targetValue = if (isLoginTab) CalmingTeal else Color.Transparent, label = "login_tab")
                val registerTabBgColor by animateColorAsState(targetValue = if (!isLoginTab) CalmingTeal else Color.Transparent, label = "register_tab")
                val loginTabTextColor by animateColorAsState(targetValue = if (isLoginTab) DeepSlateBlue else CaringIvory.copy(alpha = 0.7f), label = "login_text")
                val registerTabTextColor by animateColorAsState(targetValue = if (!isLoginTab) DeepSlateBlue else CaringIvory.copy(alpha = 0.7f), label = "register_text")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepSlateBlue)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(loginTabBgColor)
                            .clickable { isLoginTab = true }
                            .testTag("login_tab_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            color = loginTabTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(registerTabBgColor)
                            .clickable { isLoginTab = false }
                            .testTag("register_tab_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            color = registerTabTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(
                    visible = !isLoginTab,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = CalmingTeal) },
                            colors = authTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = CalmingTeal) },
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Security") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = CalmingTeal) },
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                authError?.let { err ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = err,
                        color = WarningCoral,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isLoginTab) {
                            viewModel.login(email, password, onSuccess = {})
                        } else {
                            viewModel.register(name, email, password, onSuccess = {})
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalmingTeal,
                        contentColor = DeepSlateBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLoginTab) "Log In Safely" else "Create Secure Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// --- DASHBOARD / HOME SCREEN ---

@Composable
fun HomeScreen(viewModel: MentalHealthViewModel, onNavigate: (String) -> Unit) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val moodsList by viewModel.allMoodLogsAsc.collectAsStateWithLifecycle()
    var selectedMoodText by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Friendly visual welcome banner in Elegant Dark theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOOD MOMENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElegantMutedText,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Hello, ${currentUser?.name ?: "Reflective One"}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                // Material 3 style profile indicator circle from theme spec
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElegantCompanionBg)
                        .border(1.dp, ElegantBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Tactile emoji interactive mood logger in Elegant Dark
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantBorder, RoundedCornerShape(24.dp))
                    .testTag("mood_logger_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "How are you feeling right now?",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = ElegantSecondaryPlum
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val moods = listOf(
                        Triple("SAD", "😔", WarningCoral),
                        Triple("ANXIOUS", "😕", SoftOrange),
                        Triple("STRESSED", "😐", ElegantSecondaryPlum),
                        Triple("CALM", "🙂", CalmingTeal),
                        Triple("HAPPY", "✨", CalmingTeal)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        moods.forEach { (type, emoji, color) ->
                            val isSelected = selectedMoodText == type
                            val animatedScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.3f else 1.0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "mood_emoji_scale"
                            )
                            val animatedBgAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 0.15f else 0.0f,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                label = "mood_bg_alpha"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (animatedBgAlpha > 0f) color.copy(alpha = animatedBgAlpha) else Color.Transparent)
                                    .clickable { selectedMoodText = type }
                                    .padding(8.dp)
                                    .testTag("mood_btn_$type")
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 28.sp,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = type.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else ElegantMutedText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = selectedMoodText.isNotEmpty(),
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = noteInput,
                                onValueChange = { noteInput = it },
                                label = { Text("Add optional feeling notes...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mood_note_field"),
                                maxLines = 1,
                                colors = authTextFieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val score = when (selectedMoodText) {
                                        "HAPPY" -> 5
                                        "CALM" -> 4
                                        "STRESSED" -> 3
                                        "ANXIOUS" -> 2
                                        "SAD" -> 1
                                        else -> 3
                                    }
                                    viewModel.logMood(selectedMoodText, score, noteInput)
                                    selectedMoodText = ""
                                    noteInput = ""
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("save_mood_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoothingIndigo,
                                    contentColor = ElegantSoftLavender
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Entry", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Custom canvas visual chart showing Mood Trends in Elegant Dark
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantBorder, RoundedCornerShape(24.dp))
                    .testTag("trends_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Heart-Rate Trends",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = ElegantSecondaryPlum
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Visual Guide",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (moodsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Your trends will chart here.\nTap your current feeling emoji to log!",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = ElegantMutedText
                            )
                        }
                    } else {
                        // Drawing smoothed line graph
                        MoodTrendCanvas(moodLogs = moodsList.takeLast(7))
                    }
                }
            }
        }

        // Companion breathing insight panel styled exactly like Companion Insight from HTML
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantCompanionBorder, RoundedCornerShape(24.dp))
                    .testTag("conversational_insight_guide"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantCompanionBg)
            ) {
                Column(
                    modifier = Modifier
                        .clickable { onNavigate("toolkit") }
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                color = ElegantPlumDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Companion Insight",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = ElegantLightIvory
                            )
                            Text(
                                text = "Breathe & Refocus Action",
                                fontSize = 10.sp,
                                color = ElegantMutedText
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "\"I noticed you've been focused on work goals lately. Remember to take five deep breaths between tasks today with Serene's box-lung breathing loop.\"",
                        fontSize = 13.sp,
                        color = ElegantLightIvory,
                        fontWeight = FontWeight.Light,
                        lineHeight = 18.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { onNavigate("toolkit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoothingIndigo,
                            contentColor = ElegantSoftLavender
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Open Breathing Engine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Custom canvas-drawn smoothed line-graph
@Composable
fun MoodTrendCanvas(moodLogs: List<MoodLog>) {
    val linePrimaryColor = MaterialTheme.colorScheme.primary
    val lineAccentColor = SoothingIndigo

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val totalPoints = moodLogs.size
        val xInterval = if (totalPoints > 1) width / (totalPoints - 1) else width

        val points = moodLogs.mapIndexed { index, moodLog ->
            val x = index * xInterval
            // Map scores 1..5 to height coordinates from highest on screen (=5) to lowest (=1)
            val scoreNormalized = (moodLog.score - 1) / 4f // 0f to 1f
            val y = height - (scoreNormalized * height)
            Offset(x, y)
        }

        // Draw guideline grids
        for (i in 0..4) {
            val gridY = height * (i / 4f)
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Create Path for line
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        // Control points for smooth bezier cubic curve
                        val controlX = (p1.x + p2.x) / 2
                        cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }
                } else {
                    lineTo(points.first().x, points.first().y)
                }
            }
        }

        // Draw Path with gradient stroke
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(colors = listOf(linePrimaryColor, lineAccentColor)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw point dots
        points.forEachIndexed { idx, point ->
            val baseColor = if (idx == points.lastIndex) lineAccentColor else linePrimaryColor
            drawCircle(
                color = baseColor.copy(alpha = 0.25f),
                radius = 7.dp.toPx(),
                center = point
            )
            drawCircle(
                color = baseColor,
                radius = 3.5.dp.toPx(),
                center = point
            )
        }
    }
}

// --- JOURNAL REFLECTIONS WRITING PORTAL ---

@Composable
fun JournalScreen(viewModel: MentalHealthViewModel) {
    val journalsList by viewModel.allJournals.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingJournal.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastSentimentResult.collectAsStateWithLifecycle()

    var journalTitle by remember { mutableStateOf("") }
    var journalContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Secure Wellness Journals",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Unload thoughts. Analyze sentiments to receive AI coping mechanisms securely.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Compose Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantBorder, RoundedCornerShape(24.dp))
                    .testTag("journal_draft_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = journalTitle,
                        onValueChange = { journalTitle = it },
                        label = { Text("Reflection Title (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_title_input"),
                        colors = authTextFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = journalContent,
                        onValueChange = { journalContent = it },
                        label = { Text("What thoughts are racing today?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("journal_content_input"),
                        colors = authTextFieldColors(),
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterStart),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Evaluating mental posture. Please wait...",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Button(
                                onClick = {
                                    viewModel.addJournalEntry(journalTitle, journalContent) {
                                        journalTitle = ""
                                        journalContent = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_journal_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                enabled = journalContent.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, null, tint = DeepSlateBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Evaluate Thoughts with AI",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepSlateBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Show Instant AI analysis outcomes with smooth slide-down entrance
        item {
            AnimatedVisibility(
                visible = lastResult != null,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                lastResult?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gradientBorder(colors = listOf(CalmingTeal, SoothingIndigo), cornerRadius = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MutedNavy)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🧘", fontSize = 24.sp)
                                Text(
                                    text = "Active Emotion Detected: ${result.sentiment}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = CaringIvory
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result.tip,
                                fontSize = 13.sp,
                                color = CaringIvory.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Historical Journal Scroll Log
        item {
            Text(
                text = "Past Reflections Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (journalsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElegantBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Your journal archive is empty.\nWrite your thoughts above and press save.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(journalsList, key = { it.id }) { item ->
                JournalReflectionRow(entry = item, onDelete = { viewModel.deleteJournalEntry(item) })
            }
        }
    }
}

@Composable
fun JournalReflectionRow(entry: JournalEntry, onDelete: () -> Unit) {
    val dateText = DateUtils.getRelativeTimeSpanString(
        entry.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(entry.id) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(animationSpec = tween(200)),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElegantBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateText.toString(),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sentimentColor = when (entry.sentiment.uppercase()) {
                            "JOYFUL", "PEACEFUL" -> CalmSage
                            "ANXIOUS" -> WarningCoral
                            "STRESSED" -> SoftOrange
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(sentimentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = entry.sentiment,
                                color = sentimentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = WarningCoral,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = entry.content,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                if (entry.copingTip.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🧘", fontSize = 13.sp)
                            Text(
                                text = "Serene's Gentle Suggestions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = entry.copingTip,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- EXQUISITE AI CHAT INTERFACE SCREEN ---

@Composable
fun ChatScreen(viewModel: MentalHealthViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Serene AI Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Compassionate wellness listening.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { viewModel.clearChatHistory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningCoral.copy(alpha = 0.15f),
                            contentColor = WarningCoral
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Thread", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { insidePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insidePadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen message lists
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        IntroCompanionCard()
                    }
                } else {
                    items(messages) { msg ->
                        ChatBubbleRow(msg = msg)
                    }
                }

                if (isLoading) {
                    item {
                        TypingIndicatorBubble()
                    }
                }
            }

            // Composer container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        placeholder = { Text("What concerns are hovering today?") },
                        colors = authTextFieldColors(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotEmpty()) {
                                viewModel.sendChatMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("chat_send_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text",
                            tint = DeepSlateBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntroCompanionCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(CalmingTeal.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Serene",
                tint = CalmingTeal,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Calm Dialogue",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
        )

        Text(
            text = "Serene acts as a secure container for safe expression. Ask questions, log anxiety triggers, or walk through mindfulness guides here in strict confidentiality.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )
    }
}

@Composable
fun ChatBubbleRow(msg: ChatMessage) {
    val isUser = msg.sender == "USER"
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(msg.id) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(animationSpec = tween(150)),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .wrapContentWidth(align = if (isUser) Alignment.End else Alignment.Start)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) SoothingIndigo else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg.message,
                    color = if (isUser) CaringIvory else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "Dots Pulsing")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Dot 1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Dot 2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Dot 3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).graphicsLayer { alpha = dotAlpha1 }.background(CalmingTeal, CircleShape))
            Box(modifier = Modifier.size(6.dp).graphicsLayer { alpha = dotAlpha2 }.background(CalmingTeal, CircleShape))
            Box(modifier = Modifier.size(6.dp).graphicsLayer { alpha = dotAlpha3 }.background(CalmingTeal, CircleShape))
        }
    }
}

// --- COPING TOOLKIT SCREEN & BREATHING COMPOSITION ---

@Composable
fun ToolkitScreen(viewModel: MentalHealthViewModel) {
    val breathingState by viewModel.breathingState.collectAsStateWithLifecycle()
    val progress by viewModel.breathingProgress.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Stress Grounding Toolkit",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Interactive cognitive reframing mechanisms & distress support.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Box lung breathing circle visualization
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breathing_guide_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Physiological Box Breathing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Inhale 4s -> Hold 4s -> Exhale 4s -> Rest 4s. Promotes instant parasympathetic neural calming.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Interactive Circle Drawing
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .testTag("breathing_circle_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        val targetScale = when (breathingState) {
                            "Inhale" -> 1f + (progress * 0.45f)
                            "Hold" -> 1.45f
                            "Exhale" -> 1.45f - (progress * 0.45f)
                            else -> 1f
                        }
                        val animatedScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                            label = "breathing_scale"
                        )

                        // Background ripple pulse
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = animatedScale + 0.15f
                                    scaleY = animatedScale + 0.15f
                                    alpha = 0.15f
                                }
                                .background(CalmingTeal, CircleShape)
                        )

                        // Primary core circle
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                }
                                .background(
                                    Brush.radialGradient(colors = listOf(CalmingTeal, SoothingIndigo)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = breathingState,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepSlateBlue,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${(4 - (progress * 4).toInt()).coerceIn(1, 4)}s",
                                    fontSize = 12.sp,
                                    color = DeepSlateBlue.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = CalmingTeal,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // Sensory Grounding - 5-4-3-2-1 technique
        item {
            Text(
                text = "Grounding Exercises",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5-4-3-2-1 Sensory Reset",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "A cognitive anchor to halt severe anxiety spikes. Identify the following in your physical surroundings:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val senses = listOf(
                        "5 See" to "Look around at 5 objects: color contrasts, light patterns.",
                        "4 Feel" to "Recognize 4 tactile senses: clothing weight, floor contact.",
                        "3 Hear" to "Listen for 3 background sounds: bird calls, HVAC hums.",
                        "2 Smell" to "Recall 2 familiar aromas: coffee, rain humidity.",
                        "1 Taste" to "Dwell on 1 lingering taste: toothpaste, hydration."
                    )

                    senses.forEach { (label, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(CalmingTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = label.first().toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CalmingTeal)
                            }
                            Column {
                                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        // Crisis Prevention Immediate Response Unit
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hotline_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Alert", tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Crisis Support Hotline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Text(
                        text = "You are valued and not alone. If you are experiencing acute distress, self-harm impulses, or safety concerns, please contact supportive professionals immediately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Call 988", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:741741")).apply {
                                    putExtra("sms_body", "HOME")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.25f), contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        ) {
                            Text("Text HOME", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE & USER CONFIGURATION SCREEN ---

@Composable
fun ProfileScreen(viewModel: MentalHealthViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val reflections by viewModel.allJournals.collectAsStateWithLifecycle()
    val moods by viewModel.allMoodLogs.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var quote by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name
            email = it.email
            quote = it.quotes
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Secure Profile Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage your wellness metrics and configure private identity variables.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Live stats breakdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(label = "Mood Logs", count = moods.size.toString(), modifier = Modifier.weight(1f))
                ProfileStatCard(label = "Journals", count = reflections.size.toString(), modifier = Modifier.weight(1f))
                ProfileStatCard(label = "AI Chats", count = messages.size.toString(), modifier = Modifier.weight(1f))
            }
        }

        // Edit info block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Edit Core Identity Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_field"),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_email_field"),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quote,
                        onValueChange = { quote = it },
                        label = { Text("Daily Intention Quote") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_quote_field"),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.updateProfile(name, email, quote) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_profile_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = name.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold, color = DeepSlateBlue)
                    }
                }
            }
        }

        // Mandatory AI Studio Security Warning to conform to android_secret_management skill
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Security Notice", tint = WarningCoral, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Security Warning Notice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarningCoral
                        )
                    }
                    Text(
                        text = "I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // Action block (Logout)
        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningCoral.copy(alpha = 0.15f),
                    contentColor = WarningCoral
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = "Log out")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lock Workspace (Log Out)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileStatCard(label: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// --- CORE FRONTEND DESIGN HELPERS & STYLING WRAPPERS ---

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CalmingTeal,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    focusedLabelColor = CalmingTeal,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    cursorColor = CalmingTeal
)

// Extension Modifier to draw customized gradient border
fun Modifier.gradientBorder(colors: List<Color>, cornerRadius: androidx.compose.ui.unit.Dp) = this.drawBehind {
    val strokeWidth = 1.5.dp.toPx()
    val brush = Brush.linearGradient(colors)
    drawRoundRect(
        brush = brush,
        style = Stroke(width = strokeWidth),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
    )
}
