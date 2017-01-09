package com.lh.rxbus;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by home on 2017/1/9.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
