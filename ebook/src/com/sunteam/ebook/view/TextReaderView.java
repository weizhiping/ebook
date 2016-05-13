package com.sunteam.ebook.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
import android.text.TextUtils;
import android.util.AttributeSet;
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
	 private static final float MARGIN_WIDTH = 0;		//左右与边缘的距离
	 private static final float MARGIN_HEIGHT = 0;		//上下与边缘的距离
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
	 
	 public interface OnPageFlingListener 
	 {
		 public void onLoadCompleted( int pageCount, int curPage );		//加载完成
		 public void onPageFlingToTop();	//翻到头了
		 public void onPageFlingToBottom();	//翻到尾了
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
		 
		 final float scale = context.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数
		 
		 mLineSpace *= scale;		//行间距
		 mTextSize *= scale;		//字体大小
		 
		 mWordExplainUtils.init(mContext);			//初始化例句
		 
		 TTSUtils.getInstance().OnTTSListener(this);
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
	 public boolean openBook(byte[] buffer, String charsetName, int lineNumber, int startPos, int len, int checksum) 
	 {
		 if( CHARSET_NAME.equalsIgnoreCase(charsetName) )
		 {
			 mMbBuf = buffer;
		 }
		 else
		 {
			 try 
			 {
				 mMbBuf = new String(buffer, charsetName).getBytes(CHARSET_NAME);	//转换成指定编码
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 e.printStackTrace();
			 }
			 
			 //别的编码转为gb18030的时候可能会加上BOM，gb18030的BOM是0x84 0x31 0x95 0x33，使用的时候需要跳过BOM
			 if( ( mMbBuf.length >= 4 ) && ( -124 == mMbBuf[0] ) && ( 49 == mMbBuf[1] ) && ( -107 == mMbBuf[2] ) && ( 51 == mMbBuf[3] ) )
			 {
				 mOffset = 4;
				 mReverseInfo.startPos = mOffset;
			 }
		 }
		 
		 mMbBufLen = (int)mMbBuf.length;
		 mLineNumber = lineNumber;
		 
		 mCheckSum = 0;//calcCheckSum( mMbBuf );	//计算CheckSum
		 
		 if( ( checksum != 0 ) && ( mCheckSum != checksum ) )
		 {
			 return	false;
		 }
		 
		 mReverseInfo.startPos = startPos;
		 mReverseInfo.len = len;
		 
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
					 if( 2 == count )
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
					 if( 2 == count )
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
			 while( i > 0 )
			 {
				 b0 = mMbBuf[i];
				 //if( b0 == 0x0a && i != nEnd - 1 ) 
				 if( b0 == 0x0a )
				 {	// 0x0a表示换行符
					 count++;
					 if( 2 == count )
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
		 
		 mCurPage = mLineNumber / mLineCount + 1;	//计算当前屏位置
	 }

	 //得到总页数
	 public int getPageCount()
	 {
		 return	( mSplitInfoList.size() + mLineCount - 1 ) / mLineCount;
	 }
	 
	 /**
	  * 向后翻行
	  * 
	  */
	 public boolean nextLine()
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
		 mCurPage = mLineNumber / mLineCount + 1;	//计算当前屏位置
		 
		 return	true;
	 }
	 
	 /**
	  * 向前翻行
	  * 
	  */
	 public boolean preLine()
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
		 mCurPage = mLineNumber / mLineCount + 1;	//计算当前屏位置
		 
		 return	true;
	 }	 
	 
	 /**
	  * 向后翻页
	  * 
	  */
	 public boolean nextPage() 
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
		 mCurPage++;
		 
		 return	true;
	 }
	 
	 /**
	  * 向前翻页
	  * 
	  */
	 public boolean prePage()
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
		 
		 mCurPage--;
		 
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
		 switch( mReadMode )
		 {
		 	case READ_MODE_ALL:			//全文朗读
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		nextSentence(true);
		 		break;
		 	case READ_MODE_WORD:		//逐词朗读
		 		nextWord(true);
		 		break;
		 	case READ_MODE_CHARACTER:	//逐字朗读
		 		nextCharacter(true);
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
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top), getWidth()-MARGIN_WIDTH, y+(fontMetrics.descent-fontMetrics.top) );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
						 else
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top), (xx+mPaint.measureText(str)), y+(fontMetrics.descent-fontMetrics.top) );
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
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top), getWidth()-MARGIN_WIDTH, y+(fontMetrics.descent-fontMetrics.top) );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
						 else
						 {
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top), (xx+mPaint.measureText(str)), y+(fontMetrics.descent-fontMetrics.top) );
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
		 
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 
		 if( preLine() )
		 {
			 mCurReadExplainIndex = 0;
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
			 }
		 }
		 
		 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
		 {
			 nextSentence(false);
		 }
		 else
		 {
			 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips2), mCurPage );
			 TTSUtils.getInstance().speakTips(tips);
		 }
	 }
	 
	 //向后翻行
	 public void down()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 
		 if( nextLine() )
		 {
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
			 {
				 nextSentence(false);
			 }
			 else
			 {
				 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips2), mCurPage );
				 TTSUtils.getInstance().speakTips(tips);
			 }
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
	 }
		 
	 //向前翻页
	 public void left()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 
		 if( prePage() )
		 {
			 mCurReadExplainIndex = 0;
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
			 }
		 }
		 
		 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
		 {
			 nextSentence(false);
		 }
		 else
		 {
			 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips2), mCurPage );
			 TTSUtils.getInstance().speakTips(tips);
		 }
	 }
	 
	 //向后翻页
	 public void right()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 TTSUtils.getInstance().stop();
		 
		 setReadMode(ReadMode.READ_MODE_ALL);
		 
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 
		 if( nextPage() )
		 {
			 mCurReadExplainIndex = 0;
			 this.invalidate();
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingCompleted(mCurPage);
			 }
			 
			 if( SpeakStatus.SPEAK == status )	//如果中断前是朗读状态
			 {
				 nextSentence(false);
			 }
			 else
			 {
				 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips2), mCurPage );
				 TTSUtils.getInstance().speakTips(tips);
			 }
		 }
		 else
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
	 }
	 
	 //确定
	 public void enter()
	 {
		 SpeakStatus status = TTSUtils.getInstance().getSpeakStatus();
		 
		 if( ReadMode.READ_MODE_ALL == mReadMode )	//如果当前是全文朗读模式
		 {
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
				 nextSentence(false);
			 }
		 }
		 else
		 {
			 setReadMode(ReadMode.READ_MODE_ALL);
			 if( status == SpeakStatus.STOP )
			 {
				 nextSentence(false);
			 }
			 else
			 {
				 TTSUtils.getInstance().stop();
			 }
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
			 		nextSentence(false);
			 		break;
			 	case READ_MODE_WORD:		//逐词朗读
			 		nextWord(false);
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
					 TTSUtils.getInstance().speakTips(mContext.getString(R.string.no_explain));
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
						 TTSUtils.getInstance().speakTips(mContext.getString(R.string.no_explain));
						 return;
					 }
					 else
					 {
						 String[] str = txt.split("=");
						 if( ( null == str ) || ( str.length < 2 ) )
						 {
							 TTSUtils.getInstance().speakTips(mContext.getString(R.string.no_explain));
							 return;
						 }
						 
						 String[] strExplain = str[1].split(" ");
						 if( ( null == strExplain ) || ( 0 == strExplain.length ) )
						 {
							 TTSUtils.getInstance().speakTips(mContext.getString(R.string.no_explain));
							 return;
						 }
						 
						 ArrayList<String> list2 = new ArrayList<String>();
						 
						 if( ( ch >= 'a' ) && ( ch <= 'z') )
						 {
							 for( int i = 0; i < strExplain.length; i++ )
							 {
								 list2.add(strExplain[i]);
							 }
							 list2.add(String.format(mContext.getResources().getString(R.string.en_explain_tips), ch-'a'+1));	//添加在字母表中的顺序
						 }
						 else
						 {
							 for( int i = 0; i < strExplain.length; i++ )
							 {
								 list2.add(strExplain[i]+mContext.getResources().getString(R.string.cn_explain_tips)+str[0]);
							 }
						 }
						 
						 mMapWordExplain.put(ch, list2);
					 }
				 }
			 }
			 
			 list = mMapWordExplain.get(ch);
			 if( ( list != null ) && ( list.size() > 0 ) )
			 {
				 TTSUtils.getInstance().speakTips(list.get(mCurReadExplainIndex));
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
			 }
			 return;
		 }

		 ReverseInfo oldReverseInfo = null;
		 
		 for( int i = mOffset; i < mMbBufLen; )
		 {
			 ReverseInfo ri = getNextReverseCharacterInfo( i );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = oldReverseInfo.startPos;
				 mReverseInfo.len = oldReverseInfo.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
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
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage);			//朗读反显文字
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
			 }
			 return;
		 }

		 ReverseInfo oldReverseInfo = null;
		 
		 for( int i = mOffset; i < mMbBufLen; )
		 {
			 ReverseInfo ri = getNextReverseWordInfo( i );
			 if( null == ri )
			 {
				 if( mOnPageFlingListener != null )
				 {
					 mOnPageFlingListener.onPageFlingToTop();
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = oldReverseInfo.startPos;
				 mReverseInfo.len = oldReverseInfo.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
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
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage);			//朗读反显文字
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
		 if( ( 0 == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 end = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 int len = getPreParagraphLength( end );
		 if( 0 == len )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
			 }
		 }
		 else
		 {
			 end -= len;
			 
			 for( int i = 0; i < mSplitInfoList.size(); i++ )
			 {
				 if( mSplitInfoList.get(i).startPos == end )
				 {
					 mReverseInfo.startPos = 0;
					 mReverseInfo.len = 0;
					 mLineNumber = i;
					 nextSentence( false );
					 return;
				 }
			 }
			 
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
			 }
		 }
	 }
	 
	 //到下一个段落
	 public void nextParagraph( boolean isSpeakPage )
	 {
		 TTSUtils.getInstance().stop();
		 setReadMode(ReadMode.READ_MODE_PARAGRAPH);
		 
		 int start = mReverseInfo.startPos;
		 if( ( 0 == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 int len = getNextParagraphLength( start );
		 if( 0 == len )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
		 else
		 {
			 start += len;
			 
			 for( int i = mLineNumber; i < mSplitInfoList.size(); i++ )
			 {
				 if( mSplitInfoList.get(i).startPos == start )
				 {
					 mReverseInfo.startPos = 0;
					 mReverseInfo.len = 0;
					 mLineNumber = i;
					 nextSentence( isSpeakPage );
					 return;
				 }
			 }
			 
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
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
				 }
				 break;
			 }
			 else if( ri.startPos + ri.len == mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = ri.startPos;
				 mReverseInfo.len = ri.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 else if( ri.startPos >= mReverseInfo.startPos )
			 {
				 mReverseInfo.startPos = oldReverseInfo.startPos;
				 mReverseInfo.len = oldReverseInfo.len;
				 readReverseText(false);			//朗读反显文字
				 recalcLineNumber(Action.PRE_LINE);	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 break;
			 }
			 
			 i = ri.startPos+ri.len;
			 oldReverseInfo = ri;
		 }
	 }	 
	 
	 //到下一个句子
	 private void nextSentence( boolean isSpeakPage )
	 {
		 int start = mReverseInfo.startPos + mReverseInfo.len;
		 if( ( 0 == mReverseInfo.startPos ) && ( 0 == mReverseInfo.len ) )
		 {
			 start = mSplitInfoList.get(mLineNumber).startPos;
		 }
		 
		 ReverseInfo ri = getNextReverseSentenceInfo( start );
		 if( null == ri )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
		 else
		 {
			 mReverseInfo.startPos = ri.startPos;
			 mReverseInfo.len = ri.len;
			 readReverseText(isSpeakPage);			//朗读反显文字
			 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
			 this.invalidate();
		 }
	 }

	 //得到下一个字符反显信息
	 private ReverseInfo getNextReverseCharacterInfo( int start )
	 {
		 if( start == mMbBufLen-1 )	//已经到底了
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
		 if( start == mMbBufLen-1 )	//已经到底了
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
					 if( ( CodeTableUtils.CODE[k] == ch ) && ( 0xA1A1 != ch ) )			//过滤掉空格
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
							 if( ( CodeTableUtils.CODE[k] == ch ) && ( 0xA1A1 != ch ) )	//过滤掉空格
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
	 
	 //得到下一个句子反显信息(逐段和全文模式)
	 private ReverseInfo getNextReverseSentenceInfo( int start )
	 {
		 if( start == mMbBufLen-1 )	//已经到底了
		 {
			 return	null;
		 }
		 
		 for( int i = start; i < mMbBufLen; )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 ReverseInfo ri = new ReverseInfo(i, 2);
				 
				 boolean isBreak = false;
				 char ch = PublicUtils.byte2char(mMbBuf, i);
				 for( int k = 0; k < CN_SEPARATOR.length; k++ )
				 {
					 if( CN_SEPARATOR[k] == ch )
					 {
						 isBreak = true;
						 break;
					 }
				 }	//如果一开始就是点符号则跳过反显这个点符号
				 
				 i += 2;
				 if( isBreak )
				 {
					 continue;
				 }
				 
				 for( int j = i; j < mMbBufLen; )
				 {
					 if( mMbBuf[j] < 0 )
					 {
						 ri.len += 2;
						 ch = PublicUtils.byte2char(mMbBuf, j);
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
			 else if( isAlpha( mMbBuf[i] ) || isNumber( mMbBuf[i] ) )	//英文或者数字
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
				 
				 return	ri;
			 }
			 else
			 {
				 i++;
			 }
		 }
		 
		 return	null;
	 }
	 
	 //朗读反显文字
	 private void readReverseText( boolean isSpeakPage )
	 {
		 if( mReverseInfo.len <= 0 )	//如果没有反显
		 {
			 if( isSpeakPage )
			 {
				 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips), mCurPage, getPageCount() );
				 TTSUtils.getInstance().speakTips(tips);
			 }
			 return;
		 }
		 
		 Locale locale = mContext.getResources().getConfiguration().locale;
		 String language = locale.getLanguage();
		 
		 char code = PublicUtils.byte2char(mMbBuf, mReverseInfo.startPos);
		 String str = null;
		 if( "en".equalsIgnoreCase(language) )	//英文
		 {
			 str = CodeTableUtils.getEnString(code);
		 }
		 else
		 {
			 str = CodeTableUtils.getCnString(code);
		 }
		 
		 if( null != str )
		 {
			 TTSUtils.getInstance().speakContent(str);
		 }
		 else
		 {
			 try 
			 {
				 String text = new String(mMbBuf, mReverseInfo.startPos, mReverseInfo.len, CHARSET_NAME);	//转换成指定编码
				 if( isSpeakPage )
				 {
					 String tips = String.format(mContext.getResources().getString(R.string.page_read_tips), mCurPage, getPageCount() );
					 TTSUtils.getInstance().speakContent(tips+text);
				 }
				 else
				 {
					 TTSUtils.getInstance().speakContent(text);
				 }
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 e.printStackTrace();
			 }
		 }
	 }
	 
	 //根据反显位置重新计算当前页起始位置(行号)
	 private void recalcLineNumber( Action action )
	 {
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
				 				}
				 				break;
				 			}
			 			}	//将内容翻到下一页
			 		}
			 		else
			 		{
			 			break;
			 		}
		 		}
		 		break;
		 	default:
		 		break;
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
		switch( mReadMode )
		{
			case READ_MODE_ALL:			//全文朗读
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		nextSentence(false);
		 		break;
		 	case READ_MODE_WORD:		//逐词朗读
		 		break;
		 	case READ_MODE_CHARACTER:	//逐字朗读
		 		break;
		 	default:
		 		break;
		 }
	}

	//朗读错误
	@Override
	public void onSpeakError() 
	{
		// TODO Auto-generated method stub
		
	}
}