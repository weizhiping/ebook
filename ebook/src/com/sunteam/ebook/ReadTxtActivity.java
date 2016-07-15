package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.TextReaderView;
import com.sunteam.ebook.view.TextReaderView.OnPageFlingListener;

/**
 * TXT文件显示
 * 
 * @author sylar
 */
public class ReadTxtActivity extends Activity implements OnPageFlingListener 
{
	private TextView mTvTitle = null;
	private TextView mTvPageCount = null;
	private TextView mTvCurPage = null;
	private View mLine = null;
	private TextReaderView mTextReaderView = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private FileInfo fileInfo;
	private ArrayList<FileInfo> fileInfoList = null;
	private static final int MENU_CODE = 10;
	private MenuBroadcastReceiver menuReceiver;
	private SharedPreferences shared;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,Context.MODE_PRIVATE);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
		int part = fileInfo.part;
		mColorSchemeIndex = PublicUtils.getColorSchemeIndex();
    	this.getWindow().setBackgroundDrawableResource(EbookConstants.ViewBkDrawable[mColorSchemeIndex]);
    	mTvTitle = (TextView)this.findViewById(R.id.main_title);
    	mTvPageCount = (TextView)this.findViewById(R.id.pageCount);
    	mTvCurPage = (TextView)this.findViewById(R.id.curPage);
    	mLine = (View)this.findViewById(R.id.line);
    	
    	mTvTitle.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mTvPageCount.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mTvCurPage.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mLine.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
    	
    	mTvTitle.setText(fileInfo.name);
				
    	mTextReaderView = (TextReaderView) findViewById(R.id.read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mTextReaderView.setReverseColor(this.getResources().getColor(EbookConstants.SelectBkColorID[mColorSchemeIndex]));
    	mTextReaderView.setBackgroundColor(this.getResources().getColor(EbookConstants.ViewBkColorID[mColorSchemeIndex]));
    	if( mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part), TextFileReaderUtils.getInstance().getCharsetName(), fileInfo.line, fileInfo.startPos, fileInfo.len, fileInfo.checksum) == false )
    	{
    		Toast.makeText(this, this.getString(R.string.checksum_error), Toast.LENGTH_SHORT).show();
    		back();
    	}
    	registerReceiver();
    	playMusic();
	}
	
	private void registerReceiver(){
		menuReceiver = new MenuBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.MENU_PAGE_EDIT);
		registerReceiver(menuReceiver, filter);
	}
	
	private void playMusic(){
		boolean isMusic = shared.getBoolean(EbookConstants.MUSICE_STATE, false);
		if(isMusic){
			String path = shared.getString(EbookConstants.MUSICE_PATH, null);
			if(null == path){
				path = FileOperateUtils.getFirstMusicInDir();
			}
			if(null != path){
				MediaPlayerUtils.getInstance().play(path);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_DPAD_UP:		//上
				mTextReaderView.up();
				return	true;
			case KeyEvent.KEYCODE_DPAD_DOWN:	//下
				mTextReaderView.down();
				return	true;
			case KeyEvent.KEYCODE_DPAD_LEFT:	//左
				mTextReaderView.left();
				return	true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:	//右
				mTextReaderView.right();
				return	true;
			case KeyEvent.KEYCODE_DPAD_CENTER:	//确定
			case KeyEvent.KEYCODE_ENTER:
				mTextReaderView.enter();
				return	true;
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_NUMPAD_5:		//精读
				mTextReaderView.intensiveReading();
				return	true;
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_NUMPAD_7:		//朗读上一个字
				mTextReaderView.preCharacter();
				return	true;
			case KeyEvent.KEYCODE_9:
			case KeyEvent.KEYCODE_NUMPAD_9:		//朗读下一个字
				mTextReaderView.nextCharacter(false);
				return	true;
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_NUMPAD_4:		//朗读上一个词
				mTextReaderView.preWord();
				return	true;
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_NUMPAD_6:		//朗读下一个词
				mTextReaderView.nextWord(false);
				return	true;
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_NUMPAD_2:		//朗读上一个段落
				mTextReaderView.preParagraph();
				return	true;
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_NUMPAD_8:		//朗读下一个段落
				mTextReaderView.nextParagraph(false);
				return	true;
			case KeyEvent.KEYCODE_1:
			case KeyEvent.KEYCODE_NUMPAD_1:		//开始选词
				mTextReaderView.startSelect();
				return	true;
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_NUMPAD_3:		//结束选词
				mTextReaderView.endSelect();
				return	true;
			case KeyEvent.KEYCODE_0:
			case KeyEvent.KEYCODE_NUMPAD_0:		//百科查询
				String content = mTextReaderView.getReverseText();	//得到当前反显内容
				if( !TextUtils.isEmpty(content) )
				{
					Toast.makeText(this, this.getString(R.string.baike_search_fail), Toast.LENGTH_SHORT).show();
				}
				return	true;
			case KeyEvent.KEYCODE_MENU:
				Intent intent = new Intent(this, MenuActivity.class);
				intent.putExtra("page_count", mTextReaderView.getPageCount());
				intent.putExtra("page_cur", mTextReaderView.getCurPage());
				intent.putExtra("page_text", mTextReaderView.getReverseText());
				intent.putExtra("fileinfo", fileInfo);
				startActivityForResult(intent, MENU_CODE);
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	//插入到最近浏览
	private void insertToDb(){
		fileInfo.line = mTextReaderView.getLineNumber();
		fileInfo.checksum = mTextReaderView.getCheckSum();
		fileInfo.startPos = mTextReaderView.getReverseInfo().startPos;
		fileInfo.len = mTextReaderView.getReverseInfo().len;
		DatabaseManager manager = new DatabaseManager(this);
		manager.insertBookToDb(fileInfo, EbookConstants.BOOK_RECENT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(RESULT_OK == resultCode){
			if(null != data){
				int result = data.getIntExtra("result", 0);
				switch(result){
				case 10:
					int curPage = data.getIntExtra("page", 1);
					mTextReaderView.setCurPage(curPage);
					break;
				}
			}
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onPageFlingToTop() 
	{
		// TODO Auto-generated method stub
		String tips = this.getString(R.string.to_top);
		PublicUtils.showToast(this, tips);
	}

	@Override
	public void onPageFlingToBottom() 
	{
		// TODO Auto-generated method stub
		if( fileInfo.part+1 < fileInfo.count )	//还有下一部分需要朗读
		{
			Intent intent = new Intent();
			intent.putExtra("next", EbookConstants.TO_NEXT_PART);
			setResult(RESULT_OK, intent);
			back();
		}
		else if( ( fileInfo.item+1 < fileInfoList.size() ) && !fileInfoList.get(fileInfo.item+1).isFolder )	//还有下一本书需要朗读
		{
			Intent intent = new Intent();
			intent.putExtra("next", EbookConstants.TO_NEXT_BOOK);
			setResult(RESULT_OK, intent);
			back();
		}
		else
		{
			String tips = this.getString(R.string.to_bottom);
			PublicUtils.showToast(this, tips);
		}
	}

	@Override
	public void onPageFlingCompleted(int curPage) 
	{
		// TODO Auto-generated method stub
		mTvCurPage.setText(curPage+"");
	}

	@Override
	public void onLoadCompleted(int pageCount, int curPage) 
	{
		// TODO Auto-generated method stub
		mTvPageCount.setText(pageCount+"");
		mTvCurPage.setText(curPage+"");
	}
	
	private class MenuBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e("menu", "------onreceive---:" + action);
			if(action.equals(EbookConstants.MENU_PAGE_EDIT)){
				int resultFlag = intent.getIntExtra("result_flag", 0);
				Log.e("menu", "------resultFlag---:" + resultFlag);
				switch(resultFlag){
				case 0://跳转到页码
					int curPage = intent.getIntExtra("page", 1);
					mTextReaderView.setCurPage(curPage);
					break;
				case 1://背景音乐开关
					playMusic();
					break;
				case 2://背景音乐选择
					playMusic();
					break;
				}
				
			}
		}
	}
	
	//退出此界面
	private void back()
	{
		MediaPlayerUtils.getInstance().stop();
		unregisterReceiver(menuReceiver);
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
		insertToDb();
		finish();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) 
	{  
		if( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN )
		{
			back();
			return true;   
		}     
	     
		return super.dispatchKeyEvent(event);
	}	
}
