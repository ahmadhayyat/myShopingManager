package com.example.myShopingManager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SheetDAO {
    @Query("SELECT * FROM " + Constants.TABLE_SHEETS)
    LiveData<List<Sheet>> getAll();

    @Insert
    void Insert(Sheet sheet);

    @Update
    void Update(Sheet sheet);

    @Delete
    void Delete(Sheet sheet);
}
