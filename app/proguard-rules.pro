# AAC4U ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# SQLCipher (new sqlcipher-android library)
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Keep our domain models (used in backup/restore serialization)
-keep class net.djrogers.aac4u.domain.model.** { *; }
-keep class net.djrogers.aac4u.data.local.database.entity.** { *; }
