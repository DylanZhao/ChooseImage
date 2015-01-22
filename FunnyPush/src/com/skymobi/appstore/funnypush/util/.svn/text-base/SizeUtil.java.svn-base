
package com.skymobi.appstore.funnypush.util;

import android.graphics.Point;
import android.view.Surface;

import com.skymobi.appstore.funnypush.FloatWindowService;

/**
 * @ClassName: SizeUtil
 * @Description: TODO
 * @author dylan.zhao
 * @date 2013-10-17 下午06:19:30
 */
public class SizeUtil {

    public final static float TAN_15 = 0.268f;

    public static Point getWindowSize() {
        return new Point(
                FloatWindowService.windowManager.getDefaultDisplay().getWidth(),
                FloatWindowService.windowManager.getDefaultDisplay().getHeight());
    }

    public static float getDegree(final Point step) {
        return (float) Math.toDegrees(Math.atan2(step.x, -1 * step.y));
    }

    public static int getPhoneWidth() {
        switch (FloatWindowService.windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return FloatWindowService.windowManager.getDefaultDisplay().getWidth();
            default:
                return FloatWindowService.windowManager.getDefaultDisplay().getHeight();
        }
    }
}
