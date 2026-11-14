# nota-pin-offline-fix-2026-11-11 — Total offline-playback failure: causes + fix

Skill: `android` (layer=runtime — pin file layout, ExoPlayer local playback). targetSdk 34.

## Symptom
Pins download fine (Room rows + badges + sizes all present), but NOTHING plays
offline — every source (YT + YTM + SC), even pins that played before.

## Cause 1 (ROOT, total, all sources): one-directional storage-root fallback
- `PinFiles.pinDir()` writes to the CURRENT pref root (SD-first default since
  `56e873`: external when available, else internal).
- `PinFiles.existingDir()` (the ONLY offline resolver: `isPinned()`,
  `dirBytes()`, playback) checks the current root, then falls back to
  **internal only** — it NEVER checks the external root when the pref is
  internal.
- Three everyday paths strand every pin at once:
  1. user flips location external→internal in Settings;
  2. `runPinDownload`'s `isCurrentLocBroken` auto-flip forces the pref to
     internal when external is transiently unavailable;
  3. external unmounted at playback time (`baseDir()` silently falls back to
     internal, so `pinDir` points at an empty dir).
- Result: `isPinned`=false → offline mode shows "pin first" for everything,
  while Downloads badges still list the Room rows. Looks exactly like
  "pinned files never play offline at all", and old pins "worked initially"
  (they were resolved while external was still the current root).
- Grounding — Koda (on-device playback, no server round-trip): the offline
  file IS the source of truth; resolving it must not depend on a mutable
  location pref. NewPipeExtractor: nothing about stream URLs helps here —
  the bytes are local, expiry is irrelevant, which is precisely why old pins
  must keep playing with zero network.
- Fix: resolve across BOTH roots regardless of pref (`existingDir` checks
  current → internal → external); `delete()` clears both roots;
  `dirBytes()` already goes through `existingDir` so it heals automatically.

## Cause 2 (decoder-level, hits audio-default pins): fixed names vs real container
- Every pin is stored as `video.mp4` / `audio.m4a` whatever the bytes are.
  YouTube audio is usually opus-in-webm (itag 249/250/251), DASH video-only
  is often vp9-in-webm, SoundCloud serves mp3/opus — none of which is MP4.
- Playback built `MediaItem`s from the file extension only, so the extractor
  choice depended on URI sniffing instead of a declared container.
- Grounding — yt-dlp FORMAT SELECTION: explicit `ext` filtering
  (`bestaudio[ext=m4a]`, `bestvideo[ext=mp4]`) exists for exactly this reason:
  bytes must match the container players expect. NewPipeExtractor exposes the
  container per stream (`getExtension()` → the repo already maps it to MIME
  for subtitles); googlevideo URLs additionally carry a `mime=` query param
  (the app already parses it in `castMimeFor`).
- Fix (no selection change, no re-pin): capture the HTTP `Content-Type` at
  download time into `meta.json` (`videoMime`/`audioMime`) and pass it as an
  explicit `setMimeType()` on the offline `MediaItem`s. Extension mismatch
  becomes irrelevant.

## Investigated and CLEARED (no code change)
- **Expired stream URLs instead of local file**: the pinned branch in
  `playVideo` returns before any resolve; quality-switch re-applies the
  pinned `streamData` (file:// URIs only) with `local=true`. Session restore
  re-enters the same branch. Expiry (NewPipeExtractor/googlevideo signed-URL
  lifetime) cannot affect offline playback by construction.
- **DB key (favKey) mismatch**: `PinEntity` keeps `videoId`+`url`, and
  `toVideoItem()` restores both, so `favKey()` recomputes identically
  (YT/YTM via id, SC via url). Degenerate id-less+url-less items cannot be
  pinned at all post-deepfix (null-URL toast). No migration needed.
- **Scoped-storage invalidation**: pins live in app-specific dirs
  (`filesDir`, `getExternalFilesDir`) — no permission needed, no invalidation
  across reboots. `Uri.fromFile` is fine for in-process ExoPlayer use
  (FileProvider is only for cross-app sharing).
- **SponsorBlock offsets**: segments are second-based skips on the video
  timeline (ajayyy/SponsorBlock API: `[start, end]` seconds per video ID).
  A pinned file is the same bytes → same timeline → offsets stay valid
  offline. Nothing to fix.
- **DASH-pair merge**: `MergingMediaSource(video, audio)` from two local
  progressive sources is correct as-is; the merge only failed when Cause 1
  (missing dir) or Cause 2 (wrong extractor) fired first.

## Fix applied (this file's commit)
1. `PinFiles`: `roots()` (internal + external-if-present); `existingDir`
   current → internal → external; `delete()` clears both roots.
2. `PinManager.getTo`: optional `mimeOut` captures response `Content-Type`;
   `downloadPin` stores `videoMime`/`audioMime` in `meta.json`.
3. `MainActivity` pinned branch: `setMimeType()` on the video `MediaItem`
   and on the merged external-audio source from stored meta mimes.
4. `FEATURES.md`: one entry line.

Audio-only default, no-autoplay, ar/en strings untouched (no new strings;
`&` escaping N/A).
