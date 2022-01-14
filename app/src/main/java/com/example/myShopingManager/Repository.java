package com.example.myShopingManager;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {
    SheetDAO sheetDAO;
    ItemsDAO itemsDAO;

    public Repository(Application application) {
        AppDataBase appDataBase = AppDataBase.getInstance(application);
        sheetDAO = appDataBase.sheetDAO();
        itemsDAO = appDataBase.itemsDAO();
    }

    private String getDate() {
        return Constants.getInstance().getDate();
    }

    /*Sheet repository*/
    public LiveData<List<Sheet>> getAllSheets() {
        return sheetDAO.getAll();
    }

    public void insertSheet(Sheet sheet) {
        sheet.setDate(getDate());
        new InsertSheet(sheetDAO).execute(sheet);
    }

    public void updateSheet(Sheet sheet) {
        new UpdateSheet(sheetDAO).execute(sheet);
    }

    public void deleteSheet(Sheet sheet) {
        new DeleteSheet(sheetDAO).execute(sheet);
    }

    private static class InsertSheet extends AsyncTask<Sheet, Void, Void> {
        private final SheetDAO dao;

        private InsertSheet(SheetDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Sheet... model) {
            // below line is use to insert our model in dao.
            dao.Insert(model[0]);
            return null;
        }
    }

    private static class UpdateSheet extends AsyncTask<Sheet, Void, Void> {
        private final SheetDAO dao;

        private UpdateSheet(SheetDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Sheet... model) {
            // below line is use to update our model in dao.
            dao.Update(model[0]);
            return null;
        }
    }

    private static class DeleteSheet extends AsyncTask<Sheet, Void, Void> {
        private final SheetDAO dao;

        private DeleteSheet(SheetDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Sheet... model) {
            // below line is use to delete our model in dao.
            dao.Delete(model[0]);
            return null;
        }
    }

    /*Item repository*/
    public LiveData<List<Items>> getAllItems(int sheetId) {
        return itemsDAO.getAll(sheetId);
    }

    public List<Items> getAllItemsList(int sheetId) {
        return itemsDAO.getAllList(sheetId);
    }

    public void insertItem(Items item) {
        item.setDate(getDate());
        new InsertItem(itemsDAO).execute(item);
    }

    public void updateItem(Items item) {
        new UpdateItem(itemsDAO).execute(item);
    }

    public void deleteItem(Items item) {
        new DeleteItem(itemsDAO).execute(item);
    }

    public void deleteAllItems(int sheetId) {
        new DeleteAllItem(itemsDAO).execute(sheetId);
    }

    private static class InsertItem extends AsyncTask<Items, Void, Void> {
        private final ItemsDAO dao;

        private InsertItem(ItemsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Items... model) {
            // below line is use to insert our model in dao.
            dao.Insert(model[0]);
            return null;
        }
    }

    private static class UpdateItem extends AsyncTask<Items, Void, Void> {
        private final ItemsDAO dao;

        public UpdateItem(ItemsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Items... model) {
            // below line is use to update our model in dao.
            dao.Update(model[0]);
            return null;
        }
    }

    private static class DeleteItem extends AsyncTask<Items, Void, Void> {
        private final ItemsDAO dao;

        public DeleteItem(ItemsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Items... model) {
            // below line is use to delete our model in dao.
            dao.Delete(model[0]);
            return null;
        }
    }

    private static class DeleteAllItem extends AsyncTask<Integer, Void, Void> {
        private final ItemsDAO dao;

        public DeleteAllItem(ItemsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            dao.deleteAllItems(integers[0]);
            return null;
        }
    }
}
