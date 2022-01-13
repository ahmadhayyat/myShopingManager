package com.example.myShopingManager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = Constants.TABLE_SHEETS)
public class Sheet implements Serializable {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "name")
    String sheetName;

    @ColumnInfo(name = "date")
    String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}
