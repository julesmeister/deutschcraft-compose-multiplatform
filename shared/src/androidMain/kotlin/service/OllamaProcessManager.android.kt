package service

actual object OllamaProcessManager {
    actual fun isOllamaRunning(): Boolean {
        // Android cannot run Ollama directly
        return false
    }

    actual fun startOllamaServer(): Boolean {
        // Android cannot start Ollama server
        return false
    }

    actual fun shutdown() {
        // Android cannot shutdown Ollama
    }
}
