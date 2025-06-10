package com.example.paratiapp.di

// --- Imports Necesarios ---
import com.example.paratiapp.data.RegaloRepository
import com.example.paratiapp.data.RegaloRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds // Usamos Binds para simplificar cuando la implementación tiene @Inject constructor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // Sigue siendo un módulo
@InstallIn(SingletonComponent::class) // Instalar en el componente Singleton
object AppModule { // Lo convertimos en un 'object' para métodos @Provides

    // Le decimos a Hilt cómo proveer instancias de Firebase
    @Provides
    @Singleton
    fun provideFirestoreInstance() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorageInstance() = FirebaseStorage.getInstance()
    // No necesitamos proveer FirebaseFirestore explícitamente aquí
    // porque RegaloRepositoryImpl ya lo obtiene con Firebase.firestore
    // Si quisiéramos inyectarlo en RegaloRepositoryImpl, sí lo añadiríamos:
    // @Provides
    // @Singleton
    // fun provideFirestoreInstance(): FirebaseFirestore {
    //     return Firebase.firestore
    // }
}