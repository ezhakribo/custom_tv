@echo off
REM Pixel Home - bersihkan TV. Taruh file ini di Desktop (di samping folder platform-tools), lalu klik dua kali.
cd /d "%~dp0"
echo Menyambung ke TV...
platform-tools\adb connect 192.168.18.115:5555
echo.
echo Mematikan aplikasi yang tidak perlu (bisa dikembalikan dengan pixelhome-undo.bat):
echo   - PatchWall (home Xiaomi + iklan autoplay)
platform-tools\adb shell pm disable-user --user 0 com.mitv.tvhome.atv >nul 2>&1
echo   - Home Google TV
platform-tools\adb shell pm disable-user --user 0 com.google.android.tvlauncher >nul 2>&1
echo   - Android TV Core Services (rekomendasi)
platform-tools\adb shell pm disable-user --user 0 com.google.android.tvrecommendations >nul 2>&1
echo   - Launcher Android TV lama
platform-tools\adb shell pm disable-user --user 0 com.google.android.leanbacklauncher >nul 2>&1
echo   - Rekomendasi launcher lama
platform-tools\adb shell pm disable-user --user 0 com.google.android.leanbacklauncher.recommendations >nul 2>&1
echo   - Screensaver Google
platform-tools\adb shell pm disable-user --user 0 com.google.android.backdrop >nul 2>&1
echo   - Screensaver Xiaomi
platform-tools\adb shell pm disable-user --user 0 com.mitv.dream >nul 2>&1
echo   - Analitik Xiaomi
platform-tools\adb shell pm disable-user --user 0 com.miui.tv.analytics >nul 2>&1
echo   - Statistik Xiaomi
platform-tools\adb shell pm disable-user --user 0 com.xiaomi.statistic >nul 2>&1
echo   - Pemasang aplikasi promosi otomatis
platform-tools\adb shell pm disable-user --user 0 android.autoinstalls.config.xioami.dangal >nul 2>&1
echo   - Buku panduan Mi TV
platform-tools\adb shell pm disable-user --user 0 com.xiaomi.mitv.handbook >nul 2>&1
echo.
REM Opsional: hapus "REM " di depan baris yang ingin ikut dimatikan.
echo   - Mi TV+ (channel gratis, bisa autoplay)
REM platform-tools\adb shell pm disable-user --user 0 com.mitv.tvhome.mitvplus >nul 2>&1
echo   - Prime Video
REM platform-tools\adb shell pm disable-user --user 0 com.amazon.amazonvideo.livingroom >nul 2>&1
echo   - Google TV / Play Movies
REM platform-tools\adb shell pm disable-user --user 0 com.google.android.videos >nul 2>&1
echo   - Play Games
REM platform-tools\adb shell pm disable-user --user 0 com.google.android.play.games >nul 2>&1
echo   - YouTube Music
REM platform-tools\adb shell pm disable-user --user 0 com.google.android.youtube.tvmusic >nul 2>&1
echo   - TV Manager Xiaomi
REM platform-tools\adb shell pm disable-user --user 0 com.xiaomi.mitv.tvmanager >nul 2>&1
echo   - Mi Music
REM platform-tools\adb shell pm disable-user --user 0 com.xiaomi.mimusic2 >nul 2>&1
echo   - Mi Gallery
REM platform-tools\adb shell pm disable-user --user 0 com.mitv.gallery >nul 2>&1
echo.
echo Menjadikan Pixel Home sebagai home...
platform-tools\adb shell cmd package set-home-activity com.ibo.pixelhome/.MainActivity
echo.
echo Selesai. TV akan restart sekarang.
platform-tools\adb reboot
pause
