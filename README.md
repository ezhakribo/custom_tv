# Pixel Home – a pixel-art home screen for Android TV

## Build (once, on your PC)
1. Install Android Studio (free): https://developer.android.com/studio
2. File > Open > choose this PixelHome folder. Wait for "Gradle sync" to finish.
3. Build > Build App Bundle(s) / APK(s) > Build APK(s).
4. Click "locate". The file is app\build\outputs\apk\debug\app-debug.apk

## Install on the TV (PowerShell, in your Desktop folder)
    platform-tools\adb install -r "PATH\TO\app-debug.apk"
It appears in the TV's app list as "Pixel Home".

## Make the Home button open it (optional, reversible)
Save what the TV uses now (keep this output!):
    platform-tools\adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
Switch to Pixel Home:
    platform-tools\adb shell cmd package set-home-activity com.ibo.pixelhome/.MainActivity
Switch back (use the line you saved, usually this one):
    platform-tools\adb shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity

## Customize
- Pictures: replace PNGs in app/src/main/res/drawable-nodpi/ (keep the names, 16:9 shape).
  Small pixel art (e.g. 64x36) stays sharp; art.py regenerates the originals.
- Which app a tile opens: edit the "EDIT HERE" block at the top of MainActivity.java.
- Inputs: DIGITAL TV opens the AV input, GAMES opens HDMI 3. Hold OK on an input tile to see what the TV reports.
  Full details: platform-tools\adb logcat -s PixelHome

## Remove
    platform-tools\adb uninstall com.ibo.pixelhome
(Switch the Home button back first.)

Font: Press Start 2P (SIL Open Font License, see FONT-LICENSE-OFL.txt).
