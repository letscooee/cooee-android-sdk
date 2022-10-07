# Ref. what consumer progard: https://stackoverflow.com/a/60862591/9256497
-keep class com.letscooee.models.** {*;}

# Prevent enum members from getting minify
# Ref. keep & keepclassmembers https://stackoverflow.com/questions/16479948/what-is-the-difference-between-keep-and-keepclassmembers-in-proguard
-keepclassmembers enum * {*;}

# Prevent android-retrofuture library class, interface, enum from getting minify
# https://github.com/retrostreams/android-retrofuture/blob/master/proguard-rules.pro
-keep class java9.** { *; }
-keep interface java9.** { *; }
-keep enum java9.** { *; }