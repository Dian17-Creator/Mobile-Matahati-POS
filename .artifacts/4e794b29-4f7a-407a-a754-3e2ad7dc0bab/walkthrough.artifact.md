# Perbaikan Bug Struk dan Logika Stasiun Printer

Saya telah menyelesaikan perbaikan untuk masalah item yang hilang di stasiun printer dan data pembayaran yang terpotong pada struk.

## Perubahan yang Dilakukan

### 1. Logika Stasiun Printer Berbasis Kategori
Sekarang sistem tidak lagi menebak stasiun berdasarkan nama produk, melainkan menggunakan ID Kategori dari database:
* **Kategori 2 (Minuman)** -> Masuk ke Printer **BAR**.
* **Kategori 1 (Makanan) & 3 (Snack)** -> Masuk ke Printer **KITCHEN**.

> [!NOTE]
> Perubahan ini diterapkan pada `Product.kt` (saat mapping data dari server) dan `HomeViewModel.kt` (saat mencetak dari riwayat transaksi).

### 2. Perbaikan Tampilan Struk (Tinggi Dialog)
Saya telah menghapus batasan tinggi statis (`450.dp` & `400.dp`) pada dialog struk.
* Sekarang area struk akan menggunakan ruang yang tersedia secara fleksibel.
* Seluruh data seperti **Grand Total**, **Paid**, dan **Change** akan tampil utuh tanpa terpotong selama layar masih mencukupi.

## File yang Diubah
* [Product.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/model/Product.kt)
* [HomeViewModel.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/HomeViewModel.kt)
* [ReceiptDialog.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/components/ReceiptDialog.kt)
* [SimulatedReceiptDialog.kt](file:///D:/Project/MobileMatahati_POS/app/src/main/java/id/my/matahati/pos/ui/screen/home/components/SimulatedReceiptDialog.kt)

## Verifikasi
1. **Americano:** Karena Americano memiliki `nidCategory` 2 (Minuman), sekarang ia akan otomatis masuk ke Printer **BAR** di Preview Pesanan.
2. **Paid/Change:** Data pembayaran di bagian bawah struk sekarang seharusnya terlihat jelas tanpa perlu di-scroll jika Anda menggunakan tablet.
