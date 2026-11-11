package com.mohamedadel.mytube

// P2 item 30 (audit 2026-11-11): Room 1->9 migration skeleton.
// Needs an ONLINE sync first (room-testing + runner), then:
//   ./gradlew :app:connectedDebugAndroidTest
// Schema assertions land here once the first online run exports v9;
// until then this file documents the matrix, not the verdict.
object MigrationMatrix {
    // PlaylistDb v9 — every step must hold user data (no destructive fallback in release):
    val steps = listOf(1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8, 8 to 9)
}
