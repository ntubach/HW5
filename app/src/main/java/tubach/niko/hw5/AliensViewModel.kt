package tubach.niko.hw5

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class AliensViewModel : ViewModel() {
    private val alienAlerter = AlienAlerter(viewModelScope)

    val alienAlerts: Flow<AlienAlert> = alienAlerter.alerts

//    val ufoAndLinesState: Flow<UfoAndLines> = alienAlerts.map { alienAlert ->
//
//    }.flowOn(Dispatchers.Default)

    fun startAlienReporting() {
        alienAlerter.startReporting()
    }
}

class UfoAndLines{
    val id: Int = -1
    val isActive: Boolean = false
    val initialPosition: LatLng = LatLng(0.0,0.0)
    val x: List<Line> = emptyList()
}
data class Line (
    val startLatLng: LatLng,
    val endLatLng: LatLng
)