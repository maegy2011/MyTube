# MyTube — خطة إصلاح 4 شاشات (دراسة فقط، بلا كود) — 2026-11-11

> النطاق: `projects/MyTube` — الحزمة `com.mohamedadel.mytube`
> المنهج: قراءة فعلية لملفات `.kt` (مع أدلة `file:line`) وفق مهارتي **`android`** (`skills/android/SKILL.md` — طبقات build/install/runtime/store، القواعد 1-9، Compose/state/R8) و**`code-reviewer`** (`skills/code-reviewer/SKILL.md` — المراحل الأربع: النطاق → تحليل آلي/يدوي → سياق → تقرير مصنّف بالخطورة).
> ممنوع في هذه الخطة: أي تعديل كود، أي بناء APK، أي push. ملف واحد فقط + commit محلي.

---

## 1) الشاشة الرئيسية Home / Feed

### 1-1. البنية الحالية (files + responsibilities)

| الملف | الدور | دوال/رموز مفتاحية |
|---|---|---|
| `MainActivity.kt` (4564 سطر) — المنسّق الأوحد `MyTubeApp()` ‏(:301) | كل التوجيه flags وليس NavHost؛ حالة الهوم + الترند + البحث + الفيد | `refreshSuggestions()` ‏(:1339)، `refreshTrending()` ‏(:1356)، `LaunchedEffect(prefCountry)` ‏(:1381 + تكرار :1979)، `refreshFeed()` ‏(:2775) + `oneChannel()` ‏(:2782)، `doSearch()` ‏(:2466)، `doScSearch()` ‏(:2569)، `goHome/goLibrary/goSettings/goSC` ‏(:3147-3173)، `smartBack()` ‏(:3018)، فرع العرض ‏(:4283+)، المسار `showFeed → FeedScreen` ‏(:3956)، `showSearch → SearchScreen` ‏(:3309)، `MiniPlayer` ‏(:3178) |
| `ui/library/FeedScreen.kt` ‏(:47) | واجهة اشتراكات RSS: فلتر + سحب مخصص + قائمة | `shown` ‏(:60)، `pullConn` عتبة 360px ‏(:66-87)، `LazyColumn` ‏(:122) |
| `ui/results/HomeResults.kt` | تبويبات الترند + هيكل الفارغ/الهيكلي | `TREND_TABS` ‏(:42)، `HomeEmptyState()` ‏(:56)، `ScrollableTabRow` ‏(:76)، `LazyColumn` ‏(:108)، `items(trendingItems, key=…)` ‏(:144) |
| `ui/results/SearchRow.kt` ‏(:59) | بطاقة الفيديو الموحدة + `PlaylistCard` ‏(:170) | صورة 16:9 ‏(:75-95)، أزرار fav/⋮ ‏(:150-157)، زر `onRemove` ‏(:160-164) |
| `ui/results/VideoCardStates.kt` ‏(:38) | هيكل التحميل `VideoCardSkeleton()` | — |
| `ui/search/SearchScreen.kt` ‏(:154) + `SearchViewModel.kt` ‏(:18) | طبقة البحث + `svm.onQueryChange` | — |
| `ui/player/PlayerComponents.kt` ‏(:76) | `MiniPlayer` + `EqualizerBars` | — |
| `data/youtube/YouTubeRepo.kt` | `trending(region,kiosk)` ‏(~:1375 عبر MainActivity)، `suggestions(seed)` ‏(:1345)، `search()` ‏(:171)، `feedFor/channelVideos` (عبر `oneChannel`) | — |

### 1-2. نقاط القوة

1. فصل حالات loading/empty/error في فرع العرض (:4283-4333) + زر Retry.
2. `HomeResults` يعطي مفتاحًا مستقرًا للعناصر (:144) — يمنع قفز القائمة.
3. `smartBack` (:3018) يرتب الأولويات (قناة → تعليقات → ملء شاشة → موسّع → بحث…) — سلوك متوقع.
4. `refreshSuggestions` يبذر من `recentSearches.take(3)+trending.take(2)` — تخصيص محلي بلا شبكة إضافية.
5. `MiniPlayer` فوق `NavigationBar` ومخفي في الحالات الصحيحة (`playerExpanded/showSearch/showSC/fullscreen/PiP`).

### 1-3. مشاكل (بالخطورة + الدليل)

