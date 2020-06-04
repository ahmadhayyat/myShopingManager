package com.example.myShopingManager;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    List<ItemsData> itemsDataList = new ArrayList<>();
    ItemsData itemsData = new ItemsData();
    TinyDB tinyDB;
    RecyclerView recyclerView;
    TextView noItem, tv_total;
    Integer itemId;
    ItemDataAdapter itemDataAdapter;
    DatabseAccess db;
    LinearLayout LLcal;
    int sheetId;
    String sheetName, itemsDate;
    View view;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    String date;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        sheetId = intent.getIntExtra("sheetId", 0);
        sheetName = intent.getStringExtra("sheetName");
        itemsDate = getDate();


        tinyDB = new TinyDB(MainActivity.this);
        db = new DatabseAccess(this);
        recyclerView = findViewById(R.id.recylerview);
        tv_total = findViewById(R.id.tv_total);
        LLcal = findViewById(R.id.LLcal);
        LLcal.setVisibility(View.GONE);
        noItem = findViewById(R.id.tv_noItem);
        loadItems();
        builder = new AlertDialog.Builder(MainActivity.this);
        dialog = builder.create();

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemId = tinyDB.getInt(Constants.ITEM_ID);


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
        });
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

        loadSwipeHelper();
        view = getWindow().getDecorView().getRootView();
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
        SwipeHelper swipeHelper = new SwipeHelper(this, recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Edit",
                        0,
                        Color.parseColor("#FF9502"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                itemDataAdapter.editName(pos, view);
                            }
                        }
                ));

                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                itemDataAdapter.deleteItem(pos, view);
                            }
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
        db.open();
        itemsDataList = db.getItems(sheetId);
        db.close();
        itemDataAdapter = new ItemDataAdapter(itemsDataList, MainActivity.this, sheetId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        recyclerView.setAdapter(itemDataAdapter);
        if (itemsDataList.size() > 0) {
            noItem.setVisibility(View.GONE);
        } else {
            noItem.setVisibility(View.VISIBLE);
        }
        getSupportActionBar().setTitle(sheetName + " ( " + itemsDataList.size() + " )");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_calculate) {
            db.open();
            Integer total = db.calculate(sheetId);
            db.close();
            LLcal.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    LLcal.setVisibility(View.GONE);
                }
            }, 5000);

            tv_total.setText(String.valueOf("Rs " + total));
            return true;
        }
        if (id == R.id.action_delAll) {
            db.open();
            boolean isDel = db.deleteAllItem(sheetId);
            db.close();
            loadItems();
            if (isDel) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "All items deleted", Snackbar.LENGTH_LONG);
                snackbar.setTextColor(Color.parseColor("#ff0000"));
                snackbar.show();
            }
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

}
