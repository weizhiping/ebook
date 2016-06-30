package com.sunteam.ebook.view;

import java.util.ArrayList;

import com.sunteam.ebook.R;
import com.sunteam.ebook.adapter.MainListAdapter;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.TTSSpeakMode;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


/**
 * 主要界面公用View
 * 
 * @author wzp
 *
 */

public class MainView extends View
{
	private static final String TAG = "MainView";
	
	private Context mContext = null;
	private View mView = null;
	private TextView mTvTitle = null;
	private View mLine = null;
	private ListView mLvMenu = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private MainListAdapter mAdapter = null;
	
	public View getView()
	{
		return	mView;
	}
	
	private void initView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList, TTSSpeakMode mode )
	{
		mContext = context;
		mView = LayoutInflater.from(context).inflate( R.layout.view_main, null );
		
		mTvTitle = (TextView)mView.findViewById(R.id.title);		//标题栏
    	mLine = (View)mView.findViewById(R.id.line);				//分割线
    	mLvMenu = (ListView)mView.findViewById(R.id.menu_list);		//listview
    	
    	mColorSchemeIndex = PublicUtils.getColorSchemeIndex();		//得到系统配色索引
    	
    	mView.setBackgroundResource(EbookConstants.ViewBkColorID[mColorSchemeIndex]);							//设置View的背景色
    	mTvTitle.setTextColor(mContext.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));	//设置title的背景色
    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);								//设置分割线的背景色
    	
    	mTvTitle.setText(title);
    	mAdapter = new MainListAdapter( mContext, listener, menuList, mode );
    	mLvMenu.setAdapter(mAdapter);
    	mLvMenu.setFocusable(false);	//不获取焦点
	}
	
	public MainView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList, TTSSpeakMode mode )
	{
		super(context);
		
		initView( context, listener, title, menuList, mode );
	}
	
	public MainView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList)
	{
		super(context);
		
		initView( context, listener, title, menuList, TTSSpeakMode.READ_MODE_NORMAL );
	}
	
	public void setSelection(int position){
		mAdapter.setSelectItem(position);
	}
	
	public void onResume()
	{
		if( mAdapter != null )
		{
			TTSUtils.getInstance().speakTips(mAdapter.getSelectItemContent());
		}
	}
	
	public void up()
	{
		mAdapter.up();
	}
	
	public void down()
	{
		mAdapter.down();
	}
	
	public void enter()
	{
		mAdapter.enter(false);
	}

	public void enter( boolean isAuto )
	{
		mAdapter.enter(isAuto);
	}
	
	public void updateAdapter()
	{
		mAdapter.notifyDataSetChanged();
	}
	
	public boolean isDown()
	{
		return	mAdapter.isDown();	
	}
	
	public String getCurItem()
	{
		return	mAdapter.getCurItem();
	}
	
	public int getSelectItem()
	{
		return	mAdapter.getSelectItem();
	}
}