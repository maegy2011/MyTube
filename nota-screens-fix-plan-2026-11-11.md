# MyTube — خطة إصلاح الشاشات الأربع (دراسة فقط، بلا كود) — 2026-11-11

> **النطاق:** دراسة + خطة فقط — لا تغييرات كود، لا بناء، لا push.
> **المهارات المطبقة:** `Android` (طبقة runtime، قواعد State/Recomposition/Keys/Coil/a11y) + `code-reviewer` (منهجية 4 مراحل: نطاق → تحليل → سياق → تقرير، تصنيف حرج/كبير/متوسط/صغير).
> **الحزمة:** `com.mohamedadel.mytube` — المسارات نسبية لجذر الريبو `projects/MyTube/`.

---

## 1️⃣ الشاشة الرئيسية (Home / Feed)

### البنية الحالية
- Home = حالة مشتقة (query فارغ + نتائج فارغة → `HomeEmptyState`) في `MainActivity.kt:4276–4305` — ليست route مستقلة؛ `goHome()` (~:3140) يصفّر flags فقط.
- `refreshTrending()` (`MainActivity.kt:1357–1381`): kiosk حسب `TREND_TABS`، أو region-search للأخبار، أو `refreshSuggestions()` لتبويب suggestions.
- `refreshSuggestions()` (`:1339–1355`): seeds = آخر 3 بحوث + عنصران trending؛ chips + فيديوهات (top-6 × seedين).
- `refreshFeed()` (`:2775–2816`): لكل قناة `oneChannel()` بـ try مستقل (RSS أولًا ثم extractor)، تقييد توازي 3، كاش Room 6 ساعات.
- `doSearch()` (`:2466–2510`): يلغي `searchJob`، يستدعي `YouTubeRepo.search`، خطأ → `searchError` + زر retry.
- `HomeResults.kt:56–146` (`HomeEmptyState`): تبويبات kiosk الثمانية + شارة الدولة + skeleton / empty + chips / `LazyColumn` من `SearchRow`.
- `FeedScreen.kt:47–147`: سحب مخصص (عتبة 360px)، بانر فلتر، skeleton / empty / قائمة بمفاتيح `itemsIndexed`.
- `SearchRow.kt:57–171`: بطاقة 16:9 + شارات + أفاتار + fav + ⋮ + `combinedClickable`.
- `SearchScreen.kt` (154 سطر) + `SearchViewModel.kt` (debounce 300ms) + `MiniPlayer` (`PlayerComponents.kt:76–160`).
- Coil مضبوط مركزيًا (25% ذاكرة + 100MB قرص + crossfade) في `MyTubeApp.kt:36–50`.

### نقاط القوة
- Kiosk حقيقية لكل تصنيف + بحث region صريح للأخبار.
- Feed مرن: try لكل قناة، RSS الرخيص أولًا، كاش Room، toggles.
- Skeleton يطابق هندسة البطاقة (لا قفزة layout).
- بحث نظيف: شاشة مستقلة، اقتراحات debounced، voice، استعادة سكرول.
- Mini-player فوق nav بترتيب YouTube، swipe-dismiss + إعادة فتح مع قفزة queue.

### مشاكل (بالدليل)
- 🔴 **حرج — Feed بلا ترتيب:** التعليق يقول newest-first (`:2773`) لكن `feedItems = all` (`:2812`) بلا `sortedBy` — كتل قنوات لا timeline.
- 🔴 **حرج — فشل trending صامت:** `YouTubeRepo.trending()` يبتلع كل شيء → `emptyList()` (`YouTubeRepo.kt:68,77,98`)، والواجهة تعرض chips بلا retry (`HomeResults.kt:92–109`) — عكس مسار search-error الذي فيه retry (`MainActivity.kt:4324–4327`).
- **كبير — مفاتيح LazyColumn غير مستقرة:** بادئة index (`FeedScreen.kt:138`، `MainActivity.kt:4398`) تُبطل diffing وتكسر السكرول وتعيد تحميل الصور. الصح في `HomeResults.kt:131`.
- **كبير — سحب مخصص غير مكتشَف:** عتبة 360px بلا نص release، غير مرئي لـ TalkBack، ولا سحب أصلًا في Home/trending.
- **كبير — تبويب suggestions يضرب شبكة بلا حماية** (`:1339–1355`): 3 اقتراحات + بحثان كاملان كل زيارة، بلا كاش/debounce/cancel.
- **متوسط — قائمة trending بلا scroll state** (`HomeResults.kt:111–137`): السكرول يضيع كل تبديل/تدوير.
- **متوسط — بطاقات بلا role/label** (`SearchRow.kt:74–76`؛ نفس الشيء `MusicCard` وصفوف الاقتراحات وبانر الفلتر).
- **صغير — glyph `"▶"` حرفي** (`MusicHome.kt:97`) يُقرأ كنص؛ **زر onRemove 36dp** (`SearchRow.kt:160`) تحت 48dp.

