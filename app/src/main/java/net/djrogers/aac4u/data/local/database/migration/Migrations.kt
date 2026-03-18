package net.djrogers.aac4u.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations.
 *
 * IMPORTANT: Never delete a migration once published.
 * Users upgrading from any previous version must be able to migrate forward.
 *
 * Add new migrations here as the schema evolves.
 * Example:
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE buttons ADD COLUMN newField TEXT DEFAULT ''")
 *     }
 * }
 */
object Migrations {
    // No migrations yet — version 1 is the initial schema.
    // Add migrations here as the database evolves across app versions.

    val ALL: Array<Migration> = arrayOf(
        // MIGRATION_1_2,
        // MIGRATION_2_3,
    )
}
