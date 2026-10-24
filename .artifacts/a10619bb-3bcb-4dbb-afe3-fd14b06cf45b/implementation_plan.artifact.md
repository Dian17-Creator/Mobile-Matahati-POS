# Rencana Perbaikan Tampilan Dialog Pesanan Menggantung pada Versi Mobile Phone

## Masalah saat ini
Pada tampilan smartphone (mobile phone):
1. **Dialog Terlalu Sempit / Batasan Platform**: Dialog bawaan Compose membatasi lebar default platform (`usePlatformDefaultWidth`) dan `Surface` menggunakan `width(580.dp)` serta gabungan `padding` yang sangat besar, membuat konten dalam kartu pesanan terdesak hingga selebar ~200dp.
2. **Teks Terlipat/Pecah Vertikal**: Tanggal/waktu transaksi (`2026-10-23 10:51`) terhimpit oleh ID transaksi (`#10C6226102300000012`) dan label "Ketuk untuk membuka", sehingga teks tanggal terpotong dan terlipat karakter demi karakter secara vertikal.

## Solusi yang Diusulkan
Memisahkan penanganan layout berdasarkan tipe perangkat (`isTablet` vs mobile phone):
1. **Versi Tablet (`isTablet == true`)**:
   - Mempertahankan penuh struktur layout dan kode yang ada saat ini agar tampilan di tablet tidak berubah sama sekali.
2. **Versi Mobile Phone (`isTablet == false`)**:
   - Menggunakan `DialogProperties(usePlatformDefaultWidth = false)` agar dialog memanfaatkan lebar layar hp dengan rasio yang proporsional (~92%-95% lebar layar hp).
   - Mengurangi *padding* berlebih agar ruang horizontal untuk kartu pesanan jauh lebih lapang.
   - Menyusun ulang kartu pesanan khusus tampilan mobile phone:
     - **Baris Atas**: Nomor/ID Transaksi di sisi kiri dan aksi "Ketuk untuk membuka" di sisi kanan.
     - **Baris Bawah**: Tanggal/waktu transaksi dan info meja/pelanggan disusun rapi secara bertingkat di bawah ID transaksi.
     - Hal ini menjamin nomor transaksi panjang maupun tanggal transaksi tidak akan pernah terlipat vertikal per karakter lagi.

---

## Proposed Changes

### `app/src/main/java/id/my/matahati/pos/ui/screen/home/components/HeldOrdersDialog.kt`

#### [MODIFY] [HeldOrdersDialog.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/components/HeldOrdersDialog.kt)
- Menambahkan `BoxWithConstraints` untuk mendeteksi `isTablet = maxWidth >= 600.dp`.
- Menambahkan `DialogProperties(usePlatformDefaultWidth = false)`.
- Jika `isTablet`, jalankan komponen layout tablet eksisting tanpa perubahan logic.
- Jika `!isTablet`, jalankan komponen `MobileHeldOrderItem` atau layout mobile yang lebih fleksibel dan rapi.

---

## Verification Plan

### Automated Tests
- Menjalankan Gradle build `app:assembleDebug` untuk memastikan tidak ada kesalahan kompilasi.

### Manual Verification
- Pengujian tampilan dialog "Pesanan Menggantung" di layar handphone untuk memastikan nomor transaksi, tanggal, info meja, dan teks "Ketuk untuk membuka" sejajar dan terformat rapi tanpa ada teks yang terlipat per karakter.