### فجوات YouTube-parity
لا pull في Home، لا pagination (صفحة أولى فقط)، لا Shorts shelf/أرفف أفقية، لا شارات "جديد"/فواصل unread، لا فلتر محتوى في Home، لا تمييز offline مقابل فارغ.

### فجوات وصول (a11y)
السحب بلا semantics؛ البطاقات بلا `Role.Button`/`onClickLabel`؛ إشارات لونية فقط لبعض الحالات؛ أهداف لمس صغيرة.

### إصلاحات مقترحة
**P0:**
- P0-H1 ترتيب Feed (newest-first على `publishedAt` من RSS/extractor أو round-robin) — `MainActivity.kt:2812`.
- P0-H2 حالة خطأ trending مع retry (تمرير `trendingError` وعرض زر مثل search-error) — `MainActivity.kt:1376–1378` + `HomeResults.kt:92–109`.
- P0-H3 مفاتيح مستقرة `v.id ?: v.url ?: ...` — `FeedScreen.kt:138` + `MainActivity.kt:4398`.
**P1:**
- P1-H4 سحب مشترك بـ semantics + إضافة سحب لـ Home + `rememberLazyListState` (يحل فقدان السكرول).
- P1-H5 `suggestJob` يُلغى عند تبديل التبويب + كاش chips 10 دقائق.
- P1-H6 semantics للبطاقات (role + onClickLabel) + Icon بدل `"▶"` + رفع onRemove إلى 48dp.
**P2:**
- P2-H7 رؤوس قنوات/فواصل "N new" + تمييز offline في `FeedScreen`.
- P2-H8 pagination trending (`nextPage`) + زر "عرض المزيد".
- P2-H9 تحويل `VideoListSkeleton` إلى `Column` عند التداخل.

---

## 2️⃣ الموسيقى + الشاشات المتضمنة

### البنية الحالية
- `ui/library/MusicHome.kt` (154): `MusicSourceSegment` (:44–69)، `MusicCard` (:73–112)، `MusicShelf` (:115–154، مفتاح `url+title hash` في :140).
- YTM tabs + paging: حالة `scSource/scKind/scNextPage/scHasMore/scLoadingMore/scListState` (`MainActivity.kt:518–527`)؛ `doScSearch` (:2570–2633، فلتر kind→music_* في :2578–2583، retry واحد)؛ `loadScMore` (:2660–2689) + كاشف الذيل (:2692–2696)؛ chips للأنواع YTM فقط (:3996–4010).
- بحث الموسيقى: `SearchScreen.kt` (مضمّن `fillMax=false`) + `SearchViewModel.kt` (debounce 300ms، cap 8) + `recentMusicSearches` (max 6).
- Radio: بذرة `startRadio` (:1805–1827، take 15، radioOn) + إعادة ملء `playNextInQueue` (:1884–1918، حتى 10، trim آخر 50، التفاف repeatAll)؛ نقاط دخول: long-press (:4553) + Favorites (:4147–4150)؛ `related` حتى 20.
- الذوق: `util/TasteProfile.kt` (76، محلي خالص، STOP ثنائي اللغة، max 8 terms/picks) + `refreshTaste` (:2635–2658، إثراء شبكي واحد لأعلى term) يُستدعى كل فتح تبويب (`LaunchedEffect` في :2696)؛ ملف يتيم `ui/library/MusicTasteSection.kt` (106 سطر، صفر call sites).
- الصوت المحلي: `util/LocalAudio.kt` (90) + `ui/library/LocalAudioScreen.kt` (203، chips مجلدات + long-press exclude + strict/compat + `isCurrent/isPlaying=false` ثابت في :164–165).
- المشغل: `playVideo` (:1559+، radio off للنقرة المفردة :1566–1571، `favKey` في :1575)؛ `PlayerController.kt` (رياضيات index فقط)؛ `PlayerArea.kt` (904، نفس واجهة الفيديو — بلا artwork صوتي/lyrics/related)؛ `QueuePanel.kt` (244، بلا radio badge/shuffle).

