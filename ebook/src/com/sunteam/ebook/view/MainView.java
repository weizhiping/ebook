package com.sunteam.ebook.view;

import java.util.ArrayList;

import com.sunteam.ebook.R;
import com.sunteam.ebook.adapter.MainListAdapter;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;

import android.content.Context;
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
	
	public MainView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList )
	{
		super(context);
		
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
    	mAdapter = new MainListAdapter( mContext, listener, menuList );
    	mLvMenu.setAdapter(mAdapter);
    	mLvMenu.setFocusable(false);	//不获取焦点
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
		mAdapter.enter();
	}
}