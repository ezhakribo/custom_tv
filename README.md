# Pixel Home 2.4 – home screen pixel-art untuk Android TV

## Build
Upload semua isi folder ini ke GitHub, tunggu centang hijau di Actions,
download PixelHome-apk, lalu:
    platform-tools\adb install -r "C:\Users\IBO\Desktop\PixelHome\app-debug.apk"

## Fitur
- Langit hidup: 8 sesi (tiap 3 jam), berpindah sangat halus, matahari & bulan bergerak,
  awan berjalan, bintang berkelip, lampu kota menyala-mati, kadang bintang jatuh.
- Sapaan + tanggal, jam, cuaca Bandung (Open-Meteo), jadwal sholat berikutnya (Kemenag, dihitung di TV).
- Screensaver pixel.
- Tahan OK pada tile: ganti aplikasi, pakai foto sendiri, geser kiri/kanan, kembalikan semula.
- Tahan OK pada SETELAN: suara 8-bit nyala/mati, demo langit 24 jam, atur ulang semua tile.
- Tile FAVORIT: buka channel/playlist YouTube favorit di SmartTube.
- Slideshow selebar layar di bagian bawah: 20 karya pixel art, acak, ganti tiap 10 detik
  dengan transisi dissolve pixel + geser pelan + judul. Matikan/nyalakan: tahan OK pada SETELAN > "Slideshow",
  atau  platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --ez slides false
  (gambar dibuat oleh tools/build.py)
- Karakter berambut afro (dari gambar pemilik) + 2 kucing (oranye & tuxedo) berjalan bolak-balik di bawah;
  di ujung layar ia berhenti menghadap depan sambil tersenyum lalu berbalik (juga di screensaver).
  Gambar diubah oleh tools/hero_from_image.py, kucing digambar oleh tools/cats.py.
  Matikan/nyalakan: tahan OK pada SETELAN > "Karakter berjalan", atau
      platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --ez walker false

## Tampilan (2.4)
- Warna aksen ikut waktu: kuning siang, jingga sore, merah muda petang, biru muda malam.
- Bingkai fokus "bernapas" pelan; nama tile hanya muncul pada tile yang dipilih.
- Efek kedalaman: langit dan gunung bergeser sedikit saat fokus pindah kiri/kanan.
- Animasi pembuka "PIXEL HOME" saat TV baru menyala.
- Semua gambar memakai satu palet 64 warna (tools/unify_palette.py, contoh di tools/palette.png).

## Perintah (dari folder Desktop)
Nama di sapaan:
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --es name IBO
Tile favorit:
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --es favUrl "https://www.youtube.com/@NAMACHANNEL" --es favName LOFI
Suara 8-bit mati / nyala:
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --ez sounds false
Demo langit 24 jam dalam 48 detik:
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --ez skyDemo true

## Screensaver
Simpan nilai lama dulu:
    platform-tools\adb shell settings get secure screensaver_components
Pasang:
    platform-tools\adb shell settings put secure screensaver_components com.ibo.pixelhome/com.ibo.pixelhome.PixelDream
    platform-tools\adb shell settings put secure screensaver_enabled 1
Kembalikan: put lagi nilai lama yang disimpan.

## Foto sendiri
Taruh foto (jpg/png) di folder "PixelHome" di flashdisk, colok ke TV,
tahan OK pada tile > "Pakai foto sendiri" > izinkan akses foto > pilih.

## Tombol remote (helper)
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --es learn home
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --es learn disney
    platform-tools\adb shell am start -n com.ibo.pixelhome/.MainActivity --ez forgetKeys true
Kalau helper berhenti setelah update:
    platform-tools\adb shell settings put secure enabled_accessibility_services null
    platform-tools\adb shell settings put secure enabled_accessibility_services com.ibo.pixelhome/com.ibo.pixelhome.KeyService

## Lokasi lain
Ubah LAT/LON di HomeInfo.java (cuaca & jadwal sholat), lalu build ulang.

Font: Press Start 2P (SIL Open Font License, lihat FONT-LICENSE-OFL.txt).
