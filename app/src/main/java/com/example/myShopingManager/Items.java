package com.example.myShopingManager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = Constants.TABLE_ITEMS)
public class Items {
    @PrimaryKey(autoGenerate = true)
    int Id;
    @ColumnInfo(name = Constants.ITEM_SHEETID)
    int sheetId;
    @ColumnInfo(name = Constants.COL_ITEM_NAME)
    String name;
    @ColumnInfo(name = Constants.COL_ITEM_DATE)
    String date;
    @ColumnInfo(name = Constants.COL_ITEM_PRICE)
    Integer price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public int getSheetId() {
        return sheetId;
    }

    public void setSheetId(int sheetId) {
        this.sheetId = sheetId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
