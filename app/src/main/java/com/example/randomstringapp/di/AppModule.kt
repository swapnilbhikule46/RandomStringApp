package com.example.randomstringapp.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.example.randomstringapp.data.db.AppDatabase
import com.example.randomstringapp.data.db.RandomStringDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies across the app
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "random_strings_db"
        ).build()
    }

    @Provides
    fun provideRandomStringDao(appDatabase: AppDatabase): RandomStringDao {
        return appDatabase.randomStringDao()
    }
}