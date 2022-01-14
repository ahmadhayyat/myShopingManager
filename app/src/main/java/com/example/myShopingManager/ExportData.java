package com.example.myShopingManager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ExportData {
    Context context;

    public ExportData(Context context) {
        this.context = context;
    }

    public JSONArray getResults(List<Items> itemsList) {
        JSONArray resultSet = new JSONArray();
        JSONObject rowObject;
        for (Items item : itemsList) {
            try {
                rowObject = new JSONObject();
                rowObject.put(Constants.COL_ITEM_NAME, item.name);
                resultSet.put(rowObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultSet;
    }
}
