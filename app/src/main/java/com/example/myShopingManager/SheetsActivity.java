package com.example.myShopingManager;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SheetsActivity extends AppCompatActivity {

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
    String date ;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

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
        db.open();
        sheetsDataList = db.getSheets();
        db.close();
        sheetsAdapter = new SheetsAdapter(sheetsDataList, SheetsActivity.this);
        sheetsRecyclerView.setLayoutManager(new GridLayoutManager(SheetsActivity.this, 2));
        sheetsAdapter.notifyDataSetChanged();
        sheetsRecyclerView.setAdapter(sheetsAdapter);
        if (sheetsDataList.size() > 0) {
            no_sheet.setVisibility(View.GONE);
        } else {
            no_sheet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        sheetId = sheetsAdapter.getSheetId(item.getGroupId());
        itemId = item.getGroupId();
        switch (item.getItemId()) {
            case 121:
                View view = getWindow().getDecorView().getRootView();
                sheetsAdapter.editSheetName(itemId, view);
                return true;
            case 122:
                showFileChooser();
                return true;
            case 123:
                sheetsAdapter.createFile(itemId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Import"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Please install a File Manager.", Snackbar.LENGTH_LONG);
            snackbar.setTextColor(Color.parseColor("#ff0000"));
            snackbar.show();
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
                    String path = getPathFromUri(SheetsActivity.this, uri);

                    ReadFile(path);


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

    public void ReadFile(String path) {
        boolean yes = false;
        try {
            //File yourFile = new File(Environment.getExternalStorageDirectory(),path);
            FileInputStream stream = new FileInputStream(path);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
            /*  String jsonStr = "{\n\"data\": [\n    {\n        \"id\": \"1\",\n        \"title\": \"Farhan Shah\",\n        \"duration\": 10\n    },\n    {\n        \"id\": \"2\",\n        \"title\": \"Noman Shah\",\n        \"duration\": 10\n    },\n    {\n        \"id\": \"3\",\n        \"title\": \"Ahmad Shah\",\n        \"duration\": 10\n    },\n    {\n        \"id\": \"4\",\n        \"title\": \"Mohsin Shah\",\n        \"duration\": 10\n    },\n    {\n        \"id\": \"5\",\n        \"title\": \"Haris Shah\",\n        \"duration\": 10\n    }\n  ]\n\n}\n";
             */
            //JSONObject jsonObj = new JSONObject(jsonStr);

            // Getting data JSON Array nodes
            JSONArray data = new JSONArray(jsonStr);
            System.out.println(jsonStr);
            // looping through All nodes
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);

                String name = c.getString("name");

                db.open();
                yes = db.addItems(name, sheetId, mainActivity.getDate());
                db.close();

            }
            if (yes) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Imported successfully", Snackbar.LENGTH_LONG);
                snackbar.setTextColor(Color.parseColor("#00ff80"));
                snackbar.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

}
