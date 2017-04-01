package com.sunteam.ebook;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.CustomToast;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;
import com.sunteam.ebook.word.WordParseUtils;

/**
 * 主界面
 * 
 * @author wzp
 *
 */
public class MainActivity extends Activity implements OnEnterListener
{
	private FrameLayout mFlContainer = null;//布局Layout
	private MainView mMainView = null;//显示数据的View
	private ArrayList<String> mMenuList = null;//数据源
	private FileInfo remberFile;//路径记忆实体
	private DatabaseManager manager;//数据库操作类
	private UpdateRemFileReceiver fileReceiver;//更新路径记忆实体类广播
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
        setContentView(R.layout.ebook_activity_main);
        
        TTSUtils.getInstance().init(this);	//初始化TTS
        MediaPlayerUtils.getInstance().init();	//初始化MediaPlayer
		if( !PublicUtils.checkSpeechServiceInstalled(this) ) 
		{			
			CustomToast.showToast(this, this.getString(R.string.ebook_install_tts_tips), Toast.LENGTH_LONG);
			finish();
			return;
		}
		manager = new DatabaseManager(this);
        getRecentInfo();
        initViews();
        registerReceiver();
    }
    
    private void initViews()
    {
    	PublicUtils.setColorSchemeIndex(PublicUtils.getSysColorSchemeIndex());	//设置系统配色方案
    	
    	mMenuList = new ArrayList<String>();
    	mMenuList.add( this.getString(R.string.ebook_main_menu_txt) );
    	mMenuList.add( this.getString(R.string.ebook_main_menu_daisy) );
    	mMenuList.add( this.getString(R.string.ebook_main_menu_word) );
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.ebook_fl_container);
    	mMainView = new MainView( this, this, this.getString(R.string.ebook_main_title), mMenuList);
    	mFlContainer.removeAllViews();
    	mFlContainer.addView(mMainView.getView());
    	if(null != remberFile){
    		mMainView.setSelection(remberFile.catalog);
    	}
    	
    	TTSUtils.getInstance().setInitIsSuccess(true);	//设置TTS初始化成功
    }
    //获取最近一次使用的文件
    private void getRecentInfo(){
    	remberFile = manager.queryLastBook(EbookConstants.BOOK_RECENT);
    	File dir = new File(FileOperateUtils.getMusicPath());
		if (!dir.exists() && !dir.isDirectory()){
			dir.mkdirs();
		}
    }
    
    private void registerReceiver(){
		fileReceiver = new UpdateRemFileReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.ACTION_UPDATE_FILE);
		registerReceiver(fileReceiver, filter);
	}
    
    @Override
    public void onPause()
    {
    	if( mMainView != null )
    	{
    		mMainView.onPause();
    	}
    	super.onPause();
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
		return mMainView.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		return	mMainView.onKeyUp(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) 
	{
		switch( selectItem )
		{//0为txt文档，2为word文档,1为disay
			case 0:
				initToActivity(TxtActivity.class,0);
				break;
			case 1:
				initToActivity(TxtActivity.class,1);
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
		if(null != remberFile && catalog == remberFile.catalog){
			bundle.putSerializable("file", remberFile);
		}
		intent.putExtras(bundle);
		this.startActivity(intent);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		WordParseUtils.closeMemoryFile();	//关闭内存文件
		TTSUtils.getInstance().destroy();
		MediaPlayerUtils.getInstance().destroy();
		unregisterReceiver(fileReceiver);
		
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}	
	
	private class UpdateRemFileReceiver extends BroadcastReceiver { 
	      
	    @Override  
	    public void onReceive(Context context, Intent intent) {    
	        if (intent.getAction().equals(EbookConstants.ACTION_UPDATE_FILE)) {  
	        	remberFile = manager.queryLastBook(EbookConstants.BOOK_RECENT);
	        }  
	    } 
	}
}
