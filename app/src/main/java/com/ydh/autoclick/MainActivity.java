package com.ydh.autoclick;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ydh.autoclick.common.ClipboardUtils;
import com.ydh.autoclick.common.EditeDialog;
import com.ydh.autoclick.common.SPUtils;
import com.ydh.autoclick.services.FloatingService;
import com.ydh.autoclick.services.ScrollService;


public class MainActivity extends AppCompatActivity {
    private TextView mTvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvLocation = findViewById(R.id.tv_location);
        checkPermission();
        startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 111);
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())), 0);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void buttonStart(View view) {
        Intent serviceFloat = new Intent(this, FloatingService.class);
        startService(serviceFloat);
    }

    public void buttonScroll(View view) {
        new EditeDialog(this, "请输入滑动频率",1, new EditeDialog.EditInterface() {
            @Override
            public void onClick(String s) {
                if (TextUtils.isEmpty(s)) return;
                SPUtils.setLong(SPUtils.FILE_USER, SPUtils.DURATION, Long.parseLong(s));
                Intent serviceScroll = new Intent(MainActivity.this, ScrollService.class);
                startService(serviceScroll);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                mTvLocation.setText(x + "," + y);
                break;
            case MotionEvent.ACTION_UP:
                int nowX = (int) event.getRawX();
                int nowY = (int) event.getRawY();
                ClipboardUtils.setClipboard(nowX + "," + nowY);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}