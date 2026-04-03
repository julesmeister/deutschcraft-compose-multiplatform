# DeutschCraft

A Compose Multiplatform desktop application for AI-powered German text editing and enhancement.

## Features

- **AI Suggestions**: Get intelligent text improvement suggestions using Ollama LLMs
- **Rich Text Editor**: Built-in editor with text selection and analysis
- **Smart Formatting**: Multiple suggestion types (improve, grammar, rephrase, expand)
- **Model Selection**: Choose from available Ollama models

## Project Structure

- **shared/**: Common Kotlin Multiplatform code shared across all targets
  - `commonMain/`: Shared Compose UI components and business logic
  - `ui/`: UI screens (SuggestionsPanel, EditorPanel)
  - `service/`: Network services (Ollama API client)
  - `ui/components/m3/`: Material Design 3 component library
- **desktopApp/**: JVM desktop application target

## Requirements

- JDK 17 or higher
- Gradle 8.x
- Ollama running locally (for AI features)

## Running the Desktop App

```bash
./gradlew :desktopApp:run
```

## Ollama Setup

Ensure Ollama is installed and running locally on port 11434. The app uses models like `llama3.2` by default.

```bash
ollama serve
ollama pull llama3.2
```

## Technology Stack

- **UI Framework**: Jetpack Compose Multiplatform
- **Networking**: Ktor Client
- **Serialization**: Kotlinx Serialization JSON
- **Date/Time**: Kotlinx Datetime
- **LLM Integration**: Ollama API

## License

Apache 2.0