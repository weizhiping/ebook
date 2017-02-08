package com.sunteam.ebook.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.sunteam.common.utils.Tools;
import com.sunteam.ebook.R;
import com.sunteam.ebook.entity.ReadMode;
import com.sunteam.ebook.entity.ReverseInfo;
import com.sunteam.ebook.entity.SplitInfo;
import com.sunteam.ebook.util.CodeTableUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TTSUtils.OnTTSListener;
import com.sunteam.ebook.util.TTSUtils.SpeakStatus;
import com.sunteam.ebook.util.WordExplainUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Txt电子书阅读器控件
 * 
 * @author wzp
 *
 */

 public class TextReaderView extends View implements OnGestureListener, OnDoubleTapListener, OnTTSListener
 {	 
	 private static final String TAG = "TextReaderView";
	 private static final int MSG_SPEAK_COMPLETED = 100;
	 private static final int MSG_SPEAK_ERROR = 200;
	 private float MARGIN_WIDTH = 0;		//左右与边缘的距离
	 private float MARGIN_HEIGHT = 0;		//上下与边缘的距离
	 private static final String CHARSET_NAME = "GB18030";//编码格式，默认为GB18030
	 private static final char[] CN_SEPARATOR = { 
		 0xA3BA,	//冒号
		 0xA6DC,	//冒号
		 0xA955,	//冒号
		 0xA973,	//冒号
		 
		 0xA3AC,	//逗号
		 0xA6D9,	//逗号
		 0xA96F,	//逗号
		 
		 0xA3BB,	//分号
		 0xA6DD,	//分号
		 0xA972,	//分号
		 
		 0xA1A3,	//句号
		 
		 0xA3BF,	//问号
		 0xA974,	//问号
		 
		 0xA3A1,	//叹号
		 0xA6DE,	//叹号
		 0xA975,	//叹号
		 
		 //0xA1AD,	//省略号
		 
		 0xA1A2,	//顿号
		 0xA970,	//顿号
		 
		 0xA1A1,	//空格
	 };	//中文分隔符
	 
	 private static final char[] EN_SEPARATOR = { 
		 0x3A,	//冒号
		 0x2C,	//逗号
		 0x3B,	//分号
		 0x3F,	//问号
		 //0x2E,	//句号
		 0x21,	//叹号
	 };	//英文分隔符
	 
	 private Context mContext = null;
	 private Bitmap mCurPageBitmap = null;
	 private Canvas mCurPageCanvas = null;	//当前画布
	 private Paint mPaint = null;
	 private float mLineSpace = 3.6f;		//行间距
	 private float mTextSize = 20.0f;		//字体大小
	 private int mTextColor = Color.WHITE;	//字体颜色
	 private int mBkColor = Color.BLACK;	//背景颜色
	 private int mReverseColor = Color.RED;	//反显颜色
	 private int mLineCount = 0; 			//每页可以显示的行数
	 private int mWidth = 0;				//页面控件的宽
	 private int mHeight = 0;				//页面控件的高
	 private float mVisibleWidth; 			//绘制内容的宽
	 private float mVisibleHeight;			//绘制内容的高
	 private boolean mIsFirstPage = false;	//是否是第一屏
	 private boolean mIsLastPage = false;	//是否是最后一屏
	 private ArrayList<SplitInfo> mSplitInfoList = new ArrayList<SplitInfo>();	//保存分行信息
	 private byte[] mMbBuf = null;			//内存中的图书字符
	 private int mOffset = 0;				//图书字符真正开始位置，有BOM的地方需要跳过
	 private int mLineNumber = 0;			//当前页起始位置(行号)
	 private int mMbBufLen = 0; 			//图书总长度
	 private int mCurPage = 1;				//当前页
	 private GestureDetector mGestureDetector = null;	//手势
	 private OnPageFlingListener mOnPageFlingListener = null;
	 private ReadMode mReadMode = ReadMode.READ_MODE_ALL;	//朗读模式
	 private ReverseInfo mReverseInfo = new ReverseInfo();	//反显信息
	 private WordExplainUtils mWordExplainUtils = new WordExplainUtils();
	 private HashMap<Character, ArrayList<String> > mMapWordExplain = new HashMap<Character, ArrayList<String>>();
	 private int mCurReadExplainIndex = 0;	//当前朗读的例句索引
	 private int mCheckSum = 0;				//当前buffer的checksum
	 private int mParagraphStartPos = 0;	//逐段朗读模式下段落开始位置
	 private int mParagraphLength = 0;		//逐段朗读模式下段落长度
	 private ReverseInfo mSelectInfo = new ReverseInfo();	//选词
	 private String mFilename = null;			//文件名
	 private boolean mIsAuto = false;		//是否是自动朗读进入的，如果是还需要读文件名称。
	 private int mPercent = 0;				//当前朗读进度
	 private String mSpeakText = null;		//当前朗读内容
	 private boolean mIsTextFile = false;	//当前内容是否是纯文本
	 private boolean mIsPlayParagraph = false;	//是否可以进行段落播放
	 
	 public interface OnPageFlingListener 
	 {
		 public void onLoadCompleted( int pageCount, int curPage );		//加载完成
		 public void onPageFlingToTop();								//翻到头了
		 public void onPageFlingToBottom( boolean isContinue );			//翻到尾了，是否继续读
		 public void onPageFlingCompleted( int curPage );	//翻页完成
	 }
	 
	 public TextReaderView(Context context) 
	 {
		 super(context);
		 
		 initReaderView( context );
	 }
	 
	 public TextReaderView(Context context, AttributeSet attrs) 
	 {
		 super(context, attrs);
		 
		 initReaderView( context );
	 }

	 public TextReaderView(Context context, AttributeSet attrs, int defStyle) 
	 {
		 super(context, attrs, defStyle);
		 
		 initReaderView( context );
	 }

	 private void initReaderView( Context context )
	 {
		 mContext = context;
		 
		 mGestureDetector = new GestureDetector( context, this );
		 mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//画笔
		 mPaint.setTextAlign(Align.LEFT);			//做对齐
		 
		 final int fontSize = new Tools(context).getFontSize();
		 switch( fontSize )
		 {
		 	case 24:	//小号字
		 		mTextSize = 20.0f;
		 		mLineSpace = 3.6f;
		 		break;
		 	case 30:	//中号字
		 		mTextSize = 26.0f;
		 		mLineSpace = 3.5f;
		 		MARGIN_WIDTH = 4.0f;
		 		break;
		 	case 40:	//大号字
		 		mTextSize = 35.0f;
		 		mLineSpace = 4.2f;
		 		MARGIN_WIDTH = 2.5f;
		 		break;
		 	default:
		 		break;
		 }
		 final float scale = context.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数
		 
		 mLineSpace *= scale;		//行间距
		 mTextSize *= scale;		//字体大小
		 
		 mWordExplainUtils.init(mContext);			//初始化例句
		 
		 TTSUtils.getInstance().OnTTSListener(this);
	 }
	 
	 //开始选词
	 public boolean startSelect()
	 {
		 if( mReadMode != ReadMode.READ_MODE_CHARACTER )
		 {
			 return	false;
		 }
		 
		 mSelectInfo.startPos = mReverseInfo.startPos;
		 mSelectInfo.len = mReverseInfo.len;
		 
		 return	true;
	 }
	 
	 //结束选词
	 public boolean endSelect()
	 {
		 if( mReadMode != ReadMode.READ_MODE_CHARACTER )
		 {
			 return	false;
		 }
		 
		 if( mReverseInfo.startPos >= mSelectInfo.startPos )
		 {
			 mSelectInfo.len = mReverseInfo.startPos + mReverseInfo.len - mSelectInfo.startPos;
		 }
		 else
		 {
			 mSelectInfo.len = mSelectInfo.startPos + mSelectInfo.len - mReverseInfo.startPos;
			 mSelectInfo.startPos = mReverseInfo.startPos;
		 }
		 
		 if( ( mSelectInfo.startPos >= mOffset ) && ( mSelectInfo.len > 0 ) )
		 {
			 mReverseInfo.startPos = mSelectInfo.startPos;
			 mReverseInfo.len = mSelectInfo.len;
			 
			 mSelectInfo.startPos = -1;
			 mSelectInfo.len = -1;
			 
			 this.invalidate();
		 }
		 
		 return	true;
	 }
	 
	 //设置是否可以进行段落播放
	 public void setIsPlayParagraph( boolean isPlayParagraph, boolean isInstantlyPlay )
	 {
		 mIsPlayParagraph = isPlayParagraph;
		 if( isPlayParagraph && isInstantlyPlay )
		 {
			 readReverseText(false, false, false);			//朗读反显文字
		 }
	 }
	 
	 //朗读页码
	 public void readPage()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 /*
		 if( ( SpeakStatus.SPEAK == status ) && ( TTSUtils.getInstance().getSpeakForm() == SpeakForm.CONTENT ) )	//如果中断前是内容朗读状态
		 {
			 if( mSpeakText != null && mSpeakText.length() > 0 )
			 {
				 int speakTextLen = mSpeakText.length();
				 int length = (speakTextLen * mPercent + 50)/ 100;
				 if( length < speakTextLen )
				 {
					 try 
					 {
						 byte[] buffer = mSpeakText.getBytes(CHARSET_NAME);	//转换成指定编码
						 for( int i = 0; i < length;  )	//需要判断当前位置是否为汉字的第二个字节
						 {
							 if( buffer[i] < 0 )	//汉字
							 {
								 i += 2;
							 }
							 else
							 {
								 i++;
							 }
							 
							 if( i > length )
							 {
								 length++;
							 }
							 
							 if( length < speakTextLen )
							 {
								 String str = new String(buffer, length, speakTextLen-length, CHARSET_NAME);	//转换成指定编码
								 if( !TextUtils.isEmpty(str))
								 {
									 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
									 speakContent(tips+"，"+str);
										 
									 return;
								 }
							 }
						 }
					 } 
					 catch (UnsupportedEncodingException e) 
					 {
						 e.printStackTrace();
					 }
				 }
			 }
		 }
		 */	//不论当前是否是播放状态，都停止播放。
		 
		 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
		 speakTips(tips);
	 }
	 
	 //得到反显内容
	 public String getReverseText()
	 {
		 try 
		 {
			 return	new String(mMbBuf, mReverseInfo.startPos, mReverseInfo.len, CHARSET_NAME);	//转换成指定编码
		 } 
		 catch (UnsupportedEncodingException e) 
		 {
			 e.printStackTrace();
		 }
		 
		 return	"";
	 }
	 
	 //设置翻页监听器
	 public void setOnPageFlingListener( OnPageFlingListener listener )
	 {
		 mOnPageFlingListener = listener;
	 }
	 
	 //设置朗读模式
	 public void setReadMode( ReadMode rm )
	 {
		 mReadMode = rm;
		 mCurReadExplainIndex = 0;
	 }
	 
	 //得到朗读模式
	 public ReadMode getReadMode()
	 {
		 return	mReadMode;
	 }
	 
	 //设置背景色
	 @Override
	 public void setBackgroundColor( int color )
	 {
		 mBkColor = color;
	 }
	 
	 //设置字体颜色
	 public void setTextColor( int color )
	 {
		 mTextColor = color;
	 }
	 
	 //设置反显颜色
	 public void setReverseColor( int color )
	 {
		 mReverseColor = color;
	 }
	 
	 //设置字体大小
	 public void setTextSize( float size )
	 {
		 mTextSize = size;
	 }
	 
	 //设置行间距
	 public void setSpaceSize( int size ) 
	 {
		 mLineSpace = size;
	 }
	 
	 //得到背景色
	 public int getBackgroundColor()
	 {
		 return	mBkColor;
	 }
	 
	 //得到字体颜色
	 public int getTextColor()
	 {
		 return	mTextColor;
	 }
	 
	 //得到反显颜色
	 public int getReverseColor()
	 {
		 return	mReverseColor;
	 }
	 
	 //得到字体大小
	 public float getTextSize()
	 {
		 return	mTextSize;
	 }
	 
	 //得到行间距
	 public float getSpaceSize() 
	 {
		 return	mLineSpace;
	 }

	 //得到一屏行数
	 public int getLineCount() 
	 {
		 return mLineCount;
	 }
	 
	 //是否是第一屏
	 public boolean isFirstPage() 
	 {
		 return mIsFirstPage;
	 }

	 //是否是最后一屏
	 public boolean isLastPage() 
	 {
		 return mIsLastPage;
	 }
	 
	 //得到当前屏第一行文本
	 public String getFirstLineText() 
	 {
		 return	getLineText(mLineNumber);
	 }
	 
	 //得到当前屏的反显信息
	 public ReverseInfo getReverseInfo()
	 {
		 return	mReverseInfo;
	 }
	 
	 //得到当前屏第一行的行号
	 public int getLineNumber()
	 {
		 return	mLineNumber;
	 }
	 
	 //得到当前buffer的CheckSum
	 public int getCheckSum()
	 {
		 return	mCheckSum;
	 }
	 
	 private int calcCheckSum( byte[] buffer )
	 {
		 int checksum = 0;
		 int len = buffer.length;
		 int shang = len / 4;
		 int yu = len % 4;
		 
		 for( int i = 0; i < shang; i++ )
		 {
			 checksum += PublicUtils.byte2int(buffer, i*4);
		 }
		 
		 byte[] data = new byte[4];
		 for( int i = 0; i < yu; i++ )
		 {
			 data[i] = buffer[i];
		 }
		 
		 checksum += PublicUtils.byte2int(data, 0);
		 
		 return	checksum;
	 }
	 
	 //得到指定行文本
	 private String getLineText( final int lineNumber )
	 {
		 int size = mSplitInfoList.size();
		 if( lineNumber >= 0  && lineNumber < size )
		 {
			 SplitInfo li = mSplitInfoList.get(lineNumber);
			 
			 try 
			 {
				 String str = new String(mMbBuf, li.startPos, li.len, CHARSET_NAME);	//转换成指定编码
				 if( str != null )
				 {
					 str = str.replaceAll("\n", "");
					 str = str.replaceAll("\r", "");
					 
					 return	str;
				 }
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 e.printStackTrace();
			 }
		 }
		 
		 return	"";
	 }
	 
	 //得到有效的长度，去除结尾的换行或者空格
	 private int getEffectiveLength( byte[] buf )
	 {
		 int len = mMbBuf.length;
		 for( int i = mMbBuf.length-1; i >= 0; i-- )
		 {
			 if( buf[i] > 0 )
			 {
				 if( ( 0x0d == buf[i] ) || ( 0x0a == buf[i] ) || ( 0x20 == buf[i] ) )
				 {
					 len--;
				 }
				 else
				 {
					 break;
				 }
			 }
			 else if( buf[i] < 0 )	//汉字
			 {
				 if( ( i > 0 ) && (-95 == buf[i]) && (-95 == buf[i-1]) )	//汉字空格0xA1A1
				 {
					 len--;
				 }
				 else
				 {
					 break;
				 }
			 }
			 else
			 {
				 len--;
			 }
		 }
		 
		 return	len;
	 }
	 
	 public boolean openBook( String content ) 
	 {
		 byte[] buf = null;
		 try
		 {
			 buf = content.getBytes(CHARSET_NAME);
		 }
		 catch (UnsupportedEncodingException e) 
		 {
			 e.printStackTrace();
		 }
		 
		 return	openBook( buf, CHARSET_NAME, 0, 0, 0, 0, false, "" );
	 }
	 
	 /**
	  * 
	  * @param buffer
	  * 			文本buffer
	  * @param charsetName
	  * 			编码
	  * @param lineNumber
	  *            表示书签记录的位置(行号)
	  * @param startPos
	  *            表示反显开始位置
	  * @param len
	  *            表示反显长度
	  * @param checksum
	  *            校验值
	  */
	 public boolean openBook(byte[] buffer, String charsetName, int lineNumber, int startPos, int len, int checksum, boolean isAuto, String filename) 
	 {
		 if( ( null == buffer ) || ( 0 == buffer.length) )
		 {
			 return	false;
		 }
		 mFilename = filename;
		 mIsAuto = isAuto;
		 mOffset = 0;
		 mReverseInfo.startPos = startPos;
		 
		 try 
		 {
			 String str = new String(buffer, charsetName);	//原始字符串
			 //str = str.replaceAll("(.)\\1+", "$1");		//将多个连续的相同字符替换为1个 比如abbbccd替换后为abcd
			 str = str.replaceAll("\r", "");				//去掉\r
			 str = str.replaceAll("\t{1,}", "\n");			//将多个连续的制表符替换为1个换行
			 str = str.replaceAll("\n{2,}", "\n");			//将多个连续的换行符替换为1个
			 str = str.replaceAll(" {2,}", " ");			//将多个连续的空格替换为1个
			 str = str.replaceAll("•", "·");				//因为这个GB18080是四字节编码，所以需要替换为2字节的。
			 
			 mMbBuf = str.getBytes(CHARSET_NAME);			//转换成指定编码
			 
			 //别的编码转为gb18030的时候可能会加上BOM，gb18030的BOM是0x84 0x31 0x95 0x33，使用的时候需要跳过BOM
			 if( ( mMbBuf.length >= 4 ) && ( -124 == mMbBuf[0] ) && ( 49 == mMbBuf[1] ) && ( -107 == mMbBuf[2] ) && ( 51 == mMbBuf[3] ) )
			 {
				 mOffset = 4;
				 if( mReverseInfo.startPos < mOffset )
				 {
					 mReverseInfo.startPos = mOffset;
				 }
			 }
		 } 
		 catch (UnsupportedEncodingException e) 
		 {
			 e.printStackTrace();
		 }
		 catch( Exception e )
		 {
			 e.printStackTrace();
		 }
		 
		 mMbBufLen = getEffectiveLength(mMbBuf);
		 if( mMbBufLen-mOffset <= 0 )
		 {
			 return	false;
		 }
		 
		 mLineNumber = lineNumber;
		 
		 mCheckSum = 0;//calcCheckSum( mMbBuf );	//计算CheckSum
		 
		 if( ( checksum != 0 ) && ( mCheckSum != checksum ) )
		 {
			 //return	false;
		 }
		 
		 mReverseInfo.len = len;
		 mSelectInfo.startPos = -1;
		 mSelectInfo.len = -1;
		 
		 mSplitInfoList.clear();
		 
		 if( !PublicUtils.checkIsTextFile(mMbBuf) )
		 {
			 mIsTextFile = false;
			 return	false;
		 }

		 mIsTextFile = true;
		 this.invalidate();
		 
		 return	true;
	 }

	 /**
	  * 得到从指定开始位置的上一个段落的长度
	  * @param endPos
	  * @return len
	  */
	 private int getPreParagraphLength( final int endPos ) 
	 {
		 int nEnd = endPos;
		 int i;
		 int count = 0;
		 byte b0, b1;
		 
		 if( CHARSET_NAME.equals("utf-16le") ) 
		 {
			 i = nEnd - 2;
			 while (i > 0) 
			 {
				 b0 = mMbBuf[i];
				 b1 = mMbBuf[i + 1];
				 //if( b0 == 0x0a && b1 == 0x00 && i != nEnd - 2 ) 
				 if( b0 == 0x0a && b1 == 0x00 )
				 {
					 count++;
					 if( count >= 2 )
					 {
						 i += 2;
						 break;
					 }
				 }
				 i--;
			 }
		 } 
		 else if( CHARSET_NAME.equals("utf-16be") ) 
		 {
			 i = nEnd - 2;
			 while( i > 0 ) 
			 {
				 b0 = mMbBuf[i];
				 b1 = mMbBuf[i + 1];
				 //if( b0 == 0x00 && b1 == 0x0a && i != nEnd - 2 ) 
				 if( b0 == 0x00 && b1 == 0x0a ) 
				 {
					 count++;
					 if( count >= 2 )
					 {
						 i += 2;
						 break;
					 }
				 }
				 i--;
			 }
		 } 
		 else 
		 {
			 i = nEnd - 1;
			 while( i > mOffset )
			 {
				 b0 = mMbBuf[i];
				 //if( b0 == 0x0a && i != nEnd - 1 ) 
				 if( b0 == 0x0a )
				 {	// 0x0a表示换行符
					 count++;
					 if( count >= 2 && mMbBuf[i+1] != 0x0d && mMbBuf[i+1] != 0x0a )
					 {
						 i++;
						 break;
					 }
				 }
				 i--;
			 }
		 }
		 
		 if( i < 0 )
			 i = 0;
		 
		 int nParaSize = nEnd - i;
		 
		 return	nParaSize;
	 }
	 
	 /**
	  * 得到从指定开始位置的下一个段落的长度
	  * 
	  * @param	startPos
	  * 
	  * @return	int
	  */
	 private int getNextParagraphLength( final int startPos ) 
	 {
		 int i = startPos;
		 byte b0, b1;
		 
		 //根据编码格式判断换行
		 if( CHARSET_NAME.equals("utf-16le") ) 
		 {
			 while( i < mMbBufLen - 1 ) 
			 {
				 b0 = mMbBuf[i++];
				 b1 = mMbBuf[i++];
				 if( b0 == 0x0a && b1 == 0x00 ) 
				 {
					 break;
				 }
			 }
		 } 
		 else if( CHARSET_NAME.equals("utf-16be") ) 
		 {
			 while( i < mMbBufLen - 1 ) 
			 {
				 b0 = mMbBuf[i++];
				 b1 = mMbBuf[i++];
				 if( b0 == 0x00 && b1 == 0x0a ) 
				 {
					 break;
				 }
			 }
		 } 
		 else 
		 {
			 while( i < mMbBufLen ) 
			 {
				 b0 = mMbBuf[i++];
				 if( b0 == 0x0a ) 
				 {
					 break;
				 }
			 }
		 }
		 
		 int len = i - startPos;
		 
		 return len;
	 }
	 
	 //分行
	 private void divideLines()
	 {
		 mPaint.setTextSize(mTextSize);
		 mPaint.setTypeface(Typeface.MONOSPACE);
		 float asciiWidth = mPaint.measureText(" ");	//一个ascii字符宽度
		 
		 int startPos = mOffset;
		 
		 while( startPos < mMbBufLen ) 
		 {
			 int len = getNextParagraphLength(startPos);
			 if( len <= 0 )
			 {
				 break;
			 }
			 else if( 1 == len )
			 {
				 if( 0x0a == mMbBuf[startPos+len-1] )
				 {
					 SplitInfo li = new SplitInfo(startPos, len);
					 mSplitInfoList.add(li);
					 startPos += len;						//每次读取后，记录结束点位置，该位置是段落结束位置
					 continue;
				 }
			 }
			 else if( 2 == len )
			 {
				 if( 0x0d == mMbBuf[startPos+len-2] && 0x0a == mMbBuf[startPos+len-1] )
				 {
					 SplitInfo li = new SplitInfo(startPos, len);
					 mSplitInfoList.add(li);
					 startPos += len;						//每次读取后，记录结束点位置，该位置是段落结束位置
					 continue;
				 }
			 }
			 
			 
			 byte[] buffer = new byte[len];
			 for( int i = 0; i < len; i++ )
			 {
				 buffer[i] = mMbBuf[startPos+i];
			 }
			 
			 int textWidth = 0;
			 int start = startPos;
			 int home = 0;
			 int i = 0;
			 for( i = 0; i < buffer.length; i++ )
			 {
				 if( 0x0d == buffer[i] || 0x0a == buffer[i] )
				 {
					 continue;
				 }
				 
				 if( buffer[i] < 0x80 && buffer[i] >= 0x0 )	//ascii
				 {
					 textWidth += ((int)asciiWidth);
					 if( textWidth > mVisibleWidth )
					 {
						 int length = i-home;
						 
						 SplitInfo li = new SplitInfo(start, length);
						 mSplitInfoList.add(li);
						 
						 start += length;
						 home = i;
						 i--;
						 textWidth = 0;
						 continue;
					 }
				 }
				 else
				 {
					 textWidth += (int)mTextSize;
					 if( textWidth > mVisibleWidth )
					 {
						 int length = i-home;
						 SplitInfo li = new SplitInfo(start, length);
						 mSplitInfoList.add(li);
						 
						 start += length;
						 home = i;
						 i--;
						 textWidth = 0;
						 continue;
					 }
					 i++;
				 }
			 }
			 
			 if( textWidth > 0 )
			 {
				 int length = i-home;
				 SplitInfo li = new SplitInfo(start, length);
				 mSplitInfoList.add(li);
				 
				 start += length;
				 textWidth = 0;
			 }
			 
			 startPos += len;						//每次读取后，记录结束点位置，该位置是段落结束位置
		 }
		 
		 calcCurPage();	//计算当前屏位置
	 }

	 //计算当前页
	 private void calcCurPage()
	 {
		 //mCurPage = mLineNumber / mLineCount + 1;	//计算当前屏位置
		 //计算当前页规则：以当前屏显示的行数较多的逻辑页号播报显示（逻辑页：对应分页预处理中的页号） 
		 
		 class PageInfo
		 {
			 int page;	//页码
			 int count;	//个数
			 
			 public PageInfo( int p, int c )
			 {
				 page = p;
				 count = c;
			 }
		 }
		 
		 int size = mSplitInfoList.size();
		 int maxLine = Math.min( size, mLineNumber+mLineCount );
		 HashMap<Integer, PageInfo> pageMap = new HashMap<Integer, PageInfo>();
		 
		 for( int i = mLineNumber; i < maxLine; i++ )
		 {
			 int curPage = i / mLineCount + 1;	//计算当前行在逻辑屏中的位置
			 
			 PageInfo pi = pageMap.get(curPage);
			 if( null == pi )
			 {
				 pi = new PageInfo( curPage, 1 );
				 pageMap.put(curPage, pi);
			 }
			 else
			 {
				 pi.count++;
				 pageMap.remove(curPage);
				 pageMap.put(curPage, pi);
			 }
		 }
		 
		 ArrayList<PageInfo> list = new ArrayList<PageInfo>();
		 Iterator<Integer> iterator = pageMap.keySet().iterator();
		 while(iterator.hasNext()) 
		 {
			 list.add(pageMap.get(iterator.next()));
		 }
		 
		 size = list.size();
		 PageInfo pi = null;
		 for( int i = 0; i < size; i++ )
		 {
			 if( null == pi )
			 {
				 pi = list.get(i);
			 }
			 else
			 {
				 if( pi.count < list.get(i).count )
				 {
					 pi = list.get(i);
				 }
			 }
		 }
		 
		 if( null == pi )
		 {
			 mCurPage = 1;
			 return;
		 }
		 mCurPage = pi.page;
		 
		 if( mOnPageFlingListener != null )
		 {
			 mOnPageFlingListener.onLoadCompleted(getPageCount(), mCurPage);
		 }
	 }
	 
	 //得到当前页
	 public int getCurPage()
	 {
		 return	mCurPage;
	 }
	 
	 //得到总页数
	 public int getPageCount()
	 {
		 return	( mSplitInfoList.size() + mLineCount - 1 ) / mLineCount;
	 }
	 
	 //设置页码
	 public boolean setCurPage( int page )
	 {
		 if( ( page < 1 ) || ( page > getPageCount() ) )
		 {
			 return	false;
		 }
		 
		 TTSUtils.getInstance().stop();
		 mCurPage = page;
		 mLineNumber = (mCurPage-1)*mLineCount;
		 mReverseInfo.startPos = mSplitInfoList.get(mLineNumber).startPos;
		 mReverseInfo.len = 0;
		 this.invalidate();
		 
		 initReverseInfo();	//初始化反显信息
		 
		 if( mOnPageFlingListener != null )
		 {
			 mOnPageFlingListener.onLoadCompleted(getPageCount(), mCurPage);
		 }
		 
		 return	true;
	 }
	 
	 /**
	  * 向后翻行
	  * 
	  */
	 private boolean nextLine()
	 {
		 if( mLineNumber+mLineCount >= mSplitInfoList.size() ) 
		 {
			 mIsLastPage = true;
			 return false;
		 } 
		 else
		 {
			 mIsLastPage = false;
		 }
		 
		 mLineNumber++;
		 calcCurPage();	//计算当前屏位置
		 
		 return	true;
	 }
	 
	 /**
	  * 向前翻行
	  * 
	  */
	 private boolean preLine()
	 {
		 if( mLineNumber <= 0 ) 
		 {
			 mLineNumber = 0;
			 mIsFirstPage = true;
			 
			 return	false;
		 } 
		 else
		 {
			 mIsFirstPage = false;
		 }

		 mLineNumber--;
		 calcCurPage();	//计算当前屏位置
		 
		 return	true;
	 }	 
	 
	 /**
	  * 向后翻页
	  * 
	  */
	 private boolean nextPage() 
	 {
		 if( mLineNumber+mLineCount >= mSplitInfoList.size() ) 
		 {
			 mIsLastPage = true;
			 return false;
		 } 
		 else
		 {
			 mIsLastPage = false;
		 }
		 
		 mLineNumber += mLineCount;
		 calcCurPage();	//计算当前屏位置
		 
		 return	true;
	 }
	 
	 /**
	  * 向前翻页
	  * 
	  */
	 private boolean prePage()
	 {
		 if( mLineNumber <= 0 ) 
		 {
			 mLineNumber = 0;
			 mIsFirstPage = true;
			 
			 return	false;
		 } 
		 else
		 {
			 mIsFirstPage = false;
		 }

		 mLineNumber -= mLineCount;
		 if( mLineNumber < 0 ) 
		 {
			 mLineNumber = 0;
		 }
		 
		 calcCurPage();	//计算当前屏位置
		 
		 return	true;
	 }

	 private void init(Context context) 
	 {
		 mWidth = getWidth();
		 mHeight = getHeight();
		 mVisibleWidth = mWidth - MARGIN_WIDTH * 2;
		 mVisibleHeight = mHeight - MARGIN_HEIGHT * 2;
		 mLineCount = (int)( mVisibleHeight / (mTextSize+mLineSpace ) ); 		//可显示的行数
		 
		 if( null == mCurPageBitmap )
		 {
			 mCurPageBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		 }
		 
		 if( null == mCurPageCanvas )
		 {
			 mCurPageCanvas = new Canvas(mCurPageBitmap);
		 }
		 
		 if( 0 == mSplitInfoList.size() )
		 {
			 divideLines();		//分行
			 initReverseInfo();	//初始化反显信息
			 
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onLoadCompleted(getPageCount(), mCurPage);
			 }
		 }
	 }
	 
	 //初始化反显信息
	 private void initReverseInfo()
	 {
		 if( mReverseInfo.startPos < mSplitInfoList.get(mLineNumber).startPos )
		 {
			 mReverseInfo.startPos = mOffset;
			 mReverseInfo.len = 0;
		 }	//如果反显开始于当前页之前，则从当前页开始反显。
		 
		 switch( mReadMode )
		 {
		 	case READ_MODE_ALL:			//全文朗读
		 		mParagraphStartPos = mOffset;
		 		mParagraphLength = 0;
		 		curSentence(true, false, false);
		 		break;
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		mParagraphStartPos = mOffset;
		 		mParagraphLength = 0;
		 		curParagraph(true);
		 		break;
		 	case READ_MODE_WORD:		//逐词朗读
		 		curWord(true);
		 		break;
		 	case READ_MODE_CHARACTER:	//逐字朗读
		 		curCharacter(true);
		 		break;
		 	default:
		 		break;
		 }
	 }
	 
	 @Override
	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	 {
		 int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		 int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		 int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		 int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		 int width;
		 int height ;
		 
		 Drawable bgDrawable = this.getBackground();
		 
		 if (widthMode == MeasureSpec.EXACTLY)	//一般是设置了明确的值或者是MATCH_PARENT
		 {
			 width = widthSize;
		 } 
		 else	//表示子布局限制在一个最大值内，一般为WARP_CONTENT
		 {
			 float bgWidth = bgDrawable.getIntrinsicWidth();
				
			 int desired = (int) (getPaddingLeft() + bgWidth + getPaddingRight());
			 width = desired;
		 }

		 if (heightMode == MeasureSpec.EXACTLY)	//一般是设置了明确的值或者是MATCH_PARENT
		 {
			 height = heightSize;
		 } 
		 else	//表示子布局限制在一个最大值内，一般为WARP_CONTENT
		 {
			 float bgHeight = bgDrawable.getIntrinsicHeight();
			 int desired = (int) (getPaddingTop() + bgHeight + getPaddingBottom());
			 height = desired;
		 }

		 setMeasuredDimension(width, height);
	 }
	 
	 @Override
	 protected void onDraw(Canvas canvas) 
	 {
		 // TODO Auto-generated method stub
		 super.onDraw(canvas);
		 
		 if( !mIsTextFile )
		 {
			 return;
		 }
		 
		 TTSUtils.getInstance().OnTTSListener(this);
		 init(mContext);

		 mCurPageCanvas.drawColor(mBkColor);	//先显示背景色
		 mPaint.setTextSize(mTextSize);			//字体大小
		 mPaint.setColor(mTextColor);			//字体颜色
		 
		 int size = mSplitInfoList.size();
		 
		 if( size > 0 ) 
		 {
			 Paint paint = new Paint();
			 paint.setColor(mReverseColor);
			 
			 //FontMetrics对象  
			 FontMetrics fontMetrics = mPaint.getFontMetrics(); 
			 /*
			 //计算每一个坐标  
			 float baseX = MARGIN_WIDTH;  
			 float baseY = MARGIN_HEIGHT;  
			 float topY = baseY + fontMetrics.top;  
			 float ascentY = baseY + fontMetrics.ascent;  
			 float descentY = baseY + fontMetrics.descent;  
			 float bottomY = baseY + fontMetrics.bottom;  
			 */
			 
			 float x = MARGIN_WIDTH;
			 float y = MARGIN_HEIGHT;
			 
			 for( int i = mLineNumber, j = 0; i < size && j < mLineCount; i++, j++ ) 
			 {
				 if( mReverseInfo.len > 0 )	//如果有反显
				 {
					 SplitInfo si = mSplitInfoList.get(i);	//得到当前行的信息
					 if( ( mReverseInfo.startPos >= si.startPos ) && ( mReverseInfo.startPos < si.startPos+si.len ) )	//反显开始在当前行
					 {
						 float xx = x;
						 String str = null;
						 
						 try 
						 {
							 str = new String(mMbBuf, si.startPos, mReverseInfo.startPos-si.startPos, CHARSET_NAME);	//转换成指定编码
						 } 
						 catch (UnsupportedEncodingException e) 
						 {
							 e.printStackTrace();
						 }
						 
						 if( !TextUtils.isEmpty(str) )
						 {
							 xx += mPaint.measureText(str);
						 }
						 
						 int len = Math.min(mReverseInfo.len, si.startPos+si.len-mReverseInfo.startPos);
						 
						 try 
						 {
							 str = new String(mMbBuf, mReverseInfo.startPos, len, CHARSET_NAME);	//转换成指定编码
						 } 
						 catch (UnsupportedEncodingException e) 
						 {
							 e.printStackTrace();
						 }
						 
						 if( "\r\n".equals(str) || "\n".equals(str) )	//如果是回车换行，则需要反显到行尾
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top)-1, getWidth()-MARGIN_WIDTH, y+(fontMetrics.descent-fontMetrics.top)+1 );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
						 else
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top)-1, (xx+mPaint.measureText(str)), y+(fontMetrics.descent-fontMetrics.top)+1 );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
					 }
					 else if( ( mReverseInfo.startPos < si.startPos ) && ( mReverseInfo.startPos+mReverseInfo.len-1 >= si.startPos ) )	//反显开始不在当前行，但在当前行有反显内容
					 {
						 float xx = x;
						 String str = null;
						 
						 int len = Math.min( si.len, mReverseInfo.startPos + mReverseInfo.len - si.startPos );
						 
						 try 
						 {
							 str = new String(mMbBuf, si.startPos, len, CHARSET_NAME);	//转换成指定编码
						 } 
						 catch (UnsupportedEncodingException e) 
						 {
							 e.printStackTrace();
						 }
						 
						 if( "\r\n".equals(str) || "\n".equals(str) )	//如果是回车换行，则需要反显到行尾
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top)-1, getWidth()-MARGIN_WIDTH, y+(fontMetrics.descent-fontMetrics.top)+1 );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
						 else
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top)-1, (xx+mPaint.measureText(str)), y+(fontMetrics.descent-fontMetrics.top)+1 );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
					 }
				 }
				 
				 float baseY = y - fontMetrics.top;
				 
				 mCurPageCanvas.drawText(getLineText(i), x, baseY, mPaint);	//drawText的坐标是baseX和baseY
				 
				 y += mTextSize;
				 y += mLineSpace;
			 }
		 }
		 
		 canvas.drawBitmap(mCurPageBitmap, 0, 0, null);
	 }
	 
	 @Override
	 public boolean onTouchEvent(MotionEvent event) 
	 {
		 return	mGestureDetector.onTouchEvent(event);
	 }

	 @Override
	 public boolean onDown(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
		 return true;
	 }

	 @Override
	 public void onShowPress(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
	 }
	 
	 @Override
	 public boolean onSingleTapUp(MotionEvent e) 
	 {
		 //TODO Auto-generated method stub
		 return false;
	 }

	 @Override
	 public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	 {
		 // TODO Auto-generated method stub
		 return false;
	 }

	 @Override
	 public void onLongPress(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
	 }

	 @Override
	 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	 {
		 // TODO Auto-generated method stub
		 final int FLING_MIN_DISTANCE_X = getWidth()/3;		//x方向最小滑动距离
		 final int FLING_MIN_DISTANCE_Y = getHeight()/3;	//y方向最新滑动距离
		 
		 if( e1.getX() - e2.getX() > FLING_MIN_DISTANCE_X )
		 {
			 right();	//向左滑动，向后翻页
		 }
		 else if( e2.getX() - e1.getX() > FLING_MIN_DISTANCE_X )
		 {
			 left();	//向右滑动，向前翻页
		 }
		 else if( e1.getY() - e2.getY() > FLING_MIN_DISTANCE_Y )
		 {
			 down();	//向上滑动，向后翻行
		 }
		 else if( e2.getY() - e1.getY() > FLING_MIN_DISTANCE_Y )
		 {
			 up();		//向下滑动，向前翻行
		 }
		 
		 return false; 
	 }

	 @Override
	 public boolean onSingleTapConfirmed(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
		 return false;
	 }

	 @Override
	 public boolean onDoubleTap(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
		 
		 enter();
		 
		 return false;
	 }

	 @Override
	 public boolean onDoubleTapEvent(MotionEvent e) 
	 {
		 // TODO Auto-generated method stub
		 return false;
	 }
 
	 //向前翻行
	 public void up()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 boolean isTop = false;	//是否到头了
		 
		 if( preLine() )
		 {
			 mReverseInfo.startPos = mOffset;
			 mReverseInfo.len = 0;
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 isTop = false;
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
			 }
			 
			 isTop = true;
		 }
		 
		 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
		 {
			 curSentence(false, isTop, false);
		 }
		 else
		 {
			 ReverseInfo ri = getNextReverseCharacterInfo( mSplitInfoList.get(mLineNumber).startPos );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToBottom(false);
				 }
			 }
			 else
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
			 }	//反显当前页第一行第一个字
			 
			 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
			 if( isTop )
			 {
				 tips = mContext.getString(R.string.ebook_to_top1) + tips;
			 }
			 speakTips(tips);
		 }
	 }
	 
	 //向后翻行
	 public void down()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);		
		 
		 if( nextLine() )
		 {
			 mReverseInfo.startPos = mOffset;
			 mReverseInfo.len = 0;
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
			 {
				 curSentence(false, false, false);
			 }
			 else
			 {
				 ReverseInfo ri = getNextReverseCharacterInfo( mSplitInfoList.get(mLineNumber).startPos );
				 if( null == ri )
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToBottom(false);
					 }
				 }
				 else
				 {
					 mReverseInfo.startPos = ri.startPos;
					 mReverseInfo.len = ri.len;
				 }	//反显当前页第一行第一个字
				 
				 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
				 speakTips(tips);
			 }
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
	 }
		 
	 //向前翻页
	 public void left()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 boolean isTop = false;	//是否到头了
		 
		 if( prePage() )
		 {
			 mReverseInfo.startPos = mOffset;
			 mReverseInfo.len = 0;
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 isTop = false;
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
			 }
			 
			 isTop = true;
		 }
		 
		 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
		 {
			 curSentence(false, isTop, false);
		 }
		 else
		 {
			 ReverseInfo ri = getNextReverseCharacterInfo( mSplitInfoList.get(mLineNumber).startPos );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToBottom(false);
				 }
			 }
			 else
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
			 }	//反显当前页第一行第一个字
			 
			 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
			 if( isTop )
			 {
				 tips = mContext.getString(R.string.ebook_to_top1) + tips;
			 }
			 speakTips(tips);
		 }
	 }
	 
	 //向后翻页
	 public void right()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 if( nextPage() )
		 {
			 mReverseInfo.startPos = mOffset;
			 mReverseInfo.len = 0;
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
			 {
				 curSentence(false, false, false);
			 }
			 else
			 {
				 ReverseInfo ri = getNextReverseCharacterInfo( mSplitInfoList.get(mLineNumber).startPos );
				 if( null == ri )
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToBottom(false);
					 }
				 }
				 else
				 {
					 mReverseInfo.startPos = ri.startPos;
					 mReverseInfo.len = ri.len;
				 }	//反显当前页第一行第一个字
				 
				 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips2), mCurPage );
				 speakTips(tips);
			 }
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
	 }
	 
	 //确定
	 public void enter()
	 {
		 setReadMode(ReadMode.READ_MODE_ALL);
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 
		 if( status == SpeakStatus.SPEAK )
		 {
			 TTSUtils.getInstance().pause();
		 }
		 else if( status == SpeakStatus.PAUSE )
		 {
			 TTSUtils.getInstance().resume();
		 }
		 else if( status == SpeakStatus.STOP )
		 {
			 curSentence(false, false, false);
		 }
	 }
	 
	 //精读
	 public void intensiveReading()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();	//当前朗读状态
		 if( SpeakStatus.SPEAK == status )	//如果当前正在朗读
		 {
			 TTSUtils.getInstance().pause();
		 }
		 else if( status == SpeakStatus.PAUSE )
		 {
			 TTSUtils.getInstance().resume();
		 }
		 else if( status == SpeakStatus.STOP )
		 {
			 switch( mReadMode )
			 {
			 	case READ_MODE_ALL:			//全文朗读
			 	case READ_MODE_PARAGRAPH:	//逐段朗读
			 		curSentence(false, false, false);
			 		break;
			 	case READ_MODE_WORD:		//逐词朗读
			 		curWord(false);
			 		break;
			 	case READ_MODE_CHARACTER:	//逐字朗读
			 		readExplain();
			 		break;
			 	default:
			 		break;
			 }
		 }	 
	 }
	 
	 //朗读例句
	 private void readExplain()
	 {
		 if( mReverseInfo.len > 0 )
		 {
			 char ch = PublicUtils.byte2char(mMbBuf, mReverseInfo.startPos);
			 if( ( ch >= 'A' ) && ( ch <= 'Z') )
			 {
				 ch += 0x20; 
			 }	//变为小写
			 
			 ArrayList<String> list = mMapWordExplain.get(ch);
			 if( ( null == list ) || ( 0 == list.size() ) )
			 {
				 byte[] explain = null;
				 
				 if( mMbBuf[mReverseInfo.startPos] < 0 )
				 {
					 explain = mWordExplainUtils.getWordExplain(0, ch);
				 }
				 else
				 {
					 explain = mWordExplainUtils.getWordExplain(1, ch);
				 }
				 
				 if( null == explain )
				 {
					 speakTips(mContext.getString(R.string.ebook_no_explain));
					 return;
				 }
				 else
				 {
					 String txt = null;
					 
					 try 
					 {
						 txt = new String(explain, CHARSET_NAME);	//转换成指定编码
					 } 
					 catch (UnsupportedEncodingException e) 
					 {
						 e.printStackTrace();
					 }
					 
					 if( TextUtils.isEmpty(txt) )
					 {
						 speakTips(mContext.getString(R.string.ebook_no_explain));
						 return;
					 }
					 else
					 {
						 String[] str = txt.split("=");
						 if( ( null == str ) || ( str.length < 2 ) )
						 {
							 speakTips(mContext.getString(R.string.ebook_no_explain));
							 return;
						 }
						 
						 String[] strExplain = str[1].split(" ");
						 if( ( null == strExplain ) || ( 0 == strExplain.length ) )
						 {
							 speakTips(mContext.getString(R.string.ebook_no_explain));
							 return;
						 }
						 
						 ArrayList<String> list2 = new ArrayList<String>();
						 
						 if( ( ch >= 'a' ) && ( ch <= 'z') )
						 {
							 for( int i = 0; i < strExplain.length; i++ )
							 {
								 list2.add(strExplain[i]);
							 }
							 list2.add(String.format(mContext.getResources().getString(R.string.ebook_en_explain_tips), ch-'a'+1));	//添加在字母表中的顺序
						 }
						 else
						 {
							 for( int i = 0; i < strExplain.length; i++ )
							 {
								 list2.add(strExplain[i]+mContext.getResources().getString(R.string.ebook_cn_explain_tips)+str[0]);
							 }
						 }
						 
						 mMapWordExplain.put(ch, list2);
					 }
				 }
			 }
			 
			 list = mMapWordExplain.get(ch);
			 if( ( list != null ) && ( list.size() > 0 ) )
			 {
				 speakTips(list.get(mCurReadExplainIndex));
				 if( mCurReadExplainIndex == list.size()-1 )
				 {
					 mCurReadExplainIndex = 0;
				 }
				 else
				 {
					 mCurReadExplainIndex++;
				 }
			 }
		 }
	 }
	 
	 //到上一个字符
	 public void preCharacter()
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_CHARACTER);
		 
		 int start = mReverseInfo.startPos;
		 if( start == mOffset )	//已经到顶了
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
				 speakTips(mContext.getString(R.string.ebook_to_top1));
				 
				 ReverseInfo ri = getNextReverseCharacterInfo( mOffset );
				 if( ri != null  )
				 {
					 mReverseInfo.startPos = ri.startPos;
					 mReverseInfo.len = ri.len;
					 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
					 this.invalidate();
				 }
			 }
			 return;
		 }

		 ReverseInfo oldReverseInfo = null;
		 
		 int len = getPreParagraphLength( mReverseInfo.startPos );	//得到上一个段落
		 
		 for( int i = mReverseInfo.startPos-len; i < mMbBufLen; )
		 {
			 ReverseInfo ri = getNextReverseCharacterInfo( i );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
					 speakTips(mContext.getString(R.string.ebook_to_top1));
					 
					 ReverseInfo ri2 = getNextReverseCharacterInfo( mOffset );
					 if( ri2 != null  )
					 {
						 mReverseInfo.startPos = ri2.startPos;
						 mReverseInfo.len = ri2.len;
						 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
						 this.invalidate();
					 }
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
				 readReverseText(false, false, false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = oldReverseInfo.startPos;
				 mReverseInfo.len = oldReverseInfo.len;
				 readReverseText(false, false, false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
		 }
	 }	 
	 
	 //到当前一个字符
	 public void curCharacter( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_CHARACTER);
		 
		 ReverseInfo ri = getNextReverseCharacterInfo( mReverseInfo.startPos );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage, false, false);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 this.invalidate();
		 }
	 }
	 
	 //到下一个字符
	 public void nextCharacter( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_CHARACTER);
		 
		 ReverseInfo ri = getNextReverseCharacterInfo( mReverseInfo.startPos+mReverseInfo.len );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage, false, false);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 this.invalidate();
		 }
	 }
	 
	 //到上一个单词
	 public void preWord()
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_WORD);

		 int start = mReverseInfo.startPos;
		 if( start == mOffset )	//已经到顶了
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
				 speakTips(mContext.getString(R.string.ebook_to_top1));
				 
				 ReverseInfo ri = getNextReverseWordInfo( mOffset );
				 if( ri != null )
				 {
					 mReverseInfo.startPos = ri.startPos;
					 mReverseInfo.len = ri.len;
					 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
					 this.invalidate();
				 }
			 }
			 return;
		 }

		 ReverseInfo oldReverseInfo = null;
		 
		 int len = getPreParagraphLength( mReverseInfo.startPos );	//得到上一个段落
		 
		 for( int i = mReverseInfo.startPos-len; i < mMbBufLen; )
		 {
			 ReverseInfo ri = getNextReverseWordInfo( i );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
					 speakTips(mContext.getString(R.string.ebook_to_top1));
					 
					 ReverseInfo ri2 = getNextReverseWordInfo( mOffset );
					 if( ri2 != null )
					 {
						 mReverseInfo.startPos = ri2.startPos;
						 mReverseInfo.len = ri2.len;
						 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
						 this.invalidate();
					 }
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 if( ( mOffset == ri.startPos ) && ( 0 == mReverseInfo.len ) )
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToTop();
						 speakTips(mContext.getString(R.string.ebook_to_top1));
						 
						 ReverseInfo ri2 = getNextReverseWordInfo( mOffset );
						 if( ri2 != null )
						 {
							 mReverseInfo.startPos = ri2.startPos;
							 mReverseInfo.len = ri2.len;
							 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
							 this.invalidate();
						 }
					 }
				 }
				 else
				 {
					 mReverseInfo.startPos = ri.startPos;
					 mReverseInfo.len = ri.len;
					 readReverseText(false, false, false);			//朗读反显文字
					 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
					 this.invalidate();
				 }
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 if( null == oldReverseInfo )
				 {
					 /*
					 mReverseInfo.startPos = mOffset;
					 mReverseInfo.len = 0;
					 */
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToTop();
						 speakTips(mContext.getString(R.string.ebook_to_top1));
						 
						 ReverseInfo ri2 = getNextReverseWordInfo( mOffset );
						 if( ri2 != null )
						 {
							 mReverseInfo.startPos = ri2.startPos;
							 mReverseInfo.len = ri2.len;
							 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
							 this.invalidate();
						 }
					 }
				 }
				 else
				 {
					 if( ( mOffset == ri.startPos ) && ( 0 == mReverseInfo.len ) )
					 {
						 if( mOnPageFlingListener != null )
						 {
							 mOnPageFlingListener.onPageFlingToTop();
							 speakTips(mContext.getString(R.string.ebook_to_top1));
							 
							 ReverseInfo ri2 = getNextReverseWordInfo( mOffset );
							 if( ri2 != null )
							 {
								 mReverseInfo.startPos = ri2.startPos;
								 mReverseInfo.len = ri2.len;
								 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
								 this.invalidate();
							 }
						 }
					 }
					 else
					 {
						 mReverseInfo.startPos = oldReverseInfo.startPos;
						 mReverseInfo.len = oldReverseInfo.len;
						 
						 readReverseText(false, false, false);			//朗读反显文字
						 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
						 this.invalidate();
					 }
				 }
				 
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
		 }
	 }	 	 
	 
	 //到当前一个单词
	 public void curWord( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_WORD);
		 
		 ReverseInfo ri = getNextReverseWordInfo( mReverseInfo.startPos );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage, false, false);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 this.invalidate();
		 }
	 }	 	 
	 
	 //到下一个单词
	 public void nextWord( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_WORD);
		 
		 ReverseInfo ri = getNextReverseWordInfo( mReverseInfo.startPos+mReverseInfo.len );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage, false, false);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 this.invalidate();
		 }
	 }
	 
	 //到上一个段落
	 public void preParagraph()
	 {
		 TTSUtils.getInstance().stop();
		 setReadMode(ReadMode.READ_MODE_PARAGRAPH);
		 
		 int end = mReverseInfo.startPos;
		 if( ( mOffset == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 end = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 int len = getPreParagraphLength( end );
		 if( 0 == len )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mReverseInfo = getNextReverseSentenceInfo( mOffset );
				 calcCurPage();
				 this.invalidate();
				 mOnPageFlingListener.onPageFlingToTop();
				 speakTips(mContext.getString(R.string.ebook_to_top1));
			 }
		 }
		 else
		 {
			 int newEnd = end-len;
			 
			 for( int i = 0; i < mSplitInfoList.size(); i++ )
			 {
				 if( mSplitInfoList.get(i).startPos == newEnd )
				 {
					 mReverseInfo.startPos = mOffset;
					 mReverseInfo.len = 0;
					 mLineNumber = i;
					 mParagraphStartPos = newEnd;
					 mParagraphLength = getNextParagraphLength(mParagraphStartPos);
					 
					 if( mParagraphStartPos+mParagraphLength > end )
					 {
						 mParagraphStartPos = mOffset;
						 mParagraphLength = 0;
						 if( mOnPageFlingListener != null )
						 {
							 mReverseInfo = getNextReverseSentenceInfo( mOffset );
							 calcCurPage();
							 this.invalidate();
							 mOnPageFlingListener.onPageFlingToTop();
							 speakTips(mContext.getString(R.string.ebook_to_top1));
						 }
					 }
					 else
					 {
						 if( isFilterParagraph( mParagraphStartPos, mParagraphLength ) )
						 {
							 preParagraph();	
							 return;
						 }	//跳过空行
						 
						 mIsPlayParagraph = false;
						 nextSentence( false, false, false );
					 }
					 return;
				 }
			 }
			 
			 if( mOnPageFlingListener != null )
			 {
				 mReverseInfo = getNextReverseSentenceInfo( mOffset );
				 calcCurPage();
				 this.invalidate();
				 mOnPageFlingListener.onPageFlingToTop();
				 speakTips(mContext.getString(R.string.ebook_to_top1));
			 }
		 }
	 }
	 
	 //到当前一个段落
	 public void curParagraph( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 setReadMode(ReadMode.READ_MODE_PARAGRAPH);
		 
		 int start = mReverseInfo.startPos;
		 if( ( mOffset == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 int len = getNextParagraphLength( start );
		 if( 0 == len )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 for( int i = mLineNumber; i < mSplitInfoList.size(); i++ )
			 {
				 if( mSplitInfoList.get(i).startPos == start )
				 {
					 mReverseInfo.startPos = mOffset;
					 mReverseInfo.len = 0;
					 mLineNumber = i;
					 mParagraphStartPos = start;
					 mParagraphLength = getNextParagraphLength(mParagraphStartPos);
					 
					 if( isFilterParagraph( mParagraphStartPos, mParagraphLength ) )
					 {
						 nextParagraph( isSpeakPage );	
						 return;
					 }	//跳过空行
					 
					 curSentence( isSpeakPage, false, false );
					 return;
				 }
			 }
			 
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
	 }
	 
	 //到下一个段落
	 public void nextParagraph( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 setReadMode(ReadMode.READ_MODE_PARAGRAPH);
		 
		 int start = mReverseInfo.startPos + mReverseInfo.len;
		 if( ( mOffset == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 int len = getNextParagraphLength( start );
		 if( ( 0 == len ) || ( start+len >= mMbBufLen ) )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
		 else
		 {
			 start += len;
			 
			 for( int i = mLineNumber; i < mSplitInfoList.size(); i++ )
			 {
				 if( mSplitInfoList.get(i).startPos == start )
				 {
					 mReverseInfo.startPos = mOffset;
					 mReverseInfo.len = 0;
					 mLineNumber = i;
					 mParagraphStartPos = start;
					 mParagraphLength = getNextParagraphLength(mParagraphStartPos);
					 
					 if( isFilterParagraph( mParagraphStartPos, mParagraphLength ) )
					 {
						 nextParagraph( isSpeakPage );	
						 return;
					 }	//跳过空行
					 
					 mIsPlayParagraph = isSpeakPage;
					 nextSentence( isSpeakPage, false, false );
					 return;
				 }
			 }
			 
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(false);
			 }
		 }
	 }
	 
	 //到上一个句子
	 private void preSentence()
	 {
		 int start = mReverseInfo.startPos;
		 if( start == mOffset )	//已经到顶了
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
				 speakTips(mContext.getString(R.string.ebook_to_top1));
			 }
			 return;
		 }

		 ReverseInfo oldReverseInfo = null;
		 
		 for( int i = mOffset; i < mMbBufLen; )
		 {
			 ReverseInfo ri = getNextReverseSentenceInfo( i );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
					 speakTips(mContext.getString(R.string.ebook_to_top1));
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
				 readReverseText(false, false, false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = oldReverseInfo.startPos;
				 mReverseInfo.len = oldReverseInfo.len;
				 readReverseText(false, false, false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 calcCurPage();
				 this.invalidate();
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
		 }
	 }	 
	 
	 //朗读当前句子
	 private void curSentence( boolean isSpeakPage, boolean isTop, boolean isBottom )
	 {
		 int start = mReverseInfo.startPos;
		 if( ( mOffset == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 if( ( ReadMode.READ_MODE_PARAGRAPH == mReadMode ) && ( start >= mParagraphStartPos + mParagraphLength ) && ( mParagraphLength > 0 ) )
		 {
			 return;
		 }
		 
		 ReverseInfo ri = getNextReverseSentenceInfo( start );
		 if( null == ri )
		 {
			 if( ReadMode.READ_MODE_PARAGRAPH == mReadMode )
			 {
				 preParagraph();
			 }
			 else if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(true);
			 }
		 }
		 else
		 {
			 if( ( ReadMode.READ_MODE_PARAGRAPH == mReadMode ) && ( ri.startPos + ri.len > mParagraphStartPos + mParagraphLength ) && ( mParagraphLength > 0 ) )
			 {
				 return;
			 }
			 
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage, isTop, isBottom);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 calcCurPage();
			 this.invalidate();
		 }
	 }
	 
	 //到下一个句子
	 private void nextSentence( boolean isSpeakPage, boolean isTop, boolean isBottom )
	 {
		 int start = mReverseInfo.startPos + mReverseInfo.len;
		 if( ( mOffset == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 if( ( ReadMode.READ_MODE_PARAGRAPH == mReadMode ) && ( start >= mParagraphStartPos + mParagraphLength ) && ( mParagraphLength > 0 ) )
		 {
			 return;
		 }
		 
		 ReverseInfo ri = getNextReverseSentenceInfo( start );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom(true);
			 }
		 }
		 else
		 {
			 if( ( ReadMode.READ_MODE_PARAGRAPH == mReadMode ) && ( ri.startPos + ri.len > mParagraphStartPos + mParagraphLength ) && ( mParagraphLength > 0 ) )
			 {
				 return;
			 }
			 
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 
			 if( ( ReadMode.READ_MODE_PARAGRAPH == mReadMode ) && !mIsPlayParagraph )
			 {
				 TTSUtils.getInstance().stop();
			 }
			 else
			 {
				 readReverseText(isSpeakPage, isTop, isBottom);			//朗读反显文字
			 }
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 calcCurPage();
			 this.invalidate();
		 }
	 }

	 //得到下一个字符反显信息
	 private ReverseInfo getNextReverseCharacterInfo( int start )
	 {
		 if( start == mMbBufLen )	//已经到底了
		 {
			 return	null;
		 }
		 
		 for( int i = start; i < mMbBufLen; i++ )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 ReverseInfo ri = new ReverseInfo(i, 2);
				 
				 return ri;
			 }
			 else if( mMbBuf[i] >= 0x0 && mMbBuf[i] < 0x20 )
			 {
				 if( 0x0d == mMbBuf[i] )
				 {
					 if( ( i+1 < mMbBufLen ) && ( 0x0a == mMbBuf[i+1] ) )
					 {
						 ReverseInfo ri = new ReverseInfo(i, 2);
						 
						 return	ri;
					 }
					 else
					 {
						 ReverseInfo ri = new ReverseInfo(i, 1);
						 
						 return	ri;
					 }
				 }
				 else if( 0x0a == mMbBuf[i] )
				 {
					 ReverseInfo ri = new ReverseInfo(i, 1);
					 
					 return	ri;
				 }
				 else
				 {
					 continue;
				 }
			 }
			 else
			 {
				 ReverseInfo ri = new ReverseInfo(i, 1);
				 
				 return	ri;
			 }
		 }
		 
		 return	null;
	 }
	 
	 //得到下一个词反显信息(逐词模式)
	 private ReverseInfo getNextReverseWordInfo( int start )
	 {
		 if( start == mMbBufLen )	//已经到底了
		 {
			 return	null;
		 }
		 
		 for( int i = start; i < mMbBufLen; i++ )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 ReverseInfo ri = new ReverseInfo(i, 2);
				 
				 char ch = PublicUtils.byte2char(mMbBuf, i);
				 for( int k = 0; k < CodeTableUtils.CODE.length; k++ )
				 {
					 //if( ( CodeTableUtils.CODE[k] == ch ) && ( 0xA1A1 != ch ) )			//过滤掉空格
					 if( ( CodeTableUtils.CODE[k] == ch ) )
					 {
						 return ri;
					 }
				 }	//如果是标点符号则反显这个标点符号
				 
				 for( int j = i+2; j < mMbBufLen; j+=2 )
				 {
					 if( mMbBuf[j] < 0 )
					 {
						 ch = PublicUtils.byte2char(mMbBuf, j);
						 for( int k = 0; k < CodeTableUtils.CODE.length; k++ )
						 {
							 //if( ( CodeTableUtils.CODE[k] == ch ) && ( 0xA1A1 != ch ) )	//过滤掉空格
							 if( ( CodeTableUtils.CODE[k] == ch ) )
							 {
								 return ri;
							 }
						 }	//如果是标点符号则返回前面的字符串
						 
						 ri.len += 2;
					 }
					 else
					 {
						 break;
					 }
				 }
				 
				 return ri;
			 }
			 else if( isAlpha( mMbBuf[i] ) || isNumber( mMbBuf[i] ) )	//英文或者数字
			 {
				 ReverseInfo ri = new ReverseInfo(i, 1);
				 for( int j = i+1; j < mMbBufLen; j++ )
				 {
					 if( isEscape( mMbBuf[j] ) )	//如果是转义字符
					 {
						 break;
					 }
					 else if( mMbBuf[j] < 0x0 )		//如果是中文字符
					 {
						 break;
					 }
					 else if( 0x20 == mMbBuf[j] )	//如果是空格
					 {
						 break;
					 }
					 else
					 {
						 if( 0x2E == mMbBuf[j] )
						 {
							 if( ( ( j+1 < mMbBufLen ) && ( ( 0x20 == mMbBuf[j+1] ) || ( 0x0d == mMbBuf[j+1] ) || ( 0x0a == mMbBuf[j+1] ) || ( mMbBuf[j+1] < 0 ) ) ) ||
									 ( j+1 == mMbBufLen ) )
							 {
								 return	ri;
							 }
						 }	//如果是.符号，则需要判断是否是结尾标志，如果不是就和单词放在一起。
						 else
						 {
							 for( int k = 0; k < CodeTableUtils.PunctuationEn.length; k++ )
							 {
								 if( CodeTableUtils.PunctuationEn[k] == mMbBuf[j] )
								 {
									 return	ri;
								 }	//如果是标点符号则返回前面的字符串
							 }
						 }
						 ri.len++;
					 }
				 }
				 
				 return	ri;
			 }
			 else if( mMbBuf[i] >= 0x0 && mMbBuf[i] < 0x20 )
			 {
				 if( 0x0d == mMbBuf[i] )
				 {
					 if( ( i+1 < mMbBufLen ) && ( 0x0a == mMbBuf[i+1] ) )
					 {
						 ReverseInfo ri = new ReverseInfo(i, 2);
						 
						 return	ri;
					 }
					 else
					 {
						 ReverseInfo ri = new ReverseInfo(i, 1);
						 
						 return	ri;
					 }
				 }
				 else if( 0x0a == mMbBuf[i] )
				 {
					 ReverseInfo ri = new ReverseInfo(i, 1);
					 
					 return	ri;
				 }
				 else
				 {
					 continue;
				 }
			 }
			 else if( 0x20 == mMbBuf[i] )	//跳过空格
			 {
				 continue;
			 }
			 else
			 {
				 ReverseInfo ri = new ReverseInfo(i, 1);
				 
				 return	ri;
			 }
		 }
		 
		 return	null;
	 }	 
	 
	 //是否要过滤掉此段
	 private boolean isFilterParagraph( int start, int len )
	 {
		 if( start == mMbBufLen )	//已经到底了
		 {
			 return	false;
		 }
		 
		 int end = start + len;
		 
		 for( int i  = start; i < end;  )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 boolean isBreak = false;
				 char ch = PublicUtils.byte2char(mMbBuf, i);
				 if(  0xA1A1 == ch )
				 {
					 i += 2;
					 continue;
				 }
				 for( int k = 0; k < CN_SEPARATOR.length; k++ )
				 {
					 if( CN_SEPARATOR[k] == ch )
					 {
						 isBreak = true;
						 break;
					 }
				 }	//此为汉字符号
				 
				 i += 2;
				 if( isBreak )
				 {
					 continue;
				 }
				 
				 return	false;	//证明此段中有可以朗读的汉字
			 }
			 else if( isAlpha( mMbBuf[i] ) || isNumber( mMbBuf[i] )  )	//英文或者数字
			 {
				 return	false;	//证明此段中有可以朗读的英文或者数字
			 }
			 else
			 {
				 i++;
			 }
		 }
		 
		 return	true;
	 }
	 
	 //得到下一个句子反显信息(逐段和全文模式)
	 private ReverseInfo getNextReverseSentenceInfo( int start )
	 {
		 if( start == mMbBufLen )	//已经到底了
		 {
			 return	null;
		 }
		 
		 for( int i = start; i < mMbBufLen; )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 ReverseInfo ri = new ReverseInfo(i, 2);
				 
				 boolean isBreak = false;
				 char ch1 = PublicUtils.byte2char(mMbBuf, i);
				 i += 2;
				 
				 if( 0xA3A4 == ch1 && isNumber(mMbBuf[i]) )	//￥符号
				 {
					 for( int k = i; k < mMbBufLen; )
					 {
						 if( mMbBuf[k] < 0 )
						 {
							 char c = PublicUtils.byte2char(mMbBuf, k);
							 if( 0xA3AC == c )
							 {
								 k += 2;
								 i += 2;
								 ri.len += 2;
							 }
							 else
							 {
								 break;
							 }
						 }
						 else if( isNumber(mMbBuf[k]) || ( 0x2C == mMbBuf[k] ) )
						 {
							 k++;
							 i++;
							 ri.len++;
						 }
						 else
						 {
							 break;
						 }
					 }
				 }
				 else
				 {
					 for( int k = 0; k < CN_SEPARATOR.length; k++ )
					 {
						 if( CN_SEPARATOR[k] == ch1 )
						 {
							 isBreak = true;
							 break;
						 }
					 }	//如果一开始就是点符号则跳过反显这个点符号
				 }
				 
				 for( int k = 0; k < CodeTableUtils.CODE.length; k++ )
				 {
					 if( ( CodeTableUtils.CODE[k] == ch1 ) && ( i < mMbBufLen ) && ( 0x0d == mMbBuf[i] || 0x0a == mMbBuf[i] ) )
					 {
						 isBreak = true;
						 break;
					 }
				 }	//如果一开始就是标点符号并且后面没有有用字符，则跳过。
				 
				 if( isBreak )
				 {
					 continue;
				 }
				 
				 for( int j = i; j < mMbBufLen; )
				 {
					 if( mMbBuf[j] < 0 )
					 {
						 ri.len += 2;
						 char ch2 = PublicUtils.byte2char(mMbBuf, j);
						 for( int k = 0; k < CN_SEPARATOR.length; k++ )
						 {
							 if( CN_SEPARATOR[k] == ch2 )
							 {
								 return ri;
							 }
						 }	//如果是点符号则返回前面的字符串
						 
						 j += 2;
						 
						 if( 0xA3A4 == ch2 )	//￥符号
						 {
							 for( int k = j; k < mMbBufLen; )
							 {
								 if( mMbBuf[k] < 0 )
								 {
									 char c = PublicUtils.byte2char(mMbBuf, k);
									 if( 0xA3AC == c )
									 {
										 k += 2;
										 j += 2;
										 ri.len += 2;
									 }
									 else
									 {
										 break;
									 }
								 }
								 else if( isNumber(mMbBuf[k]) || ( 0x2C == mMbBuf[k] ) )
								 {
									 k++;
									 j++;
									 ri.len++;
								 }
								 else
								 {
									 break;
								 }
							 }
						 }
					 }
					 else if( isEscape( mMbBuf[j] ) )	//如果是转义字符
					 {
						 return ri;
					 }
					 else if( 0x24 == mMbBuf[j] )	//美元符号'$'
					 {
						 ri.len++;
						 j++;
						 
						 for( int k = j; k < mMbBufLen; )
						 {
							 if( isNumber(mMbBuf[k]) || ( 0x2E == mMbBuf[k] ) || ( 0x2C == mMbBuf[k] ) )
							 {
								 k++;
								 j++;
								 ri.len++;
							 }
							 else
							 {
								 break;
							 }
						 }
					 }
					 else
					 {
						 ri.len++;
						 
						 for( int k = 0; k < EN_SEPARATOR.length; k++ )
						 {
							 if( EN_SEPARATOR[k] == mMbBuf[j] )
							 {
								 return ri;
							 }
						 }
						 
						 if( ( 0x2E == mMbBuf[j] ) && ( j+1 < mMbBufLen ) && ( 0x20 == mMbBuf[j+1] ) )
						 {
							 return	ri;
						 }
						 
						 j++;
					 }
				 }
				 
				 return ri;
			 }
			 else if( isAlpha( mMbBuf[i] ) || isNumber( mMbBuf[i] ) || isEnSeparator( mMbBuf[i] ) )	//英文或者数字或者英文符号
			 {
				 ReverseInfo ri = new ReverseInfo(i, 1);
				 i++;
				 for( int j = i; j < mMbBufLen; )
				 {
					 if( mMbBuf[j] < 0 )
					 {
						 ri.len += 2;
						 
						 char ch = PublicUtils.byte2char(mMbBuf, j);
						 for( int k = 0; k < CN_SEPARATOR.length; k++ )
						 {
							 if( CN_SEPARATOR[k] == ch )
							 {
								 return ri;
							 }
						 }	//如果是点符号则返回前面的字符串
						 
						 j += 2;
					 }
					 else if( isEscape( mMbBuf[j] ) )	//如果是转义字符
					 {
						 return ri;
					 }
					 else
					 {
						 ri.len++;
						 
						 if( ( 0x3A == mMbBuf[j] ) && ( j+1 < mMbBufLen ) && isNumber( mMbBuf[j-1] ) && isNumber( mMbBuf[j+1] ) )	//是数字
						 {
							 
						 }
						 else
						 {
							 for( int k = 0; k < EN_SEPARATOR.length; k++ )
							 {
								 if( EN_SEPARATOR[k] == mMbBuf[j] )
								 {
									 
									 return ri;
								 }
							 }
						 }
						 
						 if( ( 0x2E == mMbBuf[j] ) && ( j+1 < mMbBufLen ) && ( 0x20 == mMbBuf[j+1] ) )
						 {
							 return	ri;
						 }
						 
						 j++;
					 }
				 }
				 
				 return	ri;
			 }
			 else if( 0x24 == mMbBuf[i] )	//美元符号'$'
			 {
				 ReverseInfo ri = new ReverseInfo(i, 1);
				 i++;
				 
				 if( isNumber(mMbBuf[i]) )
				 {
					 for( int k = i; k < mMbBufLen; )
					 {
						 if( isNumber(mMbBuf[k]) || ( 0x2E == mMbBuf[k] ) || ( 0x2C == mMbBuf[k] ) )
						 {
							 k++;
							 i++;
							 ri.len++;
						 }
						 else
						 {
							 return ri;
						 }
					 }
				 }
			 }
			 else
			 {
				 i++;
			 }
		 }
		 
		 return	null;
	 }
	 
	 //朗读反显文字
	 private void readReverseText( boolean isSpeakPage, boolean isTop, boolean isBottom )
	 {
		 if( mReverseInfo.len <= 0 )	//如果没有反显
		 {
			 if( isSpeakPage )
			 {
				 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips), mCurPage, getPageCount() );
				 if( mIsAuto && !TextUtils.isEmpty(mFilename))
				 {
					 mIsAuto = false;
					 tips = mFilename+"，"+tips;
				 }
				 
				 if( isTop )
				 {
					 tips = mContext.getString(R.string.ebook_to_top1) + tips;
				 }
				 else if( isBottom )
				 {
					 tips = mContext.getString(R.string.ebook_to_bottom1) + tips;
				 }
				 
				 speakTips(tips);
			 }
			 else
			 {
				 String tips = "";
				 if( isTop )
				 {
					 tips = mContext.getString(R.string.ebook_to_top1) + tips;
				 }
				 else if( isBottom )
				 {
					 tips = mContext.getString(R.string.ebook_to_bottom1) + tips;
				 }
				 
				 speakTips(tips);
			 }
			 return;
		 }
		 
		 String str = null;
		 
		 if( ( ( mMbBuf[mReverseInfo.startPos] >= 0 ) && ( 1 == mReverseInfo.len ) ) ||		//英文
				 ( ( mMbBuf[mReverseInfo.startPos] < 0 ) && ( 2 == mReverseInfo.len ) ) ||	//中文
				 ( ( 2 == mReverseInfo.len ) && ( 0x0d == mMbBuf[mReverseInfo.startPos] ) && ( 0x0a == mMbBuf[mReverseInfo.startPos+1] ) ) )	
		 {
			 Locale locale = mContext.getResources().getConfiguration().locale;
			 String language = locale.getLanguage();
			 
			 char code = PublicUtils.byte2char(mMbBuf, mReverseInfo.startPos);
			 if( "en".equalsIgnoreCase(language) )	//英文
			 {
				 str = CodeTableUtils.getEnString(code);
			 }
			 else
			 {
				 str = CodeTableUtils.getCnString(code);
			 }
		 }
		 else if( ( mMbBuf[mReverseInfo.startPos] < 0 ) && ( mReverseInfo.len % 2  == 0 ) )
		 {
			 boolean isSpace = true;	//是否汉字空格
			 for( int i = mReverseInfo.startPos; i < mReverseInfo.startPos+mReverseInfo.len; i++ )
			 {
				 if( -95 != mMbBuf[i] )
				 {
					 isSpace = false;
					 break;
				 }
			 }	//判断是否全是A1A1
			 
			 if( isSpace )
			 {
				 Locale locale = mContext.getResources().getConfiguration().locale;
				 String language = locale.getLanguage();
				 str = "";
				 for( int i = 0; i < mReverseInfo.len/2; i++ )
				 {
					 char code = 0xA1A1;
					 if( "en".equalsIgnoreCase(language) )	//英文
					 {
						 str += CodeTableUtils.getEnString(code);
					 }
					 else
					 {
						 str += CodeTableUtils.getCnString(code);
					 }
				 }
			 }
		 }
		 
		 if( null != str )
		 {
			 if( isTop )
			 {
				 str = mContext.getString(R.string.ebook_to_top1) + str;
			 }
			 else if( isBottom )
			 {
				 str = mContext.getString(R.string.ebook_to_bottom1) + str;
			 }
			 speakContent(str);
		 }
		 else
		 {
			 try 
			 {
				 String text = new String(mMbBuf, mReverseInfo.startPos, mReverseInfo.len, CHARSET_NAME);	//转换成指定编码
				 if( isSpeakPage )
				 {
					 String tips = String.format(mContext.getResources().getString(R.string.ebook_page_read_tips), mCurPage, getPageCount() );
					 if( mIsAuto && !TextUtils.isEmpty(mFilename))
					 {
						 mIsAuto = false;
						 tips = mFilename+"，"+tips;
					 }
					 
					 String content = tips+text;
					 if( isTop )
					 {
						 content = mContext.getString(R.string.ebook_to_top1) + content;
					 }
					 else if( isBottom )
					 {
						 content = mContext.getString(R.string.ebook_to_bottom1) + content;
					 }
					 
					 speakContent(content);
				 }
				 else
				 {
					 if( isTop )
					 {
						 text = mContext.getString(R.string.ebook_to_top1) + text;
					 }
					 else if( isBottom )
					 {
						 text = mContext.getString(R.string.ebook_to_bottom1) + text;
					 }
					 speakContent(text);
				 }
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 e.printStackTrace();
			 }
		 }
	 }
	 
	 //根据反显位置重新计算当前页起始位置(行号)	//无用了
	 private void recalcLineNumber( Action action )
	 {
		 recalcLineNumberEx(0,0,0);
		 /*
		 if( mReverseInfo.len <= 0 )	//如果没有反显
		 {
			 return;
		 }
		 
		 int size = mSplitInfoList.size();
		 SplitInfo si = null;
		 
		 switch( action )
		 {
		 	case NEXT_LINE:	//下一行
		 	case NEXT_PAGE:	//下一页
		 		while( true )
		 		{
			 		int curPageLine = Math.min( mLineCount, (size-mLineNumber) );	//当前屏最大行数
					 
			 		si = mSplitInfoList.get(mLineNumber+curPageLine-1);				//得到当前屏最后一行的信息
			 		if( ( mReverseInfo.startPos >= si.startPos+si.len ) || ( mReverseInfo.startPos + mReverseInfo.len > si.startPos + si.len ) )	//反显开始在下一页，或者延伸到下一页
			 		{
			 			if( Action.NEXT_LINE == action )
			 			{
				 			if( nextLine() )
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 				}
				 			}
				 			else
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingToBottom();
				 				}
				 				break;
				 			}
			 			}	//将内容翻到下一行
			 			else
			 			{
			 				if( nextPage() )
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 				}
				 			}
				 			else
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingToBottom();
				 				}
				 				break;
				 			}
			 			}	//将内容翻到下一页
			 		}
			 		else
			 		{
			 			calcCurPage();	//计算当前屏位置
			 			if( mOnPageFlingListener != null )
		 				{
		 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
		 				}
			 			break;
			 		}
		 		}
		 		break;
		 	case PRE_LINE:	//上一行
		 	case PRE_PAGE:	//上一页
		 		while( true )
		 		{
			 		si = mSplitInfoList.get(mLineNumber);							//得到当前屏第一行的信息
			 		if( mReverseInfo.startPos < si.startPos )						//反显开始在上一页
			 		{
			 			if( Action.PRE_LINE == action )
			 			{
				 			if( preLine() )
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 				}
				 			}
				 			else
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingToTop();
				 					speakTips(mContext.getString(R.string.ebook_to_top1));
				 				}
				 				break;
				 			}
			 			}	//将内容翻到上一行
			 			else
			 			{
			 				if( prePage() )
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 				}
				 			}
				 			else
				 			{
				 				if( mOnPageFlingListener != null )
				 				{
				 					mOnPageFlingListener.onPageFlingToTop();
				 					speakTips(mContext.getString(R.string.ebook_to_top1));
				 				}
				 				break;
				 			}
			 			}	//将内容翻到下一页
			 		}
			 		else
			 		{
			 			calcCurPage();	//计算当前屏位置
			 			if( mOnPageFlingListener != null )
		 				{
		 					mOnPageFlingListener.onPageFlingCompleted(mCurPage);
		 				}
			 			break;
			 		}
		 		}
		 		break;
		 	default:
		 		break;
		 }
		 */
	 }
	 
	 //根据反显位置重新计算当前页起始位置(行号)
	 private void recalcLineNumberEx(int percent, int beginPos, int endPos)
	 {
		 if( mReverseInfo.len <= 0 )	//如果没有反显
		 {
			 return;
		 }
		 
		 if( mSplitInfoList.isEmpty() )
		 {
			 return;
		 }
		 
		 /*
		 后鼎当时是在语点中测试停止时，发音进度百分比与要发音的字符串长度乘积得到停止发音位置：
		 int l = speakText.length;
		 int j = (l * speakProgress + 50)/ 100;
		 String s = speakText.substring(0, j);
		 在停止发音时，我显示字符串s，发现发音停止位置与s显示位置基本吻合，前后相差一个字符。
		 */
		 
		 int length = (mReverseInfo.len * percent + 50)/ 100;		 
		 int size = mSplitInfoList.size();
		 
		 //Log.e( TAG, "wzp debug 0000000000 percent = "+percent+"  beginPos = "+beginPos+"  endPos = "+endPos+"  length = "+length);
		 
		 int curPageLine = Math.min( mLineCount, (size-mLineNumber) );		//当前屏最大行数
		 SplitInfo siBegin = mSplitInfoList.get(mLineNumber);				//得到当前屏第一行的信息
		 SplitInfo siEnd = mSplitInfoList.get(mLineNumber+curPageLine-1);	//得到当前屏最后一行的信息
			 
		 if( ( mReverseInfo.startPos >= siBegin.startPos ) && ( (mReverseInfo.startPos + mReverseInfo.len) <= (siEnd.startPos + siEnd.len) ) )	//反显完全在当前页
		 {
			 return;
		 }
		 else if( mReverseInfo.startPos > (siEnd.startPos + siEnd.len) )	//反显开始于当前页之后
		 {
			 if( nextPage() )
			 {
				 this.invalidate();
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 }
			 }
			 else
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToBottom(true);
				 }
			 }
		 }
		 else if( (mReverseInfo.startPos + mReverseInfo.len) < siBegin.startPos )	//反显结束于当前页之前
		 {
			 if( prePage() )
			 {
				 this.invalidate();
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
				 }
			 }
			 else
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
					 speakTips(mContext.getString(R.string.ebook_to_top1));
				 }
			 }
		 }
		 else if( ( mReverseInfo.startPos < siBegin.startPos ) && ( (mReverseInfo.startPos + mReverseInfo.len) <= (siEnd.startPos + siEnd.len) ) )	//反显开始于当前页之前并且结束于当前页
		 {
			 if( (mReverseInfo.startPos + length) < siBegin.startPos )	//如果当前朗读到的位置已经在上一页，则需要翻页
			 {
				 if( prePage() )
				 {
					 this.invalidate();
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
					 }
				 }
				 else
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToTop();
						 speakTips(mContext.getString(R.string.ebook_to_top1));
					 }
				 }
			 }
		 }
		 else if( ( mReverseInfo.startPos >= siBegin.startPos ) && ( (mReverseInfo.startPos + mReverseInfo.len) > (siEnd.startPos + siEnd.len) ) )	//反显开始于当前页并且结束于当前页之后
		 {
			 if( (mReverseInfo.startPos + length) > (siEnd.startPos + siEnd.len) )	//如果当前朗读到的位置已经在下一页，则需要翻页
			 {
				 if( nextPage() )
				 {
					 this.invalidate();
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
					 }
				 }
				 else
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToBottom(true);
					 }
				 }
			 }
		 }
		 else	//反显开始于当前页之前并且结束于当前页之后
		 {
			 if( (mReverseInfo.startPos + length) > (siEnd.startPos + siEnd.len) )	//如果当前朗读到的位置已经在下一页，则需要翻页
			 {
				 if( nextPage() )
				 {
					 this.invalidate();
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
					 }
				 }
				 else
				 {
					 if( mOnPageFlingListener != null )
					 {
						 mOnPageFlingListener.onPageFlingToBottom(true);
					 }
				 }
			 }
		 }
	 }
	 
	 //是否是英文字符
	 private boolean isAlpha( byte ch )
	 {
		 if( ( ch >= 'a' && ch <= 'z' ) || ( ch >= 'A' && ch <= 'Z' ) )
		 {
			 return	true;
		 }
		 
		 return	false;
	 }
	 
	 //是否是数字字符
	 private boolean isNumber( byte ch )
	 {
		 if( ch >= '0' && ch <= '9' )
		 {
			 return	true;
		 }
		 
		 return	false;
	 }
	 
	 //是否是特殊的转义字符，比如换行符/回车符/制表符
	 private boolean isEscape( byte ch )
	 {
		 if( 0x07 == ch || 0x08 == ch || 0x09 == ch || 0x0a == ch || 0x0b == ch || 0x0c == ch || 0x0d == ch )
		 {
			 return	true;
		 }
		 
		 return	false;
	 }
	 
	 //是否是英文符号
	 private boolean isEnSeparator( byte ch)
	 {
		 for( int k = 0; k < EN_SEPARATOR.length; k++ )
		 {
			 if( EN_SEPARATOR[k] == ch )
			 {
				 return true;
			 }
		 }
		 
		 return	false;
	 }
	 
	 private enum Action
	 {
		 NEXT_LINE, 	//下一行
		 NEXT_PAGE,		//下一页
		 PRE_LINE,		//上一行
		 PRE_PAGE,		//上一页
	 }

	//朗读完成
	@Override
	public void onSpeakCompleted() 
	{
		// TODO Auto-generated method stub
		if( mSplitInfoList.size() > 0 )
		{
			mHandler.sendEmptyMessage(MSG_SPEAK_COMPLETED);
		}
	}

	//朗读错误
	@Override
	public void onSpeakError() 
	{
		// TODO Auto-generated method stub
		if( mSplitInfoList.size() > 0 )
		{
			mHandler.sendEmptyMessage(MSG_SPEAK_ERROR);
		}
	}
	
	//发音进度
	@Override
	public void onSpeakProgress(int percent, int beginPos, int endPos) 
	{
		// TODO Auto-generated method stub
		if( TTSUtils.getInstance().getSpeakStatus() == SpeakStatus.SPEAK )
		{
			mPercent = percent;
			recalcLineNumberEx( percent, beginPos, endPos );
		}
	}
		
	private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
                case MSG_SPEAK_COMPLETED:	//朗读完成
                	switch( mReadMode )
            		{
            			case READ_MODE_ALL:			//全文朗读
            		 	case READ_MODE_PARAGRAPH:	//逐段朗读
            		 		nextSentence(false, false, false);
            		 		break;
            		 	case READ_MODE_WORD:		//逐词朗读
            		 		break;
            		 	case READ_MODE_CHARACTER:	//逐字朗读
            		 		break;
            		 	default:
            		 		break;
            		 }
                    break;
                case MSG_SPEAK_ERROR:		//朗读错误                	
                    break;
                default:
                    break;
            }
            return false;
        }
    });
	
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	private void speakContent( final String text ) 
	{
		mPercent = 0;
		mSpeakText = text;
		TTSUtils.getInstance().speakContent(text);
    }
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	private void speakTips( final String text ) 
	{
		mPercent = 0;
		mSpeakText = null;
		TTSUtils.getInstance().speakTips(text);
    }
}