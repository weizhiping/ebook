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
 * txt与word主界面
 *
 * @author sylar
 */
public class TxtActivity extends Activity implements OnEnterListener
{
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	public static boolean isTxt;//true为txt文档，false为word文档
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isTxt = getIntent().getBooleanExtra("isTxt", true);
		initViews();
	}
	
	private void initViews()
    {
    	mMenuList = new ArrayList<String>();
    	mMenuList.add( this.getString(R.string.txt_menu_catalog) );
    	mMenuList.add( this.getString(R.string.txt_menu_fav) );
    	mMenuList.add( this.getString(R.string.txt_menu_recent) );
    	
    	String title = null;
    	if( isTxt )
    	{
    		title = this.getString(R.string.main_menu_txt);
    	}
    	else
    	{
    		title = this.getString(R.string.main_menu_word);
    	}
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, title, mMenuList );
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
		// TODO Auto-generated method stub
		String name = menu;
		Intent intent = new Intent(this,TxtDetailActivity.class);
		intent.putExtra("name", name);
		intent.putExtra("flag", selectItem);
		this.startActivity(intent);
	} 
}
