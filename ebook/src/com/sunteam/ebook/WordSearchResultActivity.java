package com.sunteam.ebook;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteam.common.utils.Tools;
import com.sunteam.dict.utils.DBUtil;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.view.TextReaderView;
import com.sunteam.ebook.view.TextReaderView.OnPageFlingListener;

/**
 * 单词查找结果显示
 * 
 * @author wzp
 */
public class WordSearchResultActivity extends Activity implements OnPageFlingListener 
{
	private TextView mTvTitle = null;
	private TextView mTvPageCount = null;
	private TextView mTvCurPage = null;
	private View mLine = null;
	private TextReaderView mTextReaderView = null;
	private String word = null;
	private String explain = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		
		word = this.getIntent().getStringExtra("word");
		explain = this.getIntent().getStringExtra("explain");
		
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
    	
    	mTvTitle.setText(word);
    	mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, tools.getFontSize());
    	mTvPageCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, tools.getFontSize()/2);
    	mTvCurPage.setTextSize(TypedValue.COMPLEX_UNIT_SP, tools.getFontSize()/2);
				
    	mTextReaderView = (TextReaderView) findViewById(R.id.read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(tools.getFontColor());
    	mTextReaderView.setReverseColor(tools.getHighlightColor());
    	mTextReaderView.setBackgroundColor(tools.getBackgroundColor());
    	//mTextReaderView.setTextSize(tools.getFontSize());
    	
    	if( mTextReaderView.openBook(explain) == false )
    	{
    		Toast.makeText(this, this.getString(R.string.checksum_error), Toast.LENGTH_SHORT).show();
    		back();
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
				break;
			case KeyEvent.KEYCODE_MENU:			//加入生词库
				saveWord();
				break;
			case KeyEvent.KEYCODE_STAR:			//反查
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void saveWord()
	{
		String filePath = null;
		boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (hasSDCard) 
		{
			filePath = Environment.getExternalStorageDirectory().toString() + File.separator + EbookConstants.NEW_WORD_BOOK + File.separator;
		} 
		else
		{
			filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + EbookConstants.NEW_WORD_BOOK + File.separator;
		}
		
		File fileDir = new File(filePath);
		if( !fileDir.exists() )	//若不存在
		{	
			fileDir.mkdirs();
		}
		String fullpath = filePath+word+".txt";
		
		try
		{
			File saveFile = new File(fullpath);
			if(saveFile.exists())	//若对应文件名的文件已存在，则删除原来的文件
			{	
				saveFile.delete();
				saveFile.createNewFile();
			}

			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(explain.getBytes());
			outStream.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
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

	//退出此界面
	private void back()
	{
		TTSUtils.getInstance().stop();
		TTSUtils.getInstance().OnTTSListener(null);
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
