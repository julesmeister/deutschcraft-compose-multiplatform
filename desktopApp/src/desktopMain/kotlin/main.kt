import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Placement
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import data.db.DatabaseDriverFactory
import service.OllamaProcessManager
import java.lang.Runtime

fun main() {
    // Register shutdown hook to stop Ollama when app closes
    Runtime.getRuntime().addShutdownHook(Thread {
        OllamaProcessManager.shutdown()
    })
    
    // Create driver factory for desktop
    val driverFactory = DatabaseDriverFactory()
    
    application {
        Window(
            onCloseRequest = {
                OllamaProcessManager.shutdown()
                exitApplication()
            },
            title = "DeutschCraft Desktop",
            state = WindowState(
                placement = WindowPlacement.Maximized,
                size = DpSize(1400.dp, 900.dp)
            )
        ) {
            App(driverFactory = driverFactory)
        }
    }
}