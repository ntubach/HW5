package tubach.niko.hw5

// Inspiration and small code snippets in following file courtesy of Scott Stanchfield
// https://gitlab.com/android-development-2022-refresh/google-map/-/blob/main/app/src/main/java/com/javadude/carfinder/MainActivity.kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tubach.niko.hw5.ui.theme.HW5Theme

private val hw5Start = LatLng(38.9073, -77.0365)
private val defaultCameraPosition = CameraPosition.fromLatLngZoom(hw5Start, 11f)

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AliensViewModel>()

    // Small callback to enable reporting from Ui display after map is ready
    private fun startAlienReporting() {
        viewModel.startAlienReporting()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HW5Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = defaultCameraPosition
                    }
                    val ufosAndLinesState by viewModel.ufosAndLines.collectAsState(initial = emptyMap())

                    AlienUi(
                        cameraPositionState = cameraPositionState,
                        ufosAndLinesState = ufosAndLinesState,
                        this::startAlienReporting,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlienUi(
    cameraPositionState: CameraPositionState,
    ufosAndLinesState: Map<Int, UfoAndLines>,
    onStartAlienReporting: () -> Unit,
    modifier: Modifier
) {
    with(LocalDensity.current) {
        val boundsPadding = 50.dp.toPx()
        var mapLoaded by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var ufoIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
        val scope = rememberCoroutineScope()
        var initialBoundsSet by remember { mutableStateOf(false) }

        // This launched effect is a safety for making sure the map starts in a sane location
        LaunchedEffect(key1 = ufosAndLinesState) {
            ufosAndLinesState.let { ufos ->
                if (ufos.isEmpty() && !initialBoundsSet) {
                    initialBoundsSet = true
                    run {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                hw5Start,
                                16f
                            ), 400
                        )
                    }
                }
            }
        }

        Scaffold(
            topBar = {},
            content = { paddingValues ->
                Box(
                    modifier = modifier.padding(paddingValues),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GoogleMap(
                            cameraPositionState = cameraPositionState,
                            onMapLoaded = {
                                mapLoaded = true
                                scope.launch(Dispatchers.IO) {
                                    // Load our nice bitmap image of the ufo to the composable GMap
                                    ufoIcon =
                                        context.loadBitmapDescriptor(
                                            R.drawable.ic_ufo_flying
                                        )
                                }
                                onStartAlienReporting()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            ufosAndLinesState.let { ufos ->
                                // Use the Builder pattern for bounds to save looping
                                val bounds = LatLngBounds.Builder()
                                ufos.forEach { (key, value) ->
                                    bounds.include(value.lastPosition.position)
                                    // Only put down markers for active UFOs
                                    if (value.isActive) {
                                        MarkerInfoWindowContent(
                                            state = value.lastPosition,
                                            icon = ufoIcon,
                                            anchor = Offset(0.5f, 0.5f),
                                            title = "${stringResource(R.string.ufo)} $key",
                                        )
                                    }

                                    // Draw Polylines for each UFO's line list
                                    value.lines.forEach { line ->
                                        bounds.include(line.startLatLng)
                                        bounds.include(line.endLatLng)
                                        Polyline(
                                            points = listOf(line.startLatLng, line.endLatLng),
                                            color = MaterialTheme.colorScheme.primary,
                                            width = 8f
                                        )
                                    }
                                }
                                // Assuming we saw a UFO, update the bounds of the map using the Builder
                                if (ufos.isNotEmpty()) {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngBounds(
                                                bounds.build(),
                                                boundsPadding.toInt(),
                                            ), 900
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!mapLoaded) {
                        AnimatedVisibility(
                            visible = true,
                            modifier = Modifier.fillMaxSize(),
                            enter = EnterTransition.None,
                            exit = fadeOut()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .wrapContentSize()
                            )
                        }
                    }
                }
            }
        )
    }
}