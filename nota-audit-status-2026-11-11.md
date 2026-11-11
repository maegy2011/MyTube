# MyTube — حالة تنفيذ P0 (تدقيق 2026-11-11)

> المهارات المطبقة: `skills/android/SKILL.md` (طبقة runtime + hygiene) — الافتراض stated: targetSdk 34+.

## النتائج (كلها ✅ — لا SKIPPED)

| # | البند | الملفات | الحالة |
|---|-------|----------|--------|
| F1 | مؤقت النوم service-side: `sleepDeadlineMs` + `scheduleSleep/cancelSleep` في companion + `Handler.postDelayed(player?.pause)`؛ التوصيل عند الضبط/المسح/الاستعادة | `playback/PlaybackService.kt` + `MainActivity.kt` (onPick + restore) | ✅ |
| F2 | تحقق طول الـpin: `getTo` يقارن `done==Content-Length` (من نفس استجابة GET، بلا HEAD إضافي) + `expectedBytes` في meta.json + فحص ما بعد التنزيل (video.mp4>0 + حد نصفي ضد المبتور الشديد) | `data/local/PinManager.kt` | ✅ |
| D1 | معاينة الترجمة: خلفية `surfaceVariant` بدل المنطق المعكوس الخام | `ui/settings/SettingsScreen.kt:314` | ✅ |
| T3 | تسرب `onCustomAction`: `= null` في `onDispose` + `Activity.onDestroy` | `MainActivity.kt` | ✅ |
| L2 | PiP auto-enter: `setAutoEnterEnabled(pref_pip)` + دفع params بـfalse عند إطفاء المفتاح (إلغاء الالتصاق)؛ الدخول اليدوي بالزر يعمل دائمًا | `MainActivity.kt` (enterPip + onUserLeaveHint) | ✅ |
| L3 | Age-gate: عند `inQueue` في فرع warn (مثبت + شبكي) → toast + تخطي للعنصر التالي بدل الجمود؛ النقر المباشر ما زال يعرض حوار الموافقة | `MainActivity.kt` (فرعا playVideo) | ✅ |
| S1 (prereq) | `*.apk *.idsig` في `.gitignore` + `git rm --cached` للـ7 ملفات (تبقى محليًا على القرص) | `.gitignore` | ✅ |

ملاحظات تقنية:
- L3 استخدم منطق تخطي مضمّن (inline) بدل استدعاء `playNextInQueue()` لأنها local fun معلنة لاحقًا في نفس الـcomposable (خطأ unresolved في أول بناء — أُصلح).
- F1: حلقة المراقبة Compose باقية كاحتياط (fade-out + تحديث sleepLeft)؛ الخدمة تضمن الإيقاف لو ماتت الـActivity.
- L2: زر PiP اليدوي لا يُحجَب بالمفتاح (قرار: المفتاح يضبط التلقائي فقط).

