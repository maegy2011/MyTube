package com.mohamedadel.mytube.playback

// T4 (audit P1 2026-11-11): first slice of the God-Activity split — pure, testable
// queue-navigation helpers with zero Android/Compose dependencies.
// MainActivity's local playNextInQueue/playPrevInQueue/playRandomInQueue delegate the
// index math here; the effectful part (toasts, playVideo calls, Radio refill) stays in
// the composition for now.
// Follow-up (not this pass): move the queue state itself + radio refill + castCurrent
// into a state holder (PlayerController class) so the math and the effects live together.

// Sequential next: null at the end (caller decides repeat/radio behavior).
fun nextSequentialIndex(size: Int, current: Int): Int? =
    if (current in 0 until size - 1) current + 1 else null

// Sequential previous: null at the head.
fun prevSequentialIndex(current: Int): Int? =
    if (current > 0) current - 1 else null

// Shuffled play order as favKeys, head = next to play. The currently playing key is
// excluded so shuffle never repeats the current item; stale keys (queue changed) are
// filtered by the caller against the live queue.
fun shuffledFavKeys(
    queueKeys: List<String>,
    excludeKey: String?,
    seed: Long = System.nanoTime()
): List<String> {
    return try {
        val pool = queueKeys.filter { it != excludeKey }.shuffled(java.util.Random(seed))
        pool
    } catch (_: Exception) {
        queueKeys.filter { it != excludeKey }
    }
}
