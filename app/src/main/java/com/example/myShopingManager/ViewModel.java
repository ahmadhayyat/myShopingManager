package com.example.myShopingManager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ViewModel extends AndroidViewModel {
    Repository repository;

    public ViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    /*Sheet viewModel*/
    public void insertSheet(Sheet sheet) {
        repository.insertSheet(sheet);
    }

    public void updateSheet(Sheet sheet) {
        repository.updateSheet(sheet);
    }

    public void deleteSheet(Sheet sheet) {
        repository.deleteSheet(sheet);
    }

    public LiveData<List<Sheet>> getSheetsList() {
        return repository.getAllSheets();
    }

    /*Item viewModel*/
    public void insertItem(Items item) {
        repository.insertItem(item);
    }

    public void updateItem(Items item) {
        repository.updateItem(item);
    }

    public void deleteItem(Items item) {
        repository.deleteItem(item);
    }

    public void deleteAllItem(int sheetId) {
        repository.deleteAllItems(sheetId);
    }

    public LiveData<List<Items>> getItemsList(int sheetId) {
        return repository.getAllItems(sheetId);
    }
}
