
package com.skymobi.appstore.funnypush.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3dAnimation extends Animation {

    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final int mPivot;// 中心轴：1-X，2-Y
    private final float mDepthZ;
    private Camera mCamera;
    private final boolean mReverse;

    /**
     * @param float fromDegrees 起始角度
     * @param float toDegrees 最终角度
     * @param float depthZ 屏幕深度
     * @param int pivot 中心轴
     * @param float centerX, centerY 中心点
     */
    public Rotate3dAnimation(float fromDegrees, float toDegrees,
            float depthZ, int pivot,
            float centerX, float centerY) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mPivot = pivot;
        mReverse = toDegrees > 0;
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees
                    + ((mToDegrees - fromDegrees) * interpolatedTime);
        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();
        camera.save();
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        if (1 == mPivot) {
            camera.rotateX(degrees);
        } else if (2 == mPivot) {
            camera.rotateY(degrees);
        }
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
