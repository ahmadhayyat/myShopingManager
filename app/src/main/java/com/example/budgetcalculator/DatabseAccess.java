package com.example.budgetcalculator;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabseAccess {

    private static DatabseAccess instance;
    Cursor c = null;
    private SQLiteOpenHelper OpenHelper;
    private SQLiteDatabase db;
    TinyDB tinyDB;
    //constractor


    public DatabseAccess(Context context) {
        this.OpenHelper = new DatabaseOpenHelper(context);
        tinyDB = new TinyDB(context);
        tinyDB.putInt(Constants.TOTAL,0);
    }

    public static DatabseAccess getInstance(Context context) {

        if (instance == null) {
            instance = new DatabseAccess(context);
        }
        return instance;
    }


    public void open() {

        this.db = OpenHelper.getWritableDatabase();
    }

    public void close() {

        if (db != null) {

            this.db.close();
        }
    }


    public boolean addItems(String itemName,int sheetId,String itemsDate) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ITEM_NAME, itemName);
        contentValues.put(Constants.ITEM_SHEETID, sheetId);
        contentValues.put(Constants.ITEM_DATE, itemsDate);

        long result = db.insert(Constants.TABLE_ITEMS, null, contentValues);
        if (result == -1) {
            return false;

        } else return true;
    }


    List<ItemsData> getItems(int sheetId) {

        List<ItemsData> itemsDataList = new ArrayList<>();
        String query = " SELECT * FROM " + Constants.TABLE_ITEMS +" Where sheet ='"+sheetId+"'";

        c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                ItemsData itemsData = new ItemsData();
                itemsData.setItemID(c.getInt(0));
                itemsData.setItemName(c.getString(1));
                itemsData.setItemPrice(c.getInt(2));
                itemsData.setItemsDate(c.getString(4));

                itemsDataList.add(itemsData);
            } while (c.moveToNext());
        }
        return itemsDataList;
    }
    Integer  calculate(int sheetId) {
        tinyDB.putInt(Constants.TOTAL,0);
        Integer calculated = 0 ;
        String query = " SELECT price FROM " + Constants.TABLE_ITEMS +" where sheet ='"+sheetId+"'";

        c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                Integer total;
                total = tinyDB.getInt(Constants.TOTAL);
                total = total + c.getInt(0);
                tinyDB.putInt(Constants.TOTAL,total);
                calculated= total;
            } while (c.moveToNext());
        }
        return calculated;
    }


    public boolean updateItemsPrice(int id, int price) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ITEM_PRICE, price);

        long result = db.update(Constants.TABLE_ITEMS, contentValues, "id=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            return false;

        } else return true;
    }
    public boolean updateItemsName(int id, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ITEM_NAME, name);

        long result = db.update(Constants.TABLE_ITEMS, contentValues, "id=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            return false;

        } else return true;
    }

    public boolean deleteItem(int id) {

        long result = db.delete(Constants.TABLE_ITEMS, "id=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            return false;

        } else return true;
    }
    boolean deleteAllItem(int sheetId) {
        long result = db.delete(Constants.TABLE_ITEMS,"sheet=?",new String[]{String.valueOf(sheetId)});
        return result != -1;
    }

    public boolean addSheet(String sheetName, String sheetDate) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.SHEET_NAME, sheetName);
        contentValues.put(Constants.SHEET_DATE, sheetDate);

        long result = db.insert(Constants.TABLE_SHEETS, null, contentValues);
        if (result == -1) {
            return false;

        } else return true;
    }
    public boolean updateSheetName(int id, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.SHEET_NAME, name);

        long result = db.update(Constants.TABLE_SHEETS, contentValues, "id=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            return false;

        } else return true;
    }
    List<SheetsData> getSheets() {

        List<SheetsData> sheetsDataList = new ArrayList<>();
        String query = " SELECT * FROM " + Constants.TABLE_SHEETS;

        c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                SheetsData sheetsData = new SheetsData();
                sheetsData.setSheetId(c.getInt(0));
                sheetsData.setSheetName(c.getString(1));
                sheetsData.setSheetDate(c.getString(2));
                sheetsDataList.add(sheetsData);
            } while (c.moveToNext());
        }
        return sheetsDataList;
    }
    public boolean deleteSheet(int sheetId) {

        long result = db.delete(Constants.TABLE_SHEETS, "id=?", new String[]{String.valueOf(sheetId)});
        if (result == -1) {
            return false;

        } else return true;
    }
    public boolean deleteItemWithSheet(int id) {

        long result = db.delete(Constants.TABLE_ITEMS, "sheet=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            return false;

        } else return true;
    }

}
