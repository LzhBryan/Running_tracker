package com.example.runningtracker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.runningtracker.models.Run;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Run.class}, version = 2, exportSchema = false)
@TypeConverters({Run.TagsTypeConverter.class})
public abstract class RunDatabase extends RoomDatabase {

    public abstract RunDao runDao();

    private static volatile RunDatabase instance;
    private static final int threadCount = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(threadCount);

    public static RunDatabase getDatabase(final Context context) {
        if (instance == null) {
            // call synchronized to avoid race condition
            synchronized (RunDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    RunDatabase.class, "cat_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return instance;
    }

    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                RunDao runDao = instance.runDao();
            });
        }
    };
}
