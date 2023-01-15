package com.example.runningtracker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.runningtracker.models.Run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Run.class}, version = 1, exportSchema = false)
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
                                    RunDatabase.class, "run_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return instance;
    }

    private static final RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // populate the database when first created
            databaseWriteExecutor.execute(() -> {
                RunDao runDao = instance.runDao();
                runDao.insert(new Run(700, 2, (float) 5.83,
                        "Addicted to running dopamine",
                        new ArrayList<>(Arrays.asList("good weather",
                                "healthy lifestyle"))));
                runDao.insert(new Run(1000, 3, (float) 5.55,
                        "Fresh air outside",
                        new ArrayList<>(Arrays.asList("good weather",
                                "workout"))));
            });
        }
    };
}
