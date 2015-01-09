
package com.skymobi.appstore.funnypush.anim;

import android.graphics.Point;

public interface IAnim {

    public final static int ANIM_LINE = 1;// 直线 点到点
    public final static int ANIM_LINEAR = 2;// 射线 点+方向

    public int getType();

    public Point nextPos(int count);
}
