package com.wordpop.app

data class Word(
    val id: Int,
    val en: String,
    val ipa: String,
    val ar: String,
    val kind: String,
    val example: String,
    val exampleAr: String
) {
    val isPhrase: Boolean get() = kind == "عبارة"
}

private fun w(id: Int, en: String, ipa: String, ar: String, kind: String, ex: String, exAr: String) =
    Word(id, en, ipa, ar, kind, ex, exAr)

private fun p(id: Int, en: String, ar: String, ex: String, exAr: String) =
    Word(id, en, "", ar, "عبارة", ex, exAr)

object WordRepo {
    val all: List<Word> = listOf(
        w(1, "resilient", "/rɪˈzɪliənt/", "مرن، بيستحمل الصعوبات ويرجع أقوى", "صفة", "She stayed resilient after losing her job.", "فضلت قوية بعد ما خسرت شغلها."),
        w(2, "achieve", "/əˈtʃiːv/", "يحقق، ينجز", "فعل", "He worked hard to achieve his goals.", "اشتغل بجد عشان يحقق أهدافه."),
        w(3, "improve", "/ɪmˈpruːv/", "يحسّن، يتحسّن", "فعل", "I want to improve my English.", "عايز أحسّن إنجليزيتي."),
        w(4, "challenge", "/ˈtʃælɪndʒ/", "تحدّي", "اسم", "Learning a language is a big challenge.", "تعلم لغة تحدّي كبير."),
        w(5, "curious", "/ˈkjʊriəs/", "فضولي", "صفة", "Kids are naturally curious.", "الأطفال فضوليين بطبيعتهم."),
        w(6, "decide", "/dɪˈsaɪd/", "يقرر", "فعل", "We need to decide today.", "لازم نقرر النهاردة."),
        w(7, "borrow", "/ˈbɑːroʊ/", "يستلف (ياخد)", "فعل", "Can I borrow your pen?", "ينفع أستلف القلم بتاعك؟"),
        w(8, "lend", "/lend/", "يسلّف (يدّي)", "فعل", "Could you lend me some money?", "ممكن تسلفني شوية فلوس؟"),
        w(9, "forget", "/fərˈɡet/", "ينسى", "فعل", "Don't forget your keys.", "متنساش مفاتيحك."),
        w(10, "remember", "/rɪˈmembər/", "يفتكر", "فعل", "I remember her name.", "أنا فاكر اسمها."),
        w(11, "receive", "/rɪˈsiːv/", "يستلم", "فعل", "I received your message.", "استلمت رسالتك."),
        w(12, "prefer", "/prɪˈfɜːr/", "يفضّل", "فعل", "I prefer tea to coffee.", "أفضّل الشاي عن القهوة."),
        w(13, "expensive", "/ɪkˈspensɪv/", "غالي", "صفة", "This phone is too expensive.", "التليفون ده غالي أوي."),
        w(14, "cheap", "/tʃiːp/", "رخيص", "صفة", "The tickets were cheap.", "التذاكر كانت رخيصة."),
        w(15, "comfortable", "/ˈkʌmftərbəl/", "مريح", "صفة", "This chair is very comfortable.", "الكرسي ده مريح جدًا."),
        w(16, "dangerous", "/ˈdeɪndʒərəs/", "خطير", "صفة", "It is dangerous to swim here.", "السباحة هنا خطر."),
        w(17, "necessary", "/ˈnesəseri/", "ضروري", "صفة", "Sleep is necessary for health.", "النوم ضروري للصحة."),
        w(18, "available", "/əˈveɪləbəl/", "متاح، موجود", "صفة", "Is this room available tomorrow?", "الأوضة دي متاحة بكرة؟"),
        w(19, "delay", "/dɪˈleɪ/", "يأجّل، يعطّل", "فعل", "The flight was delayed by two hours.", "الرحلة اتأخرت ساعتين."),
        w(20, "deadline", "/ˈdedlaɪn/", "الموعد النهائي", "اسم", "The deadline is Friday.", "الموعد النهائي يوم الجمعة."),
        w(21, "schedule", "/ˈskedʒuːl/", "جدول مواعيد", "اسم", "Let me check my schedule.", "خليني أشوف جدولي."),
        w(22, "meeting", "/ˈmiːtɪŋ/", "اجتماع", "اسم", "The meeting starts at ten.", "الاجتماع بيبدأ الساعة عشرة."),
        w(23, "experience", "/ɪkˈspɪriəns/", "خبرة، تجربة", "اسم", "She has a lot of experience.", "عندها خبرة كبيرة."),
        w(24, "opportunity", "/ˌɑːpərˈtuːnəti/", "فرصة", "اسم", "This job is a great opportunity.", "الشغلانة دي فرصة ممتازة."),
        w(25, "advice", "/ədˈvaɪs/", "نصيحة", "اسم", "Thanks for your advice.", "شكرًا على نصيحتك."),
        w(26, "environment", "/ɪnˈvaɪrənmənt/", "البيئة", "اسم", "We must protect the environment.", "لازم نحافظ على البيئة."),
        w(27, "knowledge", "/ˈnɑːlɪdʒ/", "معرفة، علم", "اسم", "Knowledge is power.", "العلم قوة."),
        w(28, "patient", "/ˈpeɪʃənt/", "صبور", "صفة", "Be patient, it takes time.", "اصبر، الموضوع بياخد وقت."),
        w(29, "honest", "/ˈɑːnɪst/", "صادق، أمين", "صفة", "He is an honest man.", "هو راجل أمين."),
        w(30, "generous", "/ˈdʒenərəs/", "كريم", "صفة", "She is very generous with her time.", "هي كريمة جدًا بوقتها."),
        w(31, "shy", "/ʃaɪ/", "خجول", "صفة", "He is too shy to speak in public.", "هو خجول أوي إنه يتكلم قدام الناس."),
        w(32, "stubborn", "/ˈstʌbərn/", "عنيد", "صفة", "My brother is very stubborn.", "أخويا عنيد جدًا."),
        w(33, "succeed", "/səkˈsiːd/", "ينجح", "فعل", "If you practice, you will succeed.", "لو اتمرنت هتنجح."),
        w(34, "fail", "/feɪl/", "يفشل، يرسب", "فعل", "He failed the exam.", "رسب في الامتحان."),
        w(35, "suggest", "/səɡˈdʒest/", "يقترح", "فعل", "I suggest we leave early.", "أقترح نمشي بدري."),
        w(36, "explain", "/ɪkˈspleɪn/", "يشرح", "فعل", "Can you explain this word?", "ممكن تشرحلي الكلمة دي؟"),
        w(37, "enough", "/ɪˈnʌf/", "كفاية", "صفة", "I don't have enough time.", "معنديش وقت كفاية."),
        w(38, "almost", "/ˈɔːlmoʊst/", "تقريبًا", "ظرف", "It's almost midnight.", "الساعة قربت على 12 بالليل."),
        w(39, "although", "/ɔːlˈðoʊ/", "رغم إن", "حرف ربط", "Although it was raining, we went out.", "رغم إن الدنيا كانت بتمطر، خرجنا."),
        w(40, "appointment", "/əˈpɔɪntmənt/", "ميعاد (دكتور، مقابلة)", "اسم", "I have a doctor's appointment.", "عندي ميعاد مع الدكتور."),
        w(41, "neighbor", "/ˈneɪbər/", "جار", "اسم", "My neighbor is very friendly.", "جاري ودود جدًا."),
        w(42, "reliable", "/rɪˈlaɪəbəl/", "يُعتمد عليه", "صفة", "He is a reliable friend.", "هو صاحب بيتعتمد عليه."),
        w(43, "recommend", "/ˌrekəˈmend/", "يرشّح، ينصح بـ", "فعل", "Can you recommend a good restaurant?", "ممكن ترشحلي مطعم كويس؟"),
        w(44, "attend", "/əˈtend/", "يحضر", "فعل", "I will attend the meeting.", "هحضر الاجتماع."),
        w(45, "refund", "/ˈriːfʌnd/", "استرداد فلوس", "اسم", "I want a refund, please.", "عايز أسترد فلوسي لو سمحت."),
        w(46, "survive", "/sərˈvaɪv/", "ينجو، يعيش", "فعل", "They survived the storm.", "نجوا من العاصفة."),
        w(47, "afford", "/əˈfɔːrd/", "يقدر يدفع ثمنه", "فعل", "I can't afford a new car.", "مش قادر أشتري عربية جديدة."),
        w(48, "postpone", "/poʊstˈpoʊn/", "يأجّل لوقت تاني", "فعل", "They postponed the match.", "أجّلوا الماتش."),
        w(49, "brilliant", "/ˈbrɪljənt/", "عبقري، رائع", "صفة", "That's a brilliant idea!", "دي فكرة عبقرية!"),
        w(50, "reduce", "/rɪˈduːs/", "يقلّل", "فعل", "We must reduce costs.", "لازم نقلّل التكاليف."),

        p(51, "How's it going?", "إزيك؟ عامل إيه؟", "Hey Sam, how's it going?", "أهلاً سام، عامل إيه؟"),
        p(52, "Nice to meet you.", "اتشرفت بمعرفتك.", "Nice to meet you, I'm Omar.", "اتشرفت بمعرفتك، أنا عمر."),
        p(53, "I'm looking forward to it.", "متحمس له جدًا.", "We're going on a trip, and I'm looking forward to it.", "هنسافر رحلة وأنا متحمس لها جدًا."),
        p(54, "It's up to you.", "الأمر متروك لك، براحتك.", "Pizza or burgers? It's up to you.", "بيتزا ولا برجر؟ براحتك."),
        p(55, "Take your time.", "خد وقتك.", "There's no rush, take your time.", "مفيش استعجال، خد وقتك."),
        p(56, "No worries.", "ولا يهمك، مفيش مشكلة.", "Sorry I'm late! No worries.", "آسف إني اتأخرت! ولا يهمك."),
        p(57, "I'll get back to you.", "هرجعلك بالرد.", "Let me check and I'll get back to you.", "خليني أشوف وأرجعلك بالرد."),
        p(58, "Could you say that again?", "ممكن تعيد اللي قلته؟", "Sorry, could you say that again?", "آسف، ممكن تعيد اللي قلته؟"),
        p(59, "What do you mean?", "تقصد إيه؟", "I don't get it. What do you mean?", "مش فاهم. تقصد إيه؟"),
        p(60, "I couldn't agree more.", "أنا موافقك تمامًا.", "This movie is great. I couldn't agree more.", "الفيلم ده رائع. أنا موافقك تمامًا."),
        p(61, "Let's keep in touch.", "خلينا على تواصل.", "It was nice seeing you. Let's keep in touch.", "كان حلو إني شفتك. خلينا على تواصل."),
        p(62, "It's not a big deal.", "الموضوع بسيط، مش مشكلة.", "Don't worry, it's not a big deal.", "متقلقش، الموضوع بسيط."),
        p(63, "I'm running late.", "أنا متأخر.", "I'm running late, start without me.", "أنا متأخر، ابدأوا من غيري."),
        p(64, "Make yourself at home.", "اعتبر نفسك في بيتك.", "Come in and make yourself at home.", "اتفضل اعتبر نفسك في بيتك."),
        p(65, "What's up?", "إيه الأخبار؟ في إيه؟", "Hey! What's up?", "أهلاً! إيه الأخبار؟"),
        p(66, "Can I have the bill, please?", "ممكن الحساب لو سمحت؟", "Excuse me, can I have the bill, please?", "لو سمحت، ممكن الحساب؟"),
        p(67, "I'd like to book a table.", "عايز أحجز ترابيزة.", "I'd like to book a table for four.", "عايز أحجز ترابيزة لأربعة."),
        p(68, "How much is it?", "بكام ده؟", "This looks nice. How much is it?", "ده شكله حلو. بكام؟"),
        p(69, "Do you accept credit cards?", "بتقبلوا كروت الفيزا؟", "Do you accept credit cards here?", "بتقبلوا كروت هنا؟"),
        p(70, "I'm just looking, thanks.", "أنا بتفرج بس، شكرًا.", "Can I help you? I'm just looking, thanks.", "أقدر أساعدك؟ أنا بتفرج بس، شكرًا."),
        p(71, "Where is the bathroom?", "الحمّام فين؟", "Excuse me, where is the bathroom?", "لو سمحت، الحمّام فين؟"),
        p(72, "I have no idea.", "معنديش أي فكرة.", "Where is he? I have no idea.", "هو فين؟ معنديش أي فكرة."),
        p(73, "It depends.", "حسب الظروف.", "Will you come? It depends.", "هتيجي؟ حسب الظروف."),
        p(74, "That makes sense.", "ده منطقي، مفهوم.", "Oh, that makes sense now.", "آه، فهمت دلوقتي."),
        p(75, "I'm on my way.", "أنا في الطريق.", "Don't worry, I'm on my way.", "متقلقش، أنا في الطريق."),
        p(76, "Sorry to bother you.", "آسف على الإزعاج.", "Sorry to bother you, but I need help.", "آسف على الإزعاج، بس محتاج مساعدة."),
        p(77, "Better late than never.", "أحسن متأخر من ما يجيش خالص.", "He finally apologized. Better late than never.", "أخيرًا اعتذر. أحسن متأخر من ما يجيش."),
        p(78, "Practice makes perfect.", "بالممارسة تتقن.", "Keep speaking English. Practice makes perfect.", "كمّل كلام بالإنجليزي. بالممارسة تتقن."),
        p(79, "Break a leg!", "بالتوفيق! (قبل عرض أو امتحان)", "You have the interview today? Break a leg!", "عندك المقابلة النهاردة؟ بالتوفيق!"),
        p(80, "Keep it up!", "كمّل كده، عاش!", "Your English is getting better. Keep it up!", "إنجليزيتك بتتحسن. كمّل كده!"),
        p(81, "I'm fed up with it.", "زهقت منه، مش قادر أتحمله.", "I'm fed up with this traffic.", "زهقت من الزحمة دي."),
        p(82, "Don't give up.", "متستسلمش.", "It's hard, but don't give up.", "صعب، بس متستسلمش."),
        p(83, "Long time no see.", "من زمان مشفتكش.", "Hey Ali! Long time no see.", "أهلاً علي! من زمان مشفتكش."),
        p(84, "Can you give me a hand?", "ممكن تساعدني؟", "These bags are heavy. Can you give me a hand?", "الشنط تقيلة. ممكن تساعدني؟"),
        p(85, "That's a good point.", "نقطة كويسة.", "That's a good point. I didn't think of it.", "نقطة كويسة. مخدتش بالي منها.")
    )

    private val byId: Map<Int, Word> = all.associateBy { it.id }

    fun get(id: Int): Word? = byId[id]

    /** Picks a random word that respects the user's content filter, avoiding learned words and the last shown one. */
    fun pick(s: SettingsStore): Word {
        val base = when (s.content) {
            1 -> all.filter { !it.isPhrase }
            2 -> all.filter { it.isPhrase }
            else -> all
        }
        val learned = s.learned
        var pool = base.filter { it.id !in learned }
        if (pool.isEmpty()) pool = base
        if (pool.size > 1) pool = pool.filter { it.id != s.lastWordId }
        val chosen = pool.random()
        s.lastWordId = chosen.id
        return chosen
    }
}
