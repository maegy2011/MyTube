-keep class org.schabi.newpipe.extractor.** { *; }
# Cast sender: provider is referenced from the manifest by name (reflection)
-keep class com.mohamedadel.mytube.playback.CastOptionsProvider { *; }
-dontwarn org.schabi.newpipe.extractor.**
# Rhino (NewPipeExtractor dep): desktop-only APIs absent on Android — never called at runtime
-dontwarn java.beans.**
-dontwarn javax.script.**
