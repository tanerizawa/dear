package com.psy.dear.data.static

import com.psy.dear.domain.model.DassQuestion
import com.psy.dear.domain.model.MbtiQuestion
import com.psy.dear.domain.model.TestOption

object DassTestData {
    val questions = listOf(
        DassQuestion("Saya merasa kesal bahkan karena hal kecil", "stress"),
        DassQuestion("Saya merasa mulut saya kering", "anxiety"),
        DassQuestion("Saya tidak dapat merasakan perasaan positif sama sekali", "depression"),
        DassQuestion("Saya mengalami kesulitan bernapas", "anxiety"),
        DassQuestion("Saya merasa sulit memulai melakukan sesuatu", "depression"),
        DassQuestion("Saya cenderung bereaksi berlebihan terhadap situasi", "stress"),
        DassQuestion("Saya mengalami gemetar (misalnya di tangan)", "anxiety"),
        DassQuestion("Saya sulit untuk rileks", "stress"),
        DassQuestion("Saya merasa sedih dan murung", "depression"),
        DassQuestion("Saya tidak memiliki harapan untuk masa depan", "depression"),
        DassQuestion("Saya merasa panik tanpa alasan yang jelas", "anxiety"),
        DassQuestion("Saya merasa saya menggunakan banyak energi saraf", "stress"),
        DassQuestion("Saya khawatir akan situasi yang dapat membuat saya panik dan mempermalukan diri", "anxiety"),
        DassQuestion("Saya merasa hidup tidak berarti", "depression"),
        DassQuestion("Saya merasa mudah tersinggung", "stress"),
        DassQuestion("Saya merasa tidak berharga sebagai pribadi", "depression"),
        DassQuestion("Saya sadar detak jantung saya meningkat tanpa sebab", "anxiety"),
        DassQuestion("Saya sulit untuk menenangkan diri", "stress"),
        DassQuestion("Saya merasa takut tanpa alasan yang jelas", "anxiety"),
        DassQuestion("Saya merasa tidak punya apa-apa yang membuat saya menantikan masa depan", "depression"),
        DassQuestion("Saya merasa tangan saya berkeringat tanpa sebab", "anxiety"),
        DassQuestion("Saya merasa dekat dengan kepanikan", "anxiety"),
        DassQuestion("Saya merasa tidak dapat menikmati apapun", "depression"),
        DassQuestion("Saya merasa gugup ketika harus menghadapi situasi baru", "anxiety"),
        DassQuestion("Saya merasa tegang", "stress"),
        DassQuestion("Saya merasa saya tidak berharga", "depression"),
        DassQuestion("Saya sulit untuk beristirahat", "stress"),
        DassQuestion("Saya merasa cemas ketika berpikir tentang masa depan", "anxiety"),
        DassQuestion("Saya merasa lelah dan lesu", "depression"),
        DassQuestion("Saya merasa mudah gelisah", "stress"),
        DassQuestion("Saya merasa nafas saya cepat walaupun tidak sedang beraktivitas", "anxiety"),
        DassQuestion("Saya merasa susah tidur karena memikirkan banyak hal", "stress"),
        DassQuestion("Saya merasa tidak ada semangat", "depression"),
        DassQuestion("Saya merasa mudah marah", "stress"),
        DassQuestion("Saya merasa detak jantung saya tidak beraturan", "anxiety"),
        DassQuestion("Saya merasa sedih secara terus-menerus", "depression"),
        DassQuestion("Saya merasa sangat gugup", "anxiety"),
        DassQuestion("Saya merasa banyak ketegangan di tubuh", "stress"),
        DassQuestion("Saya merasa ingin menangis", "depression"),
        DassQuestion("Saya merasa khawatir berlebihan", "anxiety"),
        DassQuestion("Saya merasa pikiran saya sibuk dengan berbagai masalah", "stress"),
        DassQuestion("Saya merasa tidak ada yang dapat membuat saya bahagia", "depression"),
        // Tambahan pertanyaan untuk cakupan DASS yang lebih lengkap
        DassQuestion("Saya merasa sulit berkonsentrasi pada apa yang saya kerjakan", "stress"),
        DassQuestion("Saya khawatir kehilangan kendali atas pikiran saya", "anxiety"),
        DassQuestion("Saya kehilangan minat pada kegiatan yang biasanya saya nikmati", "depression"),
        DassQuestion("Saya merasa lelah meskipun tidak banyak beraktivitas", "depression"),
        DassQuestion("Saya merasakan ketegangan pada otot-otot tubuh", "stress"),
        DassQuestion("Saya terus-menerus membayangkan kemungkinan terburuk", "anxiety")
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
        MbtiQuestion("Anda merasa nyaman berbicara di depan banyak orang", "E", "I"),
        MbtiQuestion("Anda lebih suka merencanakan secara terperinci daripada spontan", "J", "P"),
        MbtiQuestion("Anda cenderung mengikuti perasaan dalam mengambil keputusan", "F", "T"),
        MbtiQuestion("Anda menikmati kegiatan sosial yang ramai", "E", "I"),
        MbtiQuestion("Anda lebih tertarik pada ide abstrak dibanding fakta konkret", "N", "S"),
        MbtiQuestion("Anda sering menunda pekerjaan hingga mendekati tenggat waktu", "P", "J"),
        MbtiQuestion("Anda lebih percaya pada intuisi dibanding pengalaman langsung", "N", "S"),
        MbtiQuestion("Anda merasa lelah setelah bertemu banyak orang", "I", "E"),
        MbtiQuestion("Anda memilih keputusan yang logis daripada emosional", "T", "F"),
        MbtiQuestion("Anda suka membuat daftar tugas dan menaatinya", "J", "P"),
        MbtiQuestion("Anda mudah bergaul dengan orang baru", "E", "I"),
        MbtiQuestion("Anda lebih fokus pada apa yang terjadi saat ini daripada kemungkinan masa depan", "S", "N"),
        MbtiQuestion("Anda sering mempertimbangkan perasaan orang lain dalam keputusan", "F", "T"),
        MbtiQuestion("Anda menikmati waktu sendirian untuk mengisi energi", "I", "E"),
        MbtiQuestion("Anda spontan dan fleksibel dalam rencana", "P", "J"),
        MbtiQuestion("Anda suka memikirkan ide-ide teoretis", "N", "S"),
        MbtiQuestion("Anda berpendapat aturan dibuat untuk diikuti", "J", "P"),
        MbtiQuestion("Anda sering mengandalkan fakta nyata dibanding intuisi", "S", "N"),
        MbtiQuestion("Anda merasa lebih nyaman mengekspresikan diri melalui tulisan daripada bicara", "I", "E"),
        MbtiQuestion("Anda cenderung menilai situasi secara objektif daripada subjektif", "T", "F"),
        // Pertanyaan tambahan MBTI
        MbtiQuestion("Anda suka berada di pusat perhatian", "E", "I"),
        MbtiQuestion("Anda lebih suka membuat keputusan setelah semua informasi tersedia", "P", "J"),
        MbtiQuestion("Anda merasa sulit memulai percakapan dengan orang asing", "I", "E"),
        MbtiQuestion("Anda lebih mengandalkan fakta dan data daripada intuisi", "S", "N"),
        MbtiQuestion("Anda menikmati menjaga jadwal yang teratur", "J", "P"),
        MbtiQuestion("Anda lebih memilih pekerjaan yang memungkinkan ekspresi kreatif daripada tugas yang terstruktur", "N", "S")
    )
    val options = listOf(
        TestOption("Sangat tidak setuju", -2),
        TestOption("Tidak setuju", -1),
        TestOption("Netral", 0),
        TestOption("Setuju", 1),
        TestOption("Sangat setuju", 2)
    )
}
