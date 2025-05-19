package com.example.paratiapp.di

// --- Imports Necesarios ---
import com.example.paratiapp.data.RegaloRepository
import com.example.paratiapp.data.RegaloRepositoryImpl
import dagger.Binds // Usamos Binds para simplificar cuando la implementación tiene @Inject constructor
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Instalar en el componente Singleton
abstract class AppModule { // Mejor usar clase abstracta con @Binds

    // Hilt sabrá cómo crear RegaloRepositoryImpl porque tiene @Inject constructor()
    // Usamos @Binds para decirle a Hilt que cuando alguien pida RegaloRepository,
    // debe proporcionar una instancia de RegaloRepositoryImpl.
    @Binds
    @Singleton // Para que la instancia sea única en toda la app
    abstract fun bindRegaloRepository(
        regaloRepositoryImpl: RegaloRepositoryImpl
    ): RegaloRepository

    // No necesitamos proveer FirebaseFirestore explícitamente aquí
    // porque RegaloRepositoryImpl ya lo obtiene con Firebase.firestore
    // Si quisiéramos inyectarlo en RegaloRepositoryImpl, sí lo añadiríamos:
    // @Provides
    // @Singleton
    // fun provideFirestoreInstance(): FirebaseFirestore {
    //     return Firebase.firestore
    // }
}