import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.db.DatabaseDriverFactory
import service.OllamaProcessManager
import java.awt.GraphicsEnvironment
import java.lang.Runtime

fun main() {
    // Register shutdown hook to stop Ollama when app closes
    Runtime.getRuntime().addShutdownHook(Thread {
        OllamaProcessManager.shutdown()
    })
    
    // Create driver factory for desktop
    val driverFactory = DatabaseDriverFactory()
    
    // Get screen size for full screen
    val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
    
    application {
        val windowState = rememberWindowState(
            size = DpSize(screenSize.width.dp, screenSize.height.dp)
        )
        
        Window(
            onCloseRequest = {
                OllamaProcessManager.shutdown()
                exitApplication()
            },
            title = "DeutschCraft Desktop",
            state = windowState,
            undecorated = true
        ) {
            App(driverFactory = driverFactory)
        }
    }
}