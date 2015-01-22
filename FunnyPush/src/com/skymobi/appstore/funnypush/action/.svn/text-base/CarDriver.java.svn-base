
package com.skymobi.appstore.funnypush.action;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.skymobi.appstore.funnypush.FloatWindowService;
import com.skymobi.appstore.funnypush.anim.IAnim;
import com.skymobi.appstore.funnypush.anim.LineAnim;
import com.skymobi.appstore.funnypush.anim.LinearAnim;
import com.skymobi.appstore.funnypush.sound.SoundPlayer;
import com.skymobi.appstore.funnypush.util.ImageUtil;
import com.skymobi.appstore.funnypush.util.SizeUtil;

/**
 * @ClassName: CarDriver
 * @Description: 汽车动画和动作驱动--走直线，碰壁反射
 * @author dylan.zhao
 * @date 2013-10-17 上午09:21:03
 */
public class CarDriver implements OnTouchListener {

    protected FloatWindowService service;
    protected final ImageView view;
    protected final WindowManager.LayoutParams params;

    private final static float carScale = 0.71f;
    private final static float carRotate = -15;

    protected IAnim anim;
    protected boolean animRunning = false;
    protected int refreshCount = 0;
    private final static int animRate = 25;// 汽车动画频率
    private final static float DRAG_BACK = 0.5f; // 拖拽阻力反弹系数
    private final static long[] shakeDrag = new long[] {
            10, 100
    };// 音效播放参数
    private final static int DIS_OF_LEVEL = 20; // 每加一档需要的拖动距离
    private final static int MAX_DIS_OF_CLICK = 5; // 小于此拖动距离的操作认为是点击

    private final static int FINISH_LINE = 160; // 终点线（触发推荐框的点）

    private final static int stubX = 0;
    public final static int stubY = 100;

    private int boundX;// 边界值
    private int boundY;// 边界值

    public CarDriver(FloatWindowService context, ImageView img,
            WindowManager.LayoutParams params) {
        this.service = context;
        this.view = img;
        this.params = params;
        setBound();
        setShowAnim();
    }

    private void setBound() {
        final Point size = SizeUtil.getWindowSize();
        boundX = (size.x - view.getWidth()) / 2;
        boundY = (size.y - view.getHeight()) / 2;
    }

    private void setShowAnim() {

        anim = new LineAnim(
                 new Point(params.x, params.y), new Point(stubX, stubY))
                .setRate(animRate)
                .setDuration(300);
        animRunning = true;
        refreshCount = 1;
    }

    protected Point getPos() {
        return new Point(params.x, params.y);
    }

    int fromX, fromY, lastX, lastY, level;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (animRunning) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fromX = lastX = (int) event.getRawX();
                fromY = lastY = (int) event.getRawY();
                carReady();
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = event.getRawY() - lastY;
                moveView((int) (event.getRawX() - lastX), (int) dy);

                int dis = (int) event.getRawY() - fromY;
                if (service.setLevel(dis / DIS_OF_LEVEL)) {
                    level = dis / DIS_OF_LEVEL;
                    if (dy > 0) {
                        params.y -= DRAG_BACK * MAX_DIS_OF_CLICK;
                        service.moveCarDelay(100);
                        service.hideDragTip();
                    }
                }
                if (dis > MAX_DIS_OF_CLICK && dis % MAX_DIS_OF_CLICK == 0) {
                    service.startShake(shakeDrag, -1);
                }
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();

