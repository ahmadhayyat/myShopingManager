package com.example.myShopingManager;

import androidx.appcompat.app.AppCompatActivity;

public abstract class AppActivity extends AppCompatActivity {
    abstract void initViews();

    abstract void initVariables();

    abstract void setupClicks();
}
