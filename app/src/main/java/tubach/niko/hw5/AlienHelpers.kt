package tubach.niko.hw5

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.HttpURLConnection

const val BASE_URL = "http://javadude.com/"

data class UfoPosition(
    val ship: Int = 0,
    val lat: Double = 0.0,
    val lon: Double = 0.0
) {
}

class AlienAlert(val ufoPositions: List<UfoPosition> = emptyList()) {
}

class AlienAlerter(
    private val coroutineScope: CoroutineScope
) {
    private val _alerts: MutableStateFlow<AlienAlert> = MutableStateFlow(AlienAlert())
    val alerts: Flow<AlienAlert>
        get() = _alerts
    private val alienApiService = AlienApiService.create()
    private var count = 1

    fun startReporting() {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                val response = alienApiService.getAliens(count)
                if (response.isSuccessful) {
                    // Capture our alien message in the Alien Alert emit
                    val ufoPositions = response.body() ?: emptyList()
                    _alerts.emit(AlienAlert(ufoPositions))
                    println("Received response: $ufoPositions")
                } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND){
                    // Break the loop when we see a 404 endpoint
                    break
                } else {
                    // Endpoint has no valid response, should not get here
                    println("Endpoint response ${response.code()} is not valid.")
                }
                count++
                delay(1000)
            }
        }
    }
}

interface AlienApiService {
    @GET("aliens/{id}.json")
    suspend fun getAliens(@Path("id") id: Int): Response<List<UfoPosition>>

    companion object {
        fun create(): AlienApiService =
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(AlienApiService::class.java)
    }
}