                break;
            case MotionEvent.ACTION_UP:
                service.setLevel(0);
                if (lastY - fromY > 2 * DIS_OF_LEVEL) {
                    startCar(); // 车子开动
                } else {
                    carScale();
                }
                break;
        }
        return true;
    }

    /**
     * 恢复尺寸，显示提示
     */
    private void carReady() {
        ImageUtil.rotateDegree(view, carRotate);
        service.showDragTip();
    }

    private void carScale() {
        ImageUtil.setScale(view, carScale);
        service.hideDragTip();
    }

    private void startCar() {
        setBound();
        service.getSoundPlayer().play(SoundPlayer.CAR_RUNNING);
        startAnim();
    }

    private void moveView(final int dx, final int dy) {
        params.x += dx;
        params.y += dy;
        if (params.y > stubY) {
            ImageUtil.rotateDegree(view, SizeUtil.getDegree(
                    new Point(stubX - params.x, stubY - params.y)));
        }
        service.refreshView(view, params);
    }

    /** 开始动画 */
    private void startAnim() {
        anim = new LinearAnim(
                checkBound(new Point(params.x, params.y)),
                new Point(stubX - params.x, stubY - params.y),
                level);
        animRunning = true;
        refreshCount = 1;
        service.startAnimTimer(this);
    }

    /**
     * 继续动画
     * 
     * @return 是否继续
     */
    public void continueAnim() {
        if (refreshCount == 1) {
            onAnimStart();
        }
        Point pos = anim.nextPos(refreshCount);
        if (anim instanceof LinearAnim) {
            checkBound(pos);
        }
        if (null != pos && outBound == OUT_NONE) {

            params.x = pos.x;
            params.y = pos.y;
            service.refreshView(view, params);
            refreshCount++;

        } else {
            refreshCount = 0;
            onAnimEnd();
        }
    }

    public static int getRate() {
        return animRate;
    }

    private final static int OUT_NONE = 0;
    private final static int OUT_RIGHT = 1;
    private final static int OUT_TOP = 2;
    private final static int OUT_LEFT = 3;
    private final static int OUT_BOTTOM = 4;

    private int outBound = OUT_NONE;
    private boolean finishTrigged = false;

    /**
     * 检查运动位置边界
     */
    private Point checkBound(Point pos) {

        if (!animRunning && pos.y > boundY) {// 启动前的下边界
            pos.y = boundY;
        }

        else if (pos.y > boundY + view.getHeight()) {// 运行时下边界
            Log.d("dylan", "crash bottom bound!!!");
            outBound = OUT_BOTTOM;

        } else if (pos.x > boundX) {// 右边界
            Log.d("dylan", "crash the right bound !!!");
            outBound = OUT_RIGHT;
            pos.x = boundX;

        } else if (pos.x < -1 * boundX) {// 左边界
            Log.d("dylan", "crash the left bound !!!");
            outBound = OUT_LEFT;
            pos.x = -1 * boundX;

        } else if (pos.y < -1 * boundY) {// 上边界
            Log.d("dylan", "crash the top bound !!!");
            outBound = OUT_TOP;
            pos.y = -1 * boundY;

        } else {
            outBound = OUT_NONE;
        }
        if (animRunning && !finishTrigged
                && pos.y > FINISH_LINE && ((LinearAnim) anim).getStep().y > 0) {
            finishTrigged = true;
            service.onCarFinishing(pos.x, FINISH_LINE);
        }
        return pos;
    }

    private void onAnimStart() {
        if (IAnim.ANIM_LINE == anim.getType()) {
            ImageUtil.scaleAndRotate(view, carScale, carRotate);
        }
    }

    private void onAnimEnd() {

        if (IAnim.ANIM_LINEAR == anim.getType()
                && outBound != OUT_BOTTOM) {
            resetAnim();
            return;
        }
        service.removeCarDriver();
        animRunning = false;

        if (IAnim.ANIM_LINEAR == anim.getType()) {
            service.removeView(view);
        }
    }

    private void resetAnim() {
        final Point step = ((LinearAnim) anim).getStep();
        switch (outBound) {
            case OUT_RIGHT:
                step.x = -1 * Math.abs(step.x);
                break;

            case OUT_TOP:
                step.y = Math.abs(step.y);
                break;

            case OUT_LEFT:
                step.x = Math.abs(step.x);
                break;

            default:
                break;
        }
        ImageUtil.rotateDegree(view, SizeUtil.getDegree(step));
        ((LinearAnim) anim).setPos(new Point(params.x, params.y)).setStep(step);
        outBound = OUT_NONE;
    }
}
