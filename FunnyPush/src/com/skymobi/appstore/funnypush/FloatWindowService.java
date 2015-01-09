
package com.skymobi.appstore.funnypush;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skymobi.appstore.funnypush.action.CarDriver;
import com.skymobi.appstore.funnypush.action.LayoutDriver;
import com.skymobi.appstore.funnypush.sound.SoundPlayer;
import com.skymobi.appstore.funnypush.util.SizeUtil;

/**
 * @ClassName: FloatWindowService
 * @Description: 后台服务-管理桌面悬浮窗的显示-隐藏-刷新
 * @author dylan.zhao
 * @date 2013-10-16 上午09:33:18
 */
public class FloatWindowService extends Service {

    public static final String OPERATION = "float_op";
    public static final int OPERATION_SHOW = 1;
    public static final int OPERATION_HIDE = 0;

    private static final int MSG_CHECK_ACTIVITY = 10;

    public final static int MSG_CAR_ANIM = 11;// 运行动画的消息
    private final static int MSG_MOVE_CAR = 12;// 移动视图位置的消息
    private final static int MSG_HIDE_TIP = 13;// 隐藏下拉提示
    private final static int MSG_REFRESH = 14;//

    public static WindowManager windowManager;
    public static Vibrator vibratorService;
    private static ActivityManager activityManager;

    private WindowManager.LayoutParams carParams;
    private WindowManager.LayoutParams panelParams;
    private WindowManager.LayoutParams tipParams;

    private static SoundPlayer soundPlayer;

    private ImageView imgCar;
    private CarDriver carDriver;
    private ImageView imgLevel = null;
    private ImageView imgTip = null;

    private RelativeLayout panel;
    // private WebView panel;
    private LayoutDriver panelDriver;

    private boolean isAdded = false; // 是否已增加悬浮窗
    private boolean isAnimTimerRunning = false;

