package de.osca.android.sgart.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.osca.android.essentials.data.client.OSCAHttpClient
import de.osca.android.sgart.data.ArtApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ArtModule {
    @Singleton
    @Provides
    fun sgartApiService(oscaHttpClient: OSCAHttpClient): ArtApiService =
        oscaHttpClient.create(ArtApiService::class.java)
}