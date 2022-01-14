package com.example.myShopingManager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppActivity implements ItemDataAdapter.ItemAdapterInteraction {
    ViewModel viewModel;
    Context context;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    RecyclerView recyclerView;
    TextView noItem, tv_total;
    ItemDataAdapter itemDataAdapter;
    DatabseAccess db;
    LinearLayout LLcal;
    int sheetId;
    String sheetName, itemsDate;
    View view;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    FloatingActionButton fab;
    SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        initVariables();
        setupClicks();
        loadItems();
        loadSwipeHelper();
    }

    @Override
    void initViews() {
        recyclerView = findViewById(R.id.recylerview);
        tv_total = findViewById(R.id.tv_total);
        LLcal = findViewById(R.id.LLcal);
        noItem = findViewById(R.id.tv_noItem);
        fab = findViewById(R.id.fab);
    }

    @Override
    void initVariables() {
        context = this;
        Intent intent = getIntent();
        sheetId = intent.getIntExtra(Constants.EXTRA_SHEET_ID, 0);
        sheetName = intent.getStringExtra(Constants.EXTRA_SHEET_NAME);
        builder = new AlertDialog.Builder(MainActivity.this);
        dialog = builder.create();
        LLcal.setVisibility(View.GONE);
        sweetAlertDialog = new SweetAlertDialog(context);
        view = getWindow().getDecorView().getRootView();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });
    }

    @Override
    void setupClicks() {
        fab.setOnClickListener(onFabClick());
    }

    private View.OnClickListener onFabClick() {
        return view1 -> {
            final View layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_item_layout, null);
            builder.setView(layout);
            final TextView et_itemName = layout.findViewById(R.id.et_itemName);
            ImageButton itemLayoutMic = layout.findViewById(R.id.itemLayoutMic);
            itemLayoutMic.setOnClickListener(v -> promptSpeechInput());
            builder.setPositiveButton("Add", (dialog, which) -> {
                Items item = new Items();
                item.setName(et_itemName.getText().toString());
                item.setPrice(0);
                item.setStatus(Constants.STATUS_NEUTRAL);
                item.setSheetId(sheetId);
                viewModel.insertItem(item);
                recyclerView.scrollToPosition(itemDataAdapter.getItemCount() - 1);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        };
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSwipeHelper() {
        new SwipeHelper(this, recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Edit",
                        0,
                        getColor(R.color.darkOrange),
                        pos -> updateItemDialog(pos)
                ));

                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        0,
                        getColor(R.color.red),
                        pos -> {
                            Items item = itemDataAdapter.getItemAt(pos);
                            viewModel.deleteItem(item);
                            Snackbar snackbar = Snackbar.make(view, "Deleted", Snackbar.LENGTH_LONG);
                            snackbar.setTextColor(getColor(R.color.red));
                            snackbar.show();
                        }
                ));
            }
        };
    }

    public String getDate() {
        String date = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault()).format(new Date());
        return date;
    }

    public void loadItems() {
        itemDataAdapter = new ItemDataAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemDataAdapter);
        itemDataAdapter.setInteraction(this);
        viewModel = new ViewModelProvider(this).get(ViewModel.class);
        viewModel.getItems(sheetId).observe(this, items -> {
            itemDataAdapter.submitList(items);
            if (items.size() > 0)
                noItem.setVisibility(View.GONE);
            else
                noItem.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(sheetName + " ( " + items.size() + " )");
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calculate) {
            List<Items> allItems = viewModel.getItemsList(sheetId);
            int cal = 0;
            for (Items i : allItems) {
                cal = cal + i.getPrice();
            }
                LLcal.setVisibility(View.VISIBLE);
                tv_total.setText(getString(R.string.rs, cal));
                new Handler().postDelayed(() -> LLcal.setVisibility(View.GONE), 5000);
            return true;
        }
        if (id == R.id.action_delAll) {
            sweetAlertDialog.setTitleText("All items will be deleted permanently")
                    .setCancelButton("Cancel", sweetAlertDialog1 -> sweetAlertDialog.dismiss())
                    .setCancelButtonBackgroundColor(getColor(R.color.darkGreen))
                    .setConfirmButton("Delete All", sweetAlertDialog -> {
                        viewModel.deleteAllItem(sheetId);
                        sweetAlertDialog.dismiss();
                        finish();
                    }).setConfirmButtonBackgroundColor(getColor(R.color.lightRed))
                    .changeAlertType(SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                assert result != null;
                createDialog(result);

            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void createDialog(ArrayList<String> speechResult) {
        dialog.dismiss();
        final View layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_item_layout, null);
        builder.setView(layout);
        final TextView et_itemName = layout.findViewById(R.id.et_itemName);
        ImageButton itemLayoutMic = layout.findViewById(R.id.itemLayoutMic);
        itemLayoutMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        if (speechResult.size() > 0) {
            et_itemName.setText(speechResult.get(0));
        }
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                db.open();
                db.addItems(et_itemName.getText().toString(), sheetId, itemsDate);
                db.close();
                loadItems();
                itemDataAdapter.notifyDataSetChanged();


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    void updateItemDialog(int position) {
        Items item = itemDataAdapter.getItemAt(position);
        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.update_item_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final EditText editText = dialogView.findViewById(R.id.et_update_itemName);
        editText.setText(String.valueOf(item.getName()));
        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        dialogBuilder.setPositiveButton("Update", null);
        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                String updatedText = editText.getText().toString();
                if (updatedText.isEmpty()) {
                    editText.setError("Item name is empty");
                    editText.requestFocus();
                    return;
                }
                item.setName(updatedText);
                viewModel.updateItem(item);
                alertDialog.dismiss();
                itemDataAdapter.notifyItemChanged(position);
                Snackbar snackbar = Snackbar.make(view, "Updated", Snackbar.LENGTH_LONG);
                snackbar.setTextColor(getColor(R.color.darkGreen));
                snackbar.show();
            });
        });
        alertDialog.show();
    }


    @Override
    public void onPriceUpdate(Items item) {
        viewModel.updateItem(item);
    }

    @Override
    public void onStatusChange(Items item) {
        viewModel.updateItem(item);
    }
}
