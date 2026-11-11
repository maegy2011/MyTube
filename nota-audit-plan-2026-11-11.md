# MyTube — تدقيق أمني/تقني/منطقي/تصميمي شامل — 2026-11-11

> النطاق: `/home/administrator/.openclaw/workspace/projects/MyTube` — حزمة `com.mohamedadel.mytube`
> الملفات: `MainActivity.kt (4002 سطر)` + `YouTubeRepo.kt (766)` + `PlayerArea/PlayerComponents` + `PinManager/PinFiles` + `PlaylistDb (v9)` + `TasteProfile` + `LocalAudio` + `CastManager` + `SettingsScreen/SettingsSections` + `MusicHome/SearchScreen/DownloadsScreen` + `ARCHITECTURE.md`
> المهارات المطبقة (إلزامي 15 أغسطس 2026): `skills/android/SKILL.md` (الطبقات build/install/runtime/store + القواعد 1-9 + الحدود) + `skills/code-reviewer/SKILL.md` (المراحل 1-4: نطاق → تحليل آلي → سياق → تقرير مصنف).
> المنهج: 6 وكلاء جامعين بالتوازي (تقنية/فنية/أمنية/منطقية/تصميمية/فجوات) — فحص استاتيكي فقط، بلا بناء (قاعدة 26 سبتمبر)، بلا push.
> مقياس الشدة المستخدم هنا: حرج = كراش/فقد بيانات/تسرب أسرار/تشغيل ليلي لا يتوقف | كبير = ANR/تسرب ذاكرة/فساد حالة | متوسط = تدهور UX/مخالفة سياسة معمارية | صغير = تجميل/احترازي.

---

## المحور 1 — تقنية (crashes/ANR/leaks/threading/lifecycle/Room)

| # | الشدة | الوصف | الدليل file:line | الإصلاح المقترح |
|---|-------|-------|------------------|-----------------|
| T1 | كبير | IO قرص + تحليل JSON على الخيط الرئيسي في `playVideo` — `PinFiles.isPinned` + `File.exists` + `readText` + `JSONObject` داخل `scope.launch` بلا `IO` → تجميد UI لكل تشغيل، ANR على قرص بطيء | `MainActivity.kt:1444-1488` | انقل كتلة الـpin إلى `withContext(Dispatchers.IO)` وأعد `PinSnapshot` جاهزًا قبل لمس الـUI |
| T2 | كبير | بناء `SimpleCache` (قرص IO) على الرئيسي عبر `MediaCache.factory` من `applyQuality` | `MainActivity.kt:1301` يستدعي؛ `playback/MediaCache.kt:47-54` يبني synchronously | سخّن مسبقًا (warm موجود) أو ابنِ البديل المباشر عند غياب النسخة، أو انقل بناء المصدر إلى `withContext(IO)` |
| T3 | كبير | تسرب Activity عبر `PlaybackService.onCustomAction` الساكن — اللامدا تلتقط Composable + scope + ctx وتبقى بعد تدمير Activity | `playback/PlaybackService.kt:29` static؛ `MainActivity.kt:1753-1761` إسناد بلا تنظيف؛ `onDestroy:255` يصفّر player فقط | في `onDispose`/`onDestroy`: `PlaybackService.onCustomAction = null` + خزن اللامدا في `remember` |
| T4 | كبير | God-Activity: ملف واحد 4002 سطر + ~100 دالة محلية + `SettingsPane` متداخلة ~1018 سطر — أي تغيير حالة يعيد تقييم نطاق ضخم (recomposition واسعة) | `MainActivity.kt:260` `MyTubeApp`؛ `:2981` `SettingsPane` | استخرج `PlayerController` كلاس خالص أولًا (queue/playVideo/applyQuality/cast) ثم `FeedController` (ARCHITECTURE.md) |
| T5 | متوسط | سلسلة `!!` هشة: `streamData!!.related` + `last!!` + `openGroup!!` + `openRemote!!` + `searchSuggestion!!` — سباق تركيب = NPE وكراش شاشة | `MainActivity.kt:1640`؛ `:2072-2074`؛ `:337`؛ `:3703`؛ `:3816,3820` | جمّد في `val` محلي + `?: return/error` — امنع `!!` في مسارات مرئية |
| T6 | متوسط | `fadeOutAndPause` بلا مقبض إلغاء — استدعاءان متتاليان يتسابقان على `volume` (صوت منخفض عالق / إيقاف متأخر) | `MainActivity.kt:1658-1672` `scope.launch` بلا `Job`؛ يُستدعى من `:1946` + `:1670` | `var fadeJob: Job؟` — ألغِ السابق قبل الإطلاق + `finally` يعيد الضبط |
| T7 | متوسط | `EqManager` Singleton أصلي لا يُحرَّر أبدًا — `attach` عند تغير الجلسة لكن لا `release` في `onDispose`/`onDestroy` | `playback/EqManager.kt:14`؛ `MainActivity.kt:1850-1862`؛ `PlaybackService.kt:83-90` | `EqManager.release()` في `onDispose` + `PlaybackService.onDestroy` |
| T8 | متوسط | `collectAsState` بدل `collectAsStateWithLifecycle` لكل استعلامات Room (~9) — مراقبة حية في الخلفية + بطارية | `MainActivity.kt:321,326,329,330,332,338,352,481,511,1244`؛ لا `lifecycle-runtime-compose` في `build.gradle.kts` | أضف `lifecycle-runtime-compose` وتحوّل إلى `collectAsStateWithLifecycle` |
| T9 | صغير | سباق إقليم NewPipe العام — `applyRegion` يكتب `Localization` مفردًا عالميًا قبل كل طلب؛ طلبان متوازيان بمنطقتين قد يتبادلا النتائج | `data/youtube/YouTubeRepo.kt:159-167` | `Mutex` حول (apply+fetch) أو ثبّت `Localization` مرة عند بدء التطبيق |
| T10 | صغير | تحويل غير مفحوص `(volumeLive.value as Float)` + `callback!!` | `MainActivity.kt:1662`؛ `:1892` | `(as? Float) ?: 1f` + `val` محلي بدل `!!` |
| T11 | ✅ إيجابي | H1 سليم: المالك الوحيد لـExoPlayer هو `PlaybackService` ويُحرَّر في `onDestroy`؛ سلاسل الترحيل 1→9 كاملة بلا fallback في الإصدار؛ كل دوال `YouTubeRepo` الشبكية `withContext(IO)`؛ `searchJob/scJob` single-flight بالإلغاء؛ StrictMode في Debug؛ لا `TransactionTooLarge` (rememberSaveable للنصوص فقط) ولا Bitmap يدوي (Coil) | `PlaybackService.kt:83-90`؛ `MainActivity.kt:312-320`؛ `YouTubeRepo.kt` ~20 `withContext(IO)` | ثبّت في المراجعات القادمة — لا إجراء |

