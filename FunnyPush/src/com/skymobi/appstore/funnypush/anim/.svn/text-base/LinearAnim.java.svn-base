
package com.skymobi.appstore.funnypush.anim;

import android.graphics.Point;

/**
 * @ClassName: LinearAnim
 * @Description: 直线运动（匀速、变速）
 * @author dylan.zhao
 * @date 2013-10-22 下午02:14:59
 */
public class LinearAnim implements IAnim {

    private final static int S_BASE = 10;
    private final static int S_UNIT = 10;
    private int fromX;
    private int fromY;
    private int stepX;
    private int stepY;

    public LinearAnim(final Point start, final Point directon, final int speed) {
        fromX = start.x;
        fromY = start.y;
        double dis = Math.hypot(directon.x, directon.y);
        stepX = (int) ((S_BASE + S_UNIT * speed) * directon.x / dis);
        stepY = (int) ((S_BASE + S_UNIT * speed) * directon.y / dis);
    }

    public LinearAnim setPos(final Point point) {
        fromX = point.x;
        fromY = point.y;
        return this;
    }

    @Override
    public Point nextPos(int count) {
        return new Point(fromX + count * stepX, fromY + count * stepY);
    }

    public Point getStep() {
        return new Point(stepX, stepY);
    }

    public LinearAnim setStep(final Point step) {
        stepX = step.x;
        stepY = step.y;
        return this;
    }

    @Override
    public int getType() {
        return IAnim.ANIM_LINEAR;
    }
}
