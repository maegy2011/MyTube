# MyTube 🎬

> A clean, fast, audio-first native YouTube client for Android — Kotlin + Jetpack Compose + Media3.

[![Build APK](https://github.com/maegy2011/MyTube/actions/workflows/build-apk.yml/badge.svg)](https://github.com/maegy2011/MyTube/actions/workflows/build-apk.yml)
[![Release APK](https://github.com/maegy2011/MyTube/actions/workflows/release.yml/badge.svg)](https://github.com/maegy2011/MyTube/actions/workflows/release.yml)
[![Latest release](https://img.shields.io/github/v/release/maegy2011/MyTube)](https://github.com/maegy2011/MyTube/releases/latest)

## Why MyTube?

- 🎧 **Audio-first** — plays YouTube as music: lowest-bandwidth audio by default, keeps playing with the screen off
- ⚡ **Lightweight** — ~7 MB release APK (R8-optimized), no WebView, no bloated SDKs
- 🔒 **Private** — no account needed, no tracking; extraction runs on-device via NewPipe Extractor
- 🎨 **Modern UI** — Material 3, Arabic RTL, dark-first design

## Features

| Area | Highlights |
|---|---|
| 🔍 Search | Smart autocomplete, trending tabs, channel pages, infinite scroll |
| ▶️ Playback | Media3 ExoPlayer · audio-only / auto / 144p–1080p quality picker |
| 🎧 Background | MediaSessionService — plays locked-screen, PiP auto on Home |
| 📻 Radio & Queue | Autoplay radio, draggable queue, resume where you left off |
| 📚 Library | Playlists (CSV import/export), favorites, watch history, backup/restore |
| 📡 Cast | Chromecast sender (default receiver) via official `media3-cast` CastPlayer |
| 🎛️ Extras | Equalizer, sleep timer, captions, playback speed, local audio files |

## Download

Grab the latest signed APK from [**Releases**](https://github.com/maegy2011/MyTube/releases/latest) — install over any previous MyTube build (same signature).

> ⚠️ Side-loaded only (not on Play Store). A signer change would require a clean reinstall.

## Build it yourself

```bash
# requirements: JDK 17, Android SDK 36
./gradlew assembleDebug          # ~28 MB, quick local builds
```

Release builds are signed on **GitHub Actions** only (family keystore lives outside the repo).

## Tech stack

- **Language:** Kotlin 2.2 · **UI:** Jetpack Compose + Material 3
- **Playback:** Media3 1.11.1 (ExoPlayer + Cast) · **Extraction:** NewPipe Extractor 0.27.1
- **Storage:** Room 2.8.5 · **DI-free**, single-activity architecture
- **CI:** GitHub Actions (`Build APK` on every push, `Release APK` on `v*` tags)

## Project layout

```
app/src/main/java/com/mohamedadel/mytube/
├── MainActivity.kt          # shell (~2.9k lines, shrinking per slice)
├── ui/                      # player · settings · playlists · share · results
├── playback/                # PlayerViewModel · PlayerEngine · CastManager · CastPlayerHolder
├── data/                    # Room DB · repos · prefs
└── util/                    # 25+ focused helper slices (NavStack, UnifiedSearch, …)
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and [`docs/FEATURES.md`](docs/FEATURES.md) for the full tour.

## Contributing

PRs welcome — keep functions ≤ 30 lines, files ≤ 300 lines, behavior identical unless the PR says otherwise. CI must stay green.

## License

Private project — all rights reserved (for now).
