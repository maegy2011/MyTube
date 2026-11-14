# مراجعة MyTube الشاملة — 2026-11-10

**التاريخ:** 10 نوفمبر 2026 • **الإصدار:** 0.2.8 (versionCode 28)
**نتيجة الترجمة:** ✅ `BUILD SUCCESSFUL` — أمر `compileDebugKotlin --offline --no-daemon --rerun-tasks` (إعادة كاملة، 2د51ث، EXIT=0)، التحذير الوحيد: `PlaylistAdd` مهمل في QueuePanel:225. ملاحظة: الادعاء بوجود سطر لا يُترجم (`p.trackSelector` في MainActivity:1178) **مرفوض** — الكود يُترجم بنجاح لأن `ExoPlayer` في media3 يوفّر `getTrackSelector()`.
**المنهج:** 41 ملف Kotlin + strings.xml + AndroidManifest.xml + build.gradle.kts + libs.versions.toml — بلا تعديل أي مصدر.

---

## PASS 1 — السلامة الاستاتيكية (14 minor)

1. `data/local/PinManager.kt:7` — استيراد `qualityHeight` غير مستخدم (minor)
2. `data/local/PinManager.kt:20` — صنف `PinRequest` ميت لا يستخدمه أحد (minor)
3. `data/local/PinManager.kt:57` — فحص `t.url ?: return` على String غير قابل للـnull، لا يعمل أبدًا ويضلل القارئ (minor)
4. `data/local/PinManager.kt:122` — استدعاء `body.byteStream()` في كل تكرار بدل الاحتفاظ بتيار واحد (minor)
5. `data/youtube/YouTubeRepo.kt:304` — فرع `if (true)` ميت من بقايا تجارب (minor)
6. `data/youtube/YouTubeRepo.kt:48` — `getService(1)` برقم سحري ينكسر بصمت لو تغيّر ترتيب الخدمات (minor)
7. `ui/library/ChannelGroups.kt:31` — استيراد `Checkbox` غير مستخدم (minor)
8. `ui/library/FeedScreen.kt:31,39` — استيراد `dp` مكرر في سطرين (minor)
9. `ui/library/FeedScreen.kt:15` — استيرادات غير مستخدمة (تُستدعى بأسماء كاملة) (minor)
10. `ui/playlists/FavoritesScreen.kt:93` و `ui/playlists/HistoryScreen.kt:215` — `Spacer` بعرض صفر لا يفعل شيئًا (minor ×2)
11. `ui/queue/QueuePanel.kt:225` — أيقونة `PlaylistAdd` مهملة، استخدم نسخة `AutoMirrored` (تحذير المترجم الوحيد) (minor)
12. `MainActivity.kt:1364` — شرط زائد `if (hasAudio) … else if (!hasAudio)` مع `else` مستحيل (minor)
13. `ui/results/SearchRow.kt:57` — معامل `onAdd` مقبول ولا يُستخدم في 5 مواقع استدعاء (minor)
14. `ui/player/PlayerArea.kt:245` — `nativeOnly=true` يجعل أفرع شريط التحكم المخصص ميتة (minor)
15. `ui/settings/SettingsScreen.kt:69` — أسماء اللغات مكتوبة يدويًا بدل موارد السلاسل (minor)
- **سليم:** لا `&` عارية في strings.xml، كل `R.string.*` الـ448 المستخدمة معرّفة، `backup_rules.xml` و`proguard-rules.pro` موجودان.

## PASS 2 — المنطق والاتساق (9 major / 24 minor)

### حرجة التشغيل (major)
1. `MainActivity.kt:237` — `setAutoEnterEnabled()` يعمل من API 31 فقط و`minSdk=26` بلا حارس إصدار → عطل `NoSuchMethodError` على أندرويد 8–11 عند دخول PiP (major)
2. `data/local/PlaylistDb.kt:235-238` — `IN (:keys)` بمجموعة تثبيت فارغة يولّد `IN ()` غير صالح ويفشل تحميل تبويبي المثبت/غير المثبت (major)
3. `data/youtube/YouTubeRepo.kt:206-216` — مسار البث المباشر يفقد `ageLimit` (يصفر) ويقدّم HLS كجودة muxed قد يسحبها المثبّت كبايتات غير قابلة للتشغيل (major)
4. `data/youtube/YouTubeRepo.kt:85` + `util/PlaybackUtils.kt:139-154` — استخراج المعرّف يفهم `watch?v=` فقط، فروابط `/shorts/` و`youtu.be` تعطي null فيُنهي `openInYoutube` بصمت (major)
5. `data/local/PinFiles.kt:60-84` — البحث/الحذف يغطي الموقع الحالي والداخلي القديم فقط، فمثبتات الخارجي تصبح خفية وغير قابلة للحذف بعد التبديل لداخلي (major)
6. `MainActivity.kt:1417-1435` — `applyQuality()` يقرأ `audioTrackIdx` للفيديو السابق فيدمج مسار الدبلجة الخطأ (major)
7. `MainActivity.kt:1375` — اختيار ترجمة المثبت يُكتب من علَم `sub.vtt` ثم يطغى عليه المطابق التلقائي من الإعدادات فيضيع اختيار التثبيت، والتثبيت من المشغّل يحفظ ترجمة واحدة ومن القائمة اثنتين (major)
8. `MainActivity.kt:2804-2814` — `stopService()` ثم `recreate()` فورًا قد يربط مشغّلًا قديمًا لا تراه الواجهة (major)
9. `ui/results/SearchRow.kt:57` — `onRemove` مقبول ولا يُعرض ولا يُستدعى → زر حذف عنصر السجل ميت بصمت (major)

