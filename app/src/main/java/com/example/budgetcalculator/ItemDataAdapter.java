package com.example.budgetcalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemDataAdapter extends RecyclerView.Adapter<ItemDataAdapter.ViewHolder> {
    List<ItemsData> itemsData;
    LayoutInflater layoutInflater;
    Context context;
    TinyDB tinyDB;
    DatabseAccess db;
    int sheetId;
    ItemDataAdapter itemDataAdapter;

    public ItemDataAdapter() {
    }

    public ItemDataAdapter(List<ItemsData> itemsData, Context context, int sheetId) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
        this.itemsData = itemsData;
        this.sheetId = sheetId;
        this.layoutInflater = LayoutInflater.from(context);
        this.db = new DatabseAccess(context);
        tinyDB.putInt(Constants.TOTAL, 0);
        itemDataAdapter = this;

    }

    @NonNull
    @Override
    public ItemDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_layout, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ItemDataAdapter.ViewHolder holder, final int position) {
        holder.itemsDate.setText(itemsData.get(position).getItemsDate());
        holder.itemName.setText(itemsData.get(position).getItemName());
        if (itemsData.get(position).getItemPrice() != 0 || itemsData.get(position).getItemPrice() != null) {
            holder.itemPrice.setText(String.valueOf(itemsData.get(position).getItemPrice()));

        } else {
            holder.itemPrice.setText("");
        }
        holder.cb_save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    db.open();
                    db.updateItemsPrice(itemsData.get(position).itemID, Integer.parseInt(holder.itemPrice.getText().toString()));
                    db.close();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            holder.cb_save.setChecked(false);
                        }
                    }, 1000);

                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lightGreen));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    public void refresh() {
        this.notifyDataSetChanged();
        ((MainActivity) context).loadItems();
    }

    public void editName(final int position, final View view) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = layoutInflater;
        View dialogView = inflater.inflate(R.layout.update_item_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final EditText editText = dialogView.findViewById(R.id.et_update_itemName);
        editText.setText(String.valueOf(itemsData.get(position).getItemName()));
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                boolean updated = db.updateItemsName(itemsData.get(position).getItemID(), editText.getText().toString());
                db.close();
                if (updated) {
                    Snackbar snackbar = Snackbar.make(view, "Updated", Snackbar.LENGTH_LONG);
                    snackbar.setTextColor(Color.parseColor("#00ff80"));
                    snackbar.show();
                    refresh();
                }
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();


    }

    public void deleteItem(int position, View view) {
        db.open();
        db.deleteItem(itemsData.get(position).getItemID());
        Snackbar snackbar = Snackbar.make(view, "Deleted", Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.parseColor("#ff0000"));
        snackbar.show();
        itemsData = db.getItems(sheetId);
        db.close();

        refresh();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemsDate;
        EditText itemPrice;
        CheckBox cb_save;
        ImageButton ib_del;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemsDate = itemView.findViewById(R.id.itemsDate);
            cb_save = itemView.findViewById(R.id.cb_save);
            ib_del = itemView.findViewById(R.id.ib_del);
        }
    }
}
