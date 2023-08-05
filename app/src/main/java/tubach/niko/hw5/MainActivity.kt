package tubach.niko.hw5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tubach.niko.hw5.ui.theme.HW5Theme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AliensViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HW5Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    viewModel.startAlienReporting()
                    AlienUi(viewModel)
                }
            }
        }
    }
}

@Composable
fun AlienUi(
    viewModel: AliensViewModel
) {


    // Use the alienAlertState to display UFOs and lines on the map
}