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
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.SuperDialog;
import com.sunteam.ebook.util.SuperDialog.DialogCallBack;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 查看书签界面
 * 
 * @author sylar
 */
public class MenuMarkCheckActivity extends Activity implements OnEnterListener,DialogCallBack {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfos;
	private boolean isDelete;
	private int position;
	private DatabaseManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		fileInfos =  (ArrayList<FileInfo>) getIntent().getSerializableExtra("fileinfos");
		isDelete = getIntent().getBooleanExtra("isdelete", false);
		manager = new DatabaseManager(this);
		initViews();
	}

	private void initViews() {
	//	fileInfos = manager.queryMarks(fileInfo.path);
		int size = fileInfos.size();
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			mMenuList.add(fileInfos.get(i).name);
		}
		String title = this.getString(R.string.menu_function);
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
		position = selectItem;
		if(isDelete){
			SuperDialog dialog = new SuperDialog(this);
			dialog.showSuperDialog(R.string.dialog_delete);
			dialog.initeCallBack(this);
			TTSUtils.getInstance().speakTips(getString(R.string.dialog_delete)
					+"，" + getString(R.string.dialog_yes) + "，" +  getString(R.string.dialog_no));
		}else{
			FileInfo info = fileInfos.get(selectItem);
			Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
			intent.putExtra("result_flag", 3);
			intent.putExtra("line", info.line);
			intent.putExtra("part", info.part);
			sendBroadcast(intent);
			ScreenManager.getScreenManager().popAllActivityExceptOne();
		}
	}

	@Override
	public void dialogConfrim() {
		TTSUtils.getInstance().speakTips(getString(R.string.dialog_delete_su));
		FileInfo info = fileInfos.get(position);
		mMenuList.remove(position);
		mMainView.updateAdapter();
		manager.deleteMarkFile(info.path, info.name);
	//	ScreenManager.getScreenManager().popAllActivityExceptOne();
	}
}
