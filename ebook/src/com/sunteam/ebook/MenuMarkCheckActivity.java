package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.view.MainView;

/**
 * 查看书签界面
 * 
 * @author sylar
 */
public class MenuMarkCheckActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private FileInfo fileInfo;
	private boolean isDelete;
	private DatabaseManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("fileinfo");
		isDelete = getIntent().getBooleanExtra("isdelete", false);
		manager = new DatabaseManager(this);
		initViews();
	}

	private void initViews() {
		Resources res = getResources();
		
		String[] menus = res.getStringArray(R.array.array_menu_mark);
		int length = menus.length;
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
		String title = this.getString(R.string.menu_function);
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回

			break;
		case KeyEvent.KEYCODE_DPAD_UP: // 上
			mMainView.up();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN: // 下
			mMainView.down();
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
			mMainView.enter();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		if(isDelete){
			
		}else{
			
		}
	}
}
