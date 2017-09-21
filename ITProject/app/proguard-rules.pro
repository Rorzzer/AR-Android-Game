# Add project specific ProGuard rules here.
-keepattributes Signature

# By default, the flags in this file are appended to flags specified
# in C:\Users\Kiptenai\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#-keepattributes Signature

-keepclassmembers class com.unimelb.comp30022.itproject.** {
  *;
}


# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-keepclassmembers class com.unimelb.comp30022.itproject.models.** {
# *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
#-keepattributes Signature

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
