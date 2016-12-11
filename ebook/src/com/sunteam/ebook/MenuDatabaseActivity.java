package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
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
	private DatabaseManager manager;
	private FileInfo fileInfo;
	private int flag;//1为收藏，2为最近使用
	private int item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		flag = getIntent().getIntExtra("flag", 1);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		manager = new DatabaseManager(this);
		initViews();
	}

	private void initViews() {
		mMenuList = new ArrayList<String>();
		mMenuList.add(getString(R.string.ebook_menu_delete_current));
		mMenuList.add(getString(R.string.ebook_menu_delete_list));
		if (2 == flag) {
			mMenuList.add(getString(R.string.ebook_menu_add_fav));
		}
		String title = this.getString(R.string.ebook_menu_function);
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

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		item = selectItem;
		
		if( 0 == selectItem){
			dialog(getResources().getString(R.string.ebook_dialog_delete));
		}else if(1 == selectItem){
			dialog(getResources().getString(R.string.ebook_dialog_clear));
		}else{
			insertToDb();
		}
	}
	
	//添加到收藏
	private void insertToDb(){
		boolean hasBook = manager.insertBookToDb(fileInfo, EbookConstants.BOOK_COLLECTION);
		String tips = getResources().getString(R.string.ebook_add_fav_success);
		if(hasBook){
			tips = getResources().getString(R.string.ebook_add_fav_fail);
		}
		PublicUtils.showToast(this, tips, new PromptListener(){
			@Override
			public void onComplete() {
				finish();
			}});
	}
	
	private void dialog(String content){
		 ConfirmDialog mConfirmDialog = new ConfirmDialog(this, content
				 ,getResources().getString(R.string.ebook_dialog_yes), getResources().getString(R.string.ebook_dialog_no));
		 
		mConfirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
			public void doConfirm() {
				String content = getResources().getString(R.string.ebook_dialog_delete_su);
				if(1 == item){
					content = getResources().getString(R.string.ebook_dialog_clear_su);
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
