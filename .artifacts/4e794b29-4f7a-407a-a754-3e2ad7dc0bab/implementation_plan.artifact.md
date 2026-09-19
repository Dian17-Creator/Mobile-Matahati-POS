# Perbaikan Logika Stasiun Printer dan Tampilan Struk

Penyelidikan menunjukkan bahwa item "Americano" hilang dari stasiun BAR karena logika penentuan stasiun masih berbasis nama produk yang sangat terbatas. Selain itu, data "Paid" dan "Change" pada struk transaksi sering terpotong karena adanya batasan tinggi (height) yang kaku pada dialog.

## Proposed Changes

### [Component] Model & Data Mapping
Mengubah logika penentuan stasiun agar menggunakan kategori produk (ID Kategori), bukan lagi mencocokkan nama produk secara manual.

#### [MODIFY] [Product.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/model/Product.kt)
* Memperbarui `ProductDto.toProduct()`:
    * Jika `nidCategory == 2` (Minuman) -> Stasiun **BAR**.
    * Jika `nidCategory == 1` (Makanan) atau `3` (Snack) -> Stasiun **DAPUR**.

### [Component] ViewModel Logic
Memperbaiki logika penentuan stasiun saat mencetak dari riwayat transaksi.

#### [MODIFY] [HomeViewModel.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/HomeViewModel.kt)
* Memperbarui `openKitchenPrintDialogFromHistory`:
    * Alih-alih menebak stasiun berdasarkan nama produk, fungsi ini akan mencari data produk di list `products` lokal untuk mendapatkan stasiun yang benar.

### [Component] UI / Dialogs
Menghapus batasan tinggi yang kaku agar struk dapat menampilkan seluruh data (Subtotal, Grand Total, Paid, Change) secara utuh pada layar yang lebih besar (Tablet).

#### [MODIFY] [ReceiptDialog.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/components/ReceiptDialog.kt)
* Menghapus `heightIn(max = 450.dp)` pada kontainer struk.
* Mengoptimalkan `Modifier.weight(1f, fill = false)` agar dialog tetap proporsional.

#### [MODIFY] [SimulatedReceiptDialog.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/components/SimulatedReceiptDialog.kt)
* Menghapus `heightIn(max = 400.dp)` pada kontainer struk.
* Memastikan tombol aksi tetap berada di posisi paling bawah.

## Verification Plan

### Automated Tests
* Tidak ada unit test yang relevan saat ini, verifikasi dilakukan secara manual melalui UI.

### Manual Verification
* **Cek Stasiun:** Tambahkan "Americano" (Kategori Minuman) ke keranjang, lalu cek di "Preview Pesanan". Pastikan ia muncul di bawah stasiun **BAR**, bukan **DAPUR**.
* **Cek Tampilan Struk:** Lakukan transaksi, lalu cek struk akhir. Pastikan data "Paid" dan "Change" terlihat tanpa harus di-scroll jika layar masih muat.
