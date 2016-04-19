package com.sunteam.ebook;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.MainView;

/**
 * 文档列表界面
 * 
 * @author sylar
 */
public class TxtDetailActivity extends Activity implements OnEnterListener {
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private String rootPath; // 查找文件根路径
	private DatabaseManager manager;
	private int flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
	}

	private void initViews() {
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		flag = intent.getIntExtra("flag", 0);
		rootPath = intent.getStringExtra("path");
		mMenuList = new ArrayList<String>();
		fileInfoList = new ArrayList<FileInfo>();
		manager = new DatabaseManager(this);

		switch (flag) // 0为目录浏览，1为我的收藏，2为最近使用
		{
		case 0:
			mMenuList.add(getString(R.string.external_storage));
			mMenuList.add(getString(R.string.tf_storage));
			// rootPath = FileOperateUtils.getSDPath() +
			// this.getString(R.string.app_name)+"/";
			// initFiles();
			break;
		case 1:
		case 2:
			initDataFiles(flag);
			break;
		case 3:
			initFiles();
			break;
		default:
			initFiles();
			break;
		}
		for (int i = 0; i < fileInfoList.size(); i++) {
			mMenuList.add(fileInfoList.get(i).name);
		}

		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, name, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
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
	public void onEnterCompleted(int selectItem, String menu) {
		if(flag == 0){
			String path;
			if(0 == selectItem){
				path = FileOperateUtils.getSDPath();
			}else{
				path = FileOperateUtils.getTFDirectory();
			}
			Log.e("txt", "-------file path-----:" + path);
			if(null != path){
				Intent intent = new Intent(this, TxtDetailActivity.class);
				intent.putExtra("path", path);
				intent.putExtra("name", mMenuList.get(selectItem));
				intent.putExtra("flag", 3);
				this.startActivity(intent);
			}else{
				
			}
			return;
		}
		// 进入到selectItem对应的界面
		FileInfo fileInfo = fileInfoList.get(selectItem);
		if (fileInfo.isFolder) {
			Intent intent = new Intent(this, TxtDetailActivity.class);
			intent.putExtra("path", fileInfo.path);
			intent.putExtra("name", fileInfo.name);
			intent.putExtra("flag", 10);
			this.startActivity(intent);
		} else {
			try {
				TextFileReaderUtils.getInstance().init(fileInfo.path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int count = TextFileReaderUtils.getInstance().getParagraphCount(); // 得到分段信息

			if (0 == count) // 文件为空
			{
				// 提示一下（语音和文字）
				TTSUtils.getInstance().speak(getString(R.string.txt_menu_null));
			} else if (1 == count) // 只有一部分
			{
				Intent intent = new Intent(this, ReadTxtActivity.class);
				intent.putExtra("name", fileInfo.name); // 路径
				intent.putExtra("part", 0); // 第几部分
				this.startActivity(intent);
				manager.insertBookToDb(fileInfo, 2);
			} else {
				// 根据count数量显示一个list，内容形如：第1部分 第2部分 ... 第n部分
				Intent intent = new Intent(this, TxtPartActivity.class);
				intent.putExtra("name", fileInfo.name); // 路径
				intent.putExtra("count", count); // 第几部分
				this.startActivity(intent);
				manager.insertBookToDb(fileInfo, 2);
			}
		}
	}

	// 初始化显示文件
	private void initFiles() {
		ArrayList<File> filesList = FileOperateUtils.getFilesInDir(rootPath);
		if (null != filesList) {
			FileInfo fileInfo;
			for (File f : filesList) {
				if (f.isDirectory()) {
					fileInfo = new FileInfo(f.getName(), f.getPath(), true);
					fileInfoList.add(fileInfo);
				} else {
					fileInfo = new FileInfo(f.getName(), f.getPath(), false);
					fileInfoList.add(fileInfo);
				}
			}
		}
	}

	// 初始化数据库文件
	private void initDataFiles(int flag) {
		fileInfoList = manager.querybooks(flag);
	}
}
