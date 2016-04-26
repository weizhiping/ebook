package com.sunteam.ebook;

import com.sunteam.ebook.util.EbookConstants;

import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.TextReaderView;
import com.sunteam.ebook.view.TextReaderView.OnPageFlingListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		
		String filename = getIntent().getStringExtra("name");
		int part = getIntent().getIntExtra("part", 0);
		
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
    	
    	mTvTitle.setText(filename);
				
    	mTextReaderView = (TextReaderView) findViewById(R.id.read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
    	mTextReaderView.setReverseColor(this.getResources().getColor(EbookConstants.SelectBkColorID[mColorSchemeIndex]));
    	mTextReaderView.setBackgroundColor(this.getResources().getColor(EbookConstants.ViewBkColorID[mColorSchemeIndex]));
    	mTextReaderView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part), TextFileReaderUtils.getInstance().getCharsetName(), 0);
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
			case KeyEvent.KEYCODE_NUMPAD_7:		//朗读上一个
				mTextReaderView.preCharacter();
				return	true;
			case KeyEvent.KEYCODE_NUMPAD_9:		//朗读下一个
				mTextReaderView.nextCharacter();
				return	true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onPageFlingToTop() 
	{
		// TODO Auto-generated method stub
		String tips = this.getString(R.string.to_top);
		Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
		TTSUtils.getInstance().speak(tips);
	}

	@Override
	public void onPageFlingToBottom() 
	{
		// TODO Auto-generated method stub
		String tips = this.getString(R.string.to_bottom);
		Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
		TTSUtils.getInstance().speak(tips);
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
