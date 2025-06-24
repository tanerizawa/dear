package com.psy.dear.di

import com.psy.dear.data.datastore.UserPreferencesRepository
import com.psy.dear.data.network.AuthInterceptor
import com.psy.dear.data.network.api.*
import com.psy.dear.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Base URL for the FastAPI backend is provided via BuildConfig.


    @Provides
    @Singleton
    fun provideAuthInterceptor(prefs: UserPreferencesRepository): AuthInterceptor {
        return AuthInterceptor(prefs)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)     // 👈 paling penting
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideJournalApiService(retrofit: Retrofit): JournalApiService = retrofit.create(JournalApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService = retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService = retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideContentApiService(retrofit: Retrofit): ContentApiService = retrofit.create(ContentApiService::class.java)
}
