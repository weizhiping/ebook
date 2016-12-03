package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.view.MainView;

/**
 * 文档部分列表界面
 * 
 * @author sylar
 */

public class TxtPartActivity extends Activity implements OnEnterListener 
{
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private FileInfo fileInfo;
	private FileInfo remberFile;//路径记忆传递
	private DatabaseManager manager;
	private boolean isAuto = false;
	private boolean isResume = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
		manager = new DatabaseManager(this);
		initViews();
	}
	
	private void initViews()
    {
		Intent intent = getIntent();
		remberFile = (FileInfo) intent.getSerializableExtra("rem_file");
		fileInfo = (FileInfo) intent.getSerializableExtra("file");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
    	int count = intent.getIntExtra("count", 0);
    	isAuto = intent.getBooleanExtra("isAuto", false);
    	
    	mMenuList = new ArrayList<String>();
    	for( int i = 1; i <= count; i++ )
    	{
    		mMenuList.add(String.format(this.getResources().getString(R.string.ebook_txt_menu_part), i ));
    	}
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.ebook_fl_container);
    	mMainView = new MainView( this, this, fileInfo.name, mMenuList );
    	mFlContainer.removeAllViews();
    	mFlContainer.addView(mMainView.getView());
    	if(null != remberFile && remberFile.path.equals(fileInfo.path)){
    		Log.e("part", "-----rem file part---:" + remberFile.part);
    		mMainView.setSelection(remberFile.part);
    	}
    	if( isAuto )
    	{
    		mMainView.enter(isAuto);
    	}
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
    	if( isResume && !isAuto )
    	{
	    	if( mMainView != null )
	    	{
	    		mMainView.onResume();
	    	}
    	}
    	
    	isResume = true;
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
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) 
	{
		
		fileInfo.part = selectItem;
		manager.updateQueryBook(fileInfo);
		Intent intent = new Intent(this,ReadTxtActivity.class);
		intent.putExtra("file", fileInfo);
		intent.putExtra("file_list", fileInfoList);
		intent.putExtra("isAuto", isAuto);
		startActivityForResult(intent, EbookConstants.REQUEST_CODE);
	} 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode) 
		{
			case EbookConstants.REQUEST_CODE:		//阅读器返回
				if( RESULT_OK == resultCode && data != null )
				{
					int next = data.getIntExtra("next", EbookConstants.TO_NEXT_PART);
					
					switch( next )
					{
						case EbookConstants.TO_NEXT_PART:	//到下一个部分
							isResume = false;
							mMainView.down(true);
							mMainView.enter(true);
							break;
						case EbookConstants.TO_NEXT_BOOK:	//到下一本书
							isResume = false;
							Intent intent = new Intent();
							intent.putExtra("next", EbookConstants.TO_NEXT_BOOK);
							setResult(RESULT_OK, intent);
							finish();
							break;
						default:
							break;
					}
				}	//阅读下一个部分
				break;
			default:
				break;
		} 	
	}	
}
