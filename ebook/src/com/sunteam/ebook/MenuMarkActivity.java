package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 书签管理界面
 * 
 * @author sylar
 */
public class MenuMarkActivity extends Activity implements OnEnterListener {
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
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
		ScreenManager.getScreenManager().pushActivity(this);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		currentText = getIntent().getStringExtra("page_text");
		currentPage = getIntent().getIntExtra("page_cur", 1);
		manager = new DatabaseManager(this);
		initViews();
	}

	private void initViews() {
		Resources res = getResources();
		
		String[] menus = res.getStringArray(R.array.ebook_array_menu_mark);
		int length = menus.length;
		mMenuList = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			mMenuList.add(menus[i]);
		}
		String title = getIntent().getStringExtra("title");
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
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
		String title =  mMenuList.get(selectItem);
		switch (selectItem) {
		case 0:
			Intent intente = new Intent(this, MenuTextEditActivity.class);
			intente.putExtra("fileinfo", fileInfo);
			intente.putExtra("page_text", currentText);
			intente.putExtra("page_cur", currentPage);
			intente.putExtra("edit_name",title);
			startActivity(intente);
			break;
		case 1:
			wantToCheckActivity(false,title);
			break;
		case 2:
			wantToCheckActivity(true,title);
			break;
		case 3:
			ArrayList<FileInfo> fileInfos = manager.queryMarks(fileInfo);
			if(0 < fileInfos.size()){
				dialog();
			}else{
				PublicUtils.showToast(this, getResources().getString(R.string.ebook_menu_mark_tips),true);
			}
			
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	private void wantToCheckActivity(boolean isDelete,String title){
		ArrayList<FileInfo> fileInfos = manager.queryMarks(fileInfo);
//		Log.e("mark", "-----file size--:" + fileInfos.size());
		if(null != fileInfos && 0 < fileInfos.size()){
			Intent intent = new Intent(this, MenuMarkCheckActivity.class);
			intent.putExtra("fileinfos", fileInfos);
			intent.putExtra("isdelete", isDelete);
			intent.putExtra("title", title);
			startActivity(intent);
		}else{
			PublicUtils.showToast(this, getString(R.string.ebook_menu_mark_tips),true);
			//ScreenManager.getScreenManager().popAllActivityExceptOne();
		}
	}
	
	private void dialog(){
		 ConfirmDialog mConfirmDialog = new ConfirmDialog(this
				 , getResources().getString(R.string.ebook_dialog_clear)
				 ,getResources().getString(R.string.ebook_dialog_yes), getResources().getString(R.string.ebook_dialog_no));
		 
		mConfirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
			public void doConfirm() {
				manager.deleteMarkFile(fileInfo, true);
				PublicUtils.showToast(MenuMarkActivity.this, getString(R.string.ebook_dialog_clear_su),true);
			}
			
			@Override
			public void doCancel() {
				if( mMainView != null ){
		    		mMainView.onResume();
		    	}
			}
		});
		mConfirmDialog.show();

	}
}
