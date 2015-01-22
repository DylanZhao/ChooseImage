package com.example.choosephotos;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.GridView;
import android.widget.Toast;

public class ChoosePhotosActivity extends Activity {
	List<ImageItem> dataList;
	GridView gridView;
	ImageGridAdapter adapter;// 自定义的适配器
	AlbumHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_image_bucket);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		initData();
		initView();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		dataList = helper.getImageList(false);

	}

	/**
	 * 初始化view视图
	 */
	private void initView() {
		gridView = (GridView) findViewById(R.id.gridview);
		adapter = new ImageGridAdapter(this, dataList, mHandler);
		gridView.setAdapter(adapter);

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ChoosePhotosActivity.this, "最多选择9张图片", 400)
				        .show();
				break;

			default:
				break;
			}
		}
	};

}
