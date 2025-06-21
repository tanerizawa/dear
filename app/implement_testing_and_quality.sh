#!/bin/bash

# Skrip untuk MEMBUAT dan MEMPERBARUI file-file yang diperlukan
# untuk infrastruktur pengujian (Unit & UI Test) dan sentralisasi string.
# Jalankan skrip ini dari dalam direktori root modul: app/

echo "Memulai implementasi Stabilitas dan Kualitas Kode..."
BASE_PATH="src/main/java/com/psy/dear"
TEST_PATH="src/test/java/com/psy/dear"
ANDROID_TEST_PATH="src/androidTest/java/com/psy/dear"

# --- 1. Buat Direktori Baru untuk Pengujian ---
echo "Membuat direktori pengujian..."
mkdir -p "$TEST_PATH/presentation/home"
mkdir -p "$TEST_PATH/data/repository"
mkdir -p "$TEST_PATH/util"
mkdir -p "$ANDROID_TEST_PATH/di"
mkdir -p "$ANDROID_TEST_PATH/util"


# --- 2. Isi File-file untuk Unit Testing (src/test) ---
echo "Menulis file untuk Unit Testing..."

# --- util/TestCoroutineRule.kt ---
cat << 'EOF' > "$TEST_PATH/util/TestCoroutineRule.kt"
package com.psy.dear.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class TestCoroutineRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
EOF

# --- data/repository/FakeJournalRepository.kt ---
cat << 'EOF' > "$TEST_PATH/data/repository/FakeJournalRepository.kt"
package com.psy.dear.data.repository

import com.psy.dear.core.Result
import com.psy.dear.domain.model.GrowthStatistics
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.OffsetDateTime

class FakeJournalRepository : JournalRepository {

    private val journals = mutableListOf<Journal>()
    private val journalsFlow = MutableStateFlow<List<Journal>>(emptyList())
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun addJournal(journal: Journal) {
        journals.add(journal)
        refreshFlow()
    }

    private fun refreshFlow() {
        journalsFlow.value = journals
    }

    override fun getJournals(): Flow<List<Journal>> = journalsFlow.asStateFlow()

    override suspend fun syncJournals(): Result<Unit> {
        return if (shouldReturnError) Result.Error(Exception("Sync failed")) else Result.Success(Unit)
    }

    override suspend fun createJournal(title: String, content: String, mood: String): Result<Unit> {
        if(shouldReturnError) return Result.Error(Exception("Create failed"))
        journals.add(Journal(id = (journals.size + 1).toString(), title, content, mood, OffsetDateTime.now()))
        refreshFlow()
        return Result.Success(Unit)
    }

    override suspend fun getGrowthStatistics(): Result<GrowthStatistics> {
        return Result.Success(GrowthStatistics(journals.size, "Senang", 0.8))
    }
}
EOF

# --- presentation/home/HomeViewModelTest.kt ---
cat << 'EOF' > "$TEST_PATH/presentation/home/HomeViewModelTest.kt"
package com.psy.dear.presentation.home

import app.cash.turbine.test
import com.psy.dear.data.repository.FakeJournalRepository
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import com.psy.dear.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeJournalRepository
    private lateinit var getJournalsUseCase: GetJournalsUseCase
    private lateinit var syncJournalsUseCase: SyncJournalsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeJournalRepository()
        getJournalsUseCase = GetJournalsUseCase(fakeRepository)
        syncJournalsUseCase = SyncJournalsUseCase(fakeRepository)

        // Tambahkan beberapa data dummy
        fakeRepository.addJournal(Journal("1", "Test 1", "Content 1", "Happy", OffsetDateTime.now()))

        viewModel = HomeViewModel(getJournalsUseCase, syncJournalsUseCase)
    }

    @Test
    fun `state reflects journals from repository on init`() = runTest {
        // ViewModel diinisialisasi di setUp, jadi kita langsung tes state-nya
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.journals.size)
            assertEquals("Test 1", initialState.journals[0].title)
            assertFalse(initialState.isLoading)
            assertNull(initialState.error)
        }
    }

    @Test
    fun `refresh updates loading state and handles success`() = runTest {
        viewModel.state.test {
            // Abaikan state awal
            awaitItem()

            // Panggil refresh
            viewModel.refresh()

            // Harapannya state menjadi loading
            var loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Setelah operasi selesai, state loading kembali false
            loadingState = awaitItem()
            assertFalse(loadingState.isLoading)
            assertNull(loadingState.error)
        }
    }

    @Test
    fun `refresh handles error from repository`() = runTest {
        // Atur repository agar mengembalikan error
        fakeRepository.setShouldReturnError(true)

        viewModel.state.test {
            awaitItem() // Abaikan state awal

            viewModel.refresh()

            awaitItem() // State loading

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals("Sync failed", errorState.error)
        }
    }
}
EOF

