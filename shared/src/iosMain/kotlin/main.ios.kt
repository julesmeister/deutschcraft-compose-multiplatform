import androidx.compose.ui.window.ComposeUIViewController
import data.db.DatabaseDriverFactory

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DatabaseDriverFactory()
    App(driverFactory = driverFactory)
}