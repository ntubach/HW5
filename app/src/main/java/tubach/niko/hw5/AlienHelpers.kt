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

// A great URL name, much const, such dude
const val BASE_URL = "http://javadude.com/"

data class UfoPosition(
    val ship: Int = 0,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class AlienAlert(val ufoPositions: List<UfoPosition> = emptyList())

class AlienAlerter(
    private val coroutineScope: CoroutineScope
) {
    private val _alerts: MutableStateFlow<AlienAlert> = MutableStateFlow(AlienAlert())
    val alerts: Flow<AlienAlert>
        get() = _alerts
    private val alienApiService = AlienApiService.create()

    // Just use a simple count for tracking url endpoint due to known constraint
    private var count = 1

    fun startReporting() {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                // Call our Retrofit2 Service to get the next Alien encounter for the Alerter
                val response = alienApiService.getAliens(count)
                if (response.isSuccessful) {
                    // Capture our alien message in the Alien Alert emit
                    val ufoPositions = response.body() ?: emptyList()
                    _alerts.value = AlienAlert(ufoPositions)
                } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
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

// Retrofit2 inspiration courtesy of Scott Stanchfield
// https://gitlab.com/605-686/android-summer-2023/-/blob/main/movies-ui2-rest/repository/src/main/java/com/androidbyexample/movies/repository/MovieApiService.kt
interface AlienApiService {
    // GET relies on Retrofit2 conversion to populate the List<UfoPosition>> val automatically upon receive
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