- **🔴 حرج — `MainActivity.kt:1381 + :1979` تكرار `LaunchedEffect(prefCountry)`**: مُشغّلان مزدوجان يستدعيان `refreshTrending()` → طلبان شبكة متوازيان عند تغيير الدولة، سباق نتائج (آخر استجابة تفوز عشوائيًا). الحذف/الدمج في موضع واحد.
- **🟠 كبير — `FeedScreen.kt:66-87` سحب مخصص بدل `PullToRefresh` الرسمي**: عتبة 360px صلبة بلا haptic ولا progress متدرج؛ يتعارض مع nested-scroll الأب ويُفشل مع TalkBack (السحب غير قابل للاكتشاف). الاستبدال بـ `PullToRefreshBox` من M3.
- **🟠 كبير — `FeedScreen.kt:122` و`HistoryScreen.kt:109,183` و`LocalAudioScreen.kt:139` و`QueuePanel.kt:81` قوائم بلا `key`**: مقابل `HomeResults.kt:144` و`PlaylistsScreen.kt:206` اللذين يملكان مفاتيح — غياب المفتاح = قفز/وميض عند التحديث وإعادة تركيب صور Coil بلا داعٍ (قاعدة `android` للأداء: stable params + key).
- **🟡 متوسط — `MainActivity.kt:1335` قائمة `trending` المبدئية صلبة بالإنجليزية** (`music/tech reviews/...`) وتُعرض قبل التحميل — تكسر التعريب وتوحي بمحتوى غير موجود.
- **🟡 متوسط — `MainActivity.kt:4283` منطق العرض الثلاثي يعتمد `results.isEmpty() && query.isBlank()`**: أي خطأ شبكة مع نتائج قديمة يُظهر شاشة فارغة بدل الخطأ؛ لا يوجد تمييز no-network مقابل empty-query.
- **🔵 صغير — `HomeResults.kt:84,104` أيقونات `contentDescription=null`**: قارئ الشاشة يتخطى سياق "الرائج الآن" (مقابل `FeedScreen.kt:118,136` و`SearchRow.kt:152,157` الجيدين).

### 1-4. فجوات UX مقابل YouTube

1. لا skeleton متدرج لكل تبويب — هيكل عام واحد.
2. لا حفظ موضع التمرير لكل تبويب ترند (التبديل يعيدك للأعلى).
3. لا "سحب للتحديث" في الرئيسية نفسها (موجود فقط في Feed المخصص).
4. لا شارة "مباشر/جديد/شائع" على البطاقات (فقط Shorts/المدة).
5. لا إلغاء بحث واضح + اقتراح مصحح `did-you-mean` غير بارز.
6. لا تحميل تدريجي (paging) للترند — دفعة واحدة.

### 1-5. فجوات الوصول

- TalkBack: أزرار البطاقة الفرعية (`PlaylistCard` السهم :208، `MoreVert` في :182) بلا وصف؛ تبويبات `ScrollableTabRow` بلا `stateDescription`.
- RTL: `SearchRow` يستخدم `AutoMirrored` بشكل صحيح (:208) ✅ لكن مؤشر السحب في Feed غير معكوس بصريًا.
- أهداف اللمس: زر الإغلاق `onRemove` ‏(:160-164) بارتفاع 36dp < حد 48dp.

### 1-6. خطوات الإصلاح (P0/P1/P2)

- **P0-1**: دمج `LaunchedEffect(prefCountry)` في موضع واحد + `cancel` للطلب السابق (`MainActivity.kt:1381/1979`).
- **P0-2**: إضافة `key` لكل `LazyColumn` بلا مفتاح (`FeedScreen.kt:122` → `key={it.url?:it.id}`، `HistoryScreen.kt:109`، `LocalAudioScreen.kt:139`، `QueuePanel.kt:81`).
- **P1-1**: استبدال السحب المخصص بـ `PullToRefreshBox` مع `refreshFeed` كـ `onRefresh` (`FeedScreen.kt:66-92`).
- **P1-2**: فصل حالة `feedError/noNetwork` عن `empty` في فرع العرض (`MainActivity.kt:4283-4330`) + رسالة مميزة لكل حالة.
- **P1-3**: تعريب القائمة المبدئية / تحميل كسول لها من `strings.xml` (`MainActivity.kt:1335`).
- **P2-1**: `contentDescription` للأيقونات الزخرفية مقابل الدلالية (`HomeResults.kt:84,104`، `SearchRow.kt:180,182,208`).
- **P2-2**: رفع زر الإغلاق إلى 48dp + `ScrollableTabRow` بـ `stateDescription` للتبويب النشط.

### 1-7. ما يجب أن يختبره محمد ميدانيًا (لا adb لدينا)

