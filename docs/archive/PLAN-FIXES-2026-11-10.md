# خطة تنفيذ توصيات التدقيق — MyTube (توزيع جانبي، لا Play)

تاريخ: 10 نوفمبر 2026. الأساس: `AUDIT-2026-11-10.md`. القيد المُسقط: M5 (إذن البطارية) — لا Play = لا سياسة = يُبقى كما هو بلا توثيق إضافي.
القاعدة العامة: إصلاح + تجميع أخضر لكل مرحلة commit مستقل. لا بناء APK ولا نشر إلا بأمر صريح (قاعدة 26 سبتمبر/2 أكتوبر).

## المرحلة 0 — تثبيت الوضع الحالي (5 دقائق)

- `git status` نظيف، `git log --oneline -3` مسجّل.
- مرجع القياس: زمن استجابة البحث/الخلاصة على شبكة طبيعية (للمقارنة بعد H2).

## المرحلة 1 — H1: تملك المشغّل للخدمة وحدها (الأخطر)

الملف: `MainActivity.kt:222`.
الحالة: `onDestroy() { player?.release(); player = null; ... }` والـ`player` هو نفس مثيل `PlaybackService.player` (مرجع مشترك، س:1142).
الخطر: خروج حقيقي من الـActivity يقتل صوت الخلفية + تحرير مزدوج (الخدمة تُحرر في `onDestroy` الخاص بها).

التنفيذ:
```kotlin
override fun onDestroy() { player = null; super.onDestroy() }  // التحرير ملك PlaybackService فقط
```
مع تعليق يمنع الانتكاس: `// NEVER release here — PlaybackService owns the player (shared instance).`

التحقق:
- تجميع أخضر.
- ميداني (عليك): شغّل صوتًا → زر الرجوع حتى خروج نهائي → الصوت يستمر + الإشعار حي. ثم أغلق من الإشعار → الصوت يتوقف (الخدمة حررت).

## المرحلة 2 — H2: مهلات OkHttp للعميلين العاريين

الموضعان:
1. `YouTubeRepo.kt:26` — `private val client = OkHttpClient()`.
2. `NewPipeDownloaderImpl` — `private val client = OkHttpClient()`.

النمط الجاهز (منسوخ من `PinManager`):
```kotlin
private val client: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .callTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
        .build()
}
```
ملاحظة: `NewPipeDownloaderImpl.execute` متزامن ويعمل داخل `withContext(IO)` أصلًا — المهلات آمنة ولا تغيّر الخيوط.
التحقق: تجميع أخضر + اختبار وضع الطيران: البحث/الخلاصة يفشلان خلال ~مهلة بخطأ سطحي بدل التجلط الصامت.

## المرحلة 3 — H3: مهلة انتظار الخدمة + مخرج مرئي

الملف: `MainActivity.kt:1133-1146` (حلقة `while (p == null) delay(100)`).
التنفيذ: عدّاد 50 محاولة (~5s)، بعدها:
```kotlin
playerError = ctx.getString(R.string.err_player_unavailable) // نص جديد ar/en
toast(...); return@LaunchedEffect  // باقي التهيئة لا يُحجب
```
+ نصان جديدان `err_player_unavailable` في `values/strings.xml` و`values-en/strings.xml`.
التحقق: تجميع + محاكاة (عطّل الخدمة مؤقتًا؟ أو راقب الكود) — التطبيق يعمل بلا مشغّل ورسالة واضحة بدل التعليق.

## المرحلة 4 — M1: النسخ الاحتياطي يستثني الضخم

الوضع: `allowBackup=true` يرفع DB + prefs + `filesDir/travel` (فيديوهات!) + صور Coil.
التنفيذ (قواعد الاستثناء بدل المنع الكامل — المفضلة/القوائم تستحق البقاء):
- ملف جديد `app/src/main/res/xml/backup_rules.xml`:
```xml
<full-backup-content>
    <exclude domain="file" path="travel/"/>
    <exclude domain="cache" path="."/>
    <exclude domain="external" path="."/>
</full-backup-content>
```
- Manifest: `android:dataExtractionRules="@xml/backup_rules"` (مع `android:fullBackupContent` للتوافق تحت API 31 — نستهدف minSdk؟ يُذكر في التنفيذ).
التحقق: تجميع + `adb backup` معاينة (أو فحص merged manifest).

## المرحلة 5 — M2: تهريب LIKE في بحث السجل

الملف: `PlaylistDb.kt:215`.
التنفيذ:
- DAO: `... WHERE title LIKE :q ESCAPE '\' ...` وتمرير النمط جاهزًا من الكوتلن:
```kotlin
fun escapeLike(s: String) = s.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")
```
- موقع الاستدعاء `historySearch(...)` يغلّف: `"%" + escapeLike(q) + "%"`.
التحقق: ابحث عن `%` و`_` حرفيًا — نتائج حرفية لا شاملة.

## المرحلة 6 — M3: خصوصية ترجمة العناوين + كاش سليم

1. إفصاح: سطر في شاشة الإعدادات (قسم الخصوصية/الترجمة): "ترجمة العناوين ترسل العنوان لجوجل (نقطة مجانية)" + مفتاح `pref_title_translate` (افتراضي ON) يوقف `localizedTitle` كليًا.
2. الكاش: استبدال `synchronizedMap` + فحص-ثم-فعل بـ`ConcurrentHashMap` + حدّ LRU بسيط (LinkedHashMap access-order أو الاحتفاظ بمسح-الكل — القرار في التنفيذ، الأسلم LRU حقيقي ~200).
التحقق: إيقاف المفتاح = العناوين أصلية دائمًا ولا طلبات شبكة ترجمة.

## المرحلة 7 — M4 + المنخفضات (دفعة نظافة واحدة)

- M4: `LaunchedEffect(player)` (س:1186) → `DisposableEffect(player)` مع `onDispose { player?.removeListener(listener) }` — يُسمّى المستمع `queueListener` بدل المجهول.
- `externalAudioHook` (س:243): `mutableStateOf` → `remember` عادي (لا شيء يقرأه في التركيب).
- Splash: تأخير 1800ms الثابت → انتظار `player != null` أو انتهاء أول `refreshTrending` أيهما أول (بحد أقصى).
- `RequestBody.create` المهمل → `toRequestBody()` (واجهة `toRequestBody(it.toByteArray())` حسب الاستخدام).
- `CommentPager`: تعليق توثيقي "single-flight، ليس آمنًا للاستدعاء المتزامن" — بلا كود.

## المرحلة 8 — الإغلاق (بأمرك فقط)

1. مراجعة `git diff` كاملًا سطرًا (درس `54d9480`).
2. تجميع release أخضر للتأكد من R8 مع keep rules.
3. بأمر صريح: bump vc26/vn0.2.6 + بناء + نشر (مهارة android-github-release).

## جدول التنفيذ

| # | الحجم | يعتمد على |
|---|------|-----------|
| 0 | 5 دقائق | — |
| H1 | سطر + تعليق | 0 |
| H2 | ~10 أسطر، ملفان | 0 |
| H3 | ~10 أسطر + نصان | 0 |
| M1 | ملف XML + سطر Manifest | 0 |
| M2 | ~5 أسطر | 0 |
| M3 | إعداد + مفتاح + كاش (~30 سطرًا) | H2 (نفس العميل) |
| M4+نظافة | ~20 سطرًا | H1 (يمس المشغّل) |
| 8 | بأمرك | الكل |

الأمر المقترح منك: **"نفّذ H1-H3"** أولًا (الأعلى أثرًا والأصغر حجمًا)، ثم **"نفّذ M1-M4"**، ثم البناء.
