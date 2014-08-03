 -optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-dontobfuscate

-keepclassmembers class ** {
    public void onEvent*(**);
}

-dontwarn com.squareup.okhttp.**
-dontwarn rx.**
-dontwarn retrofit.appengine.**
-dontwarn okio.**
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-keep interface retrofit.** { *; }

-keep class **$Properties
-keep interface de.moinapp.moin.api.**
-keep class de.moinapp.moin.api.**