## البناء
- الأمر: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:compileDebugKotlin --offline --no-daemon`
- النتيجة: **BUILD SUCCESSFUL in 2m 39s** (تحذيرات deprecation قائمة مسبقًا فقط)
- بلا assembleRelease، بلا push — حسب القواعد.

## الـcommit
- `9fdae36` — audit P0: F1 sleep service-side, F2 pin length, D1 subtitle preview, T3 leak fix, L2 PiP sticky, L3 age-gate toast

---

# MyTube — تنفيذ P1 (تدقيق 2026-11-11)

> المهارات المطبقة: `skills/android/SKILL.md` (طبقات build/runtime + القواعد 1-9 — IO خارج الرئيسي، collectAsStateWithLifecycle، حدود Binder عبر حفظ مخنوق).

## النتائج (17 ✅ + 1 ⏭️)

| # | البند | الحالة |
|---|-------|--------|
| T1 | IO الـpin إلى `Dispatchers.IO`: `isPinned` + لقطة قرص/JSON كاملة (`existingDir`/`readText`/`JSONObject`) في `withContext(IO)`؛ منطق المشغل بقي على Main | ✅ |
| T2 | بناء `SimpleCache` خارج الرئيسي: تبييت `warmAsync` على IO عند بدء التشغيل (موجود، تم التحقق) + مسار `factory` كان أصلًا باحتياطي مباشر | ✅ |
| F5 | resume/cancel للتنزيلات: `Range bytes=X-` مقابل طول `.part` (206 إلحاق وإلا إعادة من الصفر) + إلغاء تعاوني (`requestCancel` + فحص كل chunk + `Job.cancel`) + زر إلغاء في `DownloadsScreen` (صفوف معلقة بلا صف Room + زر X على الجارية) + تنظيف `staging_*` الأقدم من 24h | ✅ |
| F6 | سباق quick-pin: إضافة تفاؤلية لـ`pinningKeys` على UI قبل `launch(IO)` (مثل `requestPin`) + `alreadyAdded` في `runPinDownload` | ✅ |
| F7 | خلط بتاريخ: `shuffleOrder` (مفاتيح favKey، الرأس = التالي) + `ensureShuffleOrder` (تصفية الصحيح/إعادة بناء) + `onNext` اليدوي يمر عبر `playRandomInQueue` عند `shuffleOn` | ✅ |
| F8 | تطبيق `subTrackIdx`: `TrackSelectionOverride` صريح لنوع TEXT في `applyLangPrefs` عند `subTrackIdx>=0`، محروس بمالك الاختيار `subTrackFor` (لا override قديم بعد تغير الفيديو) | ✅ |
| T4 | بدء التفكيك: `playback/PlayerController.kt` جديد (helpers خالصة: `next/prevSequentialIndex` + `shuffledFavKeys`) + `playNext/Prev` يستخدمانها. متابعة: نقل حالة القائمة + radio refill إلى state holder | ✅ (شريحة دنيا + متابعة موثقة) |
| T5 | إزالة `!!` من المسارات المرئية: `streamData`/`last`/`openGroup`/`openRemote`/`searchSuggestion`/`searchError`/`callback` + `(as? Float)` في التلاشي + `r.durationSec ?: 0` | ✅ |
| T6 | `fadeJob`: مصفوفة حاملة + إلغاء السابق قبل الإطلاق | ✅ |
| T7 | `EqManager.release()` في `onDispose` + `PlaybackService.onDestroy` | ✅ |
| T8 | `lifecycle-runtime-compose` في الكتالوج + `collectAsState` → `collectAsStateWithLifecycle(initialValue=)` لكل استعلامات Room والاقتراحات | ✅ |
| L1 | جلسة دنيا: حفظ مخنوق كل 15s (أول 50 عنصر: favKey/url/title/id + index + position) + استعادة القائمة عند الإطلاق (بلا تشغيل تلقائي؛ الموضع يُحقن عند الضغط عبر `pendingSeekMs`) + رسالة `sess_restored` | ✅ |
| L4 | إعادة قراءة `pref_keepalive` في `ON_RESUME` (يزامن مفتاح الإعداد مع كتابة `ACTION_STOP` المباشرة) | ✅ |
| S1 | تحقق فقط: لا `*.apk`/`*.idsig` متتبعة (أُنجز في P0) | ✅ |
| S4 | قيد scheme في `openInYoutubeCtx`: `http/https` فقط قبل `startActivity` (يصد `intent://`/`file://`/المخصص) | ✅ |
| D2 | `ACCENT_OPTIONS` تُبنى من `AccentFills+AccentNames` (مصدر واحد) + D3: `LiveBadge/OnLiveBadge` في `Color.kt` مستخدمة في `ChannelScreen` + `PlayerArea` | ✅ |
| D4 | `AutoMirrored.Share` في 3 مواضع | ⏭️ SKIPPED — الأيقونة غير موجودة في `material-icons-extended` المثبت (BOM 2024.10.00، تم التحقق من الـjar)؛ تحتاج رفع مكتبة أو vector مخصص |
| D5/D6 | `MaterialTheme.shapes` بدل الخام في المواضع المدرجة (Playlists/SearchRow/Downloads/MusicHome/شارات LIVE/بانر البطارية) + تعريف `surfaceContainer*` الصريحة في الثيمات الثلاثة (داكن/فاتح/أسود) | ✅ |

