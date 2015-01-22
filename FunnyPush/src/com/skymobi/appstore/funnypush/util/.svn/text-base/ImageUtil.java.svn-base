
package com.skymobi.appstore.funnypush.util;

import android.graphics.Matrix;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 */

public class ImageUtil {

    public static void rotateDegree(final ImageView img, final float degree) {
        if (null == img) {
            return;
        }
        try {
            img.setScaleType(ScaleType.MATRIX);
            Matrix matrix = new Matrix();
            matrix.setRotate(degree,
                        img.getWidth() / 2,
                        img.getHeight() / 2);
            img.setImageMatrix(matrix);
        } catch (Exception e) {

        }
    }

    public static void clearRotation(final ImageView img) {
        rotateDegree(img, 0.0f);
    }

    public static void setScale(final ImageView img, final float scale) {
        img.setScaleType(ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale,
                    img.getWidth() / 2,
                    img.getHeight() / 2);
        img.setImageMatrix(matrix);
    }

    public static void scaleAndRotate(final ImageView img, final float scale, final float degree) {
        img.setScaleType(ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale,
                img.getWidth() / 2,
                img.getHeight() / 2);
        matrix.preRotate(degree,
                    img.getWidth() / 2,
                    img.getHeight() / 2);
        img.setImageMatrix(matrix);
    }
}