- [ ] تغيير الدولة → طلب شبكة واحد فقط (راقب التأخير/التكرار).
- [ ] سحب Feed للتحديث بإصبع واحد + TalkBack مفعّل (هل يُعلن "جارٍ التحديث"؟).
- [ ] تبديل تبويبات الترند ذهابًا وإيابًا — هل يُحفظ الموضع؟
- [ ] وضع طيران + فتح الهوم — هل الرسالة "لا اتصال" أم "لا نتائج"؟
- [ ] Mini-player ظاهر فوق شريط التنقل في كل تبويب، ومخفي في ملء الشاشة والبحث.

---

## 2) شاشة الموسيقى + الشاشات المتضمنة

### 2-1. البنية الحالية

| الملف | الدور | مفاتيح |
|---|---|---|
| `ui/library/MusicHome.kt` ‏(:44 `MusicSourceSegment`، ‏(:81 `MusicCard`، ‏(:130 `MusicShelf` | تبديل المصدر SC/YTM + رفوف أفقية | `LazyRow items(..., key=...)` ‏(:149) ✅ |
| `ui/library/MusicTasteSection.kt` ‏(:29) | توصيات legacy: chips من `profile.terms` + top-5 picks + mix | 14 باراميتر — رائحة god-composable |
| `util/TasteProfile.kt` ‏(:45 `buildTasteProfile`، ‏(:28 `tokensOf`، ‏(:38 `queryTerms` | بروفايل ذوق محلي خالص (ترجيح تاريخي) | — |
| `util/LocalAudio.kt` ‏(:42 `scanLocalAudio`، ‏(:23 `toVideoItem` id=`local:<mediaId>`) | مسح MediaStore + هوية `isLocal()` ‏(:39) | — |
| `ui/library/LocalAudioScreen.kt` ‏(:44 + `LocalAudioRoute` ‏(:162 wrapper لتجاوز حد 64KB) | متصفح الصوت: chips مجلدات + strict/compat + استبعاد/استعادة + بوابة إذن | `LazyColumn` ‏(:139) بلا key |
| `data/youtube/YouTubeRepo.kt` | `search(...music_songs/albums/playlists/artists)` ‏(:171)، `scSearch` ‏(:638)، `scStream` ‏(:677)، `searchPlaylists` ‏(:704) | — |
| `MainActivity.kt` الموسيقى | الحالة ‏(:506-552)، `doScSearch` ‏(:2569)، `refreshTaste` ‏(:2635)، `loadScMore` ‏(:2659)، `LaunchedEffect(showSC)`، الواجهة ‏(:3990-4105)، `Single scroll surface` ‏(:4008-4011) | `scTail` للإلحاق |
| `MainActivity.kt` الراديو/القائمة | `startRadio` ‏(:1805)، `playPrev/Random/Next/First` ‏(:1849/1868/1883/1918)، `queueListener` ‏(:2024، `STATE_ENDED` ‏(:2031) | حماية إطلاق مزدوج (تعليق :2010) |
| `ui/queue/QueuePanel.kt` ‏(:46 + `VideoOptionsDialog` ‏(:80) | لوحة القائمة + خيارات الضغط المطول | `LazyColumn` ‏(:81) بلا key |

### 2-2. نقاط القوة

1. `MusicShelf` بمفتاح مركب مستقر (:149) — أفضل ممارسة في الملف.
2. `TasteProfile` محلي خالص — خصوصية + يعمل أوفلاين.
3. `LocalAudioRoute` wrapper ذكي لتفادي حد 64KB للميثود.
4. `scTail` + `Single scroll surface` (:4008) يوحّد الرأس والنتائج في `LazyColumn` واحد — تمرير سلس.
5. حماية `STATE_ENDED` المزدوج (:2010) موثقة — تمنع قفز الأغاني.

### 2-3. مشاكل

- **🟠 كبير — `MusicTasteSection.kt:29` تضخم الباراميترات (14)**: كل تغيير ذوق يُعيد تركيب القسم كاملًا (لا `stable params` — مخالف `android` compose). التفكيك إلى state-holder + `remember`.
- **🟠 كبير — غياب paging حقيقي لنتائج YTM**: `loadScMore` ‏(:2659) إلحاق يدوي بـ `scTail` بلا `PagingSource` (مقابل `HistoryPaging.kt:12` الجيد) — تكرار/فقدان عند التمرير السريع.
- **🟡 متوسط — `LocalAudioScreen.kt:139` و`QueuePanel.kt:81` بلا `key`**: إعادة ترتيب القائمة (نقل/حذف) تقفز بصريًا.
- **🟡 متوسط — `MainActivity.kt:3990-4105` خلط SC/YTM في Scaffold واحد**: `scSource` يبدّل المصدر لكن `scKind` لا يُصفَّر — بحث "ألبومات" في SC ثم تبديل لـ YTM يعرض نوعًا غير مدعوم بصمت.
- **🟡 متوسط — `startRadio` ‏(:1805) بلا حد أعلى**: قائمة لا نهائية تنمو بلا `cap` — خطر `TransactionTooLarge`/ذاكرة على الجلسات الطويلة (حد Binder ~1MB — قاعدة `android` #4).
- **🔵 صغير — `MusicHome.kt:99` صورة 140dp بلا `placeholder/crossfade`**: وميض عند التمرير السريع.

### 2-4. فجوات UX مقابل YouTube Music

1. لا أغلفة ضبابية/متدرجة خلف `MusicCard` — بطاقة مسطحة.
2. لا تبويبات YTM (أغاني/ألبومات/فنانين/قوائم) كـ `TabRow` — chips يدوية.
3. لا خلط (shuffle) ولا تكرار على مستوى الرف — فقط القائمة العامة.
4. لا "تشغيل لاحقًا/إضافة للطابور" من `MusicCard` مباشرة (موجود في `VideoOptionsDialog` فقط).
5. لا مؤشر "يُشغَّل الآن" متحرك (Equalizer) داخل `MusicCard` — شارة ثابتة.
6. لا بحث صوتي في تبويب الموسيقى.

### 2-5. فجوات الوصول

- `MusicSourceSegment` تبديل مخصص بلا `role=Tab` — TalkBack يقرأه زرين منفصلين.
- `MusicCard` الصورة `contentDescription=v.title` مكرر مع العنوان النصي — إعلان مزدوج مزعج.
- تباين شارة المدة على الغلاف غير مدقق (نص أبيض على صورة فاتحة).

### 2-6. خطوات الإصلاح

- **P0-1**: `cap` لقائمة الراديو (مثلًا 200) + تمرير نافذة منزلقة في `playNextInQueue` ‏(:1883) و`startRadio` ‏(:1805).
- **P0-2**: تصفير `scKind` عند تبديل `scSource` + رسالة "غير مدعوم" الصريحة (`MainActivity.kt:3990+`).
- **P1-1**: تفكيك `MusicTasteSection` إلى `TasteUiState` مستقر + `remember` للمشتقات.
- **P1-2**: مفاتيح `LocalAudioScreen.kt:139` (`local:mediaId`) و`QueuePanel.kt:81` (`videoKey`).
- **P1-3**: `PagingSource` لنتائج SC/YTM على غرار `HistoryPaging.kt` بدل `scTail` اليدوي.
- **P2-1**: `crossfade(true)+placeholder` لصور `MusicCard` ‏(:99) و`PlaylistCover`.
- **P2-2**: `role=Tab` + `stateDescription` لتبديل المصدر.

### 2-7. اختبار ميداني لمحمد

- [ ] شغّل راديو 30+ أغنية — هل تنمو الذاكرة/يتباطأ التمرير؟
- [ ] بدّل SC↔YTM مع فلتر "ألبومات" — هل النتائج منطقية؟
- [ ] رتّب/احذف من الطابور أثناء التشغيل — هل يقفز الموضع؟
- [ ] افصل الشبكة أثناء `loadScMore` — هل رسالة خطأ أم تجمّد؟
- [ ] TalkBack على `MusicSourceSegment` — هل يُعلن التبويب النشط؟

---

## 3) شاشة المكتبة + الشاشات المتضمنة

### 3-1. البنية الحالية

| الملف | الدور | مفاتيح |
|---|---|---|
| `ui/library/LibraryScreen.kt:27` (`LibraryRow:158`) | محور تنقل + اشتراكات مع `feedCounts` | صفوف → history/favorites/downloads/playlists/local-audio |
| `ui/playlists/PlaylistsScreen.kt:110` | CRUD قوائم + إدارة أغاني: `editMode/staged` ‏(:138-145)، `viewRows/tabList` ‏(:146-160)، `selecting/selected` ‏(:140-141)، `moveRow`، `PlaylistPickerDialog:380`، `PlaylistCover:442` (كولاج 2×2 من `covers`) | `LazyColumn` ‏(:141)، `items(tabList,key=id)` ‏(:206) ✅ |
| `ui/playlists/FavoritesScreen.kt:30` | مفضلة: تشغيل/قلب/مسح/`onPlayAll/onRadio` — mutations مفوّضة للcaller ✅ | — |
| `ui/playlists/HistoryScreen.kt:43` | تبويبات Paging-3 (all/pinned/unpinned) + بحث محلي + `sizeLabel:78` | `collectAsLazyPagingItems:91`، `items(count,key=videoKey)` ‏(:184) ✅، لكن `LazyColumn:109` (فرع البحث) بلا key |
| `ui/library/ChannelScreen.kt:36` | قناة: رأس + 5 تبويبات، تحميل كسول `onTabSelect` | `ChannelVideoList:174` |
| `ui/library/ChannelGroups.kt:36` | `GroupsScreen:39` (create/rename/delete) + `GroupDetailScreen:114` + `NameDialog:157` | — |
| `ui/library/DownloadsScreen.kt:38` (`DownloadsRoute:209` wrapper) | pins + تقدم + orphans + إلغاء؛ `pending-keys-first` ‏(:115)، بانر orphans ‏(:53-68)، `PinEntity.toVideoItem:202` | — |
| `ui/queue/QueuePanel.kt:46` | reorder/remove/play-from/save-as-playlist | — |
| `data/local/PlaylistDb.kt` | Room v9 + DAO: `observePlaylists:96`، `savePlaylistEdits:147` (transaction ✅)، `historyPage*:215-221`، `historySearch:225`، `topPlayed:231`؛ كيانات `PlaylistEntity:22`…`PinEntity:117`؛ `favKey:283`، `escapeLike:280`، `FeedJson:311` | — |
| `data/local/PinManager.kt:19` | محرك التحميل: `buildPlanMulti:30`، `estimateBytes:71`، `downloadPin:156` (staging+resume `.part`+verify)، `sweepOnStartup:95` | — |
| `data/local/PinFiles.kt:12` | تخطيط `travel/<sha256>/` ‏+ `externalOrphans:79` + `cleanExternalOrphans:101` | — |
| `data/local/HistoryPaging.kt:9` | `HistoryPaging.load:12` (short-circuit للمفاتيح الفارغة :18)، `getRefreshKey:36` | — |
| `ui/library/PinConfirmDialog.kt:29` | خيارات التثبيت + `LaunchedEffect(selQuality,selAudio,selSubs):102` يعيد التقدير | — |
| `ui/results/RemotePlaylistView.kt:20/60` | عارض قوائم بعيدة + play-all | — |
| `MainActivity.kt` الربط | `exportPlaylistCsv:1171` (`q()` للتخليص :1178 ✅)، `exportFullBackup:2226`، `importFullBackup:2309` (يتجاهل `pref_pin_location` ✅)، `refreshPlCovers:1147`، `loadPlSorts:1165` | — |

### 3-2. نقاط القوة

1. `savePlaylistEdits` معاملة Room — لا قوائم نصف-معدلة.
2. `escapeLike` + `favKey` — بحث آمن ومفاتيح مفضلة مستقرة.
3. `PinManager` staging + resume + verify — تنزيل متين.
4. `Favorites/History` يفوّضان الطفرات للcaller — فصل نظيف قابل للاختبار.
5. النسخ الاحتياطي JSON شامل (قوائم+مفضلة+سجل 500+اشتراكات+مجموعات+prefs) عبر FileProvider.

### 3-3. مشاكل

- **🟠 كبير — `HistoryScreen.kt:109` فرع البحث بلا `key` ولا paging**: البحث المحلي يعرض قائمة عادية غير مفتاحية — قفز عند الكتابة السريعة؛ مقابل فرع Paging ‏(:184) الممتاز.
- **🟠 كبير — `DownloadsScreen.kt:115` عرض "pending-keys-first" بلا `key` مستقر**: تقدم التنزيل (`progress` متغير) يُعيد تركيب الصف كاملًا بدل تحديث شريط التقدم — وميض + استهلاك.
- **🟡 متوسط — `PinConfirmDialog.kt:102` إعادة تقدير الحجم على كل تغيير**: `onEst(-1)` ثم IO بلا `debounce` — ثلاث ضربات شبكة (جودة+صوت+ترجمة) عند فتح الديالوج.
- **🟡 متوسط — `ChannelScreen.kt:83` حالة التبويب `remember` فقط**: تدوير الشاشة يعيدك للتبويب الأول ويفقد موضع القائمة (مخالف قاعدة `android` #4 — يلزم `rememberSaveable`/`SavedStateHandle`).
- **🟡 متوسط — لا تأكيد حذف جماعي**: `clearFavorites` ‏(MainActivity:1239) و`clearHistory` ‏(:1245) بلا Undo/snackbar — حذف غير قابل للاسترجاع.
- **🔵 صغير — `PlaylistCover:442` كولاج بلا `contentDescription=null` المعلن**: TalkBack يقرأ 4 صور مكررة.

### 3-4. فجوات UX مقابل YouTube

1. لا سحب-للإعادة-ترتيب (drag-handle) في القوائم — فقط picker `moveRow`.
2. لا تحديد متعدد عبر long-press الممتد + شريط إجراءات سياقي موحد (موجود جزئيًا `selecting/selected` لكن بلا CAB).
3. لا فرز مرئي محفوظ لكل قائمة (الدوال `loadPlSorts` موجودة لكن لا UI فرز واضح).
4. لا استيراد CSV (تصدير فقط `exportPlaylistCsv:1171`) — YouTube لا يدعم CSV أصلًا لكن التطبيق يعد به في النصوص.
5. حالات الفارغ نصية فقط — لا رسوم/CTA "اكتشف/أضف".
6. لا "تشغيل الكل/خلط الكل" في رأس كل قائمة فرعية (موجود في المفضلة فقط).

### 3-5. فجوات الوصول

- صفوف `LibraryScreen` أيقونات `contentDescription=null` ‏(:74-109) — تعتمد على النص المجاور (مقبول) لكن TalkBack لا يعلن العدد (`feedCounts`) كجزء من الوصف.
- `Checkbox` التحديد المتعدد بلا `stateDescription` (محدد/غير محدد + الموضع).
- بانر orphans ‏(:53-68) بلا `role=Alert` — قارئ الشاشة قد يتخطاه.

### 3-6. خطوات الإصلاح

- **P0-1**: `key` لفرع بحث History ‏(:109) + `key` لصفوف Downloads ‏(:115) بمفتاح `pinKey` مستقر + تحديث التقدم عبر `derivedStateOf`.
- **P1-1**: `debounce(300ms)` لتقدير الحجم في `PinConfirmDialog.kt:102`.
- **P1-2**: `rememberSaveable` لتبويب القناة + حفظ `LazyListState` لكل تبويب (`ChannelScreen.kt:83`).
- **P1-3**: Undo عبر snackbar لـ `clearFavorites/clearHistory` + تأكيد حذف جماعي (`MainActivity.kt:1239/1245`).
- **P2-1**: `role=Alert` لبانر orphans + دمج `feedCounts` في الوصف الصوتي لصفوف المكتبة.
- **P2-2**: UI فرز مرئي + "تشغيل/خلط الكل" في رأس كل قائمة + empty-state مع CTA.

### 3-7. اختبار ميداني لمحمد

- [ ] ابحث في السجل بسرعة (3 أحرف متتالية) — هل تقفز النتائج؟
- [ ] ثبّت فيديو وراقب شريط التقدم — هل يومض الصف كاملًا؟
- [ ] دوّر الهاتف في تبويب "Shorts" داخل القناة — هل يبقى التبويب؟
- [ ] امسح المفضلة بالخطأ — هل يوجد تراجع؟
- [ ] افصل الشبكة وافتح التنزيلات — هل تعمل الأزرار أوفلاين؟

---

## 4) شاشة الإعدادات

### 4-1. البنية الحالية

| الملف | الدور |
|---|---|
| `ui/settings/SettingsScreen.kt` (734) | pure UI: كل الحالة params + الكتابة callbacks؛ قوائم `LANG/COUNTRY/UI_LANG/PLAYMODE/THEME/ACCENT/QUALITY`؛ `SettingsScreen()` + `AgeModeRow:714` الخاص |
| `ui/settings/SettingsSections.kt` (165) | لبنات: `SettingsSection()` (مطوية افتراضيًا)، `DropRow()`، `SwitchRow()` |
| `ui/player/SubtitleStyle.kt` (52) | `SubStyle` + `applySubStyle` (يفرض ستايلنا `setApplyEmbeddedStyles(false)`) + `subStyleFromPrefs` |
| `ui/player/PlayerComponents.kt` (214) | لا علاقة — `MiniPlayer`+`EqualizerBars`+`SleepTimerDialog` فقط |
| `data/UpdateCheck.kt` | `latestTag(owner,repo)` ‏+ `isNewer(latest,current)` |
| `ui/theme/Color.kt` (121) + `Theme.kt` (132) | Tier0 خام + Tier1 دلالي + `MyTubeTheme(darkTheme,black,accent,rtl)` + فرض RTL/LTR |
| `MainActivity.kt` | `SettingsPane()` ‏(~3650)، `exportFullBackup` ‏(:2226)، `importFullBackup` ‏(:2309)، `checkUpdate` ‏(:2431)، `diagnosticsReport` ‏(:2210)، `themeLive/accentLive` ‏(:182-183) + تطبيق فوري ‏(:202-209، :3750-3763) |

**الربط الحي**: الثيم/اللكنة live حقيقي ✅؛ الترجمة تُبنى عند المشغل (:3459) + حفظ فوري (:3711-3718) + preview يعكس fg/bg/opacity/scale/bold (لكن ليس edge)؛ الصوت/السرعة حفظ فوري.

### 4-2. نقاط القوة

1. `SettingsScreen` بلا منطق — callbacks فقط، قابلة للمعاينة والاختبار.
2. 7 أقسام مطوية (lang/subs/audio/player/appearance/downloads/data) — ~88 صفًا → 7 عناوين.
3. `DropRow` وحّد ~20 قائمة راديو؛ `SwitchRow` وحّد التبديلات.
4. `ACCENT_OPTIONS` من `AccentFills` (مصدر واحد).
5. النسخ الاحتياطي شامل + الاستيراد يتجاهل `pref_pin_location` (صحيح).
6. `UpdateCheck` بمهلات قصيرة (10/10/15s) لا يعلّق الإعدادات.

### 4-3. مشاكل

- **🔴 حرج — `values-en/strings.xml` يفتقد كل مفاتيح `update_*` و`backup_*`** (grep = 0 مقابل 16 في `values/strings.xml`): الواجهة الإنجليزية تعرض أزرار التحديث/النسخ **بالعربية**. الأزرار `SettingsScreen.kt:648-652` عبر `onCheckUpdate/onExportFull/onImportFull`.
- **🔴 حرج — `UpdateCheck.kt: isNewer()` مقارنة نصية ساذجة** (`trimStart('v') != ...`): أي اختلاف (حتى نسخة محلية أحدث/لاحقة مختلفة) = "يوجد تحديث" كاذب → تنزيل نسخة أقدم!
- **🟠 كبير — `MainActivity.kt:3743` تبديل `skipSilence` يُعيد تشغيل Activity كاملة** (`stopService+delay(900ms)+recreate()`): فقدان موضع/وميض مقابل إعداد صوتي — يجب التطبيق على المسار الصوتي فقط أو التأجيل للفيديو التالي.
- **🟡 متوسط — `MainActivity.kt:3572` تغيير لغة الواجهة `recreate()`**: مقبول لكن بلا حفظ موضع القائمة/التبويب — يعود المستخدم للأعلى.
- **🟡 متوسط — preview الترجمة لا يعكس `edge`**: المستخدم يضبط حدًا لا يراه إلا في المشغل.
- **🔵 صغير — `SettingsSection` الكل مطوي افتراضيًا**: القسم النشط أخيرًا لا يُحفظ — 7 نقرات كل مرة.

### 4-4. فجوات UX مقابل YouTube

1. لا بحث داخل الإعدادات (YouTube: شريط بحث إعدادات).
2. لا شارة "يتطلب إعادة تشغيل" على الإعدادات المدمرة (`skipSilence`/اللغة).
3. لا سجل إصدار/تاريخ فحص التحديث ("آخر فحص: …").
4. لا معاينة حية للثيم/اللكنة قبل الحفظ (التطبيق فوري — جيد — لكن بلا تراجع واحد).
5. لا تصدير/استيراد انتقائي (الكل أو لا شيء).
6. لا `diagnosticsReport` قابل للمشاركة من الواجهة (موجود :2210 لكن مدفون).

### 4-5. فجوات الوصول

- `DropRow` يفتح `DropdownMenu` بلا `expanded stateDescription` — TalkBack لا يعلن مفتوح/مغلق.
- `SwitchRow` الوصف الثانوي غير مربوط بـ `supportingText` دلاليًا.
- تباين `subtitle preview` على خلفية فاتحة غير مضمون.

### 4-6. خطوات الإصلاح

- **P0-1**: استكمال `values-en/strings.xml` (كل `update_*/backup_*` + أي مفتاح عربي فقط) —diff بين الملفين.
- **P0-2**: `isNewer()` مقارنة دلالية (split `.` + مقارنة عددية + تجاهل اللاحقات) + اختبار وحدة (`v1.2` vs `1.2.0` vs `v0.3.2`).
- **P0-3**: إلغاء `recreate()` لـ `skipSilence` — إعادة بناء `RenderersFactory`/المسار الصوتي فقط أو `applyOnNextVideo` + شارة "يُطبق على التالي".
- **P1-1**: حفظ/restore موضع الإعدادات عبر `rememberSaveable` + تذكّر آخر قسم مفتوح (pref).
- **P1-2**: preview الترجمة يعكس `edge` + خلفية معاينة داكنة ثابتة.
- **P2-1**: بحث إعدادات + "آخر فحص" + شارة إعادة التشغيل + مشاركة `diagnosticsReport`.

### 4-7. اختبار ميداني لمحمد

- [ ] بدّل اللغة EN — هل أي زر عربي في (التحديث/النسخ)؟
- [ ] افحص تحديثًا بنسخة محلية أحدث من المنشورة — هل يقول "يوجد تحديث" كذبًا؟
- [ ] بدّل `skipSilence` أثناء التشغيل — هل يُعاد فتح التطبيق/يتوقف الصوت؟
- [ ] غيّر لون الترجمة + الحد — هل المعاينة تطابق المشغل؟
- [ ] صدّر نسخة كاملة ثم استوردها بعد مسح البيانات — هل تعود القوائم+السجل؟

---

## 5) الطابور الموحد للتنفيذ (مرتب P0 → P2 عبر الشاشات الأربع)

| # | الأولوية | الشاشة | الإصلاح | الملفات |
|---|---|---|---|---|
| 1 | P0 | الإعدادات | تكميل `values-en` (update/backup) | `values-en/strings.xml` vs `values/strings.xml`، `SettingsScreen.kt:648-652` |
| 2 | P0 | الإعدادات | `isNewer` دلالية + اختبار وحدة | `data/UpdateCheck.kt` |
| 3 | P0 | الإعدادات | إلغاء `recreate` لـ `skipSilence` | `MainActivity.kt:3743` |
| 4 | P0 | الرئيسية | دمج `LaunchedEffect(prefCountry)` المزدوج | `MainActivity.kt:1381/1979` |
| 5 | P0 | الرئيسية/مكتبة/موسيقى | مفاتيح لكل `LazyColumn` بلا key | `FeedScreen.kt:122`، `HistoryScreen.kt:109`، `LocalAudioScreen.kt:139`، `QueuePanel.kt:81`، `DownloadsScreen.kt:115` |
| 6 | P0 | الموسيقى | cap قائمة الراديو (200) + نافذة منزلقة | `MainActivity.kt:1805/1883` |
| 7 | P0 | الموسيقى | تصفير `scKind` عند تبديل المصدر | `MainActivity.kt:3990+` |
| 8 | P1 | الرئيسية | `PullToRefreshBox` بدل السحب المخصص | `FeedScreen.kt:66-92` |
| 9 | P1 | الرئيسية | فصل `error/noNetwork` عن `empty` | `MainActivity.kt:4283-4330` |
| 10 | P1 | الموسيقى | تفكيك `MusicTasteSection` (14 param) | `MusicTasteSection.kt:29` |
| 11 | P1 | الموسيقى/المكتبة | Paging لنتائج SC + debounce تقدير Pin | `MainActivity.kt:2659`، `PinConfirmDialog.kt:102` |
| 12 | P1 | المكتبة | `rememberSaveable` تبويب القناة + Undo مسح | `ChannelScreen.kt:83`، `MainActivity.kt:1239/1245` |
| 13 | P1 | الإعدادات | حفظ آخر قسم + preview الحافة | `SettingsSections.kt`، `SettingsScreen.kt:~300` |
| 14 | P2 | الكل | وصول: أوصاف + أدوار + 48dp + تباين | `HomeResults.kt:84/104`، `SearchRow.kt:180/182/208`، `LibraryScreen.kt:74-109`، `MusicHome.kt:99` |
| 15 | P2 | الكل | UX parity: موضع تبويب، فرز، بحث إعدادات، empty-CTA | ملفات الواجهة المعنية |

## 6) قائمة الاختبار الميداني الموحدة (لمحمد — بلا adb)

**الرئيسية**: دولة→طلب واحد؛ سحب Feed + TalkBack؛ تبويبات الترند (الموضع)؛ طيران (رسالة صحيحة)؛ Mini-player (ظهور/اختفاء).
**الموسيقى**: راديو 30+ (ذاكرة)؛ SC↔YTM مع فلتر؛ حذف من الطابور أثناء التشغيل؛ فصل الشبكة أثناء `loadMore`؛ TalkBack على التبديل.
**المكتبة**: بحث سجل سريع؛ تقدم التثبيت (وميض؟)؛ تدوير في تبويب قناة؛ مسح مفضلة (تراجع؟)؛ تنزيلات أوفلاين.
**الإعدادات**: EN (عربي متسرب؟)؛ فحص تحديث بنسخة أحدث محليًا؛ `skipSilence` أثناء التشغيل؛ معاينة الترجمة مقابل المشغل؛ تصدير→مسح→استيراد.

---

*وُقّع: بركات ☕ — دراسة فقط، لم يُمس الكود ولم يُبنَ شيء. القادم بأمر محمد.*
