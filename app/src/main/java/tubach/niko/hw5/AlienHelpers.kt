package tubach.niko.hw5

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

const val BASE_URL = "http://javadude.com/"

class UfoPosition {
    val ship: Int = 0
    val lat: Double = 0.0
    val lon: Double = 0.0
}

class AlienAlert {
    val ufoPositions: List<UfoPosition> = emptyList()
}

class AlienAlerter(
    private val coroutineScope: CoroutineScope
) {
        inner class ListFlowManager<T>(
        private val fetcher: suspend () -> Response<List<T>>
    ) {
        private val _alerts: Flow<AlienAlert> = MutableStateFlow(AlienAlert())
        init {
            fetch()
        }
        val alerts: Flow<AlienAlert>
            get() = _alerts

        fun fetch() =
            coroutineScope.launch(Dispatchers.IO) {
                _alerts. = fetcher().takeIf { it.isSuccessful }?.body() ?: emptyList()
            }
    }

    private val alienApiService = AlienApiService.create()
    private var count = 1

    suspend fun startReporting() {
        coroutineScope.launch {
//            while (true) {
//
//                val url = URL("http://javadude.com/aliens/$count.json")
//                val response = url.readText()
//                println("Received response: $response")
//                count++
//                delay(1000)
//            }
        }
    }

    suspend fun getAliens(id: String): List<UfoPosition> = withContext(Dispatchers.IO) {
        alienApiService.getAliens(id)
    }

    interface AlienApiService {
        @GET("aliens/{id}")
        suspend fun getAliens(@Path("id") id: String): Response<List<UfoPosition>>

        companion object {
            fun create(): AlienApiService =
                Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(AlienApiService::class.java)
        }
    }
}