package com.psy.dear.data.static

import com.psy.dear.domain.model.DassQuestion
import com.psy.dear.domain.model.MbtiQuestion
import com.psy.dear.domain.model.TestOption

object DassTestData {
    val questions = listOf(
        DassQuestion("Saya merasa sulit untuk menenangkan diri", "depression"),
        DassQuestion("Saya merasa mulut saya kering", "anxiety"),
        DassQuestion("Saya tidak dapat merasakan perasaan positif sama sekali", "depression"),
        // ... (Tambahkan sisa pertanyaan DASS di sini)
    )
    val options = listOf(
        TestOption("Tidak pernah", 0),
        TestOption("Kadang-kadang", 1),
        TestOption("Sering", 2),
        TestOption("Hampir selalu", 3)
    )
}

object MbtiTestData {
    val questions = listOf(
        MbtiQuestion("Anda lebih suka fokus pada gambaran besar daripada detail", "N", "S"),
        MbtiQuestion("Anda merasa lebih bersemangat setelah menghabiskan waktu dengan banyak orang", "E", "I"),
        MbtiQuestion("Anda lebih mengandalkan logika daripada perasaan saat mengambil keputusan", "T", "F"),
        // ... (Tambahkan sisa pertanyaan MBTI di sini)
    )
    val options = listOf(
        TestOption("Sangat tidak setuju", -2),
        TestOption("Tidak setuju", -1),
        TestOption("Netral", 0),
        TestOption("Setuju", 1),
        TestOption("Sangat setuju", 2)
    )
}
