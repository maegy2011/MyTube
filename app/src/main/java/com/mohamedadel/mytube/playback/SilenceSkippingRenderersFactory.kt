package com.mohamedadel.mytube.playback

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor

// B3 silence skipping: DefaultRenderersFactory has NO setAudioProcessors (verified —
// only setRenderersFactory on the Builder), so we subclass and override the protected
// buildAudioSink, injecting SilenceSkippingAudioProcessor into DefaultAudioSink.
// The processor chain is fixed at player build → toggling rebuilds the player (recreate route).
class SilenceSkippingRenderersFactory(ctx: Context) : DefaultRenderersFactory(ctx) {
    @Volatile var skipSilence: Boolean = false
    private val silence = SilenceSkippingAudioProcessor()

    override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioTrackPlaybackParams: Boolean): AudioSink {
        try { silence.setEnabled(skipSilence) } catch (_: Exception) { }
        return DefaultAudioSink.Builder(context)
            .setAudioProcessors(arrayOf<AudioProcessor>(silence))
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .build()
    }
}
