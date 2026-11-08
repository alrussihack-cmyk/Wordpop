package com.wordpop.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** A piece of a sentence. Highlighted parts are the vocabulary words (wordId is null if we could not match it). */
data class Part(val text: String, val wordId: Int?, val hl: Boolean)

data class Sentence(val parts: List<Part>, val ar: String) {
    val plain: String get() = parts.joinToString("") { it.text }
}

data class Story(
    val title: String,
    val titleAr: String,
    val sentences: List<Sentence>,
    val fromAi: Boolean
) {
    val wordIds: List<Int> get() = sentences.flatMap { s -> s.parts.mapNotNull { it.wordId } }.distinct()
    val plain: String get() = sentences.joinToString(" ") { it.plain }
}

/** Turns text like "He **decided** to go" into parts, and matches each marked word to a [Word]. */
object StoryParser {
    private val irregular = mapOf("forgot" to "forget", "forgotten" to "forget", "lent" to "lend")

    private fun norm(s: String): String =
        s.lowercase()
            .replace('’', '\'')
            .filter { it.isLetter() || it == ' ' || it == '\'' }
            .trim()
            .replace(Regex("\\s+"), " ")

    fun resolve(surface: String): Int? {
        val n = norm(surface)
        if (n.isEmpty()) return null
        WordRepo.all.firstOrNull { it.isPhrase && norm(it.en) == n }?.let { return it.id }
        val base = irregular[n] ?: n
        var best: Word? = null
        var bestLen = -1
        for (w in WordRepo.all) {
            if (w.isPhrase) continue
            val en = w.en.lowercase()
            val variants =
                if ((en.endsWith("e") || en.endsWith("y")) && en.length > 3) listOf(en, en.dropLast(1)) else listOf(en)
            for (v in variants) {
                if (base.startsWith(v) && base.length - v.length <= 3 && v.length > bestLen) {
                    best = w
                    bestLen = v.length
                }
            }
        }
        return best?.id
    }

    fun parse(marked: String): List<Part> {
        val out = mutableListOf<Part>()
        marked.split("**").forEachIndexed { i, seg ->
            if (seg.isEmpty()) return@forEachIndexed
            if (i % 2 == 1) out.add(Part(seg, resolve(seg), true)) else out.add(Part(seg, null, false))
        }
        return out
    }

    fun sentence(en: String, ar: String) = Sentence(parse(en), ar.replace("**", ""))
}

/** Ready-made stories that work without internet. The app picks the one that contains most of the user's hard words. */
object StoryRepo {
    private class Raw(val title: String, val titleAr: String, val lines: List<Pair<String, String>>)

