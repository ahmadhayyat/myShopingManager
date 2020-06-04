package com.example.myShopingManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SheetsAdapter extends RecyclerView.Adapter<SheetsAdapter.ViewHolder> {
    List<SheetsData> sheetsDataList;
    LayoutInflater layoutInflater;

    Context context;
    TinyDB tinyDB;
    DatabseAccess db;
    ExportData exportData;

    public SheetsAdapter() {
    }

    public SheetsAdapter(List<SheetsData> sheetsData, Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
        this.sheetsDataList = sheetsData;
        this.layoutInflater = LayoutInflater.from(context);

        this.db = new DatabseAccess(context);
        tinyDB.putInt(Constants.TOTAL, 0);

    }

    @NonNull
    @Override
    public SheetsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.sheets_layout, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final SheetsAdapter.ViewHolder holder, final int position) {

        holder.sheetsDate.setText(sheetsDataList.get(position).getSheetDate());
        holder.sheetsName.setText(sheetsDataList.get(position).getSheetName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoMainActivity = new Intent(context, MainActivity.class);
                gotoMainActivity.putExtra("sheetId", sheetsDataList.get(position).getSheetId());
                gotoMainActivity.putExtra("sheetName", sheetsDataList.get(position).getSheetName());
                gotoMainActivity.putExtra("sheetDate", sheetsDataList.get(position).getSheetDate());
                context.startActivity(gotoMainActivity);
            }
        });

        holder.ib_delsheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.open();
                db.deleteSheet(sheetsDataList.get(position).getSheetId());
                db.deleteItemWithSheet(sheetsDataList.get(position).getSheetId());
                db.close();
                Snackbar snackbar = Snackbar.make(v, "Deleted", Snackbar.LENGTH_LONG);
                snackbar.setTextColor(Color.parseColor("#ff0000"));
                snackbar.show();
                refresh();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sheetsDataList.size();
    }


    public void refresh() {
        /*sheetsDataList = list;*/
        this.notifyDataSetChanged();
        ((SheetsActivity) context).loadSheets();
    }

    public void editSheetName(final int position,final View view){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = layoutInflater;
        final View dialogView = inflater.inflate(R.layout.update_item_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final EditText editText = dialogView.findViewById(R.id.et_update_itemName);
        editText.setText(String.valueOf(sheetsDataList.get(position).getSheetName()));
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
                boolean updated = db.updateSheetName(sheetsDataList.get(position).getSheetId(), editText.getText().toString());
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

    public boolean createFile(int position) {

        boolean isCreated = createNow(position);
        if (isCreated) {
            String fileName = sheetsDataList.get(position).getSheetName();
            share(fileName);
            return true;
        }

        return false;
    }


    private boolean createNow(int position) {
        String fileName = sheetsDataList.get(position).getSheetName();
        exportData = new ExportData(context);
        JSONArray convertedJson = exportData.getResults(sheetsDataList.get(position).sheetId);

        try {


            File checkFile = new File(Environment.getExternalStorageDirectory().toString() + "/budget calculator/");
            if (!checkFile.exists()) {
                checkFile.mkdir();
            }
            FileWriter file = new FileWriter(checkFile.getAbsolutePath() + "/" + fileName + ".json");

            file.write(String.valueOf(convertedJson));
            file.flush();
            file.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void share(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getPath());
        sb.append("/budget calculator/");
        sb.append(fileName);
        String str2 = ".json";
        sb.append(str2);

        Uri parse = Uri.parse(sb.toString());
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("*/text/plain");
        intent.putExtra("android.intent.extra.STREAM", parse);
        intent.putExtra("android.intent.extra.TEXT", parse);
        context.startActivity(Intent.createChooser(intent, "Share File"));

    }

    public int getSheetId(int p) {
        int id = sheetsDataList.get(p).getSheetId();
        return id;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView sheetsName, sheetsDate;
        ImageButton ib_delsheet;
        RelativeLayout RLmain;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sheetsName = itemView.findViewById(R.id.sheetsName);
            ib_delsheet = itemView.findViewById(R.id.ib_delsheet);
            sheetsDate = itemView.findViewById(R.id.sheetsDate);
            RLmain = itemView.findViewById(R.id.RLmain);
            RLmain.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose action");
            menu.add(this.getAdapterPosition(), 122, 1, "Import file");
            menu.add(this.getAdapterPosition(), 123, 2, "Export file");
            menu.add(this.getAdapterPosition(), 121, 0, "Edit name");
        }
    }

}
