import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- THEME COLORS ---
val BgColor = Color(0xFFF5F1E8)
val PrimaryAccent = Color(0xFF5C3D2E)
val SecondaryAccent = Color(0xFF1DB89F)
val TextDark = Color(0xFF2C2622)
val TextGray = Color(0xFF8B8680)
val CardBg = Color(0xFFFEFDFB)
val BorderColor = Color(0xFFE8E4DF)
val InputBg = Color(0xFFF9F6F1)
val RedBg = Color(0xFFFCE8E8)
val RedBorder = Color(0xFFE8D0D0)
val RedText = Color(0xFFE63946)
val LightTeal = Color(0xFFE8F5F0)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DiagnosticAppUI() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    Scaffold(
        bottomBar = { AppBottomNav() },
        containerColor = BgColor
    ) { paddingValues ->
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
}

// ==========================================
// SCREEN 1: DIAGNOSTIC HUB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticHubScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(text = "PREMIUM CARE", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = "Diagnostic Hub", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Please enter patient details and observations.", color = TextGray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Patient Focus Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search box
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search patient...", color = Color(0xFFD4D0CB)) },
                    modifier = Modifier.fillMaxWidth().background(InputBg, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = SecondaryAccent
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Patient Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryAccent))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Elena Rodriguez", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Age 34 • Female", color = TextGray, fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Recent History", color = SecondaryAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                // History item
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Oct 12, 2023", color = TextGray, fontSize = 11.sp)
                        Text("Migraine Protocol", color = TextDark, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.background(InputBg, RoundedCornerShape(4.dp)).border(1.dp, BorderColor, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("Moderate", color = TextGray, fontSize = 10.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Clinical Observations
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Clinical Observations", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = "Patient reports persistent headache...",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(InputBg, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = BorderColor)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagButton("120/80 BP")
                    TagButton("98 Glucose")
                    Text("+ Add Tag", color = SecondaryAccent, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterVertically).clickable{})
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Start Action
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = BgColor)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Start VitalAI Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TagButton(text: String) {
    Box(
        modifier = Modifier
            .background(InputBg, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFD4D0CB), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = TextDark, fontSize = 12.sp)
    }
}

// ==========================================
// SCREEN 2: SYMPTOM CLARIFICATION
// ==========================================
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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val questions = listOf(
            "Has the patient experienced a fever over 101°F in the last 48 hours?",
            "Is there any reported shortness of breath?",
            "Does the patient have a history of hypertension?"
        )
        
        questions.forEach { q ->
            QuestionCard(q)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = BgColor)
        ) {
            Text("Proceed to Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuestionCard(question: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SelectionButton("Yes", selected = true, modifier = Modifier.weight(1f))
                SelectionButton("No", selected = false, modifier = Modifier.weight(1f))
                SelectionButton("Unsure", selected = false, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SelectionButton(text: String, selected: Boolean, modifier: Modifier = Modifier) {
    val bg = if (selected) SecondaryAccent else InputBg
    val border = if (selected) SecondaryAccent else Color(0xFFD4D0CB)
    val textColor = if (selected) Color.White else TextGray
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(36.dp)
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { }
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// SCREEN 3: DIAGNOSTIC SUMMARY
// ==========================================
@Composable
fun DiagnosticSummaryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(LightTeal, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("ASSESSMENT COMPLETED", color = SecondaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("Patient ID: #8291", color = TextGray, fontSize = 12.sp)
                Text("Session ID: #A1B2", color = TextGray, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text("STEP 01: ANALYSIS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Influenza (Type A/B)", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp).clip(CircleShape).background(SecondaryAccent)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("85%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            Text("confidence", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reasoning:", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Patient exhibits classic symptoms including sudden onset fever, myalgia, and dry cough. No red flags for pneumonia.", color = Color(0xFF5C5956), fontSize = 13.sp, lineHeight = 18.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = BgColor)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refer to Clinic")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text("STEP 02: FIND TREATMENT OPTIONS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Treatments 2x2
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TreatmentCard("Hydration", "Increase fluids", Modifier.weight(1f))
            TreatmentCard("Rest", "Bed rest 48h", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TreatmentCard("Medication", "Oseltamivir", Modifier.weight(1f))
            TreatmentCard("OTC Relief", "Acetaminophen", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = RedBg),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STEP 03: CRITICAL RED FLAGS", color = RedText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                RedFlagItem("Watch for difficulty breathing")
                RedFlagItem("Fever persisting > 3 days")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent)
            ) {
                Text("Download (PDF)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = BgColor)
            ) {
                Text("Share", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TreatmentCard(title: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = SecondaryAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = TextGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun RedFlagItem(text: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = RedText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color(0xFF5C5956), fontSize = 12.sp)
    }
}

// ==========================================
// BOTTOM NAVIGATION
// ==========================================
@Composable
fun AppBottomNav() {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.border(width = 1.dp, color = BorderColor)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* TODO */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryAccent,
                selectedTextColor = SecondaryAccent,
                unselectedIconColor = Color(0xFFD4D0CB),
                unselectedTextColor = Color(0xFFD4D0CB),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO */ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Patients") },
            label = { Text("Patients", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color(0xFFD4D0CB), unselectedTextColor = Color(0xFFD4D0CB))
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO */ },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color(0xFFD4D0CB), unselectedTextColor = Color(0xFFD4D0CB))
        )
    }
}
