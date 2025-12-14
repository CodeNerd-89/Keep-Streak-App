package com.tahmid.keepstreak.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsageHistory usageHistory);

    @Query("SELECT * FROM usage_history WHERE packageName = :packageName AND date >= :startDate ORDER BY date ASC")
    List<UsageHistory> getUsageHistoryForApp(String packageName, long startDate);
}
