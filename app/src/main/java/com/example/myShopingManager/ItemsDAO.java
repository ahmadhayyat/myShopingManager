package com.example.myShopingManager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemsDAO {
    @Query("SELECT * FROM " + Constants.TABLE_ITEMS + " WHERE sheetId =:sheetId ")
    LiveData<List<Items>> getAll(int sheetId);

    @Query("DELETE FROM " + Constants.TABLE_ITEMS + " WHERE sheetId=:sheetId ")
    void deleteAllItems(int sheetId);

    @Insert
    void Insert(Items items);

    @Update
    void Update(Items items);

    @Delete
    void Delete(Items items);
}
