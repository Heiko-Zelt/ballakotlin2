# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1)
# val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderY)

#-keep public class de.heikozelt.ballakotlin2.view.** { *; }
#-keep public class de.heikozelt.ballakotlin2.view.Coordinates { *; }
-keepclassmembernames public class de.heikozelt.ballakotlin2.view.Coordinates { *; }