    AppInfo[] app = new AppInfo[2];

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!checkApp()) {
            stopSelf();
        }
        initManagers();
    }

    private void initManagers() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        vibratorService = (Vibrator)
                getSystemService(Context.VIBRATOR_SERVICE);
        soundPlayer = new SoundPlayer(this);
        // createFloatPanel();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
        switch (operation) {
            case OPERATION_SHOW:
                mHandler.removeMessages(MSG_CHECK_ACTIVITY);
                mHandler.sendEmptyMessage(MSG_CHECK_ACTIVITY);
                break;
            case OPERATION_HIDE:
                mHandler.removeMessages(MSG_CHECK_ACTIVITY);
                mHandler.removeMessages(MSG_CAR_ANIM);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundPlayer.exit();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_ACTIVITY:
                    checkActivity();
                    break;

                case MSG_REFRESH:
                    keepRefreshing();
                    break;

                case MSG_CAR_ANIM:
                    if (isAdded) {
                        continueAnim();
                    }
                    break;

                case MSG_MOVE_CAR:
                    if (isAdded) {
                        refreshView(imgCar, carParams);
                    }
                    break;
                case MSG_HIDE_TIP:
                    break;
                default:
                    break;
            }
        }

    };

    private void checkActivity() {
        if (isHome()) {
            if (!isAdded) {
                if (null != panel) {
                    windowManager.addView(panel, panelParams);
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);
                } else {
                    if (null == imgCar) {
                        createFloatCar();
                    }
                    windowManager.addView(imgCar, carParams);
                }
                isAdded = true;
            }
        } else {
            if (isAdded) {
                try {
                    windowManager.removeView(imgCar);
                } catch (Exception e) {
                }
                try {
                    windowManager.removeView(panel);
                } catch (Exception e) {
                }
                isAdded = false;
            }
        }
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_ACTIVITY, 1000);
    }

    private int times = 5;

    private void keepRefreshing() {
        Log.d("webView", "keepRefreshing,retry times left:" + times);
        refreshView(panel, panelParams);
        times--;
        if (times >= 0) {
            mHandler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);
        }
    }

    /**
     * 获得属于桌面的应用的应用包名称
     * 
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        names.add("com.miui.home");// 小米系统的桌面
        return names;
    }

    /**
     * 判断当前界面是否是桌面
     */
    public boolean isHome() {

        List<RunningTaskInfo> rti = activityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 检查两个应用信息是否完整
     */
    private boolean checkApp() {

        app[0] = new AppInfo("捕鱼达人");
        app[0].iconId = R.drawable.icon_a;
        app[0].typeName = "休闲";
        app[0].sizeString = "10.25M";
        app[0].downCount = 2500;

        app[1] = new AppInfo("至尊封神");
        app[1].iconId = R.drawable.icon_b;
        app[1].typeName = "网游";
        app[1].sizeString = "80.5M";
        app[1].downCount = 6300;

        return true;
    }

    /**
     * 创建悬浮小车
     */
    private void createFloatCar() {

        carParams = getDefaultParams();
        carParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        final int h = windowManager.getDefaultDisplay().getHeight();

        carParams.x = Math.round(SizeUtil.TAN_15 * h / 2);
        carParams.y = Math.round(carParams.x / SizeUtil.TAN_15);
        imgCar = new ImageView(this);
        imgCar.setImageResource(R.drawable.car148);
        carDriver = new CarDriver(this, imgCar, carParams);
        imgCar.setOnTouchListener(carDriver);
        mHandler.sendEmptyMessageDelayed(MSG_CAR_ANIM, 100);
    }

    public float W_D_S = 0.9F;// 最终宽度占屏幕宽度比例
    private final static float H_D_W = 0.37F;// 高度与宽度比例

    /**
     * 创建悬浮应用推荐框
     */
    private void createFloatPanel() {
        panelParams = getDefaultParams();
        final int w = SizeUtil.getPhoneWidth();
        panelParams.width = w;
        panelParams.height = w;
        panel = (RelativeLayout) RelativeLayout.inflate(this, R.layout.push_container, null);
        // panel = new WebView(this);
        // final WebSettings ws = panel.getSettings();
        // ws.setAppCacheEnabled(true);
        // ws.setAppCacheMaxSize(1024 * 1024);// 设置最大缓存1M
        // ws.setSupportZoom(true);
        // ws.setBuiltInZoomControls(true);
        // ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);// 设置缓存模式
        // // 设置支持Javascript
        // ws.setJavaScriptEnabled(true);
        // ws.setJavaScriptCanOpenWindowsAutomatically(true);
        // // panel.
        // panel.requestFocus();
        // panel.loadUrl("http://www.baidu.com");
    }

    private void setFloatSize(final RelativeLayout v) {
        final int w = SizeUtil.getPhoneWidth();
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v.getLayoutParams();
        p.width = (int) (w * W_D_S);
        p.height = (int) (w * W_D_S * H_D_W);
        v.setLayoutParams(p);
    }

    /**
     * 设置A面，两个应用
     */
    private void setFloatA() {
        if (null == panel) {
            return;
        }
        final RelativeLayout v = (RelativeLayout) panel.findViewById(R.id.content_two);
        setFloatSize(v);
        final RelativeLayout appLeft = (RelativeLayout) v.findViewById(R.id.app_left);
        final RelativeLayout appRight = (RelativeLayout) v.findViewById(R.id.app_right);

        setAppContent(appLeft, app[0]);
        setAppContent(appRight, app[1]);
        appLeft.setOnClickListener(panelDriver);
        appRight.setOnClickListener(panelDriver);
    }

    private static void setAppContent(final RelativeLayout parent, final AppInfo appInfo) {
        ((TextView) parent.findViewById(R.id.app_name)).setText(appInfo.name);
        ((ImageView) parent.findViewById(R.id.app_icon)).setImageResource(appInfo.iconId);
    }

    /**
     * 设置B面，一个应用
     */
    private void setFloatB(final int idx) {
        if (null == panel) {
            return;
        }
        final RelativeLayout v = (RelativeLayout) panel.findViewById(R.id.content_one);
        setFloatSize(v);
        ((ImageView) v.findViewById(R.id.one_icon)).setImageResource(app[idx].iconId);
        ((TextView) v.findViewById(R.id.one_name)).setText(app[idx].name);
        ((TextView) v.findViewById(R.id.one_desc)).setText(
                app[idx].typeName + " | " + app[idx].sizeString + " | 下载 " + app[idx].downCount);

        v.findViewById(R.id.one_img).setOnClickListener(panelDriver);
        v.findViewById(R.id.btn_install).setOnClickListener(panelDriver);
        v.findViewById(R.id.btn_close).setOnClickListener(panelDriver);
    }

    private WindowManager.LayoutParams getDefaultParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.type = WindowManager.LayoutParams.TYPE_PHONE;// 拉下通知栏不可见
        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
        params.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return params;
    }

    public void startAnimTimer(final CarDriver driver) {
        carDriver = driver;
        if (!isAnimTimerRunning) {
            continueAnim();
        }
    }

    private void continueAnim() {
        if (carDriver == null) {
            isAnimTimerRunning = false;
            return;
        }
        isAnimTimerRunning = true;
        carDriver.continueAnim();
        mHandler.sendEmptyMessageDelayed(MSG_CAR_ANIM, 1000 / CarDriver.getRate());
    }

    public void moveCarDelay(final long time) {
        mHandler.sendEmptyMessageDelayed(MSG_MOVE_CAR, time);
    }

    public void removeCarDriver() {
        carDriver = null;
    }

    public void removeView(final View view) {
        try {
            windowManager.removeView(view);
        } catch (Exception e) {
        }
    }

    public void refreshView(final View view, final LayoutParams params) {
        windowManager.updateViewLayout(view, params);
    }

    private final int[] imgIds = new int[] {
            R.drawable.car_fule_0,
            R.drawable.car_fule_1,
            R.drawable.car_fule_2,
            R.drawable.car_fule_3,
            R.drawable.car_fule_4,
    };
    private final int MAX_LEVEL = imgIds.length;
    private int curLevel = 0;

    private void changeLevelImage() {

        if (curLevel == 0) {
            if (null != imgLevel) {
                windowManager.removeView(imgLevel);
                imgLevel = null;
            }
        } else {
            if (null == imgLevel) {
                imgLevel = new ImageView(this);
                imgLevel.setImageResource(imgIds[curLevel - 1]);
                panelParams = getDefaultParams();
                panelParams.y = CarDriver.stubY / 2;
                windowManager.addView(imgLevel, panelParams);
            } else {
                imgLevel.setImageResource(imgIds[curLevel - 1]);
                panelParams.y = CarDriver.stubY / 2;
                windowManager.updateViewLayout(imgLevel, panelParams);
            }
        }
    }

    public boolean setLevel(final int level) {
        if (level >= 0 && level <= MAX_LEVEL && level != curLevel) {
            curLevel = level;
            changeLevelImage();
            return true;
        }
        return false;
    }

    public void startShake(final long[] patten, final int repeat) {
        vibratorService.vibrate(patten, repeat);
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    /**
     * 汽车即将开出屏幕，开始出现推荐框
     */
    public void onCarFinishing(final int x, final int y) {

        createFloatPanel();// 创建视图
        panelParams.x = 0;
        panelParams.y = y;
        windowManager.addView(panel, panelParams);
        panelDriver = new LayoutDriver(this, panel, panelParams);
        setFloatA();
        panelDriver.startAnim(x);
    }

    /**
     * 点击图标作出了选择
     */
    public void onSelect(final int id) {
        if (id == R.id.app_left) {
            setFloatB(0);
        } else {
            setFloatB(1);
        }
    }

    public void onClose() {
        removeView(panel);
        mHandler.removeMessages(MSG_CHECK_ACTIVITY);
        stopSelf();
    }

    /**
     * 视图切换到B面
     */
    public void changePanel() {
        panel.findViewById(R.id.content_two).setVisibility(View.GONE);
        panel.findViewById(R.id.content_one).setVisibility(View.VISIBLE);
    }

    private final static int TIP_MILLIS = 2000;// 下拉提示持续时间
    private final static int TIP_WIDTH = 130;// 下拉提示宽度
    private final static int TIP_HEIGHT = 50;// 下拉提示高度

    public void showDragTip() {
        if (null == imgTip) {
            imgTip = new ImageView(this);
            imgTip.setImageResource(R.drawable.drag_tip);
            tipParams = getDefaultParams();
        }
        tipParams.x = carParams.x + carParams.width + TIP_WIDTH;
        tipParams.y = carParams.y - TIP_HEIGHT / 2;
        windowManager.addView(imgTip, tipParams);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_TIP, TIP_MILLIS);
    }

    public void hideDragTip() {
        removeView(imgTip);
    }
}
