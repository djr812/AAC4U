package net.djrogers.aac4u.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN avatar TEXT NOT NULL DEFAULT '😊'")
            db.execSQL("ALTER TABLE profiles ADD COLUMN ageRange TEXT NOT NULL DEFAULT 'ADULT'")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN largeTextEnabled INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE profiles ADD COLUMN reducedAnimationsEnabled INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
