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
- (يُملأ بعد التنفيذ)