# --- 3. Isi File-file untuk UI Testing (src/androidTest) ---
echo "Menulis file untuk UI Testing..."

# --- di/TestAppModule.kt ---
cat << 'EOF' > "$ANDROID_TEST_PATH/di/TestAppModule.kt"
package com.psy.dear.di

import com.psy.dear.data.repository.*
import com.psy.dear.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

// Modul ini akan menggantikan RepositoryModule yang asli saat menjalankan instrumented test
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestAppModule {

    // Menyediakan implementasi FAKE dari repository untuk testing
    @Binds
    @Singleton
    abstract fun bindFakeAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFakeJournalRepository(impl: FakeJournalRepository): JournalRepository
}

// Implementasi Fake untuk UI Test (perlu dibuat di sini karena visibility)
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    private var _isLoggedIn = false
    override val isLoggedIn: Flow<Boolean> = flowOf(_isLoggedIn)
    override suspend fun login(email: String, password: String): Result<Unit> {
        _isLoggedIn = true
        return Result.Success(Unit)
    }
    // Implementasi lainnya...
}

@Singleton
class FakeJournalRepository @Inject constructor() : JournalRepository {
    // Implementasi fake yang relevan untuk UI test
}
EOF


# --- util/HiltExt.kt ---
cat << 'EOF' > "$ANDROID_TEST_PATH/util/HiltExt.kt"
package com.psy.dear.util

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.psy.dear.HiltTestActivity // Activity khusus untuk testing
import com.psy.dear.R

// Fungsi ekstensi untuk meluncurkan fragment di dalam HiltTestActivity
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.Theme_DearDiary,
    fragmentFactory: FragmentFactory? = null,
    crossinline action: T.() -> Unit = {}
) {
    val mainActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra("androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY", themeResId)

    ActivityScenario.launch<HiltTestActivity>(mainActivityIntent).onActivity { activity ->
        fragmentFactory?.let {
            activity.supportFragmentManager.fragmentFactory = it
        }
        val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()
        (fragment as T).action()
    }
}
EOF

# --- 4. Sentralisasi String ---
echo "Memusatkan string resources..."
# Membuat file strings.xml di res/values
mkdir -p "src/main/res/values"
cat << 'EOF' > "src/main/res/values/strings.xml"
<resources>
    <string name="app_name">Dear Diary</string>

    <!-- Auth -->
    <string name="login_title">Login</string>
    <string name="register_title">Register</string>
    <string name="email_label">Email</string>
    <string name="password_label">Password</string>
    <string name="username_label">Username</string>
    <string name="login_button">Login</string>
    <string name="register_button">Register</string>
    <string name="login_to_register_prompt">Belum punya akun? Register</string>
    <string name="register_to_login_prompt">Sudah punya akun? Login</string>

    <!-- Main Navigation -->
    <string name="nav_home">Beranda</string>
    <string name="nav_chat">Chat</string>
    <string name="nav_growth">Pertumbuhan</string>
    <string name="nav_services">Layanan</string>
    <string name="nav_profile">Profil</string>

    <!-- Home Screen -->
    <string name="home_new_entry_button_desc">Entri Baru</string>
    <string name="home_empty_state">Belum ada entri jurnal.\nTekan \'+\' untuk memulai.</string>
    <string name="home_error_state">Gagal memuat data. %1$s</string>

    <!-- Journal Editor -->
    <string name="editor_new_entry_title">Entri Baru</string>
    <string name="editor_title_label">Judul</string>
    <string name="editor_content_label">Apa yang kamu rasakan?</string>
    <string name="editor_save_button_desc">Simpan</string>
    <string name="editor_save_button">Simpan</string>

    <!-- Profile Screen -->
    <string name="profile_title">Profil</string>
    <string name="profile_logout_button">Logout</string>

</resources>
EOF


# --- 5. Memperbarui Layar Presentation untuk Menggunakan String Resource ---
echo "Memperbarui layar untuk menggunakan string resources..."

# --- presentation/auth/login/LoginScreen.kt ---
cat << 'EOF' > "$BASE_PATH/presentation/auth/login/LoginScreen.kt"
package com.psy.dear.presentation.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.dear.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess()
                is LoginEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email_label)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.password_label)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = { viewModel.login(email, password) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.login_button))
            }
            TextButton(onClick = onNavigateToRegister) {
                Text(stringResource(R.string.login_to_register_prompt))
            }
        }
    }
}
EOF

# (Anda bisa menerapkan pola yang sama untuk layar lain seperti Register, Home, Profile, dll.)

echo "----------------------------------------------------"
echo "Implementasi Stabilitas & Kualitas Kode selesai!"
echo "CATATAN PENTING: Jangan lupa untuk menambahkan dependensi pengujian ke file build.gradle.kts (app) Anda."
echo "----------------------------------------------------"
