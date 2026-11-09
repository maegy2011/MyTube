-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
# Rhino (NewPipeExtractor dep): desktop-only APIs absent on Android — never called at runtime
-dontwarn java.beans.**
-dontwarn javax.script.**
