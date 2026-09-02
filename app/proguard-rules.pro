# LetterBox ProGuard / R8 rules
#
# Shrinking is enabled; obfuscation is disabled in build.gradle so that
# stack traces remain human-readable (this is Free Software).

# Keep source file names and line numbers for meaningful stack traces.
-keepattributes SourceFile,LineNumberTable

# No obfuscation — names are kept as-is.
-dontobfuscate
