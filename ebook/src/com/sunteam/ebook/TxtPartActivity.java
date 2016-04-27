package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.FileInfo;
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
	private FileInfo fileInfo;
	
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
    	int count = intent.getIntExtra("count", 0);
    	
    	mMenuList = new ArrayList<String>();
    	for( int i = 1; i <= count; i++ )
    	{
    		mMenuList.add(String.format(this.getResources().getString(R.string.txt_menu_part), i ));
    	}
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, fileInfo.name, mMenuList );
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
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_DPAD_UP:		//上
				mMainView.up();
				return	true;
			case KeyEvent.KEYCODE_DPAD_DOWN:	//下
				mMainView.down();
				return	true;
			case KeyEvent.KEYCODE_DPAD_CENTER:	//确定
			case KeyEvent.KEYCODE_ENTER:
				mMainView.enter();
				return	true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu) 
	{
		fileInfo.part = selectItem;
		Intent intent = new Intent(this,ReadTxtActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("file", fileInfo);
		intent.putExtras(bundle);
		this.startActivity(intent);
	} 
}
