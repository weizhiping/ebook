package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
    }
    
    private void initViews()
    {
    	TTSUtils.getInstance().init(getApplicationContext());	//初始化TTS
    	
    	//此处需要从系统配置文件中得到配色方案索引
    	PublicUtils.setColorSchemeIndex(0);
    	
    	mMenuList = new ArrayList<String>();
    	mMenuList.add( this.getString(R.string.main_menu_txt) );
    	mMenuList.add( this.getString(R.string.main_menu_daisy) );
    	mMenuList.add( this.getString(R.string.main_menu_word) );
    	
    	mFlContainer = (FrameLayout)this.findViewById(R.id.fl_container);
    	mMainView = new MainView( this, this, this.getString(R.string.main_title), mMenuList );
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
		Intent intent;
		switch( selectItem )
		{
			case 0:
				intent = new Intent(this,TxtActivity.class);
				intent.putExtra("isTxt", true);
				break;
			case 1:
				intent = new Intent(this,TxtActivity.class);
				intent.putExtra("isTxt", false);
				break;
			case 2:
				intent = new Intent(this,DaisyActivity.class);
				break;
			default:
				return;
		}
		this.startActivity(intent);
	}
}
