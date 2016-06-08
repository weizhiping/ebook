package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.view.MainView;

/**
 * Daisy索引界面
 * 
 * @author sylar
 */
public class DaisyDetailActivity extends Activity implements OnEnterListener {
	private static final String TAG = "DaisyDetailActivity";
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<DiasyNode> diasList;
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;
	private FileInfo fileInfo;
	private String path;
	private int seq;
	private boolean isDirectEntry = false;	//是否直接进入阅读界面

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		path = intent.getStringExtra("path");
		catalog = intent.getIntExtra("catalogType", 0);
		seq = intent.getIntExtra("seq", -1);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfo = (FileInfo) getIntent().getSerializableExtra("fileinfo");
		diasList = (ArrayList<DiasyNode>) intent.getSerializableExtra("diasys");
		Log.e(TAG, "----file info flag--:" + fileInfo.flag);
		initViews(name);
	}

	private void initViews(String name) {
		mMenuList = new ArrayList<String>();
		initFiles();
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this,name, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
//		if (null != remberFile) {
//			mMainView.setSelection(remberFile.flag);
//		}
	}

	@Override
	public void onResume() {
		if (mMainView != null) {
			mMainView.onResume();
		}
		super.onResume();
	}

	// 初始化显示文件
	private void initFiles() {
		if (null != diasList) {
			for (DiasyNode f : diasList) {
				mMenuList.add(f.name);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP: // 上
			mMainView.up();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN: // 下
			mMainView.down();
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
			isDirectEntry = false;
			mMainView.enter();
			return true;
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_NUMPAD_5:		//直接进入阅读器界面
			isDirectEntry = true;
			mMainView.enter();
			return	true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu) {
		DiasyNode dias = diasList.get(selectItem);
		ArrayList<DiasyNode> diaysList = DaisyFileReaderUtils.getInstance().getChildNodeList(dias.seq);
		int size = diaysList.size();
		if( ( 0 == size ) || isDirectEntry )
		{
			isDirectEntry = false;
			Intent intent = new Intent(this, ReadDaisyActivity.class);
			intent.putExtra("name", menu);
			intent.putExtra("path", path);
			intent.putExtra("node",  dias);
			intent.putExtra("fileinfo", fileInfo);
			this.startActivity(intent);
		}
		else
		{
			isDirectEntry = false;
			Intent intent = new Intent(this, DaisyDetailActivity.class);
			intent.putExtra("name", menu);
			intent.putExtra("seq", dias.seq);
			intent.putExtra("catalogType", catalog);
			intent.putExtra("path", path);
			intent.putExtra("file", remberFile);
			intent.putExtra("fileinfo", fileInfo);
			intent.putExtra("diasys",  diaysList);
			this.startActivity(intent);
		}
	}
}
