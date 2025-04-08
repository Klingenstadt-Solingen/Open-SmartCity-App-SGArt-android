package de.osca.android.sgart.data

import de.osca.android.sgart.entity.KunstInWald
import retrofit2.Response
import retrofit2.http.GET

interface ArtApiService {

    @GET("classes/KunstInWald")
    suspend fun getKunstInWald(): Response<List<KunstInWald>>
}