package com.example.notes.di

import android.content.Context
import androidx.room.Room
import com.example.notes.data.local.AppDatabase
import com.example.notes.data.local.NotesDao
import com.example.notes.data.remote.NotesApi
import com.example.notes.data.repo.NotesRepositoryImpl
import com.example.notes.domain.repo.NotesRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "notes.db").build()

    @Provides
    fun provideDao(db: AppDatabase): NotesDao = db.notesDao()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // Localhost for Android emulator
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): NotesApi =
        retrofit.create(NotesApi::class.java)

    @Provides
    @Singleton
    fun provideRepo(dao: NotesDao, api: NotesApi): NotesRepository =
        NotesRepositoryImpl(dao, api)
}