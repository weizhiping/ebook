package com.sunteam.ebook;

import java.util.ArrayList;

import com.sunteam.ebook.adapter.MainMenuListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private ListView mLvMenu = null;
	private MainMenuListAdapter mAdapter = null;
	private ArrayList<String> mMainMenuList = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
    }
    
    private void initViews()
    {
    	mLvMenu = (ListView)this.findViewById(R.id.menu_list);
    	mLvMenu.setFocusable(false);
    	
    	mMainMenuList = new ArrayList<String>();
    	mMainMenuList.add( this.getString(R.string.main_menu_txt) );
    	mMainMenuList.add( this.getString(R.string.main_menu_daisy) );
    	mMainMenuList.add( this.getString(R.string.main_menu_word) );
    	
    	mAdapter = new MainMenuListAdapter( this, mMainMenuList );
    	mLvMenu.setAdapter(mAdapter);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_DPAD_UP:		//上
				mAdapter.up();
				return	true;
			case KeyEvent.KEYCODE_DPAD_DOWN:	//下
				mAdapter.down();
				return	true;
			case KeyEvent.KEYCODE_DPAD_CENTER:	//确定
				mAdapter.enter();
				return	true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}    
}
