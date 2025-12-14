package com.tahmid.keepstreak.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrackedAppDao {

    @Query("SELECT * FROM tracked_apps ORDER BY appName ASC")
    List<TrackedApp> getAll();

    @Query("SELECT * FROM tracked_apps WHERE packageName = :packageName")
    TrackedApp findByPackageName(String packageName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TrackedApp app);

    @Update
    void update(TrackedApp app);

    @Delete
    void delete(TrackedApp app);

    @Query("DELETE FROM tracked_apps")
    void deleteAll();
}
