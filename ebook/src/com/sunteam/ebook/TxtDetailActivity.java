package com.sunteam.ebook;

import java.io.File;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sunteam.ebook.adapter.TxtDetailListAdapter;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.PublicUtils;

/**
 * 文档列表界面
 * 
 * @author sylar
 */
public class TxtDetailActivity extends Activity 
{
	private ListView mLvMenu = null;
	private TxtDetailListAdapter mAdapter = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private String rootPath;//查找文件根路径
	private DatabaseManager manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txt);
	     initViews();
	    }
	    
	    private void initViews()
	    {
	    	PublicUtils.setColorSchemeIndex(mColorSchemeIndex);
	    	this.getWindow().setBackgroundDrawableResource(EbookConstants.ViewBkDrawable[mColorSchemeIndex]);
	    	Intent intent = getIntent();
	    	String name = intent.getStringExtra("name");
	    	int flag = intent.getIntExtra("flag", 0);
	    	TextView mTvTitle = (TextView)this.findViewById(R.id.txt_title);
	    	mLvMenu = (ListView)this.findViewById(R.id.txt_list);
	    	View mLine = (View)this.findViewById(R.id.line);
	    	mTvTitle.setText(name);
	    	mTvTitle.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
	    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
	    	mLvMenu.setFocusable(false);	//不让控件获得焦点，让主界面进行按键分发
	    	fileInfoList = new ArrayList<FileInfo>();
	    	
	    	manager = new DatabaseManager(this);
	    	if(flag == 0 ){//0为目录浏览，1为我的收藏，2为最近使用
	    		rootPath = FileOperateUtils.getSDPath() + this.getString(R.string.app_name)+"/";
	    		initFiles();
	    	}else if(flag == 1 || flag == 2){
	    		initDataFiles(flag);
	    	}else{
	    		rootPath = intent.getStringExtra("path");
	    		initFiles();
	    	}
	    	mAdapter = new TxtDetailListAdapter( this, fileInfoList);
	    	mLvMenu.setAdapter(mAdapter);
	    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_DPAD_UP:		//上
				mAdapter.up();
				return	true;
			case KeyEvent.KEYCODE_DPAD_DOWN:	//下 
				mAdapter.down();
				return	true;
			case KeyEvent.KEYCODE_DPAD_CENTER:	//确定
				mAdapter.enter();
				return	true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}   
	//初始化显示文件
	private void initFiles(){
		ArrayList<File> filesList = FileOperateUtils.getFilesInDir(rootPath);
		if(null != filesList){
			FileInfo fileInfo;
			for (File f : filesList) {
				if (f.isDirectory()) {
					fileInfo = new FileInfo(f.getName(),f.getPath(),true);
					fileInfoList.add(fileInfo);
				} else{
					fileInfo = new FileInfo(f.getName(),f.getPath(),false);
					fileInfoList.add(fileInfo);
				}
			}
		}
	}
	//初始化数据库文件
	private void initDataFiles(int flag){
		fileInfoList = manager.querybooks(flag);
		
	}
}