## البناء
- الأمر: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:compileDebugKotlin --offline --no-daemon`
- النتيجة: **BUILD SUCCESSFUL in 2m 34s** (تحذير `pin_orphans_fmt` المسبق فقط — خارج النطاق)
- بلا assembleRelease، بلا push — حسب القواعد.

## الـcommit
- `d987040` — audit P1: T1 pin IO, T2 cache warm verified, F5 resume+cancel, F6 optimistic pin

---

# MyTube — تنفيذ P2 (تدقيق 2026-11-11)

> المهارات المطبقة: `skills/android/SKILL.md` (طبقات build/runtime/store + حدود Binder/ANR). البناء: `compileDebugKotlin --offline` فقط — بلا assembleRelease، بلا push.

## 25 — S2/S3/S5/S6/S7/S8/S9 (أمنية صغيرة)

| البند | الحالة |
|-------|--------|
| S2 أسرار خارج git (`.jks/.keystore/.env/google-services.json` في `.gitignore`؛ لا شيء منها مُتتبَّع أصلًا) | ✅ |
| S3 نسخة D2D (`backup_rules_d2d.xml` جديد + `dataExtractionRules` يشير إليه؛ يستبعد `travel/`) | ✅ |
| S5+S9 مشاركة CSV عبر FileProvider (`file_paths.xml` + `EXTRA_STREAM` + `FLAG_GRANT_READ_URI_PERMISSION`؛ fallback نصي مقلَّم) + إصلاح toasts من خيوط IO عبر `uiToast` | ✅ |
| S6 حافظة حساسة (`EXTRA_IS_SENSITIVE` على API33+ في نسخ التشخيص) | ✅ |
| S7 توثيق الصلاحيات (تعليق مبررات `READ_PHONE_STATE`/`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`/`RECORD_AUDIO` في Manifest) | ✅ |
| S8 مهلة الشبكة (موجود مسبقًا: `callTimeout` في `NewPipeDownloaderImpl` + `YouTubeRepo` — تحقق فقط) | ✅ |
| S9 FileProvider (provider + مسارات `cache/shared/` فقط) | ✅ |

## 26 — F9/F10/F11/F12/F13/F14 (تشغيل)

| البند | الحالة |
|-------|--------|
| F9 التفاف repeatAll عند نفاد الراديو (تشغيل أول القائمة بدل التوقف) | ✅ |
| F10 تقليم الراديو (إسقاط الرأس المستهلَك فوق 60 عنصرًا مع ضبط `queueIndex`) | ✅ |
| F11 مفتاح قانوني واحد (`queueIndex` + بذور الراديو عبر `favKey` + `distinctBy`) | ✅ |
| F12 حارس `sleepEndOfTrack` بمفتاح المقطع (قديم يسقط للتقدم الطبيعي؛ يُسلَّح/يُصفَّر مع المؤقت) | ✅ |
| F13 تبديل الصوت (موثَّق: إعادة البناء مقصودة للمسارات الخارجية المدمجة؛ الـoverride مستخدم أصلًا للـmuxed + الترجمة F8) | ✅ موثَّق |
| F14 مسح `staging_*` عند بدء التشغيل (`PinManager.sweepOnStartup` من `MyTubeApp`) | ✅ |

## 27 — L5/L6/L7/L9/L10 (منطق)

| البند | الحالة |
|-------|--------|
| L5 `BootReceiver` + `RECEIVE_BOOT_COMPLETED` (يُعيد `KeepAliveService.sync()` — لا يُحيي ما أُطفئ) | ✅ |
| L6 إجراءات PiP (تشغيل/إيقاف + إغلاق عبر `PipActionReceiver`؛ تُعاد مع `isPlaying` عبر `onEnterPip`) | ✅ |
| L7 تغير الإقليم يُعيد `refreshTrending` + `refreshFeed` + toast | ✅ |
| L9 بادئة `local:` + فحص النوع (`VideoItem.isLocal()`؛ لا بادئة `yt:` عمدًا — كانت ستُيتّم المفضلة/السجل القديم) | ✅ |
| L10 خمسة `catch` صامتة في مسارات مرئية → toast (`refreshLocalAudio`/`recordHistory`/`refreshFeed`/استبعاد/إعادة المجلدات) | ✅ |

## 28 — D7/D8/D9/D10 (تصميم)

| البند | الحالة |
|-------|--------|
| D7 استثناء السياسة (`Color.kt`+`Theme.kt` + إعفاء ألوان ترجمة المستخدم في `ARCHITECTURE.md`) | ✅ |
| D8 إتاحة (أوصاف موجودة أصلًا للنقل؛ `role=Button` لـActionPills وصفوف النوم؛ حد 48dp بدل `size(36/40.dp)` في Player/SearchRow) | ✅ |
| D9 تنظيف (`PlaylistCard` بلا `onLongClick` فارغة + `clickable`؛ الإعفاء الرابع في السياسة) | ✅ |
| D10 `Scrim/ScrimSoft/OnScrim` في `Color.kt` وتوحيد `.8/.75` في الشاشات الرئيسية | ✅ |

## 29 — G-A1/G-A2/G-A4/G-A3 (فجوات)

| البند | الحالة |
|-------|--------|
| G-A1 استلام المشاركة (`SEND` نص + `VIEW` يوتيوب؛ `singleTop` + استهلاك عند الاستئناف؛ رابط → تشغيل مباشر، نص → بحث) | ✅ |
| G-A2 فحص التحديث (`UpdateCheck` + زر إعدادات؛ خامل حتى تُملأ `update_repo_owner/_name` — لا repo معلن) | ⏳ جزئي |
| G-A4 نسخة JSON شاملة (تصدير/استيراد: قوائم+مفضلة+سجل500+اشتراكات+مجموعات+إعدادات؛ الدبابيس وصف فقط) | ✅ |
| G-A3 مزامنة خلفية WorkManager | ⏭️ SKIPPED — لا اعتماد WorkManager في الكاش والبناء offline يمنع إضافة اعتماد جديد؛ موثق كمتابعة |

## 30 — اختبارات

| البند | الحالة |
|-------|--------|
| وحدة `AuditP2UnitTest` (favKey/dedupe/shuffle/queueIndex — JUnit4) | ✅ ملفات (التشغيل يحتاج مزامنة online واحدة) |
| هيكل `MigrationTest` (مصفوفة 1→9 موثقة؛ التوسع الكامل بعد أول تشغيل online) | ⏳ جزئي |
| لقطات التباين اليدوية | 📋 لمحمد (أدناه) |

## البناء
- الأمر: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:compileDebugKotlin --offline --no-daemon`
- النتيجة: **BUILD SUCCESSFUL in 3m 30s** (تحذير `pin_orphans_fmt` المسبق فقط — خارج النطاق)
- بلا assembleRelease، بلا push — حسب القواعد.

