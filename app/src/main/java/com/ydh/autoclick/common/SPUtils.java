package com.ydh.autoclick.common;

import android.content.SharedPreferences;

import com.ydh.autoclick.BaseApplication;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ydh on 2022/9/22
 */
public class SPUtils {
    public static final String FILE_USER = "cache_user";
    public static final String DURATION = "duration";

    public static void setLong(String fileNaem, String spNaem, long spValue) {
        SharedPreferences sp = BaseApplication.getContext().getSharedPreferences(fileNaem, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(spNaem, spValue);
        editor.commit();
    }

    public static long getLong(String fileNaem, String spNaem) {
        SharedPreferences sp = BaseApplication.getContext().getSharedPreferences(fileNaem, MODE_PRIVATE);
        long spValue = sp.getLong(spNaem, 0);
        return spValue;
    }
}
