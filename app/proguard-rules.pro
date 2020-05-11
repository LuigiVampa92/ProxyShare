# ----- AndroidX -----
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

# ----- AppCompat ----- (backward compatability, just in case)
-dontwarn android.support.v4.**
-dontwarn android.support.v7.**
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }

-keep class com.luigivampa92.nfcshare.hce.NdefHceService