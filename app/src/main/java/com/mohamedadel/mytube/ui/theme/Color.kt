package com.mohamedadel.mytube.ui.theme

import androidx.compose.ui.graphics.Color

// ── Tier 0 · raw palette (YouTube-red family + neutral ramps) ────────────────
// Raw values only. Nothing outside Tier 1 may reference these.
private val Red500 = Color(0xFFE53935) // brand mark (logo, splash)
private val Red600 = Color(0xFFD32F2F) // action red — white text ≥ 4.9:1 on it
private val Red300 = Color(0xFFFF8A80) // signal red — readable small text on dark surfaces (≥ 7.5:1)

private val Neutral950 = Color(0xFF0F0F12)
private val Neutral900 = Color(0xFF1A1A1F)
private val Neutral800 = Color(0xFF25252C)
private val Neutral700 = Color(0xFF3A3A44)
private val Neutral100 = Color(0xFFF0F0F4)
private val Neutral50 = Color(0xFFFAFAFA)
private val White = Color(0xFFFFFFFF)

private val InkOnDark = Color(0xFFECECEC) // 14.6–16.2:1 on dark surfaces
private val InkMetaOnDark = Color(0xFFAAAAAA) // 7.4–8.2:1 on dark surfaces
private val InkOnLight = Color(0xFF1A1A1F) // 16.6–17.3:1 on light surfaces
private val InkMetaOnLight = Color(0xFF5A5A66) // 6.5–6.8:1 on light surfaces

// ── Tier 1 · semantic aliases (meaning, not value) ───────────────────────────
// Light/dark toggle = swap the Tier-0 mapping below; call sites never change.
// Verification blue: dark-on-light / light-on-dark (both ≥ 6.3:1 as text on surfaces)
val VerifiedOnLight = Color(0xFF0B57D0)
val VerifiedOnDark = Color(0xFF7AB8FF)

val BrandMark = Red500
val SplashScrim = Color(0xFFD5392E)

val ActionLight = Red600 // fills + text on light surfaces (4.98:1 both ways)
val ActionDark = Red600 // fills in dark (white text 4.98:1); icons ≥ 3.4:1
val SignalOnDark = Red300 // small red text on dark surfaces (7.5:1)

// Dark scheme
val DarkBackground = Neutral950
val DarkSurface = Neutral900
val DarkSurfaceVariant = Neutral800
val DarkOnSurface = InkOnDark
val DarkOnSurfaceVariant = InkMetaOnDark
val DarkOutline = Neutral700

// Light scheme
val LightBackground = Neutral50
val LightSurface = White
val LightSurfaceVariant = Neutral100
val LightOnSurface = InkOnLight
val LightOnSurfaceVariant = InkMetaOnLight
val LightOutline = Color(0xFFD6D6DE)