    private val raw = listOf(
        Raw(
            "The New Neighbor", "الجار الجديد",
            listOf(
                "My new **neighbor** is a **shy** young man named Adam." to "جاري الجديد شاب خجول اسمه آدم.",
                "Yesterday I knocked on his door and he said, “**Nice to meet you**.”" to "امبارح خبطت على بابه وقال: “اتشرفت بمعرفتك.”",
                "I was **curious** about his life, so I asked a few questions." to "كنت فضولي أعرف عنه، فسألته كام سؤال.",
                "He was **honest** and told me he didn't know anyone in the city." to "كان أمين وقالي إنه ميعرفش حد في المدينة.",
                "I gave him some **advice** and **recommended** a good café nearby." to "إدّيته نصيحة ورشّحتله كافيه كويس قريب.",
                "He is **generous**: he made tea and said, “**Make yourself at home**.”" to "هو كريم: عمل شاي وقال: “اعتبر نفسك في بيتك.”",
                "I think he will be a **reliable** friend." to "أعتقد إنه هيبقى صاحب بيتعتمد عليه.",
                "When I left, I said, “**Let's keep in touch**.”" to "لما مشيت قلتله: “خلينا على تواصل.”"
            )
        ),
        Raw(
            "The Big Deadline", "الموعد النهائي",
            listOf(
                "Sara had a big **deadline** on Friday." to "سارة كان عندها موعد نهائي كبير يوم الجمعة.",
                "Her **schedule** was full, and she had to **attend** a **meeting** at nine." to "جدولها كان مليان، وكان لازم تحضر اجتماع الساعة تسعة.",
                "But the bus was **delayed**, so she called her boss and said, “**I'm running late**.”" to "بس الأتوبيس اتأخر، فاتصلت بمديرها وقالت: “أنا متأخرة.”",
                "He was **patient** and said, “**Take your time**.”" to "كان صبور وقال: “خدي وقتك.”",
                "In the meeting, Sara **explained** her plan." to "في الاجتماع شرحت سارة خطتها.",
                "She **suggested** they **postpone** the launch by one week." to "اقترحت إنهم يأجّلوا الإطلاق أسبوع.",
                "Her boss was **stubborn** at first, but he finally agreed." to "مديرها كان عنيد في الأول، بس وافق في الآخر.",
                "“**Better late than never**,” Sara said with a smile." to "قالت سارة وهي بتبتسم: “أحسن متأخر من ما يجيش خالص.”"
            )
        ),
        Raw(
            "Dinner Out", "عشا بره",
            listOf(
                "Omar wanted a nice dinner, but good restaurants are **expensive**." to "عمر عايز عشا حلو، بس المطاعم الكويسة غالية.",
                "He couldn't **afford** a fancy place, so he **preferred** a small, **cheap** one." to "مكانش قادر يدفع في مكان فخم، ففضّل مكان صغير ورخيص.",
                "He called and said, “**I'd like to book a table** for two.”" to "اتصل وقال: “عايز أحجز ترابيزة لاتنين.”",
                "A table was **available** at eight." to "كانت في ترابيزة متاحة الساعة تمانية.",
                "The chairs were **comfortable**, and the food was **almost** perfect." to "الكراسي كانت مريحة والأكل كان تقريبًا مثالي.",
                "He asked about the dessert: “**How much is it**?”" to "سأل عن الحلو: “بكام ده؟”",
                "The soup was cold, and the waiter was kind **enough** to give him a **refund**." to "الشوربة كانت باردة، والجرسون كان لطيف كفاية إنه رجّعله فلوسها.",
                "At the end he said, “**Can I have the bill, please**?” and “**Do you accept credit cards**?”" to "في الآخر قال: “ممكن الحساب لو سمحت؟” و“بتقبلوا كروت الفيزا؟”"
            )
        ),
        Raw(
            "The Mountain", "الجبل",
            listOf(
                "Leo loved hiking, but the mountain was **dangerous**." to "ليو بيحب المشي في الجبال، بس الجبل كان خطير.",
                "He had little **experience**, so it was a big **challenge**." to "عنده خبرة قليلة، فكانت تحدي كبير.",
                "He studied the maps because **knowledge** is **necessary** for safety." to "ذاكر الخرايط لأن المعرفة ضرورية للأمان.",
                "On the way, he **failed** twice to cross a river." to "في الطريق فشل مرتين يعدّي نهر.",
                "His friend said, “**Don't give up**.”" to "صاحبه قاله: “متستسلمش.”",
                "Leo was **resilient**, so he tried again and had a **brilliant** idea: he used a long rope." to "ليو كان مرن وقوي، فحاول تاني وجتله فكرة عبقرية: استخدم حبل طويل.",
                "He **succeeded**, and it was cold, but he knew he could **survive** one night." to "نجح، والدنيا كانت برد، بس عرف إنه يقدر ينجو ليلة واحدة.",
                "At the top, he looked at the **environment** around him and knew he could **achieve** anything." to "فوق القمة بص على الطبيعة حواليه وعرف إنه يقدر يحقق أي حاجة."
            )
        ),
        Raw(
            "Mona's English", "إنجليزي منى",
            listOf(
                "Mona wanted to **improve** her English." to "منى كانت عايزة تحسّن إنجليزيتها.",
                "Her teacher said, “Use new words every day, and you won't **forget** them.”" to "مدرّسها قالها: “استخدمي كلمات جديدة كل يوم ومش هتنسيها.”",
                "“**Could you say that again**?” asked Mona." to "سألت منى: “ممكن تعيدي اللي قلته؟”",
                "“Use new words every day,” the teacher repeated." to "كررت المدرّسة: “استخدمي كلمات جديدة كل يوم.”",
                "“**That makes sense**, but how can I **remember** so many words?”" to "“ده منطقي، بس إزاي أفتكر كلمات كتير كده؟”",
                "“There is no magic. **Practice makes perfect**.”" to "“مفيش سحر. بالممارسة تتقني.”",
                "“**That's a good point**,” said Mona." to "قالت منى: “نقطة كويسة.”",
                "After a month she spoke much better, and her teacher said, “**Keep it up**!”" to "بعد شهر اتكلمت أحسن بكتير، وقالتلها مدرّستها: “كمّلي كده!”"
            )
        ),
        Raw(
            "The Train Station", "محطة القطر",
            listOf(
                "Khaled was at the train station when his phone died." to "خالد كان في محطة القطر لما موبايله فصل.",
                "“**Sorry to bother you**,” he said to a woman. “Can I **borrow** your phone?”" to "قال لسيدة: “آسف على الإزعاج. ممكن أستلف موبايلك؟”",
                "“Of course,” she said. “I can **lend** it to you.”" to "قالت: “طبعًا. أقدر أسلّفهولك.”",
                "He called his brother: “**I'm on my way**, but the train is late.”" to "اتصل بأخوه: “أنا في الطريق، بس القطر متأخر.”",
                "His brother said, “**No worries**.”" to "قاله أخوه: “ولا يهمك.”",
                "Khaled had two heavy bags, so he asked a man, “**Can you give me a hand**?”" to "خالد معاه شنطتين تقال، فسأل راجل: “ممكن تساعدني؟”",
                "Then he asked, “**Where is the bathroom**?”" to "وبعدين سأل: “الحمّام فين؟”",
                "Later he **received** a message: “Pizza or burgers?” and he answered, “**It's up to you**.”" to "بعدها استلم رسالة: “بيتزا ولا برجر؟” ورد: “براحتك.”"
            )
        ),
        Raw(
            "A Fresh Start", "بداية جديدة",
            listOf(
                "Nour had a boring job, and she often said, “**I'm fed up with it**.”" to "نور كانت شغلتها مملة، وكانت دايمًا تقول: “زهقت منه.”",
                "One day she saw an **opportunity** at a new company." to "في يوم شافت فرصة في شركة جديدة.",
                "At first she could not **decide**." to "في الأول مقدرتش تقرر.",
                "She asked her sister, “Should I change?” Her sister said, “**It depends**. Are you ready to learn?”" to "سألت أختها: “أغيّر؟” قالتلها: “حسب الظروف. إنتي مستعدة تتعلمي؟”",
                "Nour **decided** to apply, and she **received** a reply in two days." to "نور قررت تقدّم، واستلمت ردّ بعد يومين.",
                "At the interview she met an old friend: “**Long time no see**!”" to "في المقابلة قابلت صديق قديم: “من زمان مشفتكش!”",
                "He asked, “**How's it going**?”" to "سألها: “عاملة إيه؟”",
                "She got the job and said, “**I'm looking forward to it**.”" to "خدت الوظيفة وقالت: “متحمسة لها جدًا.”"
            )
        ),
        Raw(
            "Green Habits", "عادات خضرا",
            listOf(
                "Rana cares about the **environment**." to "رنا بتهتم بالبيئة.",
                "She told her friend, “We must **reduce** plastic.”" to "قالت لصاحبتها: “لازم نقلّل البلاستيك.”",
                "“**I couldn't agree more**,” said her friend." to "قالت صاحبتها: “أنا موافقاكي تمامًا.”",
                "“But reusable bags are not always **available**,” she added." to "وزادت: “بس الشنط اللي بتتستخدم تاني مش دايمًا موجودة.”",
                "“**That's a good point**,” said Rana. “Let's make our own.”" to "قالت رنا: “نقطة كويسة. خلينا نعمل بتاعتنا.”",
                "They found **enough** old cloth at home." to "لقوا قماش قديم كفاية في البيت.",
                "Rana **suggested** a small workshop at school, and her friend said it was **brilliant**." to "رنا اقترحت ورشة صغيرة في المدرسة، وصاحبتها قالت إنها فكرة عبقرية.",
                "On the big day, her teacher said, “**Break a leg**!”" to "يوم الورشة قالتلها مدرّستها: “بالتوفيق!”"
            )
        )
    )

