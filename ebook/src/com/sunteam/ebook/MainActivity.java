package com.sunteam.ebook;

import java.util.ArrayList;

import com.sunteam.ebook.adapter.MainMenuListAdapter;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 主界面
 * 
 * @author wzp
 *
 */
public class MainActivity extends Activity 
{
	private TextView mTvTitle = null;
	private View mLine = null;
	private ListView mLvMenu = null;
	private MainMenuListAdapter mAdapter = null;
	private ArrayList<String> mMainMenuList = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
    }
    
    private void initViews()
    {
    	//此处需要从系统配置文件中得到配色方案索引
    	PublicUtils.setColorSchemeIndex(mColorSchemeIndex);
    	
    	this.getWindow().setBackgroundDrawableResource(EbookConstants.ViewBkDrawable[mColorSchemeIndex]);
    	mTvTitle = (TextView)this.findViewById(R.id.main_title);
    	mLine = (View)this.findViewById(R.id.line);
    	mLvMenu = (ListView)this.findViewById(R.id.menu_list);
    	
    	mTvTitle.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
    	mLvMenu.setFocusable(false);	//不让控件获得焦点，让主界面进行按键分发
    	
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
