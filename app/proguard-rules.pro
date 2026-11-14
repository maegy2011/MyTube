-keep class org.schabi.newpipe.extractor.** { *; }
# Cast sender: provider is referenced from the manifest by name (reflection)
-keep class com.mohamedadel.mytube.playback.CastOptionsProvider { *; }
# CastContext init is reflection-heavy — release R8 renamed GMS internals and
# getSharedInstance() died with "Failed to initialize CastContext" (device report v0.4.1).
-keep class com.google.android.gms.cast.** { *; }
-keep class androidx.mediarouter.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
# Rhino (NewPipeExtractor dep): desktop-only APIs absent on Android — never called at runtime
-dontwarn java.beans.**
-dontwarn javax.script.**
