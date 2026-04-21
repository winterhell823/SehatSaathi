package com.sehatsaathi.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colors ---
val BgColor = Color(0xFFF5F1E8)
val PrimaryAccent = Color(0xFF5C3D2E)
val SecondaryAccent = Color(0xFF1DB89F)
val TextDark = Color(0xFF2C2622)
val TextGray = Color(0xFF8B8680)
val CardBg = Color(0xFFFEFDFB)
val GrayBorder = Color(0xFFE8E4DF)
val InputBg = Color(0xFFF9F6F1)
val LightGrayHint = Color(0xFFD4D0CB)
val RedFlagBg = Color(0xFFFCE8E8)
val RedFlagBorder = Color(0xFFE8D0D0)
val RedFlagText = Color(0xFFE63946)
val LightTealTint = Color(0xFFE8F5F0)

@Composable
fun MainDiagnosticScreen() {
    var selectedNavIndex by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { AppBottomNavigationBar(selectedNavIndex) { selectedNavIndex = it } },
        containerColor = BgColor
    ) { paddingValues ->
        when (selectedNavIndex) {
            0 -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) { page ->
                    when (page) {
                        0 -> DiagnosticHubScreen()
                        1 -> SymptomClarificationScreen()
                        2 -> DiagnosticSummaryScreen()
                    }
                }
            }
            1 -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Records Screen", color = TextDark, fontSize = 20.sp)
                }
            }
            2 -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("VitalAI Screen", color = TextDark, fontSize = 20.sp)
                }
            }
            3 -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Settings Screen", color = TextDark, fontSize = 20.sp)
                }
            }
        }
    }
}

// ---------------- SCREEN 1: Diagnostic Hub ----------------
@Composable
fun DiagnosticHubScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text("PREMIUM CARE", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Diagnostic Hub", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Analyze symptoms and clinical data", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))

        // Patient Focus Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, GrayBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBg, RoundedCornerShape(8.dp))
                        .border(1.dp, GrayBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Search patient ID or name...", color = LightGrayHint)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Patient Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, GrayBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SecondaryAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ER", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Elena Rodriguez", color = TextDark, fontWeight = FontWeight.Bold)
                        Text("Age: 32 • Female", color = TextGray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent History", color = SecondaryAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // History Item
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("12 Oct 2023", color = TextGray, fontSize = 12.sp)
                        Text("Severe Headaches", color = TextDark, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(InputBg, RoundedCornerShape(4.dp))
                            .border(1.dp, GrayBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Moderate", color = TextGray, fontSize = 10.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Clinical Observations
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, GrayBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Clinical Observations", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(InputBg, RoundedCornerShape(8.dp))
                        .border(1.dp, GrayBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Enter notes, vitals, or scan summaries here...", color = LightGrayHint, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    ObservationButton("BP: 120/80")
                    Spacer(modifier = Modifier.width(8.dp))
                    ObservationButton("Glucose: 95")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+ Add Tag", color = SecondaryAccent, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Start Analysis Button
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BgColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start VitalAI Analysis", color = BgColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ObservationButton(text: String) {
    Box(
        modifier = Modifier
            .background(InputBg, RoundedCornerShape(8.dp))
            .border(1.dp, LightGrayHint, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = TextDark, fontSize = 12.sp)
    }
}

// ---------------- SCREEN 2: Symptom Clarification ----------------
@Composable
fun SymptomClarificationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Symptom Clarification", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Step 2 of 5 Diagnostic Process", color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))

        val questions = listOf(
            "Has the rash been spreading rapidly?",
            "Is there intense itching primarily at night?",
            "Any recent exposure to harsh chemicals?",
            "Are there any secondary signs of infection (pus)?"
        )

        questions.forEach { q ->
            SymptomCard(question = q)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Proceed to Analysis", color = BgColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SymptomCard(question: String) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, GrayBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Yes", "No", "Unsure").forEach { option ->
                    val isSelected = selectedOption == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (isSelected) SecondaryAccent else InputBg,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) SecondaryAccent else LightGrayHint,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedOption = option }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) Color.White else TextGray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ---------------- SCREEN 3: Diagnostic Summary ----------------
@Composable
fun DiagnosticSummaryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(LightTealTint, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("ASSESSMENT COMPLETED", color = SecondaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PID: 88231-X", color = TextGray, fontSize = 12.sp)
                Text("Session: 442A", color = TextGray, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("STEP 01: ANALYSIS", color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Diagnosis Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GrayBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Influenza (Type A/B)", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(SecondaryAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("85%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            Text("confidence score", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reasoning:", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Based on the sudden onset of high fever, severe body aches, and dry cough reported in the last 24 hours, the symptoms strongly correlate with current seasonal influenza patterns in the area.", color = Color(0xFF5C5956), fontSize = 13.sp, lineHeight = 20.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BgColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refer to Clinic", color = BgColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("STEP 02: FIND TREATMENT OPTIONS", color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TreatmentCard("Hydration", "Increase fluids", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            TreatmentCard("Rest", "Minimum 3 days", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TreatmentCard("Medication", "Antivirals if <48h", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            TreatmentCard("OTC Relief", "Acetaminophen", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        // Red Flags
        Card(
            colors = CardDefaults.cardColors(containerColor = RedFlagBg),
            border = BorderStroke(1.dp, RedFlagBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedFlagText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STEP 03: CRITICAL RED FLAGS", color = RedFlagText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                listOf("Difficulty breathing or shortness of breath", "Chest or severe abdominal pain", "Sudden dizziness or confusion").forEach {
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text("• ", color = RedFlagText, fontWeight = FontWeight.Bold)
                        Text(it, color = Color(0xFF5C5956), fontSize = 12.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { },
                border = BorderStroke(2.dp, PrimaryAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Download Report (PDF)", color = PrimaryAccent, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Share with Specialist", color = BgColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun TreatmentCard(title: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, GrayBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = SecondaryAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(start = 22.dp))
        }
    }
}

@Composable
fun AppBottomNavigationBar(selectedIndex: Int, onNavigate: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.border(1.dp, GrayBorder)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Diagnostics") },
            label = { Text("Diagnostics", fontSize = 10.sp) },
            selected = selectedIndex == 0,
            onClick = { onNavigate(0) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = LightGrayHint,
                unselectedTextColor = LightGrayHint,
                selectedIconColor = SecondaryAccent,
                selectedTextColor = SecondaryAccent,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Records") },
            label = { Text("Records", fontSize = 10.sp) },
            selected = selectedIndex == 1,
            onClick = { onNavigate(1) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = LightGrayHint,
                unselectedTextColor = LightGrayHint,
                selectedIconColor = SecondaryAccent,
                selectedTextColor = SecondaryAccent,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "VitalAI") },
            label = { Text("VitalAI", fontSize = 10.sp) },
            selected = selectedIndex == 2,
            onClick = { onNavigate(2) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = LightGrayHint,
                unselectedTextColor = LightGrayHint,
                selectedIconColor = SecondaryAccent,
                selectedTextColor = SecondaryAccent,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp) },
            selected = selectedIndex == 3,
            onClick = { onNavigate(3) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = LightGrayHint,
                unselectedTextColor = LightGrayHint,
                selectedIconColor = SecondaryAccent,
                selectedTextColor = SecondaryAccent,
                indicatorColor = Color.Transparent
            )
        )
    }
}