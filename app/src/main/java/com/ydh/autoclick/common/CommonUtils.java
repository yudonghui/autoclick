package com.ydh.autoclick.common;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ydh.autoclick.BaseApplication;

/**
 * Created by ydh on 2022/9/23
 */
public class CommonUtils {
    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    //获取状态栏的高度
    public static int getStatusBarHeight() {
        int resourceId = BaseApplication.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return BaseApplication.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getDisplayWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public static int getDisplayWidth(WindowManager windowManager) {
        DisplayMetrics metric = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDisplayHeight(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    public static int getDisplayHeight(WindowManager windowManager) {
        DisplayMetrics metric = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }
}
