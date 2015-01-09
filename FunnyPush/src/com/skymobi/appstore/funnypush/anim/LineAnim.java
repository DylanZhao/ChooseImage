
package com.skymobi.appstore.funnypush.anim;

import android.graphics.Point;

/**
 * @ClassName: LineAnim
 * @Description: 直线动画轨迹
 * @author dylan.zhao
 * @date 2013-10-17 上午11:59:33
 */
public class LineAnim implements IAnim {

    private int lineFromX = 0; // 直线动画起始位置
    private int lineFromY = 0; // 直线动画起始位置

    private int lineToX = 0; // 直线动画起始位置
    private int lineToY = 100; // 直线动画起始位置
    private int duration = 1000;// 动画持续时间ms
    private int refreshRate = 25;// 动画刷新频率

    public LineAnim(final Point start, final Point end) {
        lineFromX = start.x;
        lineFromY = start.y;
        lineToX = end.x;
        lineToY = end.y;
    }

    public LineAnim setDuration(final int duration) {
        this.duration = duration;
        return this;
    }

    public LineAnim setRate(final int rate) {
        this.refreshRate = rate;
        return this;
    }

    @Override
    public Point nextPos(int count) {
        final float percent = getAnimPercent(count);
        if (percent > 1.0) {
            return null;
        }
        return getPos(percent);
    }

    private Point getPos(final float percent) {
        return new Point(
                lineFromX + (int) (percent * (lineToX - lineFromX)),
                lineFromY + (int) (percent * (lineToY - lineFromY)));
    }

    // 计算动画进度百分比
    private float getAnimPercent(final int count) {
        return count / (float) (refreshRate * duration / 1000);
    }

    @Override
    public int getType() {
        return IAnim.ANIM_LINE;
    }

}
