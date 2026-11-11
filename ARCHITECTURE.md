# MyTube Architecture (ملزم — 10 نوفمبر 2026)

تطبيق محلي أحادي-النشاط، توزيع جانبي، لا حسابات، لا Play.

## الطبقات (اتجاه الاعتماد من أعلى لأسفل فقط)

1. **UI (Compose خالص):** `ui/*` — يعرض params ويرفع events عبر callbacks. ممنوع: شبكة، Room، `withContext`، `ExoPlayer` مباشرة.
2. **MainActivity (المنسق):** الحالة + التنقل + كل المنطق حاليًا (God-Activity — يُفصل تدريجيًا: `PlayerController` ثم `FeedController` ككلاسات خالصة). يملك: prefs، latches، jobs.
3. **Data:** `YouTubeRepo` (شبكة NewPipe + ترجمة + اقتراحات — كلها `withContext(IO)`)، `PlaylistDb` (Room المصدر الوحيد للحقيقة المحلية)، `PinManager/PinFiles` (ملفات travel)، `MediaCache` (كاش مؤقت).
4. **PlaybackService:** يملك مثيل `ExoPlayer` الوحيد (التحرير له وحده — H1). الـActivity تأخذ مرجعًا فقط.

## قواعد ملزمة

- الشبكة/DB خارج الخيط الرئيسي دائمًا (StrictMode في debug يُضاف لاحقًا).
- prefs عبر `mytube_prefs` فقط، مفاتيح `pref_*`.
- الفشل صامتًا ممنوع: كل `catch` إما رسالة مستخدم ar/en أو تعليق سبب مكتوب.
- التعليق الذي يصف سلوكًا يُحدَّث مع السلوك في نفس الـcommit.
- لا `allowMainThreadQueries`، لا `commitAllowingStateLoss`، لا `@Suppress` بلا سبب مكتوب.
- سياسة الألوان (10 نوفمبر، وُسّعت 11 نوفمبر D7): `primary`/`primaryContainer` فقط قابلان للتبديل (pref_accent)؛ `secondary` (تحقق) و`tertiary` (إشارة) والشعار (`BrandMark`) ثابتة. لا `Color(0xFF` خارج `Color.kt` و`Theme.kt` (الثيم يعرّف الأسطح)، مع إعفاء رابع: ألوان محتوى يختارها المستخدم فوق الفيديو (نمط الترجمة) — ليست ألوان واجهة.
- سياسة الـcatch الفارغة (مُدققة 10 نوفمبر): مسموحة فقط في 3 فئات — كتابة prefs (الفشل يُبقي القديم)، استدعاءات المشغّل العابرة، والتنظيف/teardown. أي فشل في مسار بيانات مرئي (شبكة/DB) يجب أن يصل المستخدم كرسالة أو حالة خطأ — لا استثناء.
- الإصدار في مكانين معًا: `build.gradle.kts` + نص البداية.
