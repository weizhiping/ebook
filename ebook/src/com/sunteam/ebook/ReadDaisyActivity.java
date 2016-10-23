package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ReadMode;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TTSUtils.OnTTSListener;
import com.sunteam.ebook.view.DaisyReaderView;
import com.sunteam.ebook.view.DaisyReaderView.OnPageFlingListener;

/**
 * Daisy文件显示
 * 
 * @author sylar
 */
public class ReadDaisyActivity extends Activity implements OnPageFlingListener
{
	private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";  
	private TextView mTvTitle = null;
	private TextView mTvPageCount = null;
	private TextView mTvCurPage = null;
	private View mLine = null;
	private DaisyReaderView mDaisyReaderView = null;
	private ShutdownBroadcastReceiver shutReceiver;
	private FileInfo fileInfo;
	private ArrayList<FileInfo> fileInfoList = null;
	private DiasyNode mDiasyNode = null;	//叶子节点信息
	private String diaPath;
	private MenuBroadcastReceiver menuReceiver;
	private static final int MENU_DAISY_CODE = 11;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ebook_activity_read_daisy);
		
		fileInfo = (FileInfo) getIntent().getSerializableExtra("fileinfo");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra("file_list");
		mDiasyNode = (DiasyNode) getIntent().getSerializableExtra("node");
		diaPath = getIntent().getStringExtra("path");
		String name = getIntent().getStringExtra("name");
		
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
    	mTvTitle.setText(name);
    	mTvPageCount.setHeight((int)(fontSize/2));
    	mTvCurPage.setHeight((int)(fontSize/2));
    	
    	mDaisyReaderView = (DaisyReaderView) findViewById(R.id.ebook_read_daisy_view);
    	mDaisyReaderView.setOnPageFlingListener(this);    	
    	mDaisyReaderView.setTextColor(tools.getFontColor());
    	mDaisyReaderView.setReverseColor(tools.getHighlightColor());
    	mDaisyReaderView.setBackgroundColor(tools.getBackgroundColor());
    	//mDaisyReaderView.setTextSize(tools.getFontSize());
    	
    	registerReceiver();
    	
    	if( mDaisyReaderView.openBook(diaPath, mDiasyNode.seq, 0, 0, 0, 0) == false )
    	{
    		//PublicUtils.showToast(this, this.getString(R.string.ebook_checksum_error));
    		back();
    	}
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_DPAD_UP:		//上
				mDaisyReaderView.up();
				return	true;
			case KeyEvent.KEYCODE_DPAD_DOWN:	//下
				mDaisyReaderView.down();
				return	true;
			case KeyEvent.KEYCODE_DPAD_LEFT:	//左
				mDaisyReaderView.left();
				return	true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:	//右
				mDaisyReaderView.right();
				return	true;
			case KeyEvent.KEYCODE_DPAD_CENTER:	//确定
			case KeyEvent.KEYCODE_ENTER:
				mDaisyReaderView.enter();
				return	true;
//			case KeyEvent.KEYCODE_BACK://返回保存最近使用
//				insertToDb();
//				break;
			case KeyEvent.KEYCODE_MENU:
				MediaPlayerUtils.getInstance().pause();		//暂停播放
				
				fileInfo.line = mDaisyReaderView.getLineNumber();
				fileInfo.checksum = mDaisyReaderView.getCheckSum();
				fileInfo.startPos = mDaisyReaderView.getReverseInfo().startPos;
				fileInfo.len = mDaisyReaderView.getReverseInfo().len;
				Intent intent = new Intent(this, MenuDaisyActivity.class);
				intent.putExtra("page_count", mDaisyReaderView.getPageCount());
				intent.putExtra("page_cur", mDaisyReaderView.getCurPage());
				intent.putExtra("page_text",mDiasyNode.name);
				intent.putExtra("file", fileInfo);
				startActivityForResult(intent, MENU_DAISY_CODE);
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void insertToDb(){
		Log.e("read", "----inser to db flag=--:" + fileInfo.flag);
		fileInfo.line = mDaisyReaderView.getLineNumber();
		fileInfo.checksum = mDaisyReaderView.getCheckSum();
		fileInfo.startPos = mDaisyReaderView.getReverseInfo().startPos;
		fileInfo.len = mDaisyReaderView.getReverseInfo().len;
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
				case MENU_DAISY_CODE:
					int curPage = data.getIntExtra("page", 1);
					mDaisyReaderView.setCurPage(curPage);
					break;
				}
			}
		}
	}
	
	@Override
	public void onPageFlingToTop() 
	{
		// TODO Auto-generated method stub
		String tips = this.getString(R.string.ebook_to_top);
		PublicUtils.showToast(this, tips);
	}

	@Override
	public void onPageFlingToBottom() 
	{
		// TODO Auto-generated method stub
		if( mDiasyNode.seq+1 < DaisyFileReaderUtils.getInstance().getDiasyNodeTotal() )	//还有下一部分需要朗读
		{
			Intent intent = new Intent();
			intent.putExtra("next", EbookConstants.TO_NEXT_PART);
			intent.putExtra("seq", mDiasyNode.seq);
			setResult(RESULT_OK, intent);
			back();
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
        			back();
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
	public void onPageFlingCompleted(String title, int curPage) 
	{
		// TODO Auto-generated method stub
		mTvTitle.setText(title);
		mTvCurPage.setText(curPage+"");
	}

	@Override
	public void onLoadCompleted(String title, int pageCount, int curPage) 
	{
		// TODO Auto-generated method stub
		mTvTitle.setText(title);
		mTvPageCount.setText(pageCount+"");
		mTvCurPage.setText(curPage+"");
	}
	
	private class MenuBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(EbookConstants.MENU_PAGE_EDIT)){
				int resultFlag = intent.getIntExtra("result_flag", 0);
				if(0 == resultFlag){
					int curPage = intent.getIntExtra("page", 1);
					mDaisyReaderView.setCurPage(curPage);
				}else if(1 == resultFlag){
					int flag = intent.getIntExtra("flag", 0);
					switch(flag){
					case 0:
						mDaisyReaderView.setReadMode(ReadMode.READ_MODE_SENCENTE);	//设置逐句朗读
						break;
					case 1:
						mDaisyReaderView.setReadMode(ReadMode.READ_MODE_PARAGRAPH);	//设置章节朗读
						break;
					case 2:
						mDaisyReaderView.setReadMode(ReadMode.READ_MODE_ALL);		//设置全文朗读
						break;
					}
				}else if(3 == resultFlag){
					int line = intent.getIntExtra("line", 0);
					int part = intent.getIntExtra("part", 0);
					int start = intent.getIntExtra("start", 0);
					int len = intent.getIntExtra("len", 0);
					 mDaisyReaderView.openBook(diaPath, mDiasyNode.seq, line, 0, 0, 0);
				}
				
			}
		}
	}
	
	//退出此界面
	private void back()
	{
		if( menuReceiver != null )
		{
			unregisterReceiver(menuReceiver);
			menuReceiver = null;
		}
		if( shutReceiver != null )
		{
			unregisterReceiver(shutReceiver);
			shutReceiver = null;
		}
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
		
		MediaPlayerUtils.getInstance().stop();
		MediaPlayerUtils.getInstance().OnMediaPlayerListener(null);
		insertToDb();
		sendBroadcast(new Intent(EbookConstants.ACTION_UPDATE_FILE));
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

	private class ShutdownBroadcastReceiver extends BroadcastReceiver { 
	      
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	          
	        if (intent.getAction().equals(ACTION_SHUTDOWN)) {  
	            insertToDb();
	        }  
	    } 
	}
}
