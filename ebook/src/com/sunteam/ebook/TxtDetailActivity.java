package com.sunteam.ebook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.MainView;
import com.sunteam.ebook.word.WordParseUtils;

/**
 * 文档列表界面 目录浏览、我的收藏、最近使用文件
 * 
 * @author sylar
 */
public class TxtDetailActivity extends Activity implements OnEnterListener {
	private static final String TAG = "TxtDetailActivity";
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private View menuLayout;
	private PopupWindow moreWindow;
	private ArrayList<String> mMenuList = null;
	private ArrayList<FileInfo> fileInfoList = null;
	private String rootPath; // 查找文件根路径
	private DatabaseManager manager;
	private int flag;// 0为目录浏览，1为我的收藏，2为最近使用，3为目录浏览中文件
	private int flagType;
	private int storage;//0为内部存储，1为外部存储
	private int catalog;//0为txt,2为word,1为disay
	private FileInfo remberFile;//路径记忆传递
	private int position;//路径记忆位置

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
		flagType = intent.getIntExtra("flagType", 0);
		storage = intent.getIntExtra("storage", 0);
		rootPath = intent.getStringExtra("path");
		catalog = intent.getIntExtra("catalogType", 0);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		mMenuList = new ArrayList<String>();
		fileInfoList = new ArrayList<FileInfo>();
		manager = new DatabaseManager(this);

		switch (flag) 
		{
		case 0:
			mMenuList.add(getString(R.string.external_storage));
			mMenuList.add(getString(R.string.tf_storage));
			break;
		case 1:
		case 2:
			initDataFiles(flag,catalog);
			break;
		case 3:
			initFiles();
			break;
		default:
			initFiles();
			break;
		}
		int size = fileInfoList.size();
		for (int i = 0; i < size; i++) {
			FileInfo fileInfo = fileInfoList.get(i);
			mMenuList.add(fileInfo.name);
			if(null != remberFile && remberFile.path.contains(fileInfo.path)){
				if(remberFile.flag == flag){
					position = i;
				}else if(0 == remberFile.flag){
					position = i;
				}
			}
		}

		mFlContainer = (FrameLayout) this.findViewById(R.id.fl_container);
		mMainView = new MainView(this, this, name, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		if (flag == 1 || flag == 2) {
		//	initPopu();
		}
		if(null != remberFile){
			if(flag == 0){
				mMainView.setSelection(storage);
			}else{
				mMainView.setSelection(position);
			}
		}
	}

