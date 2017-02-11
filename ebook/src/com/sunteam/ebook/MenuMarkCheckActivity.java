package com.sunteam.ebook;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
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
	private ArrayList<FileInfo> fileInfos;
	private boolean isDelete;
	private int position;
	private DatabaseManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_main);
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
		position = selectItem;
		if(isDelete){
			dialog();
		}else{
			FileInfo info = fileInfos.get(selectItem);
			Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
			intent.putExtra("result_flag", 3);
			intent.putExtra("line", info.line);
			intent.putExtra("part", info.part);
			intent.putExtra("start", info.startPos);
			intent.putExtra("len", info.len);
			sendBroadcast(intent);
			ScreenManager.getScreenManager().popAllActivityExceptOne();
		}
	}
	
	private void dialog(){
		 ConfirmDialog mConfirmDialog = new ConfirmDialog(this
				 , getResources().getString(R.string.ebook_dialog_delete)
				 ,getResources().getString(R.string.ebook_dialog_yes), getResources().getString(R.string.ebook_dialog_no));
		 
		mConfirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
			public void doConfirm() {
				/*FileInfo info = fileInfos.get(position);
				manager.deleteMarkFile(info,false);
				final boolean islast = position == (mMenuList.size() - 1)?true:false;
				mMenuList.remove(position);
				mMainView.updateAdapter();
				fileInfos.remove(position);
				PublicUtils.showToast(MenuMarkCheckActivity.this, getString(R.string.ebook_dialog_delete_su),new PromptListener() {
					
					@Override
					public void onComplete() {
						if(0 == mMenuList.size()){
							PublicUtils.showToast(MenuMarkCheckActivity.this, getResources().getString(R.string.ebook_menu_mark_null),true);
						}else{
							if( mMainView != null ){
								if(islast){
									mMainView.setSelection(0);
								}
					    		mMainView.onResume();
					    	}
						}
						
					}
				});*/
				mHandle.sendEmptyMessage(0);
			}
			
			@Override
			public void doCancel() {
				/*if( mMainView != null ){
		    		mMainView.onResume();
		    	}*/
				mHandle.sendEmptyMessage(1);
			}
		});
		mConfirmDialog.show();

	}

	private void doConfirmPositive() {
		FileInfo info = fileInfos.get(position);
		manager.deleteMarkFile(info,false);
		final boolean islast = position == (mMenuList.size() - 1)?true:false;
		mMenuList.remove(position);
		mMainView.updateAdapter();
		fileInfos.remove(position);
		PublicUtils.showToast(MenuMarkCheckActivity.this, getString(R.string.ebook_dialog_delete_su),new PromptListener() {
			
			@Override
			public void onComplete() {
				if(0 == mMenuList.size()){
					PublicUtils.showToast(MenuMarkCheckActivity.this, getResources().getString(R.string.ebook_menu_mark_null),true);
				}else{
					if( mMainView != null ){
						if(islast){
							mMainView.setSelection(0);
						}
			    		mMainView.onResume();
			    	}
				}
				
			}
		});
	}

	private void doConfirmNegative() {
		if( mMainView != null ){
    		mMainView.onResume();
    	}
	}

	@SuppressLint("HandlerLeak")
	Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // 确认
				doConfirmPositive();
				break;
			case 1: // 否
				doConfirmNegative();
				break;
			default:
				break;
			}
		}
	};
}
