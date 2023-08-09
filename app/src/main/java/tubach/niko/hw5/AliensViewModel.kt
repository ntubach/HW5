package tubach.niko.hw5

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AliensViewModel : ViewModel() {
    private val alienAlerter = AlienAlerter(viewModelScope)
    private var _ufosAndLinesStore = mutableMapOf<Int, UfoAndLines>()

    private val _ufosAndLines: Flow<Map<Int, UfoAndLines>> = alienAlerter.alerts.map { alienAlert ->
        // Copy our permanent map of UFOs
        val ufosMap = _ufosAndLinesStore

        // Set all our activity to false so new emit can set only active UFOs to true
        _ufosAndLinesStore.forEach { (key, value) ->
            _ufosAndLinesStore[key] = value.copy(isActive = false)
        }

        // Iterate over the emit
        for (ufoPosition in alienAlert.ufoPositions) {
            // Local temp for current Position
            val updatePos = LatLng(ufoPosition.lat, ufoPosition.lon)
            // Either grab the value from the map or create a new one with current Position
            val tmpUfo = ufosMap[ufoPosition.ship] ?: UfoAndLines(
                lastPosition = MarkerState(updatePos),
            )
            // Set UFO to active
            tmpUfo.isActive = true
            // Create a line based on last and current position
            val line = Line(tmpUfo.lastPosition.position, updatePos)
            // Only add new lines if the UFO moved (new UFOs will hit this)
            if (line.startLatLng != line.endLatLng)
                tmpUfo.lines.add(line)
            // Replace last known with current position
            tmpUfo.lastPosition.position = updatePos
            // Place temporary object back into the map
            ufosMap[ufoPosition.ship] = tmpUfo
        }
        // Update our holder map with new updates
        _ufosAndLinesStore = ufosMap

        // Our val is the object
        ufosMap.toMap()
    }
    val ufosAndLines: Flow<Map<Int, UfoAndLines>>
        get() = _ufosAndLines

    fun startAlienReporting() {
        alienAlerter.startReporting()
    }
}

// Helpful data classes for holding an individual UFOs state (activity and lines)
data class UfoAndLines(
    var isActive: Boolean = false,
    var lastPosition: MarkerState = MarkerState(LatLng(0.0, 0.0)),
    var lines: MutableList<Line> = ArrayList()
)

data class Line(
    val startLatLng: LatLng,
    val endLatLng: LatLng
)