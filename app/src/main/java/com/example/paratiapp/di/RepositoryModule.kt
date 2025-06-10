package com.example.paratiapp.di

import com.example.paratiapp.data.RegaloRepository
import com.example.paratiapp.data.RegaloRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRegaloRepository(
        impl: RegaloRepositoryImpl
    ): RegaloRepository
}