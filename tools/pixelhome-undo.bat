@echo off
REM Pixel Home - kembalikan semua aplikasi yang dimatikan oleh pixelhome-cleanup.bat
cd /d "%~dp0"
platform-tools\adb connect 192.168.18.115:5555
echo Menyalakan kembali:
echo   - PatchWall (home Xiaomi + iklan autoplay)
platform-tools\adb shell pm enable com.mitv.tvhome.atv >nul 2>&1
echo   - Home Google TV
platform-tools\adb shell pm enable com.google.android.tvlauncher >nul 2>&1
echo   - Android TV Core Services (rekomendasi)
platform-tools\adb shell pm enable com.google.android.tvrecommendations >nul 2>&1
echo   - Launcher Android TV lama
platform-tools\adb shell pm enable com.google.android.leanbacklauncher >nul 2>&1
echo   - Rekomendasi launcher lama
platform-tools\adb shell pm enable com.google.android.leanbacklauncher.recommendations >nul 2>&1
echo   - Screensaver Google
platform-tools\adb shell pm enable com.google.android.backdrop >nul 2>&1
echo   - Screensaver Xiaomi
platform-tools\adb shell pm enable com.mitv.dream >nul 2>&1
echo   - Analitik Xiaomi
platform-tools\adb shell pm enable com.miui.tv.analytics >nul 2>&1
echo   - Statistik Xiaomi
platform-tools\adb shell pm enable com.xiaomi.statistic >nul 2>&1
echo   - Pemasang aplikasi promosi otomatis
platform-tools\adb shell pm enable android.autoinstalls.config.xioami.dangal >nul 2>&1
echo   - Buku panduan Mi TV
platform-tools\adb shell pm enable com.xiaomi.mitv.handbook >nul 2>&1
echo   - Mi TV+ (channel gratis, bisa autoplay)
platform-tools\adb shell pm enable com.mitv.tvhome.mitvplus >nul 2>&1
echo   - Prime Video
platform-tools\adb shell pm enable com.amazon.amazonvideo.livingroom >nul 2>&1
echo   - Google TV / Play Movies
platform-tools\adb shell pm enable com.google.android.videos >nul 2>&1
echo   - Play Games
platform-tools\adb shell pm enable com.google.android.play.games >nul 2>&1
echo   - YouTube Music
platform-tools\adb shell pm enable com.google.android.youtube.tvmusic >nul 2>&1
echo   - TV Manager Xiaomi
platform-tools\adb shell pm enable com.xiaomi.mitv.tvmanager >nul 2>&1
echo   - Mi Music
platform-tools\adb shell pm enable com.xiaomi.mimusic2 >nul 2>&1
echo   - Mi Gallery
platform-tools\adb shell pm enable com.mitv.gallery >nul 2>&1
platform-tools\adb shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
echo Selesai. Home Google aktif lagi. TV akan restart.
platform-tools\adb reboot
pause
