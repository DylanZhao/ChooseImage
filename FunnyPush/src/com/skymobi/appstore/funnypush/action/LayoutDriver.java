package com.skymobi.appstore.funnypush.action;

import android.graphics.Matrix;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.skymobi.appstore.funnypush.FloatWindowService;
import com.skymobi.appstore.funnypush.R;
import com.skymobi.appstore.funnypush.anim.Rotate3dAnimation;

public class LayoutDriver implements View.OnClickListener {

	protected FloatWindowService service;
	protected final View view;
	protected final WindowManager.LayoutParams params;

	private final float scaleFactor = 0.2f;// 缩放倍数

	private final static int STAGE_SHOW = 1;
	private final static int STAGE_TURN_IN = 2;
	private final static int STAGE_TURN_OUT = 3;
	private final static int STAGE_HIDE = 4;
	private int animStage = 0;

	public LayoutDriver(FloatWindowService context, View view,
	        WindowManager.LayoutParams params) {
		this.service = context;
		this.view = view;
		this.params = params;
	}

	/**
	 * 放大显示
	 */
	public void startAnim(final int fromX) {

		final View rView = view.findViewById(R.id.content_two);
		final float pX = (fromX - params.x * scaleFactor) / (1 - scaleFactor);
		final float pivotX = 0.5f + pX / params.width;
		final ScaleAnimation scale =
		        new ScaleAnimation(scaleFactor, 1.0f, scaleFactor, 1.0f,
		                Animation.RELATIVE_TO_SELF, pivotX,
		                Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(300);
		scale.setFillAfter(false);
		animStage = STAGE_SHOW;
		scale.setAnimationListener(animListener);
		rView.startAnimation(scale);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_left:
			service.onSelect(R.id.app_left);
			animStage = STAGE_TURN_IN;
			startOvreRotate();// 3D 翻转
			break;
		case R.id.app_right:
			service.onSelect(R.id.app_right);
			animStage = STAGE_TURN_IN;
			startOvreRotate();// 3D 翻转
			break;

		case R.id.btn_install:
			// TODO 发起下载-安装

		case R.id.btn_close:

			scaleClose();
			break;

		default:
			break;
		}

	}

	/** 缩小消失 */
	private void scaleClose() {
		final View rView = view.findViewById(R.id.content_one);
		final ScaleAnimation scale =
		        new ScaleAnimation(1.0f, 0, 1.0f, 0,
		                Animation.RELATIVE_TO_SELF, 0.5f,
		                Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(300);
		scale.setFillAfter(false);
		animStage = STAGE_HIDE;
		scale.setAnimationListener(animListener);
		rView.startAnimation(scale);
	}

	float rotateX;
	float rotateY;

	/** 3D翻转 */
	private void startOvreRotate() {
		final boolean isRotateA =
		        view.findViewById(R.id.content_two).getVisibility() == View.VISIBLE;
		final View rView = view.findViewById(
		        isRotateA ? R.id.content_two : R.id.content_one);
		Rotate3dAnimation rotation;
		if (animStage == STAGE_TURN_IN) {
			rotateX = rView.getWidth() / 2.0f;
			rotateY = rView.getHeight() / 2.0f;
			rotation = new Rotate3dAnimation(0, 90, 0, 1,
			        rotateX, rotateY);
			rotation.setInterpolator(
			        new DecelerateInterpolator());
		} else {
			rotation = new Rotate3dAnimation(-90, 0, 0, 1,
			        rotateX, rotateY);
			rotation.setInterpolator(new AccelerateInterpolator());
		}
		rotation.setDuration(200);
		rotation.setFillAfter(false);
		rotation.setAnimationListener(animListener);
		rView.startAnimation(rotation);
	}

	private final AnimationListener animListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			switch (animStage) {
			case STAGE_SHOW:
				break;

			case STAGE_TURN_IN: {
				service.changePanel();
				animStage = STAGE_TURN_OUT;
				startOvreRotate();
				break;
			}
			case STAGE_TURN_OUT:
				break;

			case STAGE_HIDE:
				service.onClose();
				break;
			default:
				break;
			}
		}

		@Override
		public void onAnimationStart(Animation animation) {
			switch (animStage) {
			case STAGE_SHOW:
				rotateDividerLine();
				break;
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

	};

	/** 中间的分隔线旋转成斜线 */
	private void rotateDividerLine() {
		final ImageView img = (ImageView) view.findViewById(R.id.divider_line);
		img.setScaleType(ScaleType.MATRIX);
		Matrix matrix = new Matrix();

		matrix.setTranslate(0, img.getHeight() / 2.0f);
		matrix.postRotate(-45.0f
		        , img.getWidth() / 2.0f
		        , img.getHeight() / 2.0f);
		img.setImageMatrix(matrix);
	}
}
