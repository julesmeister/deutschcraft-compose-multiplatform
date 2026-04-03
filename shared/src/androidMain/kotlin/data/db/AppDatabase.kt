package data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.db.dao.*
import data.db.entity.*

@Database(
    entities = [
        StudyEntryEntity::class,
        GrammarMistakeEntity::class,
        StudyTopicEntity::class,
        StrengthRecordEntity::class,
        VocabularyItemEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyEntryDao(): StudyEntryDao
    abstract fun grammarMistakeDao(): GrammarMistakeDao
    abstract fun studyTopicDao(): StudyTopicDao
    abstract fun strengthRecordDao(): StrengthRecordDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deutschcraft_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
