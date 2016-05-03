package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 主界面
 * 
 * @author wzp
 *
 */
public class MainActivity extends Activity implements OnEnterListener
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
        getRecentInfo();
        initViews();
    }
    
    private void initViews()
    {
    	PublicUtils.setColorSchemeIndex(PublicUtils.getSysColorSchemeIndex());	//设置系统配色方案
    	
    	mMenuList = new ArrayList<String>();
    	mMenuList.add( this.getString(R.string.main_menu_txt) );
    	mMenuList.add( this.getString(R.string.main_menu_daisy) );
    	mMenuList.add( this.getString(R.string.main_menu_word) );
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, this.getString(R.string.main_title), mMenuList);
    	mFlContainer.removeAllViews();
    	mFlContainer.addView(mMainView.getView());
    	if(null != fileInfo){
    		mMainView.setSelection(fileInfo.catalog);
    	}
    }
    //获取最近一次使用的文件
    private void getRecentInfo(){
    	DatabaseManager manager = new DatabaseManager(this);
    	fileInfo = manager.queryLastBook(EbookConstants.BOOK_RECENT);
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
		switch( selectItem )
		{//0为txt文档，2为word文档,1为disay
			case 0:
				initToActivity(TxtActivity.class,0);
				break;
			case 1:
				initToActivity(DaisyActivity.class,1);
				break;
			case 2:
				initToActivity(TxtActivity.class,2);
				break;
			default:
				return;
		}
	}
	
	private void initToActivity(Class activity,int catalog){
		Intent intent = new Intent(this,activity);	
		intent.putExtra("catalogType", catalog);
		Bundle bundle = new Bundle();
		bundle.putSerializable("file", fileInfo);
		intent.putExtras(bundle);
		this.startActivity(intent);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		TTSUtils.getInstance().destroy();
	}
}
