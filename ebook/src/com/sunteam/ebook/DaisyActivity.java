package com.sunteam.ebook;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.view.MainView;

/**
 * Daisy主界面
 * 
 * @author sylar
 */
public class DaisyActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		catalog = getIntent().getIntExtra("catalogType", 0);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		initViews();
	}

	private void initViews() {
		mMenuList = new ArrayList<String>();
		fileInfoList = new ArrayList<FileInfo>();
		initFiles();
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this,
				this.getString(R.string.main_menu_daisy), mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		if (null != remberFile) {
			mMainView.setSelection(remberFile.flag);
		}
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
		fileInfoList = FileOperateUtils.getDaisyInDir(catalog);
		if (null != fileInfoList) {
			for (FileInfo f : fileInfoList) {
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
			mMainView.enter();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu) {

		FileInfo file = fileInfoList.get(selectItem);
		Log.e("diasy", "diasy  path------------:" + file.diasyPath);
		DaisyFileReaderUtils.getInstance().init(file.diasyPath);
		
		ArrayList<DiasyNode> diasList = DaisyFileReaderUtils.getInstance()
				.getChildNodeList(-1);
		Intent intent = null;
		if (null != diasList && diasList.size() > 0) {
			intent = new Intent(this, DaisyDetailActivity.class);
			intent.putExtra("diasys", diasList);
		} else {
			Toast.makeText(this, "-----需要进入阅读界面----", Toast.LENGTH_SHORT)
					.show();
			intent = new Intent(this, DaisyDetailActivity.class);
		}
		intent.putExtra("name", menu);
		intent.putExtra("catalogType", file.catalog);
		intent.putExtra("path", file.path);
		intent.putExtra("file", remberFile);
		this.startActivity(intent);
	}
}
