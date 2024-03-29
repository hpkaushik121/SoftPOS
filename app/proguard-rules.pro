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

# Appsflyer
-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; }
-ignorewarnings
-keep class org.** { *; }
-keep class com.nownow.** { *; }
-dontwarn com.nownow.**
-keepattributes *Annotation*
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
@retrofit.http.* <methods>; }
-keepattributes Signature
-keepclasseswithmembers class com.nownow.softpos.models.** {
    private <fields>;
}
-keepclasseswithmembers class com.nownow.softpos.models.downloadPdfModels.** {
    private <fields>;
}
-keep class androidx.appcompat.widget.** { *; }
