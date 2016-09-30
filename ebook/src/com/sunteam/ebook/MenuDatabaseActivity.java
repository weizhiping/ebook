package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 数据库功能菜单
 * 
 * @author sylar
 */
public class MenuDatabaseActivity extends Activity implements OnEnterListener {
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
		
		if( 0 == selectItem){
			dialog(getResources().getString(R.string.dialog_delete));
		}else if(1 == selectItem){
			dialog(getResources().getString(R.string.dialog_clear));
		}else{
			Intent intent = new Intent();
			intent.putExtra("data_item", selectItem);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	private void dialog(String content){
		 ConfirmDialog mConfirmDialog = new ConfirmDialog(this, content
				 ,getResources().getString(R.string.dialog_yes), getResources().getString(R.string.dialog_no));
		 
		mConfirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
			public void doConfirm() {
				String content = getResources().getString(R.string.dialog_delete_su);
				if(1 == item){
					content = getResources().getString(R.string.dialog_clear_su);
				}
				PublicUtils.showToast(MenuDatabaseActivity.this, content, new PromptListener(){
					@Override
					public void onComplete() {
						Intent intent = new Intent();
						intent.putExtra("data_item", item);
						setResult(RESULT_OK, intent);
						finish();
					}});
			}
			
			@Override
			public void doCancel() {
				if( mMainView != null ){
		    		mMainView.onResume();
		    	}
			}
		});
		mConfirmDialog.show();

	}
}
