package com.ydh.autoclick.common;

import android.widget.Toast;

import com.ydh.autoclick.BaseApplication;

/**
 * Created by ydh on 2022/9/22
 */
public class ToastUtils {
    private static Toast toast;
    public static void showToast(String message) {
        if (toast == null) {
            toast = Toast.makeText(BaseApplication.getContext(), message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
}
