package com.wordpop.app

import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ---------- brand ----------

val Purple = Color(0xFF5B4BFF)
val PurpleDark = Color(0xFF3A2FD0)
val Night = Color(0xFF14143A)
val Mint = Color(0xFF2DD4A7)
val Cloud = Color(0xFFF6F4FF)
val Muted = Color(0xFF4A4A72)
val Line = Color(0xFFEBE8FD)
val Danger = Color(0xFFD64550)
val PurpleSoft = Color(0xFFEBE8FD)
val MintDeep = Color(0xFF0E8F6A)
val AmberDeep = Color(0xFF9A6A00)

/** One of the three brand pillars: POP / HEAR / KEEP (تطلع / تسمع / تثبت). */
@Composable
fun PillarCard(title: String, label: String, icon: Int, tint: Color, accent: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = modifier) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Night)
            En(label, 11.sp, color = accent, weight = FontWeight.Bold)
        }
    }
}

@Composable
fun WordPopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Purple,
            onPrimary = Color.White,
            background = Cloud,
            surface = Color.White,
            onSurface = Night
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, content = content)
    }
}

@Composable
fun Ltr(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)
}

/** English text is always laid out left-to-right, even inside the Arabic (RTL) screens. */
@Composable
fun En(
    text: String,
    size: TextUnit,
    color: Color = Night,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    Ltr {
        Text(text, modifier = modifier, color = color, fontSize = size, fontWeight = weight, textAlign = align)
    }
}

// ---------- shared pieces ----------

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF8D8CC8),
            disabledContentColor = Color.White
        )
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, Purple),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleDark)
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (selected) Purple else Color.White)
            .border(1.5.dp, if (selected) Purple else Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else Night,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Night)
            content()
        }
    }
}

@Composable
fun SpeakButton(size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Purple)
            .clickable(role = Role.Button, onClickLabel = "نطق الكلمة", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_volume),
            contentDescription = "نطق الكلمة",
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun StepBtn(symbol: String, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Cloud)
            .border(1.5.dp, Line, CircleShape)
            .semantics { contentDescription = desc }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 24.sp, color = Night)
    }
}

@Composable
fun HourStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Night, modifier = Modifier.weight(1f))
        Ltr {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepBtn("−", "نقّص ساعة") { onChange((value + 23) % 24) }
                Text(
                    hourLabel(value),
                    modifier = Modifier.width(72.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Night
                )
                StepBtn("+", "زوّد ساعة") { onChange((value + 1) % 24) }
            }
        }
    }
}

/** Text-to-speech for the in-app screens. Returns a function that speaks English text. */
@Composable
fun rememberSpeaker(): (String) -> Unit {
    val ctx = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    val holder = remember { arrayOfNulls<TextToSpeech>(1) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(ctx.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                holder[0]?.language = Locale.US
                ready = true
            }
        }
        holder[0] = engine
        onDispose {
            engine.shutdown()
            holder[0] = null
        }
    }
    return { text -> if (ready) holder[0]?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordpop-ui") }
}

// ---------- app root ----------

@Composable
fun WordPopApp(
    overlayGranted: Boolean,
    openWordId: Int?,
    onConsumeOpenWord: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotif: () -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    var learnId by remember { mutableStateOf<Int?>(null) }
    // "home", "hard" (hard words list) or "story"
    var screen by remember { mutableStateOf("home") }

    LaunchedEffect(openWordId) {
        if (openWordId != null) {
            learnId = openWordId
            onConsumeOpenWord()
        }
    }

    val id = learnId
    if (id != null) {
        BackHandler { learnId = null }
        LearnScreen(
            store = store,
            startWord = WordRepo.get(id) ?: WordRepo.pick(store),
            onClose = { learnId = null }
        )
    } else if (screen == "story") {
        BackHandler { screen = "hard" }
        StoryScreen(store = store, onBack = { screen = "hard" })
    } else if (screen == "hard") {
        BackHandler { screen = "home" }
        HardWordsScreen(store = store, onBack = { screen = "home" }, onStory = { screen = "story" })
    } else {
        HomeScreen(
            store = store,
            overlayGranted = overlayGranted,
            onRequestOverlay = onRequestOverlay,
            onRequestNotif = onRequestNotif,
            onStartLearning = { learnId = WordRepo.pick(store).id },
            onOpenHard = { screen = "hard" }
        )
    }
}

// ---------- home ----------

