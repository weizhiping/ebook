package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.SuperDialog;
import com.sunteam.ebook.util.SuperDialog.DialogCallBack;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 数据库功能菜单
 * 
 * @author sylar
 */
public class MenuDatabaseActivity extends Activity implements OnEnterListener,DialogCallBack {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private int flag;//1为收藏，2为最近使用
	private int item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		flag = getIntent().getIntExtra("flag", 1);
		initViews();
	}

	private void initViews() {
		mMenuList = new ArrayList<String>();
		mMenuList.add(getString(R.string.menu_delete_current));
		mMenuList.add(getString(R.string.menu_delete_list));
		if (2 == flag) {
			mMenuList.add(getString(R.string.menu_add_fav));
		}
		String title = this.getString(R.string.menu_function);
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
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

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		item = selectItem;
		SuperDialog dialog = new SuperDialog(this);
		dialog.initeCallBack(this);
		if( 0 == selectItem){
			dialog.showSuperDialog(R.string.dialog_delete);
			TTSUtils.getInstance().speakTips(getString(R.string.dialog_delete)
					+"，" + getString(R.string.dialog_yes) + "，" +  getString(R.string.dialog_no));
		}else if(1 == selectItem){
			dialog.showSuperDialog(R.string.dialog_clear);
			TTSUtils.getInstance().speakTips(getString(R.string.dialog_clear)
					+"，" + getString(R.string.dialog_yes) + "，" +  getString(R.string.dialog_no));
		}else{
			Intent intent = new Intent();
			intent.putExtra("data_item", selectItem);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	@Override
	public void dialogConfrim() {
		Intent intent = new Intent();
		intent.putExtra("data_item", item);
		setResult(RESULT_OK, intent);
		finish();
	}
}
