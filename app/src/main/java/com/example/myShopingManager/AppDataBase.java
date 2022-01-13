package com.example.myShopingManager;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Sheet.class, Items.class}, version = 5)
public abstract class AppDataBase extends RoomDatabase {
    static AppDataBase instance;
    static String DB_NAME = Constants.APP_DB_NAME;
    static SheetDAO sheetDAO;
    static ItemsDAO itemsDAO;

    public static synchronized AppDataBase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDataBase.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallBack)
                    .build();
        }
        return instance;
    }

    static RoomDatabase.Callback roomCallBack = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDBAsyncTask(instance).execute();
        }
    };

    static class PopulateDBAsyncTask extends AsyncTask<Void, Void, Void> {
        PopulateDBAsyncTask(AppDataBase appDataBase) {
            sheetDAO = appDataBase.sheetDAO();
            itemsDAO = appDataBase.itemsDAO();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

    public abstract SheetDAO sheetDAO();

    public abstract ItemsDAO itemsDAO();
}
