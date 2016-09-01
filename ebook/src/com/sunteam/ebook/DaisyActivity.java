package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.view.MainView;

/**
 * Daisy主界面
 * 
 * @author sylar
 */
public class DaisyActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		catalog = getIntent().getIntExtra("catalogType", 0);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		initViews();
	}

	private void initViews() {
		mMenuList = new ArrayList<String>();
		fileInfoList = new ArrayList<FileInfo>();
		initFiles();
		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this,
				this.getString(R.string.main_menu_daisy), mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		if (null != remberFile) {
			mMainView.setSelection(remberFile.flag);
		}
	}

	// 初始化显示文件
	private void initFiles() {
		fileInfoList = FileOperateUtils.getDaisyInDir(catalog,null);
		if (null != fileInfoList) {
			for (FileInfo f : fileInfoList) {
				mMenuList.add(f.name);
			}
		}
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

		FileInfo file = fileInfoList.get(selectItem);
		if(file.hasDaisy){
			
		}else{
			DaisyFileReaderUtils.getInstance().init(file.diasyPath);
			ArrayList<DiasyNode> diasList = DaisyFileReaderUtils.getInstance()
					.getChildNodeList(-1);
			Intent intent = new Intent(this, DaisyDetailActivity.class);
			if (null != diasList && diasList.size() > 0) {
				intent.putExtra("diasys", diasList);
			}
			intent.putExtra("name", menu);
			intent.putExtra("catalogType", file.catalog);
			intent.putExtra("path", file.path);
			intent.putExtra("file", remberFile);
			this.startActivity(intent);
		}
	}
}
