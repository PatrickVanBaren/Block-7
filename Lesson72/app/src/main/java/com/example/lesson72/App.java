package com.example.lesson72;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GpsDataStorage.createInstance();
    }
}