## الـcommit
- `0df76ca` — audit P2: S2/S3/S5/S6/S7/S8/S9, F9/F10/F11/F12/F13-doc/F14, L5/L6/L7/L9/L10, D7/D8/D9/D10, G-A1/G-A2/G-A4/G-A3-doc, tests skeleton

---

## قائمة التحقق الميداني لمحمد (اختبار الجهاز — لا يُوكَّل)

> القاعدة: الوكيل لا جهاز ولا adb — كل ما يلي يُنفَّذ من محمد/beta على هاتف حقيقي (يفضل جهاز OEM قاتل للخلفية + Pixel مرجعي).

- [ ] مؤقت النوم: شغّل صوتًا → فعّل 5 دقائق → اسحب التطبيق away (ليس إغلاقًا) → هل توقف؟ (يفشل اليوم F1) → أعد بعد الإصلاح.
- [ ] مؤقت بعد reboot: اضبط مؤقتًا → أعد التشغيل → هل الملصق منطقي؟ (يفشل اليوم F3 بـuptimeMillis).
- [ ] موت العملية: شغّل قائمة → `adb shell am kill com.mohamedadel.mytube` أو "Don't keep activities" → أعد الفتح → هل عادت القائمة/الموضع؟ (يفشل اليوم L1).
- [ ] Doze/OEM: شغّل خلفية على Xiaomi/Oppo/Samsung مع البطارية المقيدة → هل استمر؟ + هل KeepAlive OFF افتراضيًا لا يزعج؟
- [ ] PiP: ادخل PiP يدويًا → أطفئ `pref_pip` → اترك التطبيق → هل دخل PiP تلقائيًا؟ (يفشل اليوم L2).
- [ ] Age-gate خلفي: قائمة فيها 18+ → تقدم تلقائي في الخلفية → هل تجمّد بصمت؟ (L3).
- [ ] تدوير/ملء الشاشة: دوّر أثناء التشغيل + fullscreen ↔ portrait lock → هل استمر الصوت/الموضع؟
- [ ] Cast حقيقي: Chromecast/Android TV → اتصال + تحميل تلقائي + انقطاع/إعادة.
- [ ] تنزيل pin كبير على شبكة متقطعة: هل يستأنف؟ هل يلغي؟ هل الملف المبتور يُرفض؟ (F2/F5).
- [ ] تخزين ممتلئ تقريبًا + بطاقة SD/تغير موقع travel: pins قديمة (legacy fallback) هل تُشغَّل؟
- [ ] ترجمة/captions: فيديو بمسارين عربيين → اختر الثاني → هل ثبت؟ (F8) + نمط مخصص مقابل `system` (F16).
- [ ] صوت: مكالمة واردة + DND + فصل سماعة بلوتوث أثناء التشغيل (audiofocus `:370`).
- [ ] RTL + TalkBack: العربية RTL + قارئ الشاشة على Player/Downloads/Settings — هل كل زر معلن؟ (D8).
- [ ] تباين AMOLED black مقابل dark + وضع فاتح: شارة LIVE + معاينة الترجمة (D1/D3/D6).
- [ ] مشاركة رابط يوتيوب من تطبيق يوتيوب → هل يظهر MyTube؟ (يفشل اليوم G-A1 — متوقع).
- [ ] إصدار الإصدار (R8): ثبّت `mytube-vX.apk` الـrelease على جهاز منخفض (2GB) — jank/startup/كراشات reflection.