## المحور 2 — فنية (playback correctness/offline/timer/captions)

| # | الشدة | الوصف | الدليل | الإصلاح |
|---|-------|-------|--------|---------|
| F1 | حرج | مؤقت النوم يعيش في حلقة Compose — يموت مع Activity بينما الخدمة تواصل التشغيل طوال الليل (لا alarm/service-side timer) | `MainActivity.kt:482-484` + حلقة المراقبة `:1943` داخل التركيب؛ `PlaybackService` لا يعرف deadline؛ الاستعادة من prefs `:643-647` متأخرة | انقل deadline إلى `PlaybackService` (`Handler.postDelayed` بـ`elapsedRealtime`) أو `AlarmManager.setExactAndAllowWhileIdle` / WorkManager one-shot يوقف عبر MediaController |
| F2 | حرج | تنزيلات الـpin بلا تحقق طول — `getTo` تُرجع true عند انقطاع مبكر → ملف مبتور يُرقَّى pin ناجحًا يفشل فقط عند التشغيل | `data/local/PinManager.kt:getTo ~150-185` — لا مقارنة `done==total`؛ `renameTo` + `meta.json` + upsert Room كناجح | بعد الحلقة: إن `total>0` اشترط `done==total` وإلا false؛ خزّن `expectedBytes` في `meta.json` وتحقق في مسار التشغيل |
| F3 | كبير | ساعة المؤقت `uptimeMillis` — تتجمد في النوم العميق وتفسد بعد reboot (تُعيد deadline ميتًا بملصق absurd) | `MainActivity.kt:3919` + `:1944-1952` + `:643-647` | `elapsedRealtime()` للضبط/المراقبة/الحفظ + ارفض deadlines أبعد من 60 دقيقة عند الاستعادة |
| F4 | كبير | سباق تلاشي المؤقت مع تقدم القائمة — انتهاء المؤقت أثناء `STATE_ENDED` يوقف المقطع *التالي* خطأً | `:1943-1952` تُطلق `fadeOutAndPause` (5s آسنك `:1657-1671`) وتمسح `sleepAtMs` فورًا؛ `queueListener :1816` يتقدم أثناء التلاشي | عند الإطلاق: `autoAdvance=false` synchronously + `fadingGuard` يُفحص في `queueListener`، أو أوقف فورًا ثم تلاشَ الصوت على لاعب متوقف |
| F5 | كبير | لا pause/resume/cancel لتنزيلات الـpin — إعادة المحاولة من الصفر (GET بلا Range + `.part` يُمسح)؛ لا زر إلغاء | `PinManager.kt:getTo` GET عادي؛ `MainActivity.kt:2341+` بلا cancel action؛ `DownloadsScreen` تعطّل play/delete أثناء inFlight بلا cancel | ادعم `Range` resume مقابل طول `.part` (206 وإلا restart) + `Job` قابل للإلغاء يظهر في `DownloadsScreen` |
| F6 | كبير | سباق quick-pin بنفس المفتاح — فحص `pinningKeys` على UI ثم `launch(IO)` يضيف المفتاح لاحقًا → نقرتان سريعتان = كتابتان متداخلتان في `staging_<sha>` واحد | `MainActivity.kt:2421+` (quickPin) مقابل `:2306` (requestPin الآمن) | أضف المفتاح تفاؤليًا على UI قبل الإطلاق (مثل requestPin) أو `Mutex/Job map` لكل مفتاح |
| F7 | كبير | خلط بلا تاريخ + زر Next اليدوي يتجاهل الخلط — تكرار فوري مع 2-3 عناصر؛ `onNext` يتقدم ترتيبيًا دائمًا (الخلط للتقدم التلقائي فقط) | `MainActivity.kt:1697-1701` `playRandomInQueue` عشوائي بلا played-set؛ `:2877` + `ForwardingPlayer.seekToNext` | قائمة ترتيب مخلوط (تُعاد عند النفاد/تغير القائمة) لـauto+manual، أو وجّه `onNext` عبر `playRandomInQueue` عند `shuffleOn` |
| F8 | كبير | اختيار ترجمة لكل فيديو لا يُطبَّق — `subTrackIdx` يُخزَّن ثم `applyLangPrefs` يقود من prefs اللغة فقط | `MainActivity.kt:2839-2861` مقابل `:688-730` لا يذكر `subTrackIdx` | `TrackSelectionOverride` صريح لـTEXT عند `subTrackIdx>=0` (يُمسح عند تغير المقطع)، أو وثّق أن الاختيار على مستوى اللغة فقط |
| F9 | متوسط | `repeatAll+radio` يتوقف عند نفاد refill بدل الالتفاف على القائمة | `queueListener :1829-1834` → `playNextInQueue :1673-1690` يضبط `radioOn=false/autoAdvance=false` | عند النفاد في وضع repeatAll: التفاف عبر `playFirstInQueue` |
| F10 | متوسط | قائمة الراديو تنمو بلا حد (+10 لكل refill، لا تقليم) — بحث `indexOfFirst` O(n) + لوحة القائمة تتضخم | `:1684` `queue=queue+fresh`؛ لا حذف للرأس المستهلَك | احتفظ بآخر ~50 + القادم، أسقط الرأس المُشغَّل مع ضبط `queueIndex` |
| F11 | متوسط | `queueIndex` بمفتاح id/url بينما dedupe بمفتاح favKey — التكرارات تُزحلق الموضع (prev/next تقفز خطأً) | `:1436` مقابل `:1709-1710`؛ `startRadio` لا يزيل تكرارات `fresh` داخليًا | مفتاح قانوني واحد (`favKey`) في كل مكان + `distinctBy favKey` لبذور الراديو |
| F12 | متوسط | `sleepEndOfTrack` لاصق — يبقى مسلحًا بعد pause/seek/error ويوقف مقطعًا مستقبليًا عشوائيًا بلا إشارة | `:488` + `:3929-3935` + استهلاك فقط في `:1817-1826` | امسحه عند play/pause/seek/track-change أو قيّده بـvideoId وتحقق عند ENDED |
| F13 | متوسط | تبديل مسار الصوت يعيد بناء المصدر كاملًا (re-buffer + re-resolve) بدل override على الـselector | `:2863-2875` → `applyQuality()` | `trackSelector` override للتبديل داخل اللاعب؛ `applyQuality` لتغير الجودة فقط |
| F14 | متوسط | تنزيل مقتول يترك `staging_*` يتيمًا (تسرب تخزين غير مرئي، يُضخّم `totalBytes`) | `PinManager.downloadPin` staging؛ `existingDir/isPinned` لا يريانه؛ `PinFiles.totalBytes` يمشي الجذر كله | sweep عند بدء التطبيق لـ`staging_*` الأقدم من N ساعات، أو staging داخل pinDir بلاحقة `.tmp` مستثناة |
| F15 | صغير | نقرة related تقتل جلسة radio نشطة (`inQueue=false` يُصفّر `radioOn`) | `playVideo :1427-1432`؛ `PlayerArea onRelatedPlay → :2899` | عند `radioOn`: أدخل بعد الحالي (`enqueueNext+play`) أو أكّد استبدال الجلسة |
| F16 | صغير | نمط الترجمة يُطبَّق في update فقط ويتسابق مع إعادة إنشاء PlayerView؛ `prefSubRole=system` يتعارض مع فرض النمط | `PlayerArea.kt:351,402` مقابل factory `:292-361`؛ `MainActivity.kt:700` | طبّق في factory أيضًا؛ عند `role==system` تخطَّ override أو صحّح وصف الـpref |
| F17 | صغير | مساعدو `PinFiles.videoFile/audioFile/subFile/metaFile` يتجاهلون fallback القديم الذي يوفره `existingDir` (فخ كامن — لا مستدعين حاليًا) | `PinFiles.kt` | وجّهها عبر `existingDir` أو احذفها |
| F18 | صغير | كاش `MediaCache` بمفاتيح URLs موقعة منتهية — spans ميتة حتى ضغط LRU | `playback/MediaCache.kt` | مقبول ككاش عابر؛ اختياريًا evict-on-error أشد أو مفاتيح `videoId+itag` (أولوية منخفضة) |

