package com.ydh.autoclick.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;

import android.graphics.PixelFormat;
import android.icu.text.StringSearch;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ydh.autoclick.R;
import com.ydh.autoclick.common.BaseRvAdapter;
import com.ydh.autoclick.common.BaseViewHolder;
import com.ydh.autoclick.common.CommonUtils;
import com.ydh.autoclick.common.Constant;
import com.ydh.autoclick.common.EditeDialog;
import com.ydh.autoclick.common.ToastUtils;
import com.ydh.autoclick.db.ClickEntity;
import com.ydh.autoclick.db.DbInterface;
import com.ydh.autoclick.db.DbManager;
import com.ydh.autoclick.entitys.DataEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ydh on 2022/9/20
 */
public class FloatingService extends BaseService {

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;
    private long createTime;
    private int num = 0;
    private int statusBarHeight;//状态栏高度
    private int dp20;
    private int dp300;
    private int dp200;
    private List<ClickEntity> mClickList = new ArrayList<>();
    private List<DataEntity> mDataList = new ArrayList<>();
    private BaseRvAdapter<DataEntity> mRvAdapter;
    private Context mContext;
    public Handler mHandler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        dp300 = CommonUtils.dp2px(300);
        dp200 = CommonUtils.dp2px(300);
        dp20 = CommonUtils.dp2px(20);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = dp300;
        layoutParams.height = dp200;
        statusBarHeight = CommonUtils.getStatusBarHeight();
        initTimeTask();
    }

    private List<ClickEntity> eventList = new ArrayList<>();//需要执行的数组
    private int currentPosition = 0;//当前执行的
    private boolean isCycle = true;

    private void initTimeTask() {
        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                ClickEntity clickEntity = eventList.get(currentPosition);
                if (Constant.ACTION_CLICK.equals(clickEntity.getType()))
                    autoClickView(clickEntity.getStartX(), eventList.get(currentPosition).getStartY());
                else if (Constant.ACTION_CLICK.equals(clickEntity.getType())) {
                    autoSlideView(clickEntity.getStartY(), clickEntity.getEndY());
                }
                currentPosition++;
                if (currentPosition < eventList.size())
                    mHandler.postDelayed(this, eventList.get(currentPosition).getActionTime() - eventList.get(currentPosition).getCreateTime());
                else {
                    if (isCycle) {//循环
                        currentPosition = 1;
                        mHandler.postDelayed(this, eventList.get(currentPosition).getActionTime() - eventList.get(currentPosition).getCreateTime());
                    }
                }
            }
        };
    }

    private void initData() {
        DbManager.getInstance().queryByTime(new DbInterface<List<ClickEntity>>() {

            @Override
            public void success(List<ClickEntity> result) {
                if (result != null) {
                    mDataList.clear();
                    HashMap<Long, List<ClickEntity>> map = new HashMap<>();
                    for (int i = 0; i < result.size(); i++) {
                        long createTime = result.get(i).getCreateTime();
                        if (map.containsKey(createTime)) {
                            map.get(createTime).add(result.get(i));
                        } else {
                            List<ClickEntity> value = new ArrayList<>();
                            value.add(result.get(i));
                            map.put(createTime, value);
                        }
                    }
                    for (Map.Entry<Long, List<ClickEntity>> entry : map.entrySet()) {
                        List<ClickEntity> value = entry.getValue();
                        if (value != null) {
                            for (int i = 0; i < value.size(); i++) {
                                if (value.get(i).getCreateTime() == value.get(i).getActionTime()) {
                                    DataEntity dataEntity = new DataEntity();
                                    dataEntity.setName(value.get(i).getName());
                                    dataEntity.setCycle(value.get(i).isCycle());
                                    dataEntity.setCreateTime(value.get(i).getCreateTime());
                                    dataEntity.setChild(value);
                                    mDataList.add(dataEntity);
                                    break;
                                }
                            }
                        }
                    }
                    mRvAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void fail() {

            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        initData();
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isStart = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.float_display, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            final FrameLayout flContent = displayView.findViewById(R.id.fl_content);
            final LinearLayout viewBg = displayView.findViewById(R.id.view_bg);
            //viewBg.setOnTouchListener(new FloatingOnTouchListener());
            ImageView ivAdd = displayView.findViewById(R.id.iv_add);
            TextView tvDelete = displayView.findViewById(R.id.tv_delete);
            RecyclerView rvData = displayView.findViewById(R.id.rv_data);
            final TextView tvFinish = displayView.findViewById(R.id.tv_finish);
            ivAdd.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    new EditeDialog(mContext, "请输入事件名称", 2, new EditeDialog.EditInterface() {
                        @Override
                        public void onClick(String s) {
                            isStart = true;
                            tvFinish.setVisibility(View.VISIBLE);
                            viewBg.setVisibility(View.GONE);
                            flContent.setOnTouchListener(new ClickListener(flContent));
                            layoutParams.width = CommonUtils.getDisplayWidth(windowManager);
                            layoutParams.height = CommonUtils.getDisplayHeight(windowManager);
                            windowManager.updateViewLayout(displayView, layoutParams);
                            createTime = System.currentTimeMillis();
                            num = 0;
                            mClickList.clear();
                            mClickList.add(new ClickEntity(createTime, createTime, s, true));
                        }
                    });

                }
            });
            tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flContent.setOnTouchListener(null);
                    windowManager.removeView(displayView);
                    onDestroy();
                }
            });
            tvFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbManager.getInstance().insert(mClickList, new DbInterface() {
                        @Override
                        public void success(Object result) {
                            isStart = false;
                            tvFinish.setVisibility(View.GONE);
                            viewBg.setVisibility(View.VISIBLE);
                            flContent.setOnTouchListener(null);
                            mClickList.clear();
                            layoutParams.width = dp300;
                            layoutParams.height = dp200;
                            layoutParams.x = 0;
                            layoutParams.y = 0;
                            flContent.removeViews(2, flContent.getChildCount() - 2);
                            windowManager.updateViewLayout(displayView, layoutParams);
                            initData();
                        }

                        @Override
                        public void fail() {

                        }
                    });
                }
            });
            initAdapter(rvData);
            windowManager.addView(displayView, layoutParams);
        }
    }

    private void initAdapter(RecyclerView rvData) {
        rvData.setLayoutManager(new LinearLayoutManager(displayView.getContext()));
        mRvAdapter = new BaseRvAdapter<DataEntity>(displayView.getContext(), mDataList, R.layout.item_click) {


            @Override
            protected void bindData(BaseViewHolder holder, final DataEntity data, final int position) {
                holder.setText(R.id.tv_name, data.getName() + "(" + (data.getChild().size() - 1) + ")");
                holder.getView(R.id.tv_delete_item).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DbManager.getInstance().delete(data.getCreateTime(), new DbInterface() {
                            @Override
                            public void success(Object result) {
                                mDataList.remove(position);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void fail() {

                            }
                        });
                    }
                });
                holder.getView(R.id.tv_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtils.showToast("执行");
                        eventList.clear();
                        eventList.addAll(mDataList.get(position).getChild());
                        if (eventList.size() > 1) {
                            currentPosition = 1;
                            isCycle = mDataList.get(position).isCycle();
                            mHandler.postDelayed(runnable, eventList.get(currentPosition).getActionTime() - eventList.get(currentPosition).getCreateTime());
                        }
                    }
                });
            }
        };
        rvData.setAdapter(mRvAdapter);
    }

    private class ClickListener implements View.OnTouchListener {
        private int startX;
        private int startY;
        private int endX;
        private int endY;
        private FrameLayout flContent;

        public ClickListener(FrameLayout flContent) {
            this.flContent = flContent;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isStart) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_UP:
                    endX = (int) event.getRawX();
                    endY = (int) event.getRawY();
                    if (Math.abs(endX - startX) + Math.abs(endY - startY) < 50) {
                        num++;
                        TextView textView = new TextView(displayView.getContext());
                        textView.setBackgroundResource(R.drawable.shape_circle_gray_box);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp20, dp20);
                        textView.setLayoutParams(layoutParams);
                        textView.setGravity(Gravity.CENTER);
                        textView.setText("" + num);
                        textView.setX(startX);
                        textView.setY(startY - statusBarHeight);
                        flContent.addView(textView);
                        mClickList.add(new ClickEntity(createTime, System.currentTimeMillis(), Constant.ACTION_CLICK, startX, startY - statusBarHeight));
                    } else {
                        mClickList.add(new ClickEntity(createTime, System.currentTimeMillis(), Constant.ACTION_SCROLL, startX, startY - statusBarHeight, endX, endY));
                    }
                    break;
                default:
                    break;
            }

            return false;
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    Log.e("坐标：X", x + "");
                    Log.e("坐标：Y", y + "");
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

}
