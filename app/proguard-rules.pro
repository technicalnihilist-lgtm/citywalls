# CityWalls release rules.
-keepattributes Signature
-keep class com.citywalls.app.data.remote.dto.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
