package com.example.myShopingManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {
    public static final String APP_DB_NAME = "shoppingManagerDB";
    public static final String ITEM_ID = "id";
    public static final String ITEMS_LIST = "itemList";
    public static final String TABLE_ITEMS = "items";
    public static final String COL_ITEM_NAME = "name";
    public static final String ITEM_SHEETID = "sheetId";
    public static final String COL_ITEM_DATE = "date";
    public static final String COL_ITEM_PRICE = "price";
    public static final String TOTAL = "total";
    public static final String SHEET_NAME = "name";
    public static final String COL_SHEET_DATE = "date";
    public static final String TABLE_SHEETS = "sheet";
    public static final String EXTRA_SHEET_ID = "sheetId";
    public static final String EXTRA_SHEET_NAME = "sheetName";

    private static Constants instance;

    public static synchronized Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }

    public String getDate() {
        return new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault()).format(new Date());
    }
}