@Composable
fun HomeScreen(
    store: SettingsStore,
    overlayGranted: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotif: () -> Unit,
    onStartLearning: () -> Unit,
    onOpenHard: () -> Unit
) {
    val ctx = LocalContext.current
    var enabled by remember { mutableStateOf(store.enabled) }
    var interval by remember { mutableIntStateOf(store.intervalMin) }
    var startH by remember { mutableIntStateOf(store.startHour) }
    var endH by remember { mutableIntStateOf(store.endHour) }
    var content by remember { mutableIntStateOf(store.content) }
    var displaySec by remember { mutableIntStateOf(store.displaySec) }
    var autoSpeak by remember { mutableStateOf(store.autoSpeak) }
    val learned = store.learned.size
    val total = WordRepo.all.size

    // Re-sends START so the service picks up the new interval and refreshes its notification.
    fun refresh() {
        if (enabled) sendToService(ctx, OverlayService.ACTION_START)
    }

    fun toggle(on: Boolean) {
        if (on) {
            if (!overlayGranted) {
                onRequestOverlay()
                return
            }
            onRequestNotif()
            store.enabled = true
            enabled = true
            sendToService(ctx, OverlayService.ACTION_START)
        } else {
            store.enabled = false
            enabled = false
            Scheduler.cancel(ctx)
            ctx.stopService(Intent(ctx, OverlayService::class.java))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cloud)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(52.dp))
            Column {
                Text("ووردبوب", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Night)
                Text("نخلّي الإنجليزي هو اللي يلاقيك", fontSize = 15.sp, color = Muted)
            }
        }

        Surface(shape = RoundedCornerShape(24.dp), color = Night, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("الكلمات العائمة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        if (enabled) "شغّال: ${intervalLabel(interval)}، من ${hourLabel(startH)} لحد ${hourLabel(endH)}" else "متوقف",
                        fontSize = 15.sp,
                        color = Color(0xFFC9C8F0)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { toggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Night,
                        checkedTrackColor = Mint,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF3A3A78),
                        uncheckedBorderColor = Color(0xFF8D8CC8)
                    )
                )
            }
        }

        if (!overlayGranted) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFCEFC4), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "لازم تسمح للتطبيق يظهر فوق التطبيقات التانية، من غير كده الكلمات مش هتظهر.",
                        fontSize = 16.sp,
                        color = Night
                    )
                    PrimaryButton("افتح الإعدادات واسمح", Modifier.fillMaxWidth()) { onRequestOverlay() }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("جرّب كلمة دلوقتي", Modifier.weight(1f)) {
                if (overlayGranted) sendToService(ctx, OverlayService.ACTION_SHOW_NOW) else onRequestOverlay()
            }
            SecondaryButton("ابدأ أتعلّم", Modifier.weight(1f)) { onStartLearning() }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PillarCard("تطلع", "POP", R.drawable.ic_pop, PurpleSoft, Purple, Modifier.weight(1f))
            PillarCard("تسمع", "HEAR", R.drawable.ic_volume, Color(0xFFD1F9EC), MintDeep, Modifier.weight(1f))
            PillarCard("تثبت", "KEEP", R.drawable.ic_bookmark, Color(0xFFFCEFC4), AmberDeep, Modifier.weight(1f))
        }

        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFD1F9EC), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("كلماتك الصعبة وقصتك", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Night)
                Text(
                    if (store.hard.isEmpty()) "احفظ أي كلمة صعبة من الشريط العائم، وبعدها نألّفلك قصة منها تراجعها."
                    else "عندك ${store.hard.size} كلمة صعبة. افتحها وألّف قصة تراجعهم فيها.",
                    fontSize = 15.sp,
                    color = Night
                )
                SecondaryButton("افتح كلماتك وقصتك", Modifier.fillMaxWidth()) { onOpenHard() }
            }
        }

        Section("تقدّمك") {
            Text("حفظت $learned من $total", fontSize = 17.sp, color = Night)
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else learned.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Mint,
                trackColor = Line
            )
        }

        Section("كل قد إيه تظهرلك كلمة؟") {
            ChipRow {
                listOf(15, 30, 60, 120, 180, 360).forEach { m ->
                    Chip(intervalLabel(m), interval == m) {
                        interval = m
                        store.intervalMin = m
                        refresh()
                    }
                }
            }
        }

        Section("ساعات الظهور") {
            HourStepper("من", startH) {
                startH = it
                store.startHour = it
                refresh()
            }
            HourStepper("لحد", endH) {
                endH = it
                store.endHour = it
                refresh()
            }
            Text("بره الساعات دي مش هتظهرلك كلمات.", fontSize = 14.sp, color = Muted)
        }

        Section("نوع الكلمات") {
            ChipRow {
                listOf(0 to "كلمات وعبارات", 1 to "كلمات بس", 2 to "عبارات بس").forEach { (v, label) ->
                    Chip(label, content == v) {
                        content = v
                        store.content = v
                    }
                }
            }
        }

        Section("شكل الظهور") {
            Text("مدة ظهور الشريط", fontSize = 16.sp, color = Muted)
            ChipRow {
                listOf(6, 10, 15, 25).forEach { s ->
                    Chip("$s ثانية", displaySec == s) {
                        displaySec = s
                        store.displaySec = s
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "انطق الكلمة تلقائيًا أول ما تظهر",
                    fontSize = 17.sp,
                    color = Night,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = autoSpeak,
                    onCheckedChange = {
                        autoSpeak = it
                        store.autoSpeak = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple)
                )
            }
        }

        Section("لو الكلمات بتتأخر") {
            Text(
                "بعض الموبايلات بتوقف التطبيقات في الخلفية. خلّي ووردبوب \"بدون قيود\" في إعدادات البطارية.",
                fontSize = 15.sp,
                color = Muted
            )
            SecondaryButton("افتح إعدادات البطارية", Modifier.fillMaxWidth()) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                } catch (e: Exception) {
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ---------- learn ----------

@Composable
fun LearnScreen(store: SettingsStore, startWord: Word, onClose: () -> Unit) {
    val speak = rememberSpeaker()
    var word by remember { mutableStateOf(startWord) }
    var step by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<String?>(null) }
    var learnedCount by remember { mutableIntStateOf(store.learned.size) }
    var hardIds by remember { mutableStateOf(store.hard) }

    val options = remember(word) {
        val others = WordRepo.all
            .filter { it.id != word.id && it.isPhrase == word.isPhrase && it.ar != word.ar }
            .shuffled()
            .take(2)
            .map { it.ar }
        (others + word.ar).shuffled()
    }

    fun next() {
        word = WordRepo.pick(store)
        step = 0
        picked = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cloud)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "رجوع", onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "رجوع", tint = Night)
            }
            Text(
                "تعلّم كلمة",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Night,
                modifier = Modifier.weight(1f)
            )
            Text("حفظت $learnedCount من ${WordRepo.all.size}", fontSize = 14.sp, color = Muted)
        }

        if (step == 0) {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE9E6FF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(word.kind, fontSize = 15.sp, color = PurpleDark, fontWeight = FontWeight.Bold)
                    }
                    En(
                        word.en, 44.sp, Night, FontWeight.ExtraBold, TextAlign.Center,
                        Modifier.fillMaxWidth()
                    )
                    if (word.ipa.isNotEmpty()) {
                        En(word.ipa, 20.sp, Muted, FontWeight.Normal, TextAlign.Center, Modifier.fillMaxWidth())
                    }
                    SpeakButton(56.dp) { speak(word.en) }
                    Text(
                        word.ar,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Night,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مثال", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PurpleDark)
                    Ltr {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(word.example, fontSize = 20.sp, color = Night, modifier = Modifier.weight(1f))
                            SpeakButton(44.dp) { speak(word.example) }
                        }
                    }
                    Text(word.exampleAr, fontSize = 18.sp, color = Muted)
                }
            }

            PrimaryButton("جاهز؟ اختبرني", Modifier.fillMaxWidth()) { step = 1 }
            SecondaryButton("كلمة تانية", Modifier.fillMaxWidth()) { next() }
            ChipRow {
                val isHard = word.id in hardIds
                Chip(if (isHard) "في الكلمات الصعبة ✓" else "ضيفها للكلمات الصعبة", isHard) {
                    store.toggleHard(word.id)
                    hardIds = store.hard
                }
            }
        } else {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("إيه معنى الكلمة دي؟", fontSize = 17.sp, color = Muted)
                    En(
                        word.en, 38.sp, Night, FontWeight.ExtraBold, TextAlign.Center,
                        Modifier.fillMaxWidth()
                    )
                    SpeakButton(48.dp) { speak(word.en) }
                }
            }

            options.forEach { opt ->
                val p = picked
                val isCorrect = opt == word.ar
                val isPicked = p == opt
                val shape = RoundedCornerShape(18.dp)
                val bg = when {
                    p == null -> Color.White
                    isCorrect -> Color(0xFFD1F9EC)
                    isPicked -> Color(0xFFFFE3E5)
                    else -> Color.White
                }
                val borderColor = when {
                    p == null -> Line
                    isCorrect -> Mint
                    isPicked -> Danger
                    else -> Line
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .clip(shape)
                        .background(bg)
                        .border(2.dp, borderColor, shape)
                        .clickable(enabled = p == null, role = Role.Button) {
                            picked = opt
                            if (isCorrect) {
                                store.markLearned(word.id)
                                learnedCount = store.learned.size
                            } else {
                                store.addHard(word.id)
                                hardIds = store.hard
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(opt, fontSize = 19.sp, color = Night, modifier = Modifier.weight(1f))
                    if (p != null && isCorrect) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = "الإجابة الصح",
                            tint = Night,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (p != null && isPicked) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = "إجابة غلط",
                            tint = Danger,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            val p = picked
            if (p != null) {
                val ok = p == word.ar
                Text(
                    if (ok) "برافو! الكلمة اتحفظت." else "مش مشكلة، اتضافت للكلمات الصعبة وهنراجعها في القصة.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Night
                )
                PrimaryButton("كلمة تانية", Modifier.fillMaxWidth()) { next() }
                if (!ok) {
                    SecondaryButton("راجع الكلمة", Modifier.fillMaxWidth()) {
                        step = 0
                        picked = null
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
