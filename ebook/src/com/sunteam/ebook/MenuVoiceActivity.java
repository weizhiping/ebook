package com.sunteam.ebook;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 朗读语音界面
 * 
 * @author sylar
 */
public class MenuVoiceActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		initViews();
	}

	private void initViews() {
		Resources res = getResources();
		
		String[] menus = res.getStringArray(R.array.ebook_array_menu_voice);
		int length = menus.length;
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
		String title = getIntent().getStringExtra("title");
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
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
/*
	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		String title = mMenuList.get(selectItem);
		switch (selectItem) {
		case 0://中文角色
			startToDetail(title,0);
			break;
		case 1://英文角色
			startToDetail(title,1);
			break;
		case 2://语速
			startToNumEdit(title,TTSUtils.getInstance().getSpeed(),0);
			break;
		case 3://语调
			startToNumEdit(title,TTSUtils.getInstance().getPitch(),1);
			break;
//		case 4://语音风格
//			startToDetail(4);
//			break;
		case 4://语音音效
			startToDetail(title,4);
			break;
		}
	}
*/
	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		String title = mMenuList.get(selectItem);
		switch (selectItem) {
		case 0://中文角色，英文角色
			Locale locale = this.getResources().getConfiguration().locale;
			String language = locale.getLanguage();
			if( "en".equalsIgnoreCase(language) )	//英文
			{
				startToDetail(title,1);
			}
			else
			{
				startToDetail(title,0);
			}
			break;
		case 1://语速
			startToNumEdit(title,TTSUtils.getInstance().getSpeed(),0);
			break;
		case 2://语调
			startToNumEdit(title,TTSUtils.getInstance().getPitch(),1);
			break;
//		case 3://语音风格
//			startToDetail(4);
//			break;
		case 3://语音音效
			startToDetail(title,4);
			break;
		}
	}
	
	private void startToDetail(String title,int flag){
		Intent intent = new Intent(this,MenuVoiceDetailActivity.class);
		intent.putExtra("voice_flag", flag);
		intent.putExtra("title", title);
		startActivity(intent);
	}
	
	private void startToNumEdit(String name,int current,int flage){
		Intent intent = new Intent(this,MenuNumEditActivity.class);
		intent.putExtra("edit_name", name);
		intent.putExtra("edit_current", current);
		intent.putExtra("edit_max", 20);
		intent.putExtra("edit_flage", flage);
		startActivity(intent);
	}
}
