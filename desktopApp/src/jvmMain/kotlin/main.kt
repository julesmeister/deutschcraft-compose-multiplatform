import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime

fun main() {
    // Set up global exception handler to catch Compose crashes
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val logFile = File("crash_log.txt")
        logFile.writeText("""
            |CRASH at ${LocalDateTime.now()}
            |Thread: ${thread.name}
            |Exception: ${throwable.javaClass.name}: ${throwable.message}
            |
            |FULL STACK TRACE:
            |${throwable.stackTraceToString()}
            |
            |CAUSE CHAIN:
            |${generateSequence(throwable) { it.cause }.joinToString("\n\n") { "${it.javaClass.name}: ${it.message}" }}
        """.trimMargin())
        throwable.printStackTrace()
    }
    
    application {
        Window(onCloseRequest = ::exitApplication) {
            MainView()
        }
    }
}