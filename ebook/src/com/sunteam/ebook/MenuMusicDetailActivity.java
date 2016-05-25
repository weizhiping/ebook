package com.sunteam.ebook;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 背景音乐详细界面
 * 
 * @author sylar
 */
public class MenuMusicDetailActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileList = null;
	private int flag;
	private SharedPreferences shared;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,Context.MODE_PRIVATE);
		flag = getIntent().getIntExtra("music_flag", 0);
		fileList = new ArrayList<FileInfo>();
		mMenuList = new ArrayList<String>();
		initViews();
	}

	private void initViews() {
		Resources res = getResources();
		String[] menus = null;
		if(0 == flag){
			menus = res.getStringArray(R.array.array_menu_music_on);
			int length = menus.length;
			for (int i = 0; i < length; i++) {
				mMenuList.add(menus[i]);
			}
		}else{
			initFiles();
		}
		String title = this.getString(R.string.menu_function);
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
	}
	
    @Override
    public void onResume()
    {
    	if( mMainView != null )
    	{
    		mMainView.onResume();
    	}
    	super.onResume();
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回

			break;
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
		Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
		Editor edit = shared.edit();
		if(0 == flag){
			intent.putExtra("result_flag", 1);
			if(0 == selectItem){
				edit.putBoolean(EbookConstants.MUSICE_STATE, true);
			}else{
				edit.putBoolean(EbookConstants.MUSICE_STATE, false);
			}
		}else{
			intent.putExtra("result_flag", 2);
			edit.putString(EbookConstants.MUSICE_PATH, fileList.get(selectItem).path);
		}
		edit.commit();
		sendBroadcast(intent);
		ScreenManager.getScreenManager().popAllActivityExceptOne();
	}
	
	// 初始化显示文件
		private void initFiles() {
			ArrayList<File> filesList = FileOperateUtils.getMusicInDir();
			if (null != filesList) {
				for (File f: filesList) {
					FileInfo info = new FileInfo();
					info.name = f.getName();
					info.path = f.getPath();
					mMenuList.add(info.name);
					fileList.add(info);
				}
			}
		}
}
