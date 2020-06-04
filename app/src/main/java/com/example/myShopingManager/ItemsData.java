package com.example.myShopingManager;

public class ItemsData {

    String itemName,itemsDate;
    int itemID;
    Integer itemPrice;
    public ItemsData() {
    }
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemsDate() {
        return itemsDate;
    }

    public void setItemsDate(String itemsDate) {
        this.itemsDate = itemsDate;
    }
}
