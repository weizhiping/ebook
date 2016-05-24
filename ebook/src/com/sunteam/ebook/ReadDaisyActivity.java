package com.sunteam.ebook;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.DaisyReaderView;
import com.sunteam.ebook.view.DaisyReaderView.OnPageFlingListener;

/**
 * Daisy文件显示
 * 
 * @author sylar
 */
public class ReadDaisyActivity extends Activity implements OnPageFlingListener 
{
	private TextView mTvTitle = null;
	private TextView mTvPageCount = null;
	private TextView mTvCurPage = null;
	private View mLine = null;
	private DaisyReaderView mDaisyReaderView = null;
	private int mColorSchemeIndex = 0;	//系统配色索引
	private FileInfo fileInfo;
	private DiasyNode mDiasyNode = null;	//叶子节点信息
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_daisy);
		
		fileInfo = (FileInfo) getIntent().getSerializableExtra("file");
		mDiasyNode = (DiasyNode) getIntent().getSerializableExtra("node");
		String path = getIntent().getStringExtra("path");
		String name = getIntent().getStringExtra("name");
		
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
    	
    	mTvTitle.setText(name);
				
    	mDaisyReaderView = (DaisyReaderView) findViewById(R.id.read_daisy_view);
    	mDaisyReaderView.setOnPageFlingListener(this);
    	mDaisyReaderView.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mDaisyReaderView.setReverseColor(this.getResources().getColor(EbookConstants.SelectBkColorID[mColorSchemeIndex]));
    	mDaisyReaderView.setBackgroundColor(this.getResources().getColor(EbookConstants.ViewBkColorID[mColorSchemeIndex]));
    	if( mDaisyReaderView.openBook(path, mDiasyNode.seq, 0, 0, 0, 0) == false )
    	{
    		Toast.makeText(this, this.getString(R.string.checksum_error), Toast.LENGTH_SHORT).show();
    		finish();
    	}
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
			case KeyEvent.KEYCODE_BACK://返回保存最近使用
				//insertToDb();
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void insertToDb(){
		fileInfo.line = mDaisyReaderView.getLineNumber();
		fileInfo.checksum = mDaisyReaderView.getCheckSum();
		fileInfo.startPos = mDaisyReaderView.getReverseInfo().startPos;
		fileInfo.len = mDaisyReaderView.getReverseInfo().len;
		DatabaseManager manager = new DatabaseManager(this);
		manager.insertBookToDb(fileInfo, EbookConstants.BOOK_RECENT);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
		
		MediaPlayerUtils.getInstance().stop();
		MediaPlayerUtils.getInstance().OnMediaPlayerListener(null);
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
}