## المحور 3 — أمنية (exported/deep-links/WebView/traffic/secrets/backup/logs/clipboard)

| # | الشدة | الوصف | الدليل | الإصلاح |
|---|-------|-------|--------|---------|
| S1 | كبير | ثنائيات APK موقعة مُتتبَّعة في git + غياب `*.apk` من `.gitignore` — تضخم + توزيع من السجل | `git ls-files`: `MyTube-v0.2.0/2.2/2.3(+idsig)`, `mytube-v0.2.9/3.0/3.1.apk`؛ `.gitignore` 5 أسطر فقط | أضف `*.apk *.idsig` ثم `git rm --cached *.apk *.idsig` (يبقيها محليًا) |
| S2 | متوسط | لا حماية `.gitignore` ضد الأسرار المستقبلية — AGENTS.md يقصد تتبع keystore/g-services/.env محليًا = `git push` واحد يسرّبها | لا `*.jks/*.keystore/*.env/google-services.json` في الشجرة (✅ حاليًا) ولا في `.gitignore` (❌) | أضفها إلى `.gitignore` + `skip-worktree` للحساسة محليًا، أو hook `pre-push` يرفض |
| S3 | متوسط | `allowBackup=true` + `dataExtractionRules` بلا بلوك D2D — Room (قوائم+سجل) + `mytube_prefs` تخرج إلى Drive backup | `AndroidManifest.xml:22-25`؛ `res/xml/backup_rules.xml` بلوك `full-backup-content` فقط يستبعد `travel/` والكاش ✅ | أضف بلوك `<data-extraction-rules><cloud-backup>…` مماثلًا، أو `allowBackup=false` إن البيانات قابلة للبناء |
| S4 | متوسط | فتح `v.url` بلا تحقق scheme — `intent://`/`file://`/scheme مخصص من بيانات feed خارجية يمرر لـintent resolution (خطف/تسريب) | `util/PlaybackUtils.kt:178` `ACTION_VIEW, Uri.parse(raw)` (سطر 182 `vnd.youtube` مقيد بـ`setPackage` ✅ يُحتذى) | السماح بـ`http/https` فقط قبل `startActivity` |
| S5 | صغير | تصدير CSV عبر `EXTRA_TEXT` بلا حد — قوائم آلاف الصفوف = `TransactionTooLargeException` عند chooser | `MainActivity.kt:1062-1066` `StringBuilder` كامل كنص | اكتب إلى `cacheDir` وشارك عبر `FileProvider+EXTRA_STREAM` (يتطلب provider — غير موجود، انظر S9) |
| S6 | صغير | نسخ تقرير التشخيص للحافظة بلا علامة حساسية (prefs+أخطاء مشغل تبقى في سجل Gboard على API33+) | `MainActivity.kt:1986-1987` `ClipData.newPlainText` | `EXTRA_IS_SENSITIVE=true` على API33+ أو زر نسخ مع مسح بعد مدة |
| S7 | صغير | صلاحيات واسعة: `READ_PHONE_STATE` + `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` + `RECORD_AUDIO` — مبررة ظاهريًا (audiofocus calls `:3038`، بحث صوتي `:2217`، إعفاء بطارية `:818`) لكنها ترفع بصمة الخطر وتتطلب تبرير Play مستقبلًا | `AndroidManifest.xml:11,14,15` | راجع الضرورة: `READ_PHONE_STATE` → استبدل بـ`AudioManager` focus-loss فقط؛ وثّق كل واحدة في `PLAY-Console` لاحقًا |
| S8 | صغير | شبكة NewPipe بلا pinning/timeouts صريحة ظاهرة — `NewPipeDownloaderImpl` يعتمد defaults (OkHttp connect/read/write 10s بلا callTimeout → تعليق لانهائي محتمل) | `data/youtube/NewPipeDownloaderImpl.kt`؛ `YouTubeRepo.kt` لا `CertificatePinner/callTimeout` | اضبط `callTimeout` صريحًا (مثلًا 30s)؛ pinning اختياري مع backup pin |
| S9 | صغير | لا `FileProvider` إطلاقًا — أي مشاركة ملفات مستقبلية ستُغري بـ`file://` (يُكسر من API24 `FileUriExposedException`) | grep `FileProvider` فارغ | أضف provider عند تنفيذ S5 |
| S10 | ✅ إيجابي | `exported` سليم: `MainActivity=true` (LAUNCHER)، الخدمات `false`، `MediaButtonReceiver=true` (مطلوب)؛ لا WebView (لا JS-interface/file-access/mixed-content)؛ لا cleartext (`usesCleartextTraffic` غائب = محظور افتراضيًا)؛ لا مفاتيح في `BuildConfig/strings`؛ `local.properties` غير مُتتبَّع | `AndroidManifest.xml:32,43,51,56` | لا إجراء — حافظ |

