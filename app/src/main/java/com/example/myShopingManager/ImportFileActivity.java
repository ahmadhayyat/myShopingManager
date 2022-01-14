package com.example.myShopingManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class ImportFileActivity extends AppActivity {
    TextView fileTv, importFileBtn;
    ViewModel viewModel;
    String path;
    int sheetId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_file);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        initVariables();
        setupClicks();
    }

    @Override
    void initViews() {
        fileTv = findViewById(R.id.fileTv);
        importFileBtn = findViewById(R.id.importFileBtn);
    }

    @Override
    void initVariables() {
        Intent intent = getIntent();
        sheetId = intent.getIntExtra(Constants.EXTRA_SHEET_ID, 0);
        viewModel = new ViewModelProvider(this).get(ViewModel.class);
    }

    @Override
    void setupClicks() {
        importFileBtn.setOnClickListener(onImportBtnClick());
        fileTv.setOnClickListener(view -> showFileChooser());
    }

    private View.OnClickListener onImportBtnClick() {
        return view -> {
            if (fileTv.getText().length() <= 0) {
                fileTv.setError("File not selected");
                fileTv.requestFocus();
            } else {
                ReadFile(path);
            }
        };
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        importFileLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri = data.getData();
                    Log.d("TAG", "File Uri: " + uri.toString());
                    // Get the path
                    path = AppUtils.getPathFromUri(ImportFileActivity.this, uri);
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    fileTv.setText(filename);
                }
            });

    public void ReadFile(String path) {
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
            JSONArray data = new JSONArray(jsonStr);
            System.out.println(jsonStr);
            // looping through All nodes
            Items item;
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);
                String name = c.getString("name");
                item = new Items();
                item.setName(name);
                item.setStatus(Constants.STATUS_NEUTRAL);
                item.setPrice(0);
                item.setSheetId(sheetId);
                viewModel.insertItem(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
