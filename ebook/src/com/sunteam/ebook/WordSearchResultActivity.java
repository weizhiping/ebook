package com.sunteam.ebook;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
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
	private boolean isFinish;//是否读完
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//禁止休眠
		setContentView(R.layout.ebook_activity_read_txt);
		
		word = this.getIntent().getStringExtra("word");
		explain = this.getIntent().getStringExtra("explain");
		
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
    	mTvPageCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, (fontSize-2*EbookConstants.LINE_SPACE)/2*scale);
    	mTvCurPage.setTextSize(TypedValue.COMPLEX_UNIT_PX, (fontSize-2*EbookConstants.LINE_SPACE)/2*scale);
    	mTvTitle.setHeight((int)fontSize); // 设置控件高度
    	mTvTitle.setText(word);
				
    	mTextReaderView = (TextReaderView) findViewById(R.id.ebook_read_txt_view);
    	mTextReaderView.setOnPageFlingListener(this);
    	mTextReaderView.setTextColor(tools.getFontColor());
    	mTextReaderView.setReverseColor(tools.getHighlightColor());
    	mTextReaderView.setBackgroundColor(tools.getBackgroundColor());
    	//mTextReaderView.setTextSize(tools.getFontSize());
    	
    	if( mTextReaderView.openBook(explain) == false )
    	{
    		PublicUtils.showToast(this, this.getString(R.string.ebook_checksum_error));
    		back();
    	}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		isFinish = false;
		mTextReaderView.setIsPlayParagraph(!isFinish, false);
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
			case KeyEvent.KEYCODE_MENU:			//加入生词库
				saveWord();
				return	true;
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
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_NUMPAD_2:		//朗读上一个段落
				mTextReaderView.setIsPlayParagraph(!isFinish, true);
				return	true;
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_NUMPAD_8:		//朗读下一个段落
				mTextReaderView.setIsPlayParagraph(!isFinish, true);
				return	true;
			default:
				break;
		}
		return super.onKeyUp(keyCode, event);
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
				/*
				saveFile.delete();
				saveFile.createNewFile();
				*/
				
				PublicUtils.showToast( this, this.getString(R.string.ebook_file_already_exist), new PromptListener() 
				{
					public void onComplete() 
					{
						mTextReaderView.enter();	//需要恢复全文朗读
					}
				});
				
				return;	//如果已经存在，提示即可
			}

			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(explain.getBytes());
			outStream.close();
			
			PublicUtils.showToast( this, this.getString(R.string.ebook_save_newword_success), new PromptListener() 
			{
				public void onComplete() 
				{
					mTextReaderView.enter();	//需要恢复全文朗读
				}
			});
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			PublicUtils.showToast( this, this.getString(R.string.ebook_save_newword_fail), new PromptListener() 
			{
				public void onComplete() 
				{
					mTextReaderView.enter();	//需要恢复全文朗读
				}
			});
		}
	}
	
	@Override
	public void onPageFlingToTop() 
	{
		// TODO Auto-generated method stub
		isFinish = true;
		String tips = this.getString(R.string.ebook_to_top);
		PublicUtils.showToast(this, tips);
	}

	@Override
	public void onPageFlingToBottom( boolean isContinue ) 
	{
		// TODO Auto-generated method stub
		isFinish = true;
		String tips = this.getString(R.string.ebook_to_bottom);
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