## المحور 4 — منطقية (state inconsistencies)

| # | الشدة | الوصف | الدليل | الإصلاح |
|---|-------|-------|--------|---------|
| L1 | كبير | موت العملية يمسح الجلسة كاملة — `remember` فقط لكل حالة التشغيل (`playing/queue/queueIndex/playerExpanded/isFullscreen/radioOn/videoModeQueue/playMode/agePending`)؛ `rememberSaveable` لـ`query` فقط؛ لا `SavedStateHandle` (لا ViewModel) ولا حفظ موضع | `MainActivity.kt:272,273,310,293,295,297,292,1046,801,535` مقابل `:263-264`؛ الدوران مغطى بـ`configChanges` في Manifest لكن الموت/\"Don't keep\"/multi-window يمسح كل شيء | احفظ جلسة دنيا (`favKey+url/queue favKeys/index/positionMs` throttled) في `mytube_prefs`/DataStore واستعد عند الإطلاق، أو ViewModel+`SavedStateHandle` |
| L2 | كبير | PiP auto-enter يلتصق بعد أول PiP يدوي — `setAutoEnterEnabled(true)` في كل بناء params ويُحفَظ؛ `onUserLeaveHint` يفحص `pref_pip` لكن auto-enter API31 قد يشتعل بدونه → PiP يظهر بعد إطفاء المفتاح | `MainActivity.kt:223-243` + `:247-251` | `setAutoEnterEnabled(pref)` حسب القيمة الحالية، و`setPictureInPictureParams(false)` عند إطفاء المفتاح |
| L3 | كبير | Age-gate يجمّد التقدم التلقائي بصمت — عنصر 18+ عبر queue/radio في وضع `warn` يضبط `agePending+loading=false` بلا toast (التطبيق قد يكون في الخلفية والحوار `:3939` لا يُرى) | `:1588` + `:1498` | عند auto-advance: تخطَّ العنصر المحجوب مع toast، أو أوقف التقدم برسالة مرئية/إشعار |
| L4 | كبير | `KeepAlive ↔ toggle` desync — `ACTION_STOP` من الإشعار يكتب `pref_keepalive=false` مباشرة؛ `prefKeepalive` يُقرأ مرة عند بدء التشغيل فقط → المفتاح يبقى ON كذبًا | `playback/KeepAliveService.kt:~30`؛ `MainActivity.kt:619` مقابل hook الاستئناف `:831` | أعد قراءة `pref_keepalive` في `ON_RESUME` أو `OnSharedPreferenceChangeListener` |
| L5 | متوسط | `KeepAlive` ميت بعد reboot رغم ON — لا `BOOT_COMPLETED` receiver؛ `START_STICKY` يحيي بعد القتل فقط | Manifest بلا receiver | أضف receiver يستدعي `KeepAliveService.sync()` |
| L6 | متوسط | PiP بلا إجراءات وسائط — لا `RemoteAction`s؛ فرع العرض `:2705+` بـ`PlayerView(useController=false)` أو thumbnail بلا play/pause/close | `:219-222` | أضف play/pause(+close) `RemoteAction`s API26+ تُحدَّث مع `isPlaying` |
| L7 | متوسط | تغير الإقليم لا يُعيد تحميل الخلاصة/القوائم الظاهرة — نتائج قديمة بمنطقة سابقة بلا إشارة | `onCountry :3008-3034` مقابل `refreshTrending :1195-1250` | أعد `refreshFeed` + أعلن `regionChanged` toast/state |
| L8 | متوسط | `videoModeQueue` معزولة اسميًا فقط — تبديل وضع video/music قد يسرّب عناصر بين القائمتين عند radio refill | `:1046` | افصل قوائم لكل وضع أو وثّق نموذج القائمة الواحدة |
| L9 | صغير | تصادم IDs محلي/بعيد — صوت محلي (path-based) مقابل `videoId` يوتيوب في نفس القائمة/السجل/المفضلة (favKey يخفف لكن type confusion ممكنة في البحث/التشغيل) | `util/LocalAudio.kt` مقابل `PlaylistDb` entities | بادئة نوع صريحة (`local:`/`yt:`) في favKey + فحص النوع قبل التشغيل |
| L10 | صغير | `catch (_: Exception) {}` صامت في مسارات بيانات مرئية — يخالف سياسة الـcatch (مسموح: كتابة prefs/استدعاءات المشغل العابرة/teardown فقط) | grep `catch (_` في `MainActivity.kt` (العشرات) | كل catch في مسار شبكة/DB مرئي: رسالة ar/en أو حالة خطأ — لا استثناء |

