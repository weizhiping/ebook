package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
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
	private boolean isAuto = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initViews();
	}
	
	private void initViews()
    {
		Intent intent = getIntent();
		fileInfo = (FileInfo) intent.getSerializableExtra("file");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
    	int count = intent.getIntExtra("count", 0);
    	isAuto = intent.getBooleanExtra("isAuto", false);
    	
    	mMenuList = new ArrayList<String>();
    	for( int i = 1; i <= count; i++ )
    	{
    		mMenuList.add(String.format(this.getResources().getString(R.string.txt_menu_part), i ));
    	}
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, fileInfo.name, mMenuList );
    	mFlContainer.removeAllViews();
    	mFlContainer.addView(mMainView.getView());
    	
    	if( isAuto )
    	{
    		mMainView.enter();
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
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) 
	{
		fileInfo.part = selectItem;
		Intent intent = new Intent(this,ReadTxtActivity.class);
		intent.putExtra("file", fileInfo);
		intent.putExtra("file_list", fileInfoList);
		startActivityForResult(intent, EbookConstants.REQUEST_CODE);
	} 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode) 
		{
			case EbookConstants.REQUEST_CODE:		//阅读器返回
				if( RESULT_OK == resultCode )
				{
					int next = data.getIntExtra("next", EbookConstants.TO_NEXT_PART);
					
					switch( next )
					{
						case EbookConstants.TO_NEXT_PART:	//到下一个部分
							mMainView.down();
							mMainView.enter();
							break;
						case EbookConstants.TO_NEXT_BOOK:	//到下一本书
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
