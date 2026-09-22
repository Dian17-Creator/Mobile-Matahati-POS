# Walkthrough: Perbaikan Pemulihan Diskon Pesanan Menggantung (Draft)

Saya telah memperbaiki masalah di mana nilai diskon hilang ketika pesanan yang digantung (Draft) dibuka kembali.

## Perubahan yang Dilakukan

### [HomeScreen.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/HomeScreen.kt)
Pada blok callback `onOpenOrder` dalam `HeldOrdersDialog`:
1. **Membaca Nilai Diskon Draft:** Sistem sekarang membaca `heldOrder.discount` dari data pesanan yang disimpan.
2. **Memulihkan ke State UI:** Jika ada nilai diskon (> 0), nilai tersebut langsung dimasukkan ke `manualDiscountInput` sehingga otomatis terhitung kembali ke keranjang belanja (baik potongan berasal dari voucher sebelumnya maupun diskon manual).
3. **Mencegah Duplikasi:** `selectedVoucher` di-reset ke `null` agar potongan tidak terhitung dua kali lipat.

## Hasil Pengujian & Alur Kerja
- Saat pesanan dengan diskon (misal Rp 10.000 atau dari Voucher) digantung/disimpan ke Draft, nilainya tersimpan di backend.
- Saat pesanan dibuka kembali dari dialog **Pesanan Menggantung**, baris **Diskon** di panel keranjang sekarang langsung muncul kembali beserta nominal potongannya, dan Total Akhir belanja tetap akurat.
