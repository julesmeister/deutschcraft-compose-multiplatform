import data.db.DatabaseDriverFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() {
    val context = LocalContext.current
    val driverFactory = remember { DatabaseDriverFactory(context) }
    App(driverFactory = driverFactory)
}
