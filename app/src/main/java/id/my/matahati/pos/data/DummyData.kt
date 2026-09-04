package id.my.matahati.pos.data

import id.my.matahati.pos.model.Category
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.model.SalesSummary

object DummyData {
    val summary = SalesSummary(
        totalSalesToday = 2450000.0,
        totalTransactions = 38,
        activeShift = "Shift Pagi",
        cashierName = "April"
    )

    val categories = listOf(
        Category(id = "all", name = "Semua Kategori", iconEmoji = "🍽️"),
        Category(id = "food", name = "Makanan", iconEmoji = "🍲"),
        Category(id = "drink", name = "Minuman", iconEmoji = "☕"),
        Category(id = "snack", name = "Camilan", iconEmoji = "🍿"),
        Category(id = "package", name = "Paket Hemat", iconEmoji = "🍱")
    )

    val products = listOf(
        Product(
            id = "P1",
            name = "BURGER AYAM",
            price = 21000.0,
            categoryId = "food",
            stock = 30,
            iconEmoji = "🍔"
        ),
        Product(
            id = "P2",
            name = "AMERICAN PENYET (DADA)",
            price = 27500.0,
            categoryId = "food",
            stock = 25,
            iconEmoji = "🍗"
        ),
        Product(
            id = "P3",
            name = "AMERICAN PENYET (PAHA)",
            price = 27500.0,
            categoryId = "food",
            stock = 25,
            iconEmoji = "🍗"
        ),
        Product(
            id = "P4",
            name = "AMERICANO HOT",
            price = 18000.0,
            categoryId = "drink",
            stock = 50,
            iconEmoji = "☕"
        ),
        Product(
            id = "P5",
            name = "AMERICANO ICE",
            price = 18000.0,
            categoryId = "drink",
            stock = 50,
            iconEmoji = "🥤"
        ),
        Product(
            id = "P6",
            name = "BAKMIE GORENG MATA HATI",
            price = 25000.0,
            categoryId = "food",
            stock = 20,
            iconEmoji = "🍜"
        ),
        Product(
            id = "P7",
            name = "BLUE SKY PARADISE",
            price = 19000.0,
            categoryId = "drink",
            stock = 35,
            iconEmoji = "🍹"
        ),
        Product(
            id = "P8",
            name = "BUBBLE MILK",
            price = 20000.0,
            categoryId = "drink",
            stock = 40,
            iconEmoji = "🧋"
        ),
        Product(
            id = "P9",
            name = "BUBBLE GUM MILK HOT",
            price = 20000.0,
            categoryId = "drink",
            stock = 30,
            iconEmoji = "🧋"
        ),
        Product(
            id = "P10",
            name = "AYAM GEPREK SAMBAL MATAH",
            price = 22000.0,
            categoryId = "food",
            stock = 15,
            iconEmoji = "🍗"
        ),
        Product(
            id = "P11",
            name = "NASI GORENG SPESIAL",
            price = 25000.0,
            categoryId = "food",
            stock = 20,
            iconEmoji = "🍛"
        ),
        Product(
            id = "P12",
            name = "ROTI BAKAR COKELAT",
            price = 15000.0,
            categoryId = "snack",
            stock = 12,
            iconEmoji = "🍞"
        ),
        Product(
            id = "P13",
            name = "KENTANG GORENG CRISPY",
            price = 12000.0,
            categoryId = "snack",
            stock = 30,
            iconEmoji = "🍟"
        ),
        Product(
            id = "P14",
            name = "ES TEH MANIS JUMBO",
            price = 8000.0,
            categoryId = "drink",
            stock = 80,
            iconEmoji = "🍹"
        )
    )
}