## المحور 5 — تصميمية (RTL/Material3/a11y/contrast)

| # | الشدة | الوصف | الدليل | الإصلاح |
|---|-------|-------|--------|---------|
| D1 | حرج | معاينة الترجمة بلونين خام ومنطق معكوس — أسود ثابت حتى في الوضع الفاتح | `ui/settings/SettingsScreen.kt:314` `background(if (…) Color(0xFF3A3A44) else Color(0xFF1A1A1F))` | `colorScheme.surfaceVariant+onSurfaceVariant` أو ثوابت `Color.kt` |
| D2 | كبير | `ACCENT_OPTIONS` تُعيد تعريف `AccentFills` حرفيًا — مصدران للحقيقة يتباعدان | `SettingsScreen.kt:87-92` مقابل `ui/theme/Color.kt` | ابنِ من `AccentFills+AccentNames` |
| D3 | كبير | شارة LIVE مكررة بلون خام في ملفين + تباين حدّي (~4.4:1 لنص صغير) | `ui/library/ChannelScreen.kt:217` + `ui/player/PlayerArea.kt:581` `Color(0xFFB71C1C)` | `LiveBadge+onLiveBadge` في `Color.kt` ووحّد الاستخدام |
| D4 | متوسط | أيقونة `Share` غير معكوسة (تشير يمينًا حتى في RTL) في 3 مواضع | `ui/playlists/PlaylistsScreen.kt:188`؛ `ui/queue/QueuePanel.kt:184`؛ `ui/player/PlayerArea.kt:690` | `Icons.AutoMirrored.Rounded.Share` |
| D5 | متوسط | `RoundedCornerShape(4/6/8/12/24.dp)` خام مبعثرة في ~12 ملفًا بينما `MyTubeShapes` (8/12/16/20/28) معرّفة ولا تُستخدم خارج الثيم | `PlaylistsScreen.kt:239,288,291`؛ `SearchRow.kt:87,92,182`؛ `DownloadsScreen.kt:130,153`؛ `MusicHome.kt:49` | `MaterialTheme.shapes.extraSmall/small/medium/large`؛ الشارات الصغيرة وحّدها على `extraSmall` |
| D6 | متوسط | الثيم المخصص لا يعرّف `surfaceContainer*` — المستخدم `surfaceContainerLow/High/Lowest` يسقط على defaults الداكنة/الفاتحة العامة، وفي وضع `black=true` (AMOLED) رمادي داكن بدل أسود | `SearchRow.kt:174`؛ `MusicHome.kt:50,60,94`؛ `PlayerArea.kt:111,747`؛ `PlayerComponents.kt:98,110` | عرّف `surfaceContainer*` في الثيمين + قيمة AMOLED، أو توقف عن استخدامها |
| D7 | متوسط | `Theme.kt` نفسه يحمل 11 `Color(0xFF` — مخالفة حرفية لنص السياسة (\"لا خارج `Color.kt`\") | `ui/theme/Theme.kt:23-29,41-47,82-88,100` | القرار الأرخص: وسّع الاستثناء في `ARCHITECTURE.md` (\"`Color.kt`+`Theme.kt`\") أو عرّف `White/ink` في `Color.kt` |
| D8 | متوسط | أهداف لمس <48dp + `contentDescription=null` على أيقونات تفاعلية (TalkBack أعمى) + `clickable` بلا role | grep `size(20/22/24.dp)` + `IconButton` + `contentDescription = null` في `ui/` | `contentDescription` ar/en لكل `Icon` تفاعلي؛ حد أدنى 48dp (أو `minimumInteractiveComponentSize`)؛ `role=Button` |
| D9 | صغير | `onLongClick={}` فارغة (؟) + ألوان ترجمة المستخدم (`Yellow/Black/Cyan…`) تُحسب مخالفة وهي إعفاء مشروع | `SearchRow.kt`؛ `SettingsScreen.kt:315-317` | احذف الفارغة أو نفّذها؛ أضف فئة إعفاء رابعة في السياسة (ألوان محتوى يختارها المستخدم فوق الفيديو) |
| D10 | صغير | `Black.copy(0.8f)` scrim مكرر في 8+ مواقع فوق الصور/الفيديو (مبرر سلوكيًا — نص فوق صورة) | `SearchRow/PlayerArea/MusicHome/PlaylistsScreen` | وحّد `Scrim+OnScrim` في `Color.kt` |
| D11 | ✅ إيجابي | RTL سليم إجمالًا: لا `Alignment.Left/Right` ولا `TextAlign.Left/Right` ولا `absoluteOffset`؛ كل الأسهم الاتجاهية `AutoMirrored`؛ `LocalLayoutDirection` حسب اللغة؛ كل `Typography` بـ`TextDirection.Content`؛ `CenterEnd` واعٍ للاتجاه (ليس عيبًا)؛ أيقونات الوسائط غير معكوسة عمدًا (صحيح per Material) | فحص `ui/` كاملًا | لا إجراء |

## المحور 6 — فجوات (parity/diagnostics/tests)

### (A) ميزات ناقصة مقابل YouTube-parity

| # | الفجوة | الدليل | الأولوية |
|---|--------|--------|----------|
| G-A1 | لا استلام روابط يوتيوب المشاركة — لا `ACTION_SEND`/`ACTION_VIEW`+BROWSABLE (المقترح #4 في FEATURES ما زال مفتوحًا) | `AndroidManifest.xml:35-38` intent وحيد MAIN/LAUNCHER | عالية — مسار اكتشاف أساسي |
| G-A2 | لا فحص تحديث — sideload بلا حلقة توزيع (المقترح #7 GitHub Releases لم يُنفذ) | لا `UpdateCheck/latestRelease` في `java/` | عالية — المستخدمون عالقون بصمت |
| G-A3 | لا مزامنة خلفية للاشتراكات/إشعارات فيديوهات جديدة — `refreshFeed()` يدوي فقط؛ لا WorkManager/Periodic/Alarm/BOOT | grep صفر حقيقي | متوسطة |
| G-A4 | لا تصدير/استيراد شامل (كل القوائم+المفضلة+السجل+الإعدادات) — موجود: استيراد اشتراكات OPML/JSON/CSV/ZIP + تصدير CSV لقائمة واحدة فقط | `YouTubeRepo.kt:372`؛ `MainActivity.kt:1051-1069` | متوسطة — فقدان عند تبديل الجهاز |
| G-A5 | لا Android Auto | grep `CarAppService/automotive` فارغ | منخفضة |
| G-A6 | لا Widget / لا App Shortcuts | grep فارغ | منخفضة |
| G-A10 | Equalizer: إعادة الربط عند تغير الجلسة يدوية قد تفوت على أجهزة تدوّر الجلسة بصمت | `EqManager.kt` + `:1796-1802,1856` | منخفضة |
| G-A11 | Cast: أساس مغلق (route+تحميل تلقائي v0.2.8) لكن بلا اختبار ميداني + receiver افتراضي فقط | `CastManager.kt:24` | متوسطة (ميداني) |
| مغلق ✅ | سرعة التشغيل (`pref_speed/pitch` حي+محفوظ)؛ الفصول (`ChapterItem+streamSegments` + نقر-للبحث)؛ التعليقات (`CommentPager` + ترقيم + `pref_show_comments`)؛ الخلفية (H1+KeepAlive specialUse OFF+إشعار MediaSession+إعفاء بطارية `:818`)؛ مؤقت النوم (deadline مطلق+ثبات+تلاشي+بعد المقطع)؛ DND جزئي (`applyAudioFocus :370` + `pref_audiofocus`) | — | — |

### (B) فجوات التشخيص

- لا crash reporting (sideload بلا vitals بديل — الاعتماد على تقارير يدوية فقط).
- لا StrictMode في الإصدار (Debug فقط) + لا ANR-watchdog + لا Timber/release-tree (Log مباشر يتسرب للـlogcat الإصداري).
- لا رموز أخطاء مستخدمة موحدة (`err_*` موجودة جزئيًا — راجع `res/values/strings.xml` العدد).
- تقرير التشخيص يدوي النسخ (S6 أعلاه) بلا تنقيح PII/URLs.
- `MediaCache.bytesUsed` يبالغ (spans ميتة F18) — رقم التشخيص مضلل.

### (C) فجوات الاختبار — صفر مجلد اختبار؛ `PlaylistDb` v9 بلا migration tests

---

## قائمة الإصلاح المرتبة (P0/P1/P2)

### P0 — قبل أي إصدار قادم (حرج: كراش/فقد/ليل مفتوح)

1. **F1 مؤقت النوم service-side** — التشغيل الليلي اللانهائي أسوأ من أي ميزة. (`MainActivity.kt:482` → `PlaybackService`)
2. **F2 تحقق طول الـpin** — ملفات مبتورة تُباع كناجحة. (`PinManager.kt:getTo`)
3. **D1 معاينة الترجمة المعكوسة** — لون خام + منطق مقلوب في الشاشة الأكثر حساسية لكبار السن. (`SettingsScreen.kt:314`)
4. **T3 تسرب `onCustomAction`** — سطر واحد `= null` في `onDispose/onDestroy`. (`PlaybackService.kt:29`)
5. **L2 PiP auto-enter اللاصق** — يحترم `setAutoEnterEnabled(pref)`. (`MainActivity.kt:223-243`)
6. **L3 age-gate الصامت** — تخطَّ + toast بدل الجمود الخلفي. (`:1588/:1498`)

### P1 — كبير (ANR/فساد/تسرب/سياسة)

7. T1 IO الـpin إلى `Dispatchers.IO` (`:1444-1488`).
8. T2 بناء `SimpleCache` خارج الرئيسي (`MediaCache.kt:47-54`).
9. F5 resume/cancel للتنزيلات (Range + Job) — شبكات متقطعة = إعادة من الصفر اليوم.
10. F6 سباق quick-pin (مفتاح تفاؤلي على UI).
11. F7 خلط بتاريخ + Next يحترم الخلط.
12. F8 تطبيق `subTrackIdx` عبر override (أو توثيق الصدق).
13. T4 بدء تفكيك God-Activity (`PlayerController` أولًا) — لا يُؤجَّل أكثر.
14. T5 إزالة `!!` من المسارات المرئية.
15. T6 `fadeJob` قابل للإلغاء.
16. T7 `EqManager.release()`.
17. T8 `collectAsStateWithLifecycle` + dependency.
18. L1 حفظ جلسة دنيا + استعادة (يقتل أكبر شكوى موت-عملية).
19. L4 إعادة قراءة `pref_keepalive` في `ON_RESUME`.
20. S1 إخراج APKs من git + `.gitignore` (`*.apk *.idsig`).
21. S4 قيد scheme `http/https` في `PlaybackUtils.kt:178`.
22. D2/D3 توحيد `AccentFills` + `LiveBadge` في `Color.kt`.
23. D4 `AutoMirrored.Share` في 3 مواضع.
24. D5/D6 توحيد shapes + تعريف `surfaceContainer*` (أو التوقف عن استخدامها).

### P2 — متوسط/صغير + سياسات + فجوات

25. S2/S3/S5/S6/S7/S8/S9 (backup D2D، FileProvider، clipboard sensitive، timeouts، صلاحيات).
26. F9/F10/F11/F12/F13/F14 (repeatAll+radio، تقليم الراديو، favKey واحد، sticky flag، audio override، sweep staging).
27. L5 BOOT receiver؛ L6 PiP actions؛ L7 إعادة تحميل بعد تغير الإقليم؛ L9 بادئة `local:/yt:`؛ L10 تطهير catch الصامت.
28. D7 توسيع استثناء السياسة (`Color.kt+Theme.kt`)؛ D8 a11y (descriptions + 48dp + roles)؛ D9/D10 تنظيفات.
29. G-A1 استلام روابط المشاركة (intent-filter)؛ G-A2 فحص تحديث GitHub Releases؛ G-A4 تصدير/استيراد شامل؛ G-A3 مزامنة خلفية اختيارية.
30. اختبارات: Room migration tests 1→9 + unit لـ`favKey/dedupe/queueIndex` + screenshot لأماكن التباين الحدّي.

---

## قائمة التحقق الميداني لمحمد (اختبار الجهاز — لا يُوكَّل)

> القاعدة: الوكيل لا جهاز ولا adb — كل ما يلي يُنفَّذ من محمد/beta على هاتف حقيقي (يفضل جهاز OEM قاتل للخلفية + Pixel مرجعي).

- [ ] مؤقت النوم: شغّل صوتًا → فعّل 5 دقائق → اسحب التطبيق away (ليس إغلاقًا) → هل توقف؟ (يفشل اليوم F1) → أعد بعد الإصلاح.
- [ ] مؤقت بعد reboot: اضبط مؤقتًا → أعد التشغيل → هل الملصق منطقي؟ (يفشل اليوم F3 بـuptimeMillis).
- [ ] موت العملية: شغّل قائمة → `adb shell am kill com.mohamedadel.mytube` أو \"Don't keep activities\" → أعد الفتح → هل عادت القائمة/الموضع؟ (يفشل اليوم L1).
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

---

## ملاحق

- الإيجابيات المثبتة: H1 ملكية ExoPlayer، ترحيلات Room 1→9 بلا fallback إصداري، كل شبكة `YouTubeRepo` على IO، single-flight للإلغاء، StrictMode Debug، لا WebView، لا cleartext، exported سليم، RTL سليم إجمالًا.
- ملاحظة سياسات: `ARCHITECTURE.md` يحتاج تحديثين صغيرين — (1) استثناء `Theme.kt` من قاعدة `Color(0xFF` أو توحيده، (2) فئة إعفاء رابعة لألوان محتوى الترجمة التي يختارها المستخدم.
- التوافق مع `AUDIT-2026-11-10.md`: هذا التقرير يوسّعه بالمحاور 2/5/6 ويضيف أدلة سطرية جديدة (F1/F2/L2/D1/S1) — لا يلغيه.

*وُلّد 2026-11-11 — تدقيق استاتيكي بستة محاور، بلا بناء، بلا push. الـcommit التالي يحفظ هذا الملف فقط.*

---

## تنفيذ P1 — 2026-11-11 (يُقرأ مع `nota-audit-status-2026-11-11.md`)

- نُفّذت البنود 7–24 كاملة: 17 ✅ + بند واحد ⏭️ (D4: أيقونة `AutoMirrored.Share` غير موجودة في نسخة `material-icons-extended` المثبتة — تحقق من الـjar).
- التفاصيل لكل بند + نتيجة البناء (`BUILD SUCCESSFUL`) في قسم P1 من ملف الحالة.
