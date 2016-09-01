package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.SuperDialog;
import com.sunteam.ebook.util.SuperDialog.DialogCallBack;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 书签管理界面
 * 
 * @author sylar
 */
public class MenuMarkActivity extends Activity implements OnEnterListener,DialogCallBack {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private FileInfo fileInfo;
	private String currentText;
	private int currentPage;
	private DatabaseManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		currentText = getIntent().getStringExtra("page_text");
		currentPage = getIntent().getIntExtra("page_cur", 1);
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
		String title = getIntent().getStringExtra("title");
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, title, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
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
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		switch (selectItem) {
		case 0:
			Intent intente = new Intent(this, MenuTextEditActivity.class);
			intente.putExtra("fileinfo", fileInfo);
			intente.putExtra("page_text", currentText);
			intente.putExtra("page_cur", currentPage);
			intente.putExtra("edit_name", mMenuList.get(0));
			startActivity(intente);
			break;
		case 1:
			wantToCheckActivity(false);
			break;
		case 2:
			wantToCheckActivity(true);
			break;
		case 3:
			SuperDialog dialog = new SuperDialog(this);
			dialog.showSuperDialog(R.string.dialog_clear);
			dialog.initeCallBack(this);
			TTSUtils.getInstance().speakTips(getString(R.string.dialog_clear)
					+"，" + getString(R.string.dialog_yes) + "，" +  getString(R.string.dialog_no));
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	private void wantToCheckActivity(boolean isDelete){
		ArrayList<FileInfo> fileInfos = manager.queryMarks(fileInfo.path);
		if(0 < fileInfos.size()){
			Intent intent = new Intent(this, MenuMarkCheckActivity.class);
			intent.putExtra("fileinfos", fileInfos);
			intent.putExtra("isdelete", isDelete);
			startActivity(intent);
		}else{
			PublicUtils.showToast(this, getString(R.string.menu_mark_tips));
			ScreenManager.getScreenManager().popAllActivityExceptOne();
		}
	}

	@Override
	public void dialogConfrim() {
		TTSUtils.getInstance().speakTips(getString(R.string.dialog_clear_su));
		manager.deleteMarkFile(fileInfo.path, null);
		ScreenManager.getScreenManager().popAllActivityExceptOne();
	}
}
