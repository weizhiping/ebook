package com.sunteam.ebook;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
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
	private static final int MENU_CODE = 10;
	private MenuBroadcastReceiver menuReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
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
    		finish();
    	}
    	registerReceiver();
	}
	
	private void registerReceiver(){
		menuReceiver = new MenuBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.MENU_PAGE_EDIT);
		registerReceiver(menuReceiver, filter);
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
			case KeyEvent.KEYCODE_BACK://返回保存最近使用
				insertToDb();
				break;
			case KeyEvent.KEYCODE_MENU:
				Intent intent = new Intent(this, MenuActivity.class);
				intent.putExtra("page_count", mTextReaderView.getPageCount());
				intent.putExtra("page_cur", mTextReaderView.getCurPage());
				startActivityForResult(intent, MENU_CODE);
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
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
		unregisterReceiver(menuReceiver);
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
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
		String tips = this.getString(R.string.to_bottom);
		PublicUtils.showToast(this, tips);
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
				int curPage = intent.getIntExtra("page", 1);
				Log.e("menu", "------curPage---:" + curPage);
				mTextReaderView.setCurPage(curPage);
			}
		}
	}
}