### نقاط القوة
- Source segment صادق (SC مقابل YTM) + kind filters مطابقة للـ extractor.
- Taste على الجهاز، ثنائي اللغة، بلا حساب — يحترم الخصوصية.
- Radio refill آمن (trim + wrap + favKey dedupe).
- عزل فشل playlist-cards؛ paging بذيل + زر More؛ `upgradeThumb/bestThumb` حتى w540.

### مشاكل (بالدليل)
- 🔴 **حرج — راديو SoundCloud ميت:** `scStream` يُرجع `related` فارغًا (`YouTubeRepo.kt:~560–578`) → `toast_radio_fail` دائمًا (`MainActivity.kt:1818`).
- 🔴 **حرج — راديو الصوت المحلي مسموم:** `content://` يدخل queue (`:1596–1614`) لكن refill يحتاج `related=null` (`:1601–1605`) → توقف صامت؛ لا `isLocal` guard في `startRadio` (`:1805`).
- **كبير — `MusicTasteSection.kt` يتيم:** صفر استخدام، والمنطق مكرر inline (`:4013–4061`).
- **كبير — refreshTaste يقصف شبكة كل فتح تبويب** (`:2696` + `:2635–2658`): DB + بحث YTM بلا TTL/debounce.
- **كبير — مفاتيح shelf غير مستقرة** (`MusicHome.kt:140`): نفس الأغنية في picks وmix بمفتاحين.
- **كبير — لا correction parity:** `suggestion/corrected` موجودة في `SearchPage` ولا تُعرض في الموسيقى.
- **كبير — شاشة Local لا تُبرز المشغَّل أبدًا** (`LocalAudioScreen.kt:164–165` ثابت false).
- **متوسط — segment بلا Role.Tab/selected** (`MusicHome.kt:57–64`)؛ **بطاقات بلا onClickLabel + وصف مكرر** (99 مقابل 106) + badge حرفي؛ **nested clickable على Folder chips** (`:111–116`)؛ **radio بلا حالة مرئية** (`radioOn` في :336 لا يظهر)؛ **لا scroll-to-top** عند تغيير source/kind؛ **تبويب albums يبدو مكسورًا** (tracks فارغة بالتصميم).
- **صغير:** `taste_mix_fmt` داخل try-catch (:4050)؛ STOP ناقص؛ cutoff الـ 30s غير معلن.

### فجوات YouTube-parity
رفّان فقط (8+6) مقابل ~10 في YTM؛ لا artist hub / album detail / lyrics / related-songs / recently-played rail / بحث أثناء الكتابة / offline-mix / إشارة dislike.

### فجوات وصول
Segment صناديق بلا tab semantics؛ badge يُقرأ حرفيًا؛ وصف الصورة مكرر؛ exclude بالضغط المطول فقط بلا بديل؛ تباين badge في الفاتح غير مدقق؛ لا collection semantics للأرفف.

### إصلاحات مقترحة
**P0:**
- P0-M1 إخفاء Radio لعناصر SC أو بذرة من `scSearch` — guard في `MainActivity.kt:1805,4553`.
- P0-M2 guard محلي: `startRadio` يرفض `isLocal` + تخطي refill للمحلي — `:1805` + `:1892` (`isLocal` جاهز في `LocalAudio.kt:38`).
- P0-M3 حذف `MusicTasteSection.kt` أو توحيد الأرفف عبره — مالك واحد.
**P1:**
- P1-M4 شارة radioOn + زر إيقاف في `QueuePanel.kt:60–70`.
- P1-M5 كاش taste بـ TTL 5 دقائق + debounce + `rememberSaveable`.
- P1-M6 مفاتيح `favKey` مستقرة + dedupe picks/mix.
- P1-M7 صف correction في نتائج الموسيقى (مرآة البحث الرئيسي).
- P1-M8 تمرير playing-url إلى `LocalAudioScreen` وإبراز المشغَّل.
- P1-M9 بديل قائمة ⋮ لـ folder-exclude + hint.
**P2:**
- P2-M10 semantics (Tab/Card/badge) + إلغاء ازدواج الوصف.
- P2-M11 `scrollToItem(0)` بعد تغيير source/kind/query.
- P2-M12 نص تفسيري لتبويب albums + توسيع STOP + إعلان قاعدة 30s.

---

## 3️⃣ المكتبة + الشاشات المتضمنة

