package com.sunteam.ebook;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.view.MainView;
import com.sunteam.ebook.view.MainView.OnTTSSpeakListener;

/**
 * 背景音乐详细界面
 * 
 * @author sylar
 */
public class MenuMusicDetailActivity extends Activity implements OnTTSSpeakListener, 
		OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileList = null;
	private int flag;// 0开关 1 音乐选择
	private SharedPreferences shared;
	private int musicPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ebook_activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,
				Context.MODE_PRIVATE);
		flag = getIntent().getIntExtra("music_flag", 0);
		fileList = new ArrayList<FileInfo>();
		mMenuList = new ArrayList<String>();
		initViews();
		MediaPlayerUtils.getInstance().stop();
	}

	private void initViews() {
		if (0 == flag) {
			initSwitch();
		} else {
			initFiles();
		}
		String title = getIntent().getStringExtra("title");
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mMainView = new MainView(this, this, title, mMenuList, this);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		if(0 == flag){
			boolean isOpen = shared.getBoolean(EbookConstants.MUSICE_STATE, false);
			if(isOpen){
				mMainView.setSelection(0);
			}else{
				mMainView.setSelection(1);
			}
		}else if(0 < musicPosition){
			mMainView.setSelection(musicPosition);	
		}
	}

	@Override
	public void onPause() {
		if (mMainView != null) {
			mMainView.onPause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		if (mMainView != null) {
			mMainView.onResume();
		}
		if(1 == flag && 0 == fileList.size()){
			PublicUtils.showToast(this, getResources().getString(R.string.ebook_menu_muisc_null),true);
		}
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(1 == flag){
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:// 返回
				MediaPlayerUtils.getInstance().stop();
				Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
				intent.putExtra("result_flag", 2);
				sendBroadcast(intent);
				break;
			}
		}
		return mMainView.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mMainView.onKeyUp(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
		Editor edit = shared.edit();
		if (0 == flag) {
			intent.putExtra("result_flag", 1);
			if (0 == selectItem) {
				edit.putBoolean(EbookConstants.MUSICE_STATE, true);
			} else {
				edit.putBoolean(EbookConstants.MUSICE_STATE, false);
			}
		} else {
			intent.putExtra("result_flag", 2);
			edit.putString(EbookConstants.MUSICE_PATH,
					fileList.get(selectItem).path);
		}
		edit.commit();
		sendBroadcast(intent);
		PublicUtils.showToast(MenuMusicDetailActivity.this, getResources().getString(R.string.ebook_setting_success),true);
	}

	// 初始化开关
	private void initSwitch() {
		String[] menus = getResources().getStringArray(R.array.ebook_array_menu_music_on);
		int length = menus.length;
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
	}

	// 初始化显示文件
	private void initFiles() {
		String path = shared.getString(EbookConstants.MUSICE_PATH, null);
		ArrayList<File> filesList = FileOperateUtils.getMusicInDir();
		if (null != filesList && 0 < filesList.size()) {
			for (int i=0; i< filesList.size(); i++) {
				File f = filesList.get(i);
				FileInfo info = new FileInfo();
				info.name = f.getName();
				info.path = f.getPath();
				mMenuList.add(info.name);
				fileList.add(info);
				if(f.getPath().equals(path)){
					musicPosition = i;
				}
			}
		}
	}

	@Override
	public void onCompleted() {
		if(1 == flag){
			MediaPlayerUtils.getInstance().stop();
			int position = mMainView.getSelectItem();
			MediaPlayerUtils.getInstance().play(fileList.get(position).path);
		}
	}
}
