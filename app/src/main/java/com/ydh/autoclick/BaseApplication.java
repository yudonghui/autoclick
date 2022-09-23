package com.ydh.autoclick;

import android.app.Application;
import android.content.Context;

/**
 * Created by ydh on 2022/9/21
 */
public class BaseApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
