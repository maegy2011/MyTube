# MyTube (MVP-1 v0.0.1)
Clean native YouTube client — Kotlin + Compose + M3 + NewPipe Extractor only.
- Search + smart autocomplete (debounce 300ms, extractor suggestions, search fires immediately)
- Results as-is, infinite scroll, Coil lazy loading
- Playback Media3: default audio-only lowest quality, auto by bandwidth, quality list [audio-only, auto, 144p..1080p]
- Background audio-only (MediaSessionService), PiP auto on Home + manual button, plays with screen locked
- Fallback: "Open in YouTube" button (vnd.youtube intent)
- Package: com.mohamedadel.mytube, minSdk 26, target 34
- Extractor: always latest (dynamic +) per owner decision — rebuild risk accepted