	private void initPopu() {
		menuLayout = LayoutInflater.from(this).inflate(R.layout.activity_main,
				null);
		ArrayList<String> menuList = new ArrayList<String>();
		menuList.add(getString(R.string.menu_delete_current));
		menuList.add(getString(R.string.menu_delete_list));
		if (2 == flag) {
			menuList.add(getString(R.string.menu_add_fav));
		}
		MainView menuView = new MainView(this, this,
				getString(R.string.menu_function), menuList);
		FrameLayout menuContainer = (FrameLayout) this
				.findViewById(R.id.fl_container);
		menuContainer.removeAllViews();
		menuContainer.addView(menuView.getView());
		moreWindow = new PopupWindow(menuLayout, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		// moreWindow.showAtLocation(menuLayout, Gravity.CENTER, 0, 0);
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
	public void onEnterCompleted(int selectItem, String menu) {
		if(flag == 0){
			String path;
			if(0 == selectItem){
				path = FileOperateUtils.getSDPath();
			}else{
				path = FileOperateUtils.getTFDirectory();
			}
			if(null != path){
				Intent intent = new Intent(this, TxtDetailActivity.class);
				intent.putExtra("path", path);
				intent.putExtra("name", mMenuList.get(selectItem));
				intent.putExtra("flag", 3);
				intent.putExtra("flagType", flagType);
				intent.putExtra("catalogType", catalog);
				intent.putExtra("storage", selectItem);
				intent.putExtra("file", remberFile);
				this.startActivity(intent);
			}else{
				TTSUtils.getInstance().speak(this.getString(R.string.tf_does_not_exist));
				Toast.makeText(this, this.getString(R.string.tf_does_not_exist), Toast.LENGTH_LONG).show();
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
			intent.putExtra("flagType", flagType);
			intent.putExtra("catalogType", catalog);
			intent.putExtra("file", remberFile);
			this.startActivity(intent);
		} else {
			String name = FileOperateUtils.getFileExtensions(fileInfo.name);
			if(name.equalsIgnoreCase(EbookConstants.BOOK_WORD)||
					 name.equalsIgnoreCase(EbookConstants.BOOK_WORDX)){
				new WordAsyncTask().execute(fileInfo);
			}else{
				showFiles(fileInfo, fileInfo.path);
			}
		}
	}

	// 显示文件内容
	private void showFiles(FileInfo fileInfo, final String fullpath) {
		fileInfo.flag = flagType;
		try {
			TextFileReaderUtils.getInstance().init(fullpath);
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
			Bundle bundle = new Bundle();
			bundle.putSerializable("file", fileInfo);
			intent.putExtras(bundle);
			this.startActivity(intent);
//			manager.insertBookToDb(fileInfo, 2);
		} else {
			// 根据count数量显示一个list，内容形如：第1部分 第2部分 ... 第n部分
			Intent intent = new Intent(this, TxtPartActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("file", fileInfo);
			intent.putExtras(bundle);
			intent.putExtra("count", count); // 第几部分
			this.startActivity(intent);
//			manager.insertBookToDb(fileInfo, 2);
		}
	}

	// 初始化显示文件
	private void initFiles() {
		ArrayList<File> filesList;
		if (catalog == 0) {
			filesList = FileOperateUtils.getFilesInDir(rootPath,
					EbookConstants.BOOK_TXT, EbookConstants.BOOK_TXT);
		} else {
			filesList = FileOperateUtils.getFilesInDir(rootPath,
					EbookConstants.BOOK_WORD, EbookConstants.BOOK_WORDX);
		}
		if (null != filesList) {
			FileInfo fileInfo;
			for (File f: filesList) {
				if (f.isDirectory()) {
					fileInfo = new FileInfo(f.getName(), f.getPath(), true,catalog,flagType,storage);
					fileInfoList.add(fileInfo);
				} else {
					fileInfo = new FileInfo(f.getName(), f.getPath(), false,catalog,flagType,storage);
					fileInfoList.add(fileInfo);
				}
			}
		}
	}

	// 初始化数据库文件
	private void initDataFiles(int flag,int catalog) {
		fileInfoList = manager.querybooks(flag,catalog);
	}
	/**
	 * word转换txt
	 * @author sylar
	 *
	 */
	private class WordAsyncTask extends AsyncTask<FileInfo, Void, String> {
		private FileInfo fileInfo = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			String tips = TxtDetailActivity.this.getString(R.string.word_parse_tips);
			TTSUtils.getInstance().speak(tips);
			PublicUtils.showProgress(TxtDetailActivity.this, tips); 
		}

		@Override
		protected String doInBackground(FileInfo... params) {
			fileInfo = params[0];
			String newPath = null;
			String name = FileOperateUtils.getFileExtensions(fileInfo.name);
			if(name.equalsIgnoreCase(EbookConstants.BOOK_WORD)){
				 newPath = WordParseUtils.doc2txt(fileInfo.path);
			}else if(name.equalsIgnoreCase(EbookConstants.BOOK_WORDX)){
				 newPath = WordParseUtils.docx2txt(fileInfo.path);
			}
			return	newPath;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			PublicUtils.cancelProgress();
			if( !TextUtils.isEmpty(result) )
			{
				showFiles(fileInfo, result);
			}
			else
			{
				String tips = TxtDetailActivity.this.getString(R.string.word_parse_fail);
				Toast.makeText(TxtDetailActivity.this, tips, Toast.LENGTH_SHORT).show();
				TTSUtils.getInstance().speak(tips);
			}
		}
	}
}
