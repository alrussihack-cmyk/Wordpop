package com.wordpop.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val HardBg = Color(0xFFE9E6FF)
private val OkBg = Color(0xFFD5F7EC)

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = "رجوع", onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_back), contentDescription = "رجوع", tint = Night)
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Night, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WordRow(w: Word, trailing: @Composable () -> Unit, onSpeak: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            En(w.en, 20.sp, Night, FontWeight.Bold, TextAlign.Start, Modifier.fillMaxWidth())
            Text(w.ar, fontSize = 15.sp, color = Muted)
        }
        SpeakButton(40.dp, onSpeak)
        trailing()
    }
}

// ---------- hard words list ----------

@Composable
fun HardWordsScreen(store: SettingsStore, onBack: () -> Unit, onStory: () -> Unit) {
    val speak = rememberSpeaker()
    var hardIds by remember { mutableStateOf(store.hard) }
    var key by remember { mutableStateOf(store.aiKey) }
    val words = WordRepo.all.filter { it.id in hardIds }

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
        ScreenHeader("كلماتك الصعبة", onBack)

        Surface(shape = RoundedCornerShape(24.dp), color = Night, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${words.size} كلمة صعبة", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "بتتضاف من علامة الحفظ في الشريط العائم، أو لما تغلط في الاختبار، أو من زرار \"ضيفها للصعبة\".",
                    fontSize = 15.sp,
                    color = Color(0xFFC9C8F0)
                )
                PrimaryButton("ألّف لي قصة من كلماتي", Modifier.fillMaxWidth(), enabled = words.isNotEmpty()) { onStory() }
                if (words.size in 1..2) {
                    Text(
                        "كل ما كلماتك أكتر القصة بتبقى أحلى. جرّب تضيف 3 كلمات على الأقل.",
                        fontSize = 14.sp,
                        color = Color(0xFFC9C8F0)
                    )
                }
            }
        }

        if (words.isEmpty()) {
            Section("لسه مفيش كلمات") {
                Text(
                    "دوس على علامة الحفظ في أي كلمة تظهرلك فوق التطبيقات، وهتلاقيها هنا.",
                    fontSize = 16.sp,
                    color = Muted
                )
            }
        } else {
            Section("الكلمات") {
                words.forEach { w ->
                    WordRow(
                        w,
                        trailing = {
                            Chip("عرفتها", false) {
                                store.removeHard(w.id)
                                hardIds = store.hard
                            }
                        },
                        onSpeak = { speak(w.en) }
                    )
                }
            }
        }

        Section("قصص بالذكاء الاصطناعي (اختياري)") {
            Text(
                "من غير مفتاح، التطبيق بيفتح قصة جاهزة من مكتبته فيها أكتر عدد من كلماتك. ولو حطيت مفتاح Claude API، بتتألف قصة جديدة من كلماتك بالظبط كل مرة (محتاج نت).",
                fontSize = 15.sp,
                color = Muted
            )
            Ltr {
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        key = it
                        store.aiKey = it.trim()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
            Text(
                "المفتاح بيتحفظ على موبايلك بس، والاستخدام بيتحسب على حسابك في Anthropic.",
                fontSize = 14.sp,
                color = Muted
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ---------- story ----------

@Composable
private fun SentenceText(s: Sentence, hard: Set<Int>, onWord: (Int) -> Unit) {
    val annotated = buildAnnotatedString {
        s.parts.forEach { p ->
            if (p.hl) {
                val id = p.wordId
                val isHard = id != null && id in hard
                pushStringAnnotation("w", (id ?: -1).toString())
                withStyle(
                    SpanStyle(
                        color = if (isHard) PurpleDark else Night,
                        fontWeight = FontWeight.Bold,
                        background = if (isHard) HardBg else OkBg
                    )
                ) { append(p.text) }
                pop()
            } else {
                append(p.text)
            }
        }
    }
    Ltr {
        ClickableText(
            text = annotated,
            style = TextStyle(fontSize = 21.sp, lineHeight = 32.sp, color = Night),
            onClick = { off ->
                annotated.getStringAnnotations("w", off, off).firstOrNull()?.item?.toIntOrNull()?.let {
                    if (it >= 0) onWord(it)
                }
            }
        )
    }
}

@Composable
fun StoryScreen(store: SettingsStore, onBack: () -> Unit) {
    val speak = rememberSpeaker()
    var story by remember { mutableStateOf<Story?>(null) }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showAr by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var seed by remember { mutableIntStateOf(0) }
    var hardIds by remember { mutableStateOf(store.hard) }

    LaunchedEffect(seed) {
        loading = true
        notice = null
        selected = null
        val hard = store.hard
        val picked = WordRepo.all.filter { it.id in hard }.shuffled().take(6)
        val key = store.aiKey
        var result: Story? = null
        if (key.isNotBlank() && picked.isNotEmpty()) {
            result = withContext(Dispatchers.IO) {
                try {
                    StoryApi.generate(key, picked)
                } catch (e: Exception) {
                    null
                }
            }
            if (result == null) {
                notice = "ماقدرتش أألّف قصة جديدة (النت أو المفتاح فيهم مشكلة)، فده قصة جاهزة من المكتبة."
            }
        }
        story = result ?: StoryRepo.pickOffline(hard, story?.title)
        loading = false
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
        ScreenHeader("قصتك", onBack)

        val st = story
        if (loading || st == null) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(color = Purple)
                    Text("بنألّف قصتك من كلماتك...", fontSize = 17.sp, color = Night)
                }
            }
        } else {
            val n = st.wordIds.count { it in hardIds }
            notice?.let {
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFFFF1CC), modifier = Modifier.fillMaxWidth()) {
                    Text(it, fontSize = 15.sp, color = Night, modifier = Modifier.padding(14.dp))
                }
            }

            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    En(st.title, 28.sp, Night, FontWeight.ExtraBold, TextAlign.Start, Modifier.fillMaxWidth())
                    Text(st.titleAr, fontSize = 16.sp, color = Muted)
                    Text(
                        (if (st.fromAi) "قصة جديدة اتألفت لك" else "قصة جاهزة") + " · فيها $n من كلماتك الصعبة",
                        fontSize = 14.sp,
                        color = PurpleDark,
                        fontWeight = FontWeight.Bold
                    )
                    ChipRow {
                        Chip("اسمع القصة", false) { speak(st.plain) }
                        Chip(if (showAr) "اخفي الترجمة" else "اعرض الترجمة", showAr) { showAr = !showAr }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    st.sentences.forEach { s ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            SentenceText(s, hardIds) { selected = it }
                            if (showAr && s.ar.isNotBlank()) {
                                Text(s.ar, fontSize = 16.sp, color = Muted)
                            }
                        }
                    }
                    Text(
                        "البنفسجي = كلماتك الصعبة، الأخضر = كلمات تانية في القصة. دوس على أي كلمة تشوف معناها.",
                        fontSize = 14.sp,
                        color = Muted
                    )
                }
            }

            val sel = selected?.let { WordRepo.get(it) }
            if (sel != null) {
                Surface(shape = RoundedCornerShape(24.dp), color = HardBg, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WordRow(sel, trailing = {}, onSpeak = { speak(sel.en) })
                        if (sel.ipa.isNotEmpty()) En(sel.ipa, 17.sp, Muted, FontWeight.Normal, TextAlign.Start)
                        En(sel.example, 17.sp, Night, FontWeight.Normal, TextAlign.Start, Modifier.fillMaxWidth())
                        Text(sel.exampleAr, fontSize = 15.sp, color = Muted)
                    }
                }
            }

            Section("كلمات القصة") {
                st.wordIds.mapNotNull { WordRepo.get(it) }.forEach { w ->
                    val isHard = w.id in hardIds
                    WordRow(
                        w,
                        trailing = {
                            Chip(if (isHard) "لسه صعبة" else "ضيفها", isHard) {
                                store.toggleHard(w.id)
                                hardIds = store.hard
                            }
                        },
                        onSpeak = { speak(w.en) }
                    )
                }
                Text("لو بقت سهلة عليك دوس على \"لسه صعبة\" عشان تشيلها.", fontSize = 14.sp, color = Muted)
            }

            PrimaryButton("قصة تانية", Modifier.fillMaxWidth()) { seed++ }
        }

        Spacer(Modifier.height(8.dp))
    }
}
