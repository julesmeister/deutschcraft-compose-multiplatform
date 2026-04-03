package service

import java.io.BufferedReader
import java.io.InputStreamReader

actual object OllamaProcessManager {
    private var ollamaProcess: Process? = null

    actual fun isOllamaRunning(): Boolean {
        return try {
            val process = ProcessBuilder("ollama", "list")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    actual fun startOllamaServer(): Boolean {
        return try {
            // Check if already running by making a quick HTTP check
            val checkProcess = ProcessBuilder(
                "curl", "-s", "http://127.0.0.1:11434"
            ).redirectErrorStream(true).start()
            val checkOutput = BufferedReader(InputStreamReader(checkProcess.inputStream)).readText()
            checkProcess.waitFor()
            
            if (checkOutput.contains("Ollama is running")) {
                return true
            }

            // Start the server
            val processBuilder = if (System.getProperty("os.name").lowercase().contains("windows")) {
                ProcessBuilder("cmd", "/c", "start", "ollama", "serve")
            } else {
                ProcessBuilder("ollama", "serve")
            }
            
            ollamaProcess = processBuilder
                .redirectErrorStream(true)
                .start()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual fun shutdown() {
        ollamaProcess?.let { process ->
            try {
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    // On Windows, we need to kill the ollama process tree
                    Runtime.getRuntime().exec("taskkill /F /IM ollama.exe")
                } else {
                    process.destroy()
                    // Force kill if not terminated after 5 seconds
                    if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        process.destroyForcibly()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ollamaProcess = null
        }
    }
}
