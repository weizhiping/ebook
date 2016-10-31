package com.sunteam.ebook;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ReadMode;
import com.sunteam.ebook.util.CustomToast;
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
	private static final String TAG = "ReadTxtActivity";
	private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";  
	private TextView mTvTitle = null;
	private TextView mTvPageCount = null;
	private TextView mTvCurPage = null;
	private View mLine = null;
	private TextReaderView mTextReaderView = null;
	private FileInfo fileInfo;
	private ArrayList<FileInfo> fileInfoList = null;
	private static final int MENU_CODE = 10;
	private MenuBroadcastReceiver menuReceiver;
	private ShutdownBroadcastReceiver shutReceiver;
	private SharedPreferences shared;
	private boolean isAuto = false;
	private boolean isReadPage = false;	//是否朗读页码
	private boolean isFinish;//是否读完
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ebook_activity_read_txt);
		
		isAuto = this.getIntent().getBooleanExtra("isAuto", false);
		shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,Context.MODE_PRIVATE);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
		int part = fileInfo.part;
		
		Tools tools = new Tools(this);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(tools.getBackgroundColor())); // 设置窗口背景色
    	mTvTitle = (TextView)this.findViewById(R.id.ebook_main_title);
    	mTvPageCount = (TextView)this.findViewById(R.id.ebook_pageCount);
    	mTvCurPage = (TextView)this.findViewById(R.id.ebook_curPage);
    	mLine = (View)this.findViewById(R.id.ebook_line);
    	
    	mTvTitle.setTextColor(tools.getFontColor());
    	mTvPageCount.setTextColor(tools.getFontColor());
    	mTvCurPage.setTextColor(tools.getFontColor());
    	mLine.setBackgroundColor(tools.getFontColor()); // 设置分割线的背景色
    	
    	final float scale = this.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数;
		float fontSize = tools.getFontSize() * scale;
    	mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale);
    	mTvPageCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, (fontSize-3*EbookConstants.LINE_SPACE)/2*scale);
    	mTvCurPage.setTextSize(TypedValue.COMPLEX_UNIT_PX, (fontSize-3*EbookConstants.LINE_SPACE)/2*scale);
    	mTvTitle.setHeight((int)fontSize); // 设置控件高度
    	mTvTitle.setText(fileInfo.name);
    	mTvPageCount.setHeight((int)(fontSize/2));
    	mTvCurPage.setHeight((int)(fontSize/2));
    	
    	mTextReaderView = (TextReaderView) findViewById(R.id.ebook_read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(tools.getFontColor());
    	mTextReaderView.setReverseColor(tools.getHighlightColor());
    	mTextReaderView.setBackgroundColor(tools.getBackgroundColor());
    	//mTextReaderView.setTextSize(tools.getFontSize());
    	
    	registerReceiver();
    	playMusic();
    	
    	if (0 == fileInfo.count) // 文件为空
		{
    		TTSUtils.getInstance().stop();
			TTSUtils.getInstance().OnTTSListener(null);
			PublicUtils.showToast( this, this.getString(R.string.ebook_txt_menu_null), new PromptListener() {
				@Override
				public void onComplete() 
				{
					// TODO Auto-generated method stub
					back(true);
				}
			});
			
			return;
		} 
    	
    	if( mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part), TextFileReaderUtils.getInstance().getCharsetName(), fileInfo.line, fileInfo.startPos, fileInfo.len, fileInfo.checksum, isAuto, fileInfo.name) == false )
    	{
    		
    		TTSUtils.getInstance().stop();
			TTSUtils.getInstance().OnTTSListener(null);
			PublicUtils.showToast( this, this.getString(R.string.ebook_checksum_error), new PromptListener() {
				@Override
				public void onComplete() 
				{
					// TODO Auto-generated method stub
					back(true);
				}
			});
    	}
    	
    	/*
    	mTvTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				fileInfo.line = mTextReaderView.getLineNumber();
				Intent intent = new Intent(ReadTxtActivity.this, MenuActivity.class);
				intent.putExtra("page_count", mTextReaderView.getPageCount());
				intent.putExtra("page_cur", mTextReaderView.getCurPage());
				intent.putExtra("page_text", mTextReaderView.getReverseText());
				intent.putExtra("file", fileInfo);
				startActivityForResult(intent, MENU_CODE);
			}
		});
		*/
	}
	
	private void registerReceiver(){
		menuReceiver = new MenuBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.MENU_PAGE_EDIT);
		registerReceiver(menuReceiver, filter);
		
		shutReceiver = new ShutdownBroadcastReceiver();
		IntentFilter shutFilter = new IntentFilter();
		shutFilter.addAction(ACTION_SHUTDOWN);
		registerReceiver(shutReceiver, shutFilter);
		 
	}
	
	private void playMusic(){
		boolean isMusic = shared.getBoolean(EbookConstants.MUSICE_STATE, false);
		if(isMusic){
			String path = shared.getString(EbookConstants.MUSICE_PATH, null);
			if(null == path){
				path = FileOperateUtils.getFirstMusicInDir();
			}else{
				File file = new File(path);
				if(!file.exists()){
					path = FileOperateUtils.getFirstMusicInDir();
				}
			}
			if(null != path){
				MediaPlayerUtils.getInstance().play(path);
			}
		}else{
			MediaPlayerUtils.getInstance().stop();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if( isReadPage )
		{
			mTextReaderView.readPage();		//朗读页码
		}
		isReadPage = true;
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
				if( mTextReaderView.startSelect() )
				{
					PublicUtils.showToast(this, this.getString(R.string.ebook_select_start));
				}
				return	true;
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_NUMPAD_3:		//结束选词
				if( mTextReaderView.endSelect() )
				{
					PublicUtils.showToast(this, this.getString(R.string.ebook_select_end));
				}
				return	true;
			case KeyEvent.KEYCODE_0:
			case KeyEvent.KEYCODE_NUMPAD_0:		//百科查询
				return	true;
			case KeyEvent.KEYCODE_MENU:
				fileInfo.line = mTextReaderView.getLineNumber();
				fileInfo.startPos = mTextReaderView.getReverseInfo().startPos;
				fileInfo.len = mTextReaderView.getReverseInfo().len;
				Intent intent = new Intent(this, MenuActivity.class);
				intent.putExtra("page_count", mTextReaderView.getPageCount());
				intent.putExtra("page_cur", mTextReaderView.getCurPage());
				intent.putExtra("page_text", mTextReaderView.getReverseText());
				intent.putExtra("file", fileInfo);
				startActivityForResult(intent, MENU_CODE);
				break;
			case KeyEvent.KEYCODE_STAR:			//反查
				String content = mTextReaderView.getReverseText();	//得到当前反显内容
				PublicUtils.jumpFanCha(this, content);
				break;
			case KeyEvent.KEYCODE_POUND:		//#号键
				mTextReaderView.readPage();		//朗读页码
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	//插入到最近浏览
	private void insertToDb(){
		Log.e(TAG, "----init===== to db------" + isFinish + "--name--:" + fileInfo.name);
		if(isFinish){
			fileInfo.line = 0;
			fileInfo.checksum = 0;
			fileInfo.startPos = 0;
			fileInfo.len = 0;
		}else{
			fileInfo.line = mTextReaderView.getLineNumber();
			fileInfo.checksum = mTextReaderView.getCheckSum();
			fileInfo.startPos = mTextReaderView.getReverseInfo().startPos;
			fileInfo.len = mTextReaderView.getReverseInfo().len;
		}
		
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
					isReadPage = false;
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
		unregisterReceiver(menuReceiver);
		unregisterReceiver(shutReceiver);
		super.onDestroy();
	}
	
	@Override
	public void onPageFlingToTop() 
	{
		// TODO Auto-generated method stub
		/*
		String tips = this.getString(R.string.ebook_to_top);
		PublicUtils.showToast(this, tips);
		*/
		CustomToast.showToast(this, this.getString(R.string.ebook_to_top), Toast.LENGTH_SHORT);
	}

	@Override
	public void onPageFlingToBottom( boolean isContinue ) 
	{
		isFinish = true;
		// TODO Auto-generated method stub
		if( !isContinue || ( mTextReaderView.getReadMode() != ReadMode.READ_MODE_ALL ) )
		{
			PublicUtils.showToast(this, this.getString(R.string.ebook_to_bottom));
			return;
		}
		
		if( fileInfo.part+1 < fileInfo.count )	//还有下一部分需要朗读
		{
			Intent intent = new Intent();
			intent.putExtra("next", EbookConstants.TO_NEXT_PART);
			setResult(RESULT_OK, intent);
			back(false);
		}
		else if( ( fileInfo.item+1 < fileInfoList.size() ) && !fileInfoList.get(fileInfo.item+1).isFolder )	//还有下一本书需要朗读
		{
			TTSUtils.getInstance().OnTTSListener(null);
			PublicUtils.showToast(this, this.getString(R.string.ebook_already_read), new PromptListener(){

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
        			intent.putExtra("next", EbookConstants.TO_NEXT_BOOK);
        			setResult(RESULT_OK, intent);
        			back(false);
				}
			});
		}
		else
		{
			String tips = this.getString(R.string.ebook_has_finished_reading_the_last_book);
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
			if(action.equals(EbookConstants.MENU_PAGE_EDIT)){
				int resultFlag = intent.getIntExtra("result_flag", 0);
				switch(resultFlag){
				case 0://跳转到页码
					isReadPage = false;
					int curPage = intent.getIntExtra("page", 1);
					mTextReaderView.setCurPage(curPage);
					break;
				case 1://背景音乐开关
					playMusic();
					break;
				case 2://背景音乐选择
					playMusic();
					break;
				case 3:
					int line = intent.getIntExtra("line", 0);
					int part = intent.getIntExtra("part", 0);
					int start = intent.getIntExtra("start", 0);
					int len = intent.getIntExtra("len", 0);
					 mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part)
							 , TextFileReaderUtils.getInstance().getCharsetName(), line, start, len, 0, false, fileInfo.name);
					break;
				}
				
			}
		}
	}
	
	//退出此界面
	private void back( boolean isSetResult )
	{
		MediaPlayerUtils.getInstance().stop();
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
		if( isSetResult )
		{
			setResult(RESULT_OK);
		}
		insertToDb();
		sendBroadcast(new Intent(EbookConstants.ACTION_UPDATE_FILE));
		finish();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) 
	{  
		if( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN )
		{
			back(true);
			return true;   
		}     
	     
		return super.dispatchKeyEvent(event);
	}

	private class ShutdownBroadcastReceiver extends BroadcastReceiver { 
	      
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        Log.e(TAG, "Shut down this system, ShutdownBroadcastReceiver onReceive()");  
	        Log.e(TAG, "-----------------------------shut down-----------------");  
	        if (intent.getAction().equals(ACTION_SHUTDOWN)) {  
	            insertToDb();
	        }  
	    } 
	}
}
