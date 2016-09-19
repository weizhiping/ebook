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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.common.utils.Tools;
import com.sunteam.dict.utils.DBUtil;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TTSUtils.OnTTSListener;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.TextReaderView;
import com.sunteam.ebook.view.TextReaderView.OnPageFlingListener;

/**
 * TXT文件显示
 * 
 * @author sylar
 */
public class ReadTxtActivity extends Activity implements OnPageFlingListener, OnTTSListener
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		
		isAuto = this.getIntent().getBooleanExtra("isAuto", false);
		shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,Context.MODE_PRIVATE);
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
		int part = fileInfo.part;
		
		Tools tools = new Tools(this);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(tools.getBackgroundColor())); // 设置窗口背景色
    	mTvTitle = (TextView)this.findViewById(R.id.main_title);
    	mTvPageCount = (TextView)this.findViewById(R.id.pageCount);
    	mTvCurPage = (TextView)this.findViewById(R.id.curPage);
    	mLine = (View)this.findViewById(R.id.line);
    	
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
    	
    	mTextReaderView = (TextReaderView) findViewById(R.id.read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(tools.getFontColor());
    	mTextReaderView.setReverseColor(tools.getHighlightColor());
    	mTextReaderView.setBackgroundColor(tools.getBackgroundColor());
    	//mTextReaderView.setTextSize(tools.getFontSize());
    	if( mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part), TextFileReaderUtils.getInstance().getCharsetName(), fileInfo.line, fileInfo.startPos, fileInfo.len, fileInfo.checksum, isAuto, fileInfo.name) == false )
    	{
    		Toast.makeText(this, this.getString(R.string.checksum_error), Toast.LENGTH_SHORT).show();
    		back();
    	}
    	registerReceiver();
    	playMusic();
    	
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
				if( TextUtils.isEmpty(content) )
				{
					PublicUtils.showToast( this, this.getString(R.string.baike_search_fail) );
				}
				else
				{
					DBUtil dbUtils = new DBUtil();
					String result = dbUtils.search(content);
					if( TextUtils.isEmpty(result) )
					{
						PublicUtils.showToast( this, this.getString(R.string.baike_search_fail) );
					}
					else
					{
						TTSUtils.getInstance().stop();
						TTSUtils.getInstance().OnTTSListener(null);
						Intent intent = new Intent( this, WordSearchResultActivity.class );
						intent.putExtra("word", content);
						intent.putExtra("explain", result);
						this.startActivity(intent);
					}
				}
				return	true;
			case KeyEvent.KEYCODE_MENU:
				fileInfo.line = mTextReaderView.getLineNumber();
				Intent intent = new Intent(this, MenuActivity.class);
				intent.putExtra("page_count", mTextReaderView.getPageCount());
				intent.putExtra("page_cur", mTextReaderView.getCurPage());
				intent.putExtra("page_text", mTextReaderView.getReverseText());
				intent.putExtra("file", fileInfo);
				startActivityForResult(intent, MENU_CODE);
				break;
			case KeyEvent.KEYCODE_STAR:			//反查
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
		unregisterReceiver(menuReceiver);
		unregisterReceiver(shutReceiver);
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
			TTSUtils.getInstance().OnTTSListener(ReadTxtActivity.this);
			TTSUtils.getInstance().speakContent(ReadTxtActivity.this.getString(R.string.already_read));
		}
		else
		{
			String tips = this.getString(R.string.has_finished_reading_the_last_book);
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
					 mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part)
							 , TextFileReaderUtils.getInstance().getCharsetName(), line, 0, 0, 0, false, fileInfo.name);
					break;
				}
				
			}
		}
	}
	
	//退出此界面
	private void back()
	{
		MediaPlayerUtils.getInstance().stop();
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


	//朗读完成
	@Override
	public void onSpeakCompleted() 
	{
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(0);
	}

	//朗读错误
	@Override
	public void onSpeakError() 
	{
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(1);
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
                case 0:		//朗读完成
                case 1:		//朗读错误
                	Intent intent = new Intent();
        			intent.putExtra("next", EbookConstants.TO_NEXT_BOOK);
        			setResult(RESULT_OK, intent);
        			back();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

	private class ShutdownBroadcastReceiver extends BroadcastReceiver { 
	      
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        Log.e(TAG, "Shut down this system, ShutdownBroadcastReceiver onReceive()");  
	          
	        if (intent.getAction().equals(ACTION_SHUTDOWN)) {  
	            insertToDb();
	        }  
	    } 
	}

}
