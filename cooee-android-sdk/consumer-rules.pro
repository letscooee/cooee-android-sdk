# Ref. what consumer progard: https://stackoverflow.com/a/60862591/9256497
-keep class com.letscooee.models.** {*;}
-keepclassmembers enum * {*;}

# https://github.com/retrostreams/android-retrofuture/blob/master/proguard-rules.pro
-keep class java9.** { *; }
-keep interface java9.** { *; }
-keep enum  java9.** { *; }