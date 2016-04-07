package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sunteam.ebook.adapter.TxtDetailListAdapter;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
/**
 * 文档列表界面
 * @author sylar
 *
 */
public class TxtDetailActivity extends Activity {
	private ListView mLvMenu = null;
	private TxtDetailListAdapter mAdapter = null;
	private ArrayList<String> mMainMenuList = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private int flag;//0为目录浏览，1为我的收藏，2为最近使用
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txt);
		flag = getIntent().getIntExtra("flag", 0);
	     initViews();
	    }
	    
	    private void initViews()
	    {
	    	PublicUtils.setColorSchemeIndex(mColorSchemeIndex);
	    	TextView mTvTitle = (TextView)this.findViewById(R.id.txt_title);
	    	mLvMenu = (ListView)this.findViewById(R.id.txt_list);
	    	View mLine = (View)this.findViewById(R.id.line);
	    	if(flag == 0){
	    		mTvTitle.setText(R.string.txt_menu_catalog);
	    	}else if(flag == 1){
	    		mTvTitle.setText(R.string.txt_menu_fav);
	    	}else{
	    		mTvTitle.setText(R.string.txt_menu_recent);
	    	}
	    	mTvTitle.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
	    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
	    	mLvMenu.setFocusable(false);	//不让控件获得焦点，让主界面进行按键分发
	    	
	    	mMainMenuList = new ArrayList<String>();
	    	mMainMenuList.add( this.getString(R.string.app_name) );
	    	mMainMenuList.add( this.getString(R.string.app_name) );
	    	mMainMenuList.add( this.getString(R.string.app_name) );
	    	
	    	mAdapter = new TxtDetailListAdapter( this, mMainMenuList);
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