### ثانوية (minor)
- `PinManager.kt:155-158` — شريط التقدم يستخدم `estTotal/2` حتى للتثبيت أحادي التيار فيعرض نصف الحجم (minor)
- `PinManager.kt:136` — ملف `.part` اليتيم لا يُحذف عند فشل `renameTo` فيتراكم (minor)
- `data/local/HistoryPaging.kt:9-13` — مفاتيح التثبيت تُلتقط عند الإنشاء فالتبويبات تقدَم بعد تثبيت/إزالة (minor)
- `PlaylistDb.kt:122-143` — أعضاء المجموعات بلا مفتاح أجنبي للاشتراكات فتبقى صفوف يتيمة تضخم العدّادات (minor)
- `YouTubeRepo.kt:582` — `localizedTitle` يمسح الكاش كله عند الامتلاء بدل إخلاء الأقدم (minor)
- `data/youtube/SponsorBlock.kt:25-51` — خريطة غير متزامنة من خيوط IO والحد يمسح الكل (minor)
- `data/youtube/NewPipeDownloaderImpl.kt:22` — إجبار غير-POST على GET بلا نوع محتوى قد يربك استدعاءات المستخرج (minor)
- `util/PlaybackUtils.kt:25-46` — `autoUrl` بلا سقف 4K والتثبيت يرثه فيسحب 2160p بصمت (minor)
- `PinManager.kt:38-44` — `PinPlan.ageLimit` يُسجل في meta.json ولا يفرضه شيء (minor)
- `MainActivity.kt:1255-1275` — `castCurrent()` يتجاوز `cap4k()` فيدفع 4K للشاشة رغم إطفاء السقف (minor)
- `MainActivity.kt:1689-1699` — حلقة SponsorBlock كل 500ms قد تقفز فوق مقاطع قصيرة والسعي لا يستثني البث الحي (minor)
- `MainActivity.kt:1194-1208` — `ON_START` يعيد تفعيل الفيديو حتى لجلسات الصوت فقط (minor)
- `MainActivity.kt:3418-3425` — موافقة تحذير العمر تعيد الاستخراج الكامل بدل إكمال العنصر المحلول (minor)
- `MainActivity.kt:2468-2488` — ترجمة خارج `LANG_OPTIONS` تسقط على لغة الجهاز فيختلف المعروض عن الشريحة (minor)
- `PlayerArea.kt:297,362` — فكّ `player!!` آمن اليوم لكنه قابل للكسر مستقبلًا (minor)
- `PlayerArea.kt:355` — وضع الصوت بلا مشغّل يعرض مصغّرة بلا زر تشغيل (minor)
- `HistoryScreen.kt:125` — عناوين الحزم تعتمد `lastBucket` متغيرًا أثناء التركيب (minor)
- `QueuePanel.kt:195` — اختيار الأيقونات بمقارنة التسميات المترجمة يتبدل مع أي ترجمة (minor)
- `ChannelScreen.kt:155` — تبويب المباشر بلا شارة حية بينما المشغّل يعرضها (minor)
- `ChannelGroups.kt:215` — حوار الإضافة يبقى مفتوحًا فيتيح إضافة مزدوجة بالنقر السريع (minor)
- `SearchScreen.kt:48` — لا واجهة فلاتر أغانٍ/ألبومات/قوائم في البحث (minor)
- `SettingsScreen.kt:197` — مفتاح 4K معروض والترشيح الفعلي خارج ملفات الواجهة (minor)
- `MainActivity.kt:919,932` و`strings.xml` — التنبيهات عبر الموارد سليمة؛ كلمة "أخبار" المكتوبة يدويًا استعلام بحث لا واجهة (سليم)

## العدّادات
| | critical | major | minor |
|---|---|---|---|
| PASS 1 (استاتيكي) | 0 (ادعاء واحد مرفوض بالترجمة) | 0 | 15 |
| PASS 2 (منطق) | 0 | 9 | 24 |
| **الإجمالي** | **0** | **9** | **39** |

**الأولوية المقترحة:** حارس PiP حسب الإصدار + حماية `IN ()` الفارغة أولًا (عطلان مباشران)، ثم إصلاح روابط Shorts وتحيّز المسار الصوتي، ثم الباقي.
