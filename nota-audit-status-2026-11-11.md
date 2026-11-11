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
- (يُملأ بعد الـcommit)