### البنية الحالية
| ملف | الدور |
|---|---|
| `ui/library/LibraryScreen.kt` (183) | Hub فقط: صفوف History/Favorites/Playlists/Downloads/LocalAudio/Groups/Feed/Import-subs + اشتراكات inline مع `feedCounts` |
| `ui/playlists/PlaylistsScreen.kt` (549) | CRUD محلي كامل + محرر staged بـ txn واحد + sorts + multi-select + picker + share/export CSV |
| `ui/playlists/HistoryScreen.kt` (216) | Paging-3 (all/pinned/unpinned) + بحث محلي + buckets زمنية + pin/replay/delete/clear |
| `ui/playlists/FavoritesScreen.kt` (106) | قائمة `FavoriteEntity` + play/remove/clear + Radio |
| `ui/library/DownloadsScreen.kt` (263) | مدير pins من Room (لا FS scan) + pending مع progress/cancel + orphans + `totalBytes` |
| `ui/library/ChannelGroups.kt` (258) | Groups CRUD + تفاصيل أعضاء |
| `ui/library/ChannelScreen.kt` (246) | هيدر + 5 تبويبات (Videos/Shorts/Live/Playlists/About مقتضب 220 حرف) |
| `ui/library/FeedScreen.kt` (147) | فيد مدمج + فلتر + سحب مخصص |
| `ui/library/LocalAudioScreen.kt` (203) | صوت الجهاز + chips مجلدات + strict/compat |
| `ui/results/RemotePlaylistView.kt` (124) | عرض عن بُعد: قراءة+تشغيل فقط |
| `ui/queue/QueuePanel.kt` (244) | reorder ±1 + remove + play-from + save-as-playlist + قائمة long-press |
| `data/local/PlaylistDb.kt` (551) | Room v9 + `savePlaylistEdits` txn + history paging/search/trim-1000 + `topPlayed` + migrations 1→9 |
| `data/local/HistoryPaging.kt` (41) | `PagingSource` يدوي + حماية `IN ()` |
| `MainActivity.kt` | `exportPlaylistCsv` (:1171–1210) + clears + nav library + lambdas (:4199–4212) |

### نقاط القوة
- Hub-and-spoke حقيقي والرجوع دائمًا للمكتبة.
- محرر staged بحفظ transaction واحدة.
- History أنضج شاشة (Paging + tabs + states).
- Downloads يفصل pending/done + إلغاء تعاوني + منظف orphans + تقدير حجم حي.
- Covers مشتقة (`playlistThumbs`) بلا migration؛ i18n منضبط (`R.string`).

### مشاكل (بالدليل)
- 🔴 **حرج — تدمير بلا تأكيد:** `onClear` favorites (`FavoritesScreen.kt:58–60`) وhistory (`HistoryScreen.kt:71–73`) و`onClearItems` (`PlaylistsScreen.kt:353`) و`onClearAll` pins (`DownloadsScreen.kt:89–93`) وbulk-remove (`:263`) — كلها فورية.
- 🔴 **حرج — params ميتة:** `onMoveItem/onDeleteItem` (`PlaylistsScreen.kt:98–99`) صفر call sites — أي caller يمرر lambdas حقيقية يُتجاهل.
- **كبير — لا CSV import** (export فقط `:1171–1210`)؛ **remote playlists درجة ثانية** (`RemotePlaylistView.kt:51–123` بلا save/pin-all)؛ **بحث history مقصوص 200 silently** (`PlaylistDb.kt:historySearch` بلا لافتة)؛ **favorites بلا بحث/sort** (جدار عند المئات)؛ **empty downloads عارٍ** (`:144–157` نص فقط)؛ **group feed بنسخة مضللة** (`ChannelGroups.kt:175` يعطّل الزر، وFeed يعرض `feed_empty` عامًا).
- **متوسط:** صف orphans محشور في الهيدر (`:56–94`)؛ `Spacer` مزدوج (`:230–231`)؛ mode chips تظهر فقط والقائمة فارغة (`:282–287`)؛ multi-select يتصفّر عند التبديل وبلا select-all؛ Queue بلا empty-CTA/clear-all/swipe؛ بانر الفلتر clickable بلا affordance؛ سحب 360px غير قياسي؛ About القناة وصف فقط.
- **صغير:** 42× `contentDescription=null` على أيقونات ذات معنى؛ tint حذف غير موحد؛ magic string `"audio-only (default)"`؛ buckets منتصف-الليل المحلي (DST)؛ CSV يُسقط `videoId` واسم القائمة.

