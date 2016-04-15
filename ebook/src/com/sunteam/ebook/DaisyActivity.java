package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.view.MainView;

/**
 * Daisy主界面
 * 
 * @author sylar
 */
public class DaisyActivity extends Activity implements OnEnterListener
{
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initViews();
	}
	
	private void initViews()
    {
    	mMenuList = new ArrayList<String>();
    	mMenuList.add( this.getString(R.string.main_menu_daisy) );
    	mMenuList.add( this.getString(R.string.main_menu_daisy) );
    	mMenuList.add( this.getString(R.string.main_menu_daisy) );
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, this.getString(R.string.main_menu_daisy), mMenuList );
    	mFlContainer.removeAllViews();
    	mFlContainer.addView(mMainView.getView());
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
		// TODO Auto-generated method stub
		String name = menu;
		Intent intent = new Intent(this,TxtDetailActivity.class);
		intent.putExtra("name", name);
		intent.putExtra("flag", selectItem);
		this.startActivity(intent);
	}  
}
