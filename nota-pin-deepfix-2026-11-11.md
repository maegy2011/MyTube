# nota-pin-deepfix-2026-11-11 — Deep pin/download repair (YT + YTM + SC)

Skill: `android` (layer=runtime — pin flow, extractor streams, Compose rows). targetSdk 34 (per AGENTS.md state).

## 1. Audit (per-source gaps)

- **YouTube (youtube.com watch URLs)** — `YouTubeRepo.stream()` resolves fine (progressive muxed + DASH video-only + audio tracks).
  Gap Y1: googlevideo URLs carry expiry (~hours). `requestPin` resolves at dialog time; `confirmPin` rebuilds the plan from the same stale `pinSd`. User waits → download 403s → generic `toast_pin_fail`. No retry.
  Gap Y2: `requestPin`/`quickPin` do `YouTubeRepo.streamRouted(v.url ?: return@launch)` — null URL exits **silently** (no toast).
- **YouTube Music** — two flavors: (1) `music_songs` search items carry youtube.com URLs → resolve fine. (2) YTM album/playlist tracks opened via `fetchPlaylist` (RemotePlaylistView) may carry `music.youtube.com/watch?v=…` URLs — `stream()` hands the raw URL to the YouTube extractor (only `fetchPlaylist` normalizes music→www). Pin/play-resolve of such tracks fails → generic toast.
  Fix: normalize `music.youtube.com → www.youtube.com` (+ `youtu.be/… → www.youtube.com/watch?v=…`) centrally in `stream()`.
- **SoundCloud** — `scStream()` picks highest-bitrate audio; `buildPlan(QUALITY_AUDIO_ONLY)` works via the `audioLowestUrl` fallback (audioTracks empty).
  Gap S1: no HLS detection — if the extractor hands an `.m3u8` URL, the pin stores playlist bytes as `audio.m4a` (corrupt, silent).
  Gap S2: `scStream` null (network) vs HLS-only are indistinguishable → generic toast either way.
  Known limit (kept): when a host hides Content-Length, `getTo` cannot verify full length — truncated reads are accepted. Documented, not fixed.
- **Progress (single source)** — `pinProgress: Map<key, Float>` in MainActivity already drives Downloads rows + the system notification. But originating cards/rows show only static `pinning` text, and queue/player show no %.
- **Badges** — plain `"📌 quality • size"` tertiary text; no icon, and `SearchRow`'s `mergeDescendants=false` + title-only contentDescription hides the badge from TalkBack entirely. Queue rows show no badge at all.
- **Cancel UX** — cancelling an in-flight pin toasts `toast_pin_fail` (scary + wrong). `cancelPinDownload` already toasts `cancel`; the download coroutine must stay silent on cancel.

## 2. Plan (implemented below)

1. `YouTubeRepo`: normalize YTM/short URLs in `stream()`; `scStream` prefers non-HLS, new `StreamData.audioHlsOnly` flag when only HLS exists.
2. `PinManager`: `isCancelRequested()` peek (non-consuming) for cancel-aware toasts.
3. `MainActivity`: null-URL toast; HLS toast (`pin_err_stream`); `runPinDownload(..., retry)` — one fresh-resolve retry on failure; cancel-aware toasts (no fail toast on cancel / job-cancel); thread `progressOf` + queue pin params + player `pinProgress` into every list.
4. New `PinBadge` pill (icon + tint + `cd_pinned_fmt` content-description); used in `SearchRow`, `DownloadsScreen` rows, `QueuePanel` rows.
5. `SearchRow(pinning + pinProgress)`: determinate bar + % (single `pinProgress` source) in every list; `PlayerArea` pill shows % while pinning; queue rows show badge + bar.
6. Strings ar+en: `pin_err_stream`, `cd_pinned_fmt` (`&` escaped, no raw Color — theme tokens only).
7. FEATURES.md entry; compile `compileDebugKotlin --offline`; commit (no build/push).

## 3. Results

- BUILD: `./gradlew :app:compileDebugKotlin --offline --no-daemon` → **BUILD SUCCESSFUL** (Android skill, runtime layer; targetSdk 34).
- Fixed per source:
  - YT: stale googlevideo URLs now get one fresh-resolve retry (`PinRetry` + rebuild); null-URL pin attempts toast instead of dying silently.
  - YTM: `music.youtube.com`/`youtu.be` normalized in `stream()` — album/playlist tracks resolve like www items.
  - SC: HLS-only tracks detected (`audioHlsOnly`) and refused with `pin_err_stream` (ar+en) instead of a corrupt `audio.m4a`; audio-only default + Range-resume unchanged.
- Progress surfaces (single `pinProgress` source): originating card/row bar+%, Downloads rows, player pin pill %, queue rows %, system notification %.
- Badges: new `PinBadge` pill (PushPin icon, primary-container theme tokens, `cd_pinned_fmt` TalkBack description) in search rows, queue, favorites/history (via SearchRow), downloads.
- Cancel: user-cancelled pins stay silent (cancel toast only) — no more false "pin failed".
- Commit: (hash below).
