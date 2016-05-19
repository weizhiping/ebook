package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.view.MainView;

/**
 * 朗读语音子界面界面
 * 
 * @author sylar
 */
public class MenuVoiceDetailActivity extends Activity implements
		OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private int voiceFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		initData();
		initViews();
	}
	
	private void initData(){
		voiceFlag = getIntent().getIntExtra("voice_flag", 0);
		Resources res = getResources();
		String[] menus = null;
		switch (voiceFlag) {
		case 0:
			menus = res.getStringArray(R.array.array_menu_voice_china);
			break;
		case 1:
			menus = res.getStringArray(R.array.array_menu_voice_english);
			break;
		case 4:
			menus = res.getStringArray(R.array.array_menu_voice_style);
			break;
		case 5:
			menus = res.getStringArray(R.array.array_menu_voice_effect);
			break;
		}
		int length = menus.length;
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
	}

	private void initViews() {
		
		String title = this.getString(R.string.menu_function);
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
	}

	@Override
	public void onResume() {
		if (mMainView != null) {
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
			ScreenManager.getScreenManager().popAllActivityExceptOne();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu) {
		switch (selectItem) {
		case 0:
			
			break;
		case 1:

			break;
		case 2:

			break;
		case 3:

			break;
		case 4:

			break;
		case 5:

			break;
		}
		ScreenManager.getScreenManager().popAllActivityExceptOne();
	}
}
