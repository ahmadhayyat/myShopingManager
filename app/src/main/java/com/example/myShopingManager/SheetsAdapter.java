package com.example.myShopingManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SheetsAdapter extends ListAdapter<Sheet, SheetsAdapter.ViewHolder> {
    SheetAdapterInteraction sheetAdapterInteraction;
    List<SheetsData> sheetsDataList;

    Context context;
    ExportData exportData;

    public SheetsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Sheet> DIFF_CALLBACK = new DiffUtil.ItemCallback<Sheet>() {
        @Override
        public boolean areItemsTheSame(Sheet oldItem, Sheet newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Sheet oldItem, Sheet newItem) {
            // below line is to check the course name, description and course duration.
            return oldItem.getSheetName().equals(newItem.getSheetName())
                    && oldItem.getDate().equals(newItem.getDate());
        }
    };

    @NonNull
    @Override
    public SheetsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sheets_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SheetsAdapter.ViewHolder holder, int position) {
        final Sheet sheet = getSheetAt(position);
        holder.sheetsDate.setText(sheet.getDate());
        holder.sheetsName.setText(sheet.getSheetName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoMainActivity = new Intent(context, MainActivity.class);
                gotoMainActivity.putExtra(Constants.EXTRA_SHEET_ID, sheet.getId());
                gotoMainActivity.putExtra(Constants.EXTRA_SHEET_NAME, sheet.getSheetName());
                //gotoMainActivity.putExtra("sheetDate", sheetsDataList.get(position).getSheetDate());
                context.startActivity(gotoMainActivity);
            }
        });

        holder.ib_delsheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetAdapterInteraction.onDelete(sheet);
            }
        });
    }

    public Sheet getSheetAt(int position) {
        return getItem(position);
    }

    public void editSheetName(final int position, Context context) {
        final Sheet sheet = getSheetAt(position);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.update_item_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final EditText editText = dialogView.findViewById(R.id.et_update_itemName);
        editText.setText(String.valueOf(sheet.getSheetName()));
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Update", null);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String updatedText = editText.getText().toString();
                        if (updatedText.isEmpty()) {
                            editText.setError("Sheet name is empty");
                            editText.requestFocus();
                            return;
                        }
                        sheet.setSheetName(updatedText);
                        sheetAdapterInteraction.onUpdate(position, sheet);
                        dialogInterface.dismiss();
                    }
                });
            }
        });
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
        return sheetsDataList.get(p).getSheetId();
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

    public void setSheetAdapterInteraction(SheetAdapterInteraction sheetAdapterInteraction) {
        this.sheetAdapterInteraction = sheetAdapterInteraction;
    }

    public interface SheetAdapterInteraction {
        void onDelete(Sheet sheet);

        void onUpdate(int position, Sheet sheet);
    }
}
