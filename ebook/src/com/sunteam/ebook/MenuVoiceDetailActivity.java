package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.entity.TTSSpeakMode;
import com.sunteam.ebook.util.TTSUtils;
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
		setContentView(R.layout.ebook_activity_main);
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
			menus = res.getStringArray(R.array.ebook_array_menu_voice_china);
			break;
		case 1:
			menus = res.getStringArray(R.array.ebook_array_menu_voice_english);
			break;
		case 4:
			menus = res.getStringArray(R.array.ebook_array_menu_voice_effect);		
			break;
//		case 5:
//			menus = res.getStringArray(R.array.ebook_array_menu_voice_style);
//			break;
		}
		int length = menus.length;
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
	}

	private void initViews() {
		String title = getIntent().getStringExtra("title");
		switch(voiceFlag){
		case 0:
			mMainView = new MainView(this, this, title, mMenuList,TTSSpeakMode.READ_MODE_CN);
			mMainView.setSelection(TTSUtils.getInstance().getCurRoleCnIndex());
			break;
		case 1:
			mMainView = new MainView(this, this, title, mMenuList,TTSSpeakMode.READ_MODE_EN);
			mMainView.setSelection(TTSUtils.getInstance().getCurRoleEnIndex());
			break;
		case 4:
			mMainView = new MainView(this, this, title, mMenuList,TTSSpeakMode.READ_MODE_EFFECT);
			mMainView.setSelection(TTSUtils.getInstance().getCurEffectIndex());
			break;
		}
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
	}
    
    @Override
    public void onPause()
    {
    	if( mMainView != null )
    	{
    		mMainView.onPause();
    	}
    	super.onPause();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		return mMainView.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		return	mMainView.onKeyUp(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		String voice = mMenuList.get(selectItem);
		switch (voiceFlag) {
		case 0:
			TTSUtils.getInstance().setRoleCn(this, voice, new PromptListener() {
				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					ScreenManager.getScreenManager().popAllActivityExceptOne();
				}
				
			});
			break;
		case 1:
			TTSUtils.getInstance().setRoleEn(this, voice, new PromptListener() {
				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					ScreenManager.getScreenManager().popAllActivityExceptOne();
				}
				
			});
			break;
		case 4:
			TTSUtils.getInstance().setEffect(this, voice, new PromptListener() {
				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					ScreenManager.getScreenManager().popAllActivityExceptOne();
				}
				
			});
			break;
		}
	}
}
