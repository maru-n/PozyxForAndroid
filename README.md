# StargazerForAndroid

This is a library to use Stargazer by Hagisonic (http://www.hagisonic.com/), localization device on Android.

USB-Serial connection depends on usb-serial-for-android https://github.com/mik3y/usb-serial-for-android.

**This is only for My personal project. Plese don't use for production!**

## Install Sample App
https://github.com/maru-n/StargazerForAndroid/raw/master/app/app-release.apk

## Library Usage

### Installation

build.gradle

```diff
 allprojects {
     repositories {
         ...
+        maven {url "https://oss.sonatype.org/content/repositories/snapshots" }
+        maven {url "https://raw.githubusercontent.com/maru-n/StargazerForAndroid/master/repository" }
         ...
     }
 }
```

app/build.gradle

```diff
 dependencies {
     ...
+    compile 'jp.co.mti.marun.android:stargazer-for-android:+' 
     ...
 }
```

### Example code

https://github.com/maru-n/StargazerForAndroid/tree/master/app/src/main/java/jp/co/mti/marun/android/stargazer/example


### Multi ID Map file format

```
# id, angle, x, y, z
24836 0 0 0 0
25092 0 1.5 0 0
24594 0 0 -1.5 0
24706 0 1.5 -1.5 0
```