### فجوات YouTube-parity
لا Watch-later افتراضية؛ لا checkmarks "محفوظ في"؛ لا privacy/description/total-duration؛ لا recently/most-played رغم وجود `topPlayed`؛ لا bulk-subs/OPML-export؛ لا defaults لجودة/mوقع pins؛ لا مؤشر offline-first؛ queue بلا drag/swipe/persist-note.

### فجوات وصول
أزرار أيقونية معتمدة على نص الصف؛ أزرار queue 32dp (`QueuePanel.kt:122–141`)؛ لا `stateDescription` للتحديد؛ progress بلا إعلان %؛ emoji-covers معتمة؛ pickers طويلة بلا بحث؛ long-press-exclude بلا بديل.

### إصلاحات مقترحة
**P0:**
- P0-L1 ✅ (2026-11-11 — `a518c1f` + `7af7b8a`: `ConfirmDeleteDialog` مشترك لكل تدمير (favorites/history/clear-items/clear-pins/bulk/row-delete) — الملفات أعلاه + تمرير counts في `MainActivity.kt`.
- P0-L2 ✅ (2026-11-11 — `7af7b8a`: حُذفت params الميتة + حُدّث callers `:4199–4212`).
- P0-L3 ✅ (2026-11-11 — `a518c1f` + `7af7b8a`: empty-state للـ Downloads (أيقونة + hint + زر "كيف تثبّت" بخطوات)).
**P1:**
- P1-L4 **CSV import** (`importPlaylistCsv` بجانب export + launcher + `videoId` في التصدير أولًا).
- P1-L5 **Remote → local save** (Save-as-playlist + Pin-all في `RemotePlaylistView`).
- P1-L6 بحث + sort للـ Favorites (مرآة History + `favoritesSearch` DAO).
- P1-L7 لافتة "أول 200" في بحث history.
- P1-L8 نسخة group-feed فارغة مميزة.
- P1-L9 إظهار mode chips في محرر Edit + subtitle بالوضع الحالي.
**P2:**
- P2-L10 استخراج orphan-banner + توحيد tints + ثابت للنص السحري.
- P2-L11 ممر a11y (48dp + descriptions + state + progress + بحث pickers).
- P2-L12 قسم "الأكثر إعادة" من `topPlayed` في `LibraryScreen`.
- P2-L13 سحب queue + clear-all + توثيق البقاء.

---

## 4️⃣ الإعدادات

### البنية الحالية
- `ui/settings/SettingsSections.kt` (165): `SettingsSection` (:30–65، كلها `expandedDefault=false`) + `DropRow` (:69–121) + `SwitchRow` (:125–165).
- `ui/settings/SettingsScreen.kt` (734): UI خالص (~60 param + ~45 callback)، 7 أقسام `LazyColumn` (:268–660): اللغة/الترجمة (مع **معاينة subtitle** :336–350)/الصوت/المشغّل/المظهر/التنزيلات (update-check + export/import :672–680)/البيانات + 4 `AlertDialog` تأكيد (:235–266).
- `MainActivity.kt`: theme/accent live (`themeLive/accentLive` :182–183، بلا recreate)؛ region reload (:3555–3563)؛ UI-lang recreate (:3565–3571)؛ subtitle live (`subStyleFromPrefs` في :3459)؛ `checkUpdate()` (:2431–2444)؛ `exportFullBackup()` (:2226+) + `importFullBackup()` (:2309+، dedupe favorites، لاحقة `" (import)"`)؛ `copyDiagnostics()` (:2446–2463).
- `data/UpdateCheck.kt` (41): `latestTag` + `isNewer`؛ `ui/player/SubtitleStyle.kt` (52)؛ `ui/theme/Theme.kt` (132).

### نقاط القوة
- تدفق أحادي حقيقي (صفر state في الشاشة).
- 7 بطاقات قابلة للطي بدل ~88 صفًا؛ `DropRow/SwitchRow` يقتلان 20+ radio lists.
- تطبيق حي للمظهر والترجمة (معاينة WYSIWYG صادقة عدا edge).
- كل تدمير في الإعدادات مؤكد بـ dialogs مع counts.
- Update dormant-عند-غياب-الإعداد + timeouts قصيرة؛ backup شامل data+prefs.

### مشاكل (بالدليل)
- **كبير — `isNewer` مقارنة نصية لا semver** (`UpdateCheck.kt:33–37`): أي tag مختلف = "أحدث" في الاتجاهين.
- **كبير — المعاينة تتجاهل edge** (`SettingsScreen.kt:336–350`): `subEdge/subRole` بلا انعكاس — أدق إعداد مقروئية لا يُتحقق.
- **كبير — accent dots 40dp بلا labels** (`:596–612`): تحت 48dp + تمييز لوني فقط.
- **كبير — `DropRow` سطر واحد ellipsis** (`SettingsSections.kt:96–104,111`): أسماء عربية طويلة تُقطع.
- **متوسط — expand/open بـ `remember` لا `rememberSaveable`** (`SettingsSections.kt:36,81`): التدوير يطوي كل شيء.
- **متوسط — import يبتلع أخطاء العناصر + لاحقة عمياء** (`MainActivity.kt:2309–2360`): `catch{}` لكل playlist بلا تقرير + `" (import)"` دائمًا بلا كشف مكرر.
- **متوسط — backup في `cacheDir/shared` بلا تنظيف** + مشاركة `prefs.all` كاملة.
- **متوسط — لغات UI ثنائية hardcoded** (`:77–80`) مقابل 5 لغات محتوى.
- **متوسط — صفوف radio-mimic بأيقونات `contentDescription=null`** (`:526–540,627–650,700–734`) غير مرئية للقارئ.
- **صغير:** edge codes إنجليزية خام (`:381–385`)؛ speed/pitch 6 قيم + label مشترك؛ mute label متقلب (`:474–478`)؛ cache-max بلا off؛ sliders بلا content-description.

### فجوات YouTube-parity
لا جودة per-network؛ لا master-switch ترجمة دائمة؛ سرعة 6 قيم عمومية بلا per-video؛ لا font-size/density/reset-defaults؛ pins بلا per-item quality/Wi-Fi-only/شريط تخزين؛ لا بحث داخل الإعدادات؛ update toast-only بلا changelog/زر؛ backup بلا timestamp/auto.

### فجوات وصول
Headers بلا `Role.Button/stateDescription`؛ radio-mimic بلا `RadioButton/selectable`؛ dots لونية 40dp؛ sliders بلا semantics؛ disabled باللون فقط؛ ellipsis بلا مسار قراءة موسّع.

### إصلاحات مقترحة
**P0:**
- P0-S1 إصلاح `isNewer` semver حقيقي + unit test — `UpdateCheck.kt:33–37`.
- P0-S2 رسم edge في المعاينة (نفس ثوابت `SubtitleStyle.kt:40–46`) — `SettingsScreen.kt:336–350`.
- P0-S3 توطين edge entries (`sub_edge_*` ar+en) — `:381–385`.
**P1:**
- P1-S4 semantics للheaders + تحويل AF/age/pin-loc إلى `RadioButton/selectable` — `SettingsSections.kt:38–58` + `:526–540,627–650,700–734`.
- P1-S5 إعادة بناء accent (48dp + labels + selectable group) — `:593–613`.
- P1-S6 `rememberSaveable` للطي/الفتح — `SettingsSections.kt:36,81`.
- P1-S7 semantics للsliders + تثبيت label الـ mute.
- P1-S8 متانة import (عدّ المتخطى + كشف مكرر + validate `v`).
**P2:**
- P2-S9 بحث داخل الإعدادات.
- P2-S10 dialog تحديث مع changelog + زر تنزيل.
- P2-S11 صف metadata للـ backup + auto-backup.
- P2-S12 سرعات أدق + resource للغات UI + reset-per-section.

---

## ✅ الطابور الموحد (أولوية عبر الشاشات)

| # | الإصلاح | الملفات | الأثر |
|---|---|---|---|
| P0-1 | تأكيد كل تدمير في Library | `PlaylistsScreen` + `FavoritesScreen:58` + `HistoryScreen:71` + `DownloadsScreen:89` | ✅ `7af7b8a` + `a518c1f` (row-delete) |
| P0-2 | ترتيب Feed + مفاتيح مستقرة | `MainActivity.kt:2812` + `FeedScreen.kt:138` + `MainActivity.kt:4398` | timeline صح + سكرول/صور |
| P0-3 | حالة خطأ trending مع retry | `YouTubeRepo.trending` + `HomeResults.kt:92–109` | offline قابل للفهم |
| P0-4 | guards الراديو (SC + محلي) | `MainActivity.kt:1805,1892,4553` | إيقاف فشل صامت |
| P0-5 | توحيد/حذف `MusicTasteSection.kt` | ملف يتيم + `:4013–4061` | إيقاف تضليل المحررين |
| P0-6 | إصلاح `isNewer` + معاينة edge + توطين edge | `UpdateCheck.kt:33` + `SettingsScreen.kt:336,381` | تحديثات صادقة + ترجمة مرئية |
| P0-7 | تفعيل/حذف `onMoveItem/onDeleteItem` | `PlaylistsScreen.kt:98–99` + callers | ✅ `7af7b8a` (حذف + تحديث callers) |
| P1-1 | semantics البطاقات/الأقسام/Radio-rows/Accent/Sliders | `SearchRow`, `MusicHome`, `SettingsSections`, `SettingsScreen`, `QueuePanel` | TalkBack صالح للاستعمال |
| P1-2 | خنق suggestions + TTL للـ taste | `MainActivity.kt:1339,2635,2696` | شبكة/battery |
| P1-3 | CSV import + Remote→local save + بحث Favorites | `MainActivity.kt:1171` + `RemotePlaylistView` + DAO | parity الت portable |
| P1-4 | إبراز المشغَّل المحلي + scroll-to-top + correction | `LocalAudioScreen` + `scListState` + `doScSearch` | تماسك الموسيقى |
| P1-5 | متانة backup/import + `rememberSaveable` للطي | `MainActivity.kt:2309` + `SettingsSections.kt:36,81` | ثقة + حالة |
| P2 | بحث إعدادات + dialog تحديث + shelves/filters polish + a11y targets + queue drag | متفرقة أعلاه | تلميع |

---

## 🧪 قائمة الفحص الميداني لمحمد (بلا adb — جهاز حقيقي، واجهة عربية)

**Home:** اشترك 3+ قنوات وتحقق newest-first عبر القنوات (مكسور الآن)؛ وضع طائرة → افتح Home (منتظر: نسخة خطأ + retry)؛ اسحب feed (يُطلق مرة)؛ تنقّل سريع بين 8 trend tabs على بيانات (لا spinners متراكمة)؛ سكرول عميق + تدوير + رجوع (يضيع الآن)؛ TalkBack لبطاقة (فعل "play {title}")؛ RTL البطاقات والشارات؛ fling على جهاز ضعيف قبل/بعد المفاتيح؛ قناة ميتة واحدة لا تكسر Feed؛ mini-player فوق nav + إعادة فتح على الصف الصحيح.
**Music (12):** رفوف فارغة بلا history ثم بعد 3 فيديوهات (picks ≤8 + mix ≤6)؛ SC→YTM والأنواع (albums بطاقات فقط، artists تفتح قناة)؛ paging للذيل (auto-append + More، عدّ المكرر)؛ radio YT حتى النهاية 3 مرات (refill ≤10)؛ radio SC = `radio_fail` (سجّل)؛ radio محلي = توقف صامت (سجّل)؛ فتح Music 5 مرات بطائرة ON (لا ANR)؛ صوت محلي strict/compat + exclude/restore + غياب highlight (سجّل)؛ TalkBack للsegment/البطاقات/badge؛ queue (PlayNext×2 + End×1 + save-as-playlist + Play-All يطفي radio)؛ RTL؛ thumbs w540 مقابل t500.
**Library (11):** مسح تدميري (favorites/history/clear-items/clear-pins/bulk — كلها بلا تأكيد الآن)؛ CSV export (عربي/اقتباس/فواصل، لاحظ غياب `videoId`)؛ تأكيد غياب import؛ remote playlist بلا Save/Pin-all؛ history 50+ فيديو + بحث عربي + `%`/`_` + سلوك cap-200؛ pins (صوت+فيديو+multi-sub، إلغاء، kill أثناء pin→orphan→clean)؛ groups (create/rename/delete + flag unsubscribed + نسخة group-feed)؛ queue (أسهم الأطراف، play-from-middle، اسم مكرر، تدوير)؛ local-audio permission-denied + exclude + rescan؛ TalkBack للمكتبة + أزرار 32dp؛ RTL headers.
**Settings (10):** كل البطاقات مطوية أولًا ثم التدوير يطوي (سجّل)؛ edge shadow/raised/none لا يغير المعاينة لكن يغير المشغل (وثّق)؛ edge إنجليزية في عربي (screenshot)؛ dots صعبة + بلا labels + ثيمات فورية مع بقاء queue؛ update بلا Wi-Fi (toast ≤15s) وtoast-only؛ export→import مرتين (تكرار `" (import)"`) + JSON فاسد يدويًا؛ TalkBack للheaders/radio/sliders/dots؛ أسماء دول طويلة + mute/slider؛ EQ (مدعوم/غير مدعوم)؛ kill بعد تغيير prefs → الثبات.

---

*وُضع savepoint git سابق لهذه الدراسة؟ يُنشأ commit واحد لهذا الملف فقط (بلا push) بعد الكتابة.*

---

## ملحق (أ): نتائج دراسة موازية — لم تُدمج في المتن لتعارض المصادر، تُتحقق قبل التنفيذ

> كتب زميل دراسة موازية بنفس الاسم (commit `fa5fdcc`) قبل اكتمال هذه الدراسة. النقاط أدناه فريدة من نسخته، تُدرج هنا حفظًا لعمله — تُتحقق file:line قبل أي إصلاح.

1. **🔴 حرج (محتمل) — `LaunchedEffect(prefCountry)` مزدوج** (`MainActivity.kt:1381` + `:1979`): مشغّلان يستدعيان `refreshTrending()` → طلبان متوازيان وسباق نتائج. **تحقق:** هل السطر 1979 ما زال موجودًا في النسخة الحالية أم بقايا دراسة قديمة؟ ثم الدمج في موضع واحد + cancel للطلب السابق.
2. **🔴 حرج (محتمل) — `values-en/strings.xml` يفتقد مفاتيح `update_*` و`backup_*`**: الواجهة الإنجليزية تعرض أزرار التحديث/النسخ بالعربية (`SettingsScreen.kt:648–652`). **تحقق:** `grep -c 'update_\|backup_' app/src/main/res/values-en/strings.xml` مقابل `values/strings.xml` — إن صحّ، الاستكمال P0.
3. **🟠 كبير (محتمل) — `skipSilence` يُعيد تشغيل Activity** (`MainActivity.kt:3743`: `stopService+delay(900ms)+recreate()`). **تحقق:** السطر موجود؟ إن صحّ → إعادة بناء المسار الصوتي فقط أو `applyOnNextVideo` + شارة.
4. **🟡 متوسط — قوائم بلا `key` إضافية:** `HistoryScreen.kt:109` (فرع البحث)، `LocalAudioScreen.kt:139`، `QueuePanel.kt:81`، `DownloadsScreen.kt:115` (تقدم التنزيل يُعيد تركيب الصف). مقابل: `HomeResults.kt:144` و`PlaylistsScreen.kt:206` و`HistoryScreen.kt:184` فيها مفاتيح ✅. الإصلاح يوسّع P0-H3 ليشملها.
5. **🟡 متوسط — `trending` المبدئية إنجليزية صلبة** (`MainActivity.kt:1335`): تُعرض قبل التحميل وتكسر التعريب → تعريب/تحميل كسول من `strings.xml`.
6. **🟡 متوسط — منطق العرض الثلاثي** (`MainActivity.kt:4283`): `results.isEmpty() && query.isBlank()` لا يميز no-network عن empty → فصل `feedError/noNetwork` (مكمل لـ P0-H2).
7. **🟡 متوسط — `scKind` لا يُصفَّر عند تبديل `scSource`** (`MainActivity.kt:3990+`): بحث "ألبومات" في SC ثم YTM يعرض نوعًا غير مدعوم بصمت → تصفير + رسالة صريحة.
8. **🟡 متوسط — `MusicTasteSection.kt` تضخم باراميترات (14)**: يعيد تركيب القسم كاملًا مع كل تغيير ذوق → تفكيك إلى state-holder (بديل/مكمل لـ P0-M3).
9. **ملاحظة خلافية — cap قائمة الراديو:** الدراسة الموازية تدعي نموًا بلا حد (`startRadio :1805`)؛ هذه الدراسة وجدت trim للرأس (آخر 50 + القادم، `:1900–1907`). **الحسم ميداني/كود:** قراءة `playNextInQueue` الحالية — إن كان الـ trim موجودًا فلا مشكلة، وإلا فـ cap 200 + نافذة منزلقة P0.
10. **صغير — `MusicHome.kt:99` صورة 140dp بلا placeholder/crossfade** → وميض عند التمرير (Coil مركزي فيه crossfade — تحقق هل يسري على `MusicCard`).

*أُضيف هذا الملحق 2026-11-11 حفظًا لعمل الزميل — لا يلغي المتن، يُحسم بالتحقق قبل التنفيذ بأمر محمد.*
