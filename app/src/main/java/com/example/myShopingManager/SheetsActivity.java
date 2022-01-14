package com.example.myShopingManager;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SheetsActivity extends AppCompatActivity implements SheetsAdapter.SheetAdapterInteraction {

    private static final int FILE_SELECT_CODE = 1000;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    RecyclerView sheetsRecyclerView;
    List<SheetsData> sheetsDataList = new ArrayList<>();
    DatabseAccess db;
    TextView no_sheet;
    ExportData exportData;
    SheetsAdapter sheetsAdapter;
    int sheetId = 0;
    TinyDB tinyDB;
    Integer itemId = 0;
    MainActivity mainActivity;
    ArrayList<String> speechResult = new ArrayList<>();
    AlertDialog.Builder builder;
    AlertDialog dialog;
    String date;
    ViewModel viewModel;
    SweetAlertDialog sweetAlertDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkPermissions();
        builder = new AlertDialog.Builder(SheetsActivity.this);
        dialog = builder.create();
        date = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault()).format(new Date());
        context = this;
        sweetAlertDialog = new SweetAlertDialog(context);
    }

    void runApp() {


        mainActivity = new MainActivity();
        exportData = new ExportData(SheetsActivity.this);
        tinyDB = new TinyDB(this);
        sheetsRecyclerView = findViewById(R.id.sheetsRecylerview);
        db = new DatabseAccess(SheetsActivity.this);
        no_sheet = findViewById(R.id.no_sheet);
        loadSheets();


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View layout = LayoutInflater.from(SheetsActivity.this).inflate(R.layout.add_sheet_layout, null);
                final EditText et_sheetName = layout.findViewById(R.id.et_sheetName);
                final ImageButton sheetLayoutMic = layout.findViewById(R.id.sheetLayoutMic);
                sheetLayoutMic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        promptSpeechInput();
                    }
                });
                builder.setView(layout);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sheet sheet = new Sheet();
                        sheet.setSheetName(et_sheetName.getText().toString());
                        viewModel.insertSheet(sheet);
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

        sheetsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry! Your device doesn\\'t support speech input",
                    Toast.LENGTH_SHORT).show();
        }


    }

    private void checkPermissions() {
        String[] permissionArrays = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, 11111);
        } else {
            runApp();
        }

    }

    public void loadSheets() {
        sheetsAdapter = new SheetsAdapter();
        sheetsRecyclerView.setLayoutManager(new GridLayoutManager(SheetsActivity.this, 2));
        sheetsRecyclerView.setAdapter(sheetsAdapter);
        sheetsAdapter.setSheetAdapterInteraction(this);
        viewModel = new ViewModelProvider(this).get(ViewModel.class);
        viewModel.getSheetsList().observe(this, sheets -> {
            sheetsAdapter.submitList(sheets);
            if (sheets.size() > 0) {
                no_sheet.setVisibility(View.GONE);
            } else {
                no_sheet.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        itemId = item.getGroupId();
        switch (item.getItemId()) {
            case 121:
                sheetsAdapter.editSheetName(itemId, this);
                return true;
            case 122:
                Intent intent = new Intent(this, ImportFileActivity.class);
                intent.putExtra(Constants.EXTRA_SHEET_ID, sheetsAdapter.getSheetAt(itemId).getId());
                startActivity(intent);
                return true;
            case 123:
                createFile(itemId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("TAG", "File Uri: " + uri.toString());
                    // Get the path
                    String path = AppUtils.getPathFromUri(SheetsActivity.this, uri);

                    Log.d("TAG", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    assert result != null;
                    createDialog(result);

                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean openActivityOnce = true;
        boolean openDialogOnce = true;
        boolean isPermitted = false;
        if (requestCode == 11111) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                isPermitted = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        //execute when 'never Ask Again' tick and permission dialog not show
                    } else {
                        if (openDialogOnce) {
                            alertView();
                        }
                    }
                }
            }
            if (isPermitted) {
                runApp();
            }

        }
    }

    private void alertView() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Permission Denied")
                .setCancelable(false)
                .setIcon(R.drawable.ic_warning)
                .setMessage(R.string.msg)

                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        SheetsActivity.super.onBackPressed();
                    }
                })
                .setPositiveButton("Yes, Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        checkPermissions();

                    }
                }).show();
    }

    void createDialog(ArrayList<String> speechResult) {
        dialog.dismiss();
        final View layout = LayoutInflater.from(SheetsActivity.this).inflate(R.layout.add_sheet_layout, null);
        final EditText et_sheetName = layout.findViewById(R.id.et_sheetName);
        if (speechResult.size() > 0) {
            et_sheetName.setText(speechResult.get(0));
        }
        final ImageButton sheetLayoutMic = layout.findViewById(R.id.sheetLayoutMic);
        sheetLayoutMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        builder.setView(layout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                db.open();
                db.addSheet(et_sheetName.getText().toString(), date);
                db.close();
                loadSheets();
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

    public boolean createFile(int position) {
        Sheet sheet = sheetsAdapter.getSheetAt(position);
        String sheetName = sheet.getSheetName();
        int sheetId = sheet.getId();
        boolean isCreated = createNow(sheetId, sheetName);
        if (isCreated) {
            share(sheetName);
            return true;
        }
        return false;
    }

    private boolean createNow(int sheetId, String sheetName) {
        List<Items> itemsList = viewModel.getItemsList(sheetId);
        Log.i("EXPORTT", itemsList.size() + "");
        JSONArray convertedJson = exportData.getResults(itemsList);
        Log.i("EXPORTT", convertedJson.toString());
        try {
            File checkFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name) + "/");
            if (!checkFile.exists()) {
                checkFile.mkdir();
            }
            FileWriter file = new FileWriter(checkFile.getAbsolutePath() + "/" + sheetName + ".json");
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
        sb.append("/" + getString(R.string.app_name) + "/");
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

    @Override
    public void onDelete(Sheet sheet) {
        sweetAlertDialog.setTitleText("Sheet will be deleted permanently")
                .setCancelButton("Cancel", sweetAlertDialog1 -> sweetAlertDialog.dismiss())
                .setCancelButtonBackgroundColor(getColor(R.color.darkGreen))
                .setConfirmButton("Delete", sweetAlertDialog -> {
                    viewModel.deleteAllItem(sheet.id);
                    viewModel.deleteSheet(sheet);
                    sweetAlertDialog.dismiss();
                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Deleted", Snackbar.LENGTH_LONG);
                    snackbar.setTextColor(getColor(R.color.red));
                    snackbar.show();
                }).setConfirmButtonBackgroundColor(getColor(R.color.lightRed))
                .changeAlertType(SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    @Override
    public void onUpdate(int position, Sheet sheet) {
        viewModel.updateSheet(sheet);
        sheetsAdapter.notifyItemChanged(position);
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Updated", Snackbar.LENGTH_LONG);
        snackbar.setTextColor(getColor(R.color.darkGreen));
        snackbar.show();
    }
}
