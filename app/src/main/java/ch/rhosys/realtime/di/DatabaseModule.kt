package ch.rhosys.realtime.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ch.rhosys.realtime.data.local.db.ALL_MIGRATIONS
import ch.rhosys.realtime.data.local.db.AppDatabase
import ch.rhosys.realtime.data.local.db.dao.PlaceDao
import ch.rhosys.realtime.data.local.db.dao.RecurringRouteDao
import ch.rhosys.realtime.data.local.db.dao.SavedRouteDao
import ch.rhosys.realtime.data.local.db.dao.TripHistoryDao
import ch.rhosys.realtime.data.local.repository.RoomPlaceRepository
import ch.rhosys.realtime.data.local.repository.RoomRouteRepository
import ch.rhosys.realtime.domain.PlaceRepository
import ch.rhosys.realtime.domain.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "realtime.db")
            .addMigrations(*ALL_MIGRATIONS)
            // Only wipe on a downgrade (e.g. switching branches locally) — a missing
            // upgrade migration should fail loudly, never silently delete user data.
            .fallbackToDestructiveMigrationOnDowngrade()
            // Android Auto Backup snapshots this .db file as a plain file copy, with no
            // hook to checkpoint first. Room's default WAL mode can leave recent writes
            // sitting in a separate -wal file that a file-copy backup would miss entirely,
            // so force the classic single-file journal instead.
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

    @Provides fun providePlaceDao(db: AppDatabase): PlaceDao = db.placeDao()
    @Provides fun provideSavedRouteDao(db: AppDatabase): SavedRouteDao = db.savedRouteDao()
    @Provides fun provideRecurringRouteDao(db: AppDatabase): RecurringRouteDao = db.recurringRouteDao()
    @Provides fun provideTripHistoryDao(db: AppDatabase): TripHistoryDao = db.tripHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {
    @Binds @Singleton abstract fun bindPlaceRepository(impl: RoomPlaceRepository): PlaceRepository
    @Binds @Singleton abstract fun bindRouteRepository(impl: RoomRouteRepository): RouteRepository
}
