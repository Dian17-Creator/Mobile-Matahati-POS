# Rencana Perbaikan Bug Diskon Pesanan Menggantung (Draft)

Ditemukan masalah di mana nilai diskon (baik manual maupun voucher) hilang ketika pesanan yang "menggantung" (draft) dibuka kembali. Hal ini disebabkan karena logika pemulihan pesanan belum menyertakan pemulihan nilai diskon ke dalam state UI.

## Analisis Masalah
- Saat menyimpan pesanan ke draft (`holdCurrentCart`), nilai diskon sudah dikirim ke server.
- Namun, saat membuka kembali draft (`onOpenOrder`), aplikasi hanya memulihkan item keranjang, meja, tipe pesanan, dan pelanggan.
- State `manualDiscountInput` dan `selectedVoucher` di `HomeScreen.kt` tidak diperbarui dengan nilai diskon dari data draft.

## Perubahan yang Diusulkan

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/HomeScreen.kt)
- Memperbarui fungsi `onOpenOrder` di dalam `HeldOrdersDialog` untuk:
    - Mengambil nilai `discount` dari `heldOrder`.
    - Memasukkan nilai tersebut ke dalam `manualDiscountInput`.
    - Memastikan `selectedVoucher` diatur ke `null` untuk menghindari perhitungan ganda (karena nilai diskon dari voucher sudah tercakup dalam total nominal diskon yang dipulihkan).

## Rencana Verifikasi

### Tes Manual
1. **Simpan Draft dengan Diskon:** Tambahkan item -> Berikan diskon manual Rp 10.000 -> Klik Tahan Pesanan (Draft).
2. **Buka Draft:** Buka menu Pesanan Menggantung -> Pilih pesanan tadi -> Pastikan nilai Diskon Rp 10.000 muncul kembali di panel keranjang.
3. **Draft dengan Voucher:** Ulangi langkah di atas menggunakan Voucher -> Pastikan nilai nominal diskon voucher muncul kembali sebagai diskon di keranjang (meskipun status voucher master tidak terpilih kembali, nilainya tetap terjaga).
