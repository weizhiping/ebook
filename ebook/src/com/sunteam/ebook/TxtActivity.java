package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.view.MainView;

/**
 * txt与word主界面
 * 
 * @author sylar
 */
public class TxtActivity extends Activity implements OnEnterListener {
	private static final String TAG = "TxtActivity";
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;
	private UpdateRemFileReceiver fileReceiver;
	private DatabaseManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
		catalog = getIntent().getIntExtra("catalogType", 0);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		manager = new DatabaseManager(this);
		initViews();
		registerReceiver();
	}

	private void initViews() {
		mMenuList = new ArrayList<String>();
		mMenuList.add(this.getString(R.string.ebook_txt_menu_catalog));
		mMenuList.add(this.getString(R.string.ebook_txt_menu_fav));
		mMenuList.add(this.getString(R.string.ebook_txt_menu_recent));

		String title = null;
		if (catalog == 0) {
			title = this.getString(R.string.ebook_main_menu_txt);
		} else if (catalog == 2) {
			title = this.getString(R.string.ebook_main_menu_word);
		} else if (catalog == 1) {
			title = this.getString(R.string.ebook_main_menu_daisy);
		}

		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		
		if (null != remberFile) {
			mMainView.setSelection(remberFile.flag);
		}
	}
    
	private void registerReceiver(){
		fileReceiver = new UpdateRemFileReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.ACTION_UPDATE_FILE);
		registerReceiver(fileReceiver, filter);
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
		// TODO Auto-generated method stub
		String name = menu;
		Intent intent = new Intent(this, TxtDetailActivity.class);
		intent.putExtra("name", name);
		intent.putExtra("flag", selectItem);
		intent.putExtra("flagType", selectItem);
		intent.putExtra("catalogType", catalog);
		intent.putExtra("file", remberFile);
		this.startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(fileReceiver);
	}
	
	private class UpdateRemFileReceiver extends BroadcastReceiver { 
	      
	    @Override  
	    public void onReceive(Context context, Intent intent) {    
	        if (intent.getAction().equals(EbookConstants.ACTION_UPDATE_FILE)) {  
	        	remberFile = manager.queryLastBook(EbookConstants.BOOK_RECENT);
	        }  
	    } 
	}
}
