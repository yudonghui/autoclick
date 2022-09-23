package com.ydh.autoclick.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Point;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModel;

import com.ydh.autoclick.common.CommonUtils;

/**
 * Created by ydh on 2022/9/23
 */
public class BaseService extends AccessibilityService {

    private int dp180;

    @Override
    public void onCreate() {
        super.onCreate();
        dp180 = CommonUtils.dp2px(180);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @RequiresApi(Build.VERSION_CODES.N)
    protected void autoSlideView(float startY, float endY) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(dp180, startY);
        path.lineTo(dp180, endY);
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 500))
                .build();
        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    protected void autoClickView(float startX, float startY) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(startX, startY);
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 5))
                .build();
        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    protected void autoBackView() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
}
