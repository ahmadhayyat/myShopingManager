package com.example.myShopingManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public static final String COL_ITEM_STATUS = "status";
    public static final String TOTAL = "total";
    public static final String SHEET_NAME = "name";
    public static final String COL_SHEET_DATE = "date";
    public static final String TABLE_SHEETS = "sheet";
    public static final String EXTRA_SHEET_ID = "sheetId";
    public static final String EXTRA_SHEET_NAME = "sheetName";
    public static final int STATUS_NEUTRAL = 0;
    public static final int STATUS_PURCHASED = 1;
    public static final int STATUS_NOT_AVAILABLE = 2;
    public static final int STATUS_NO_NEED= 3;
    private static Constants instance;
    private ArrayList<Integer> purchasedItems;

    public static synchronized Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }

    public String getDate() {
        return new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault()).format(new Date());
    }

    public List<Integer> getPurchasedItems() {
        if (this.purchasedItems != null)
            return purchasedItems;
        else return new ArrayList<>();
    }

    public void setPurchasedItems(ArrayList<Integer> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    public void setPurchasedItems(int itemId) {
        if (this.purchasedItems == null)
            purchasedItems = new ArrayList<>();
        this.purchasedItems.add(itemId);
    }
}
