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

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2
    )
}