    private val stories: List<Story> by lazy {
        raw.map { r ->
            Story(r.title, r.titleAr, r.lines.map { (en, ar) -> StoryParser.sentence(en, ar) }, false)
        }
    }

    /** The ready-made story that covers the most words from [hard]; avoids repeating [avoidTitle] when possible. */
    fun pickOffline(hard: Set<Int>, avoidTitle: String?): Story {
        val scored = stories.map { it to it.wordIds.count { id -> id in hard } }
        val max = scored.maxOf { it.second }
        var pool = scored.filter { it.second == max }.map { it.first }
        if (pool.size > 1 && avoidTitle != null) pool = pool.filter { it.title != avoidTitle }
        return pool.random()
    }
}

/** Optional: asks Claude to write a brand-new story from the user's hard words. Needs the user's own API key. */
object StoryApi {
    // Change this if Anthropic retires the model; any current Claude model name works.
    private const val MODEL = "claude-haiku-5-5"

    fun generate(key: String, words: List<Word>): Story {
        val list = words.joinToString("\n") { "- ${it.en} (${it.ar})" }
        val prompt = """
            You are writing a short English story for an Arabic-speaking learner (level A2-B1).
            Target words and phrases the learner finds hard:
            $list

            Rules:
            - Write a coherent, fun story of 7 to 9 short sentences in simple English.
            - Use EVERY target word or phrase at least once. A single word may change form (for example "decided"); phrases must stay exactly as given.
            - Wrap each use of a target word or phrase in double asterisks, like **resilient**. Wrap nothing else.
            - Give each sentence a natural Egyptian Arabic translation (colloquial, no asterisks).
            - Reply with JSON only, no code fences, exactly in this shape:
            {"title":"short English title","titleAr":"Arabic title","sentences":[{"en":"...","ar":"..."}]}
        """.trimIndent()

        val body = JSONObject()
            .put("model", MODEL)
            .put("max_tokens", 1800)
            .put(
                "messages",
                org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
            )

        val conn = URL("https://api.anthropic.com/v1/messages").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.doOutput = true
            conn.setRequestProperty("content-type", "application/json")
            conn.setRequestProperty("x-api-key", key)
            conn.setRequestProperty("anthropic-version", "2023-06-01")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode != 200) throw Exception("HTTP ${conn.responseCode}")
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val text = JSONObject(resp).getJSONArray("content").getJSONObject(0).getString("text")
            val json = JSONObject(text.substring(text.indexOf('{'), text.lastIndexOf('}') + 1))
            val arr = json.getJSONArray("sentences")
            val sentences = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StoryParser.sentence(o.getString("en"), o.optString("ar", ""))
            }
            if (sentences.size < 3) throw Exception("story too short")
            return Story(json.optString("title", "Your story"), json.optString("titleAr", "قصتك"), sentences, true)
        } finally {
            conn.disconnect()
        }
    }
}
