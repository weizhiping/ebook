package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sunteam.ebook.adapter.TxtMenuListAdapter;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
/**
 * txt与word主界面
 * @author sylar
 *D:\Program Files\TortoiseGit\bin\TortoiseGitPlink.exe
 */
public class TxtActivity extends Activity {
	private ListView mLvMenu = null;
	private TxtMenuListAdapter mAdapter = null;
	private ArrayList<String> mMainMenuList = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private boolean isTxt;//true为txt文档，false为word文档
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txt);
		isTxt = getIntent().getBooleanExtra("isTxt", true);
	     initViews();
	    }
	    
	    private void initViews()
	    {
	    	PublicUtils.setColorSchemeIndex(mColorSchemeIndex);
	    	this.getWindow().setBackgroundDrawableResource(EbookConstants.ViewBkDrawable[mColorSchemeIndex]);
	    	TextView mTvTitle = (TextView)this.findViewById(R.id.txt_title);
	    	mLvMenu = (ListView)this.findViewById(R.id.txt_list);
	    	View mLine = (View)this.findViewById(R.id.line);
	    	if(!isTxt){
	    		mTvTitle.setText(R.string.main_menu_word);
	    	}
	    	mTvTitle.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
	    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
	    	mLvMenu.setFocusable(false);	//不让控件获得焦点，让主界面进行按键分发
	    	
	    	mMainMenuList = new ArrayList<String>();
	    	mMainMenuList.add( this.getString(R.string.txt_menu_catalog) );
	    	mMainMenuList.add( this.getString(R.string.txt_menu_fav) );
	    	mMainMenuList.add( this.getString(R.string.txt_menu_recent) );
	    	
	    	mAdapter = new TxtMenuListAdapter( this, mMainMenuList);
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
