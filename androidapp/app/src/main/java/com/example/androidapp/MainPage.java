package com.example.androidapp;

import android.app.Activity;
import android.os.Bundle;

public class MainPage extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // upon creation, inflate and initialize the layout
        setContentView(R.layout.mainpage);
    }
}
