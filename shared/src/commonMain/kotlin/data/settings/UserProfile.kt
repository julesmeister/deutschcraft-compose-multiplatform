package data.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val username: String,
    val targetLanguage: String = "German",
    val nativeLanguage: String = "English",
    val currentLevel: String = "A1",
    val learningGoals: List<String> = emptyList()
)
