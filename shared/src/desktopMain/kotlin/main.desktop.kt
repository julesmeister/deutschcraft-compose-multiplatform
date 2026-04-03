import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import data.db.DatabaseDriverFactory

actual fun getPlatformName(): String = "Desktop"

@Composable fun MainView() {
    val driverFactory = DatabaseDriverFactory()
    App(driverFactory = driverFactory)
}

@Preview
@Composable
fun AppPreview() {
    val driverFactory = DatabaseDriverFactory()
    App(driverFactory = driverFactory)
}