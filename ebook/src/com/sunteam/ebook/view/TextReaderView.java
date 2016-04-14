package com.sunteam.ebook.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Txt电子书阅读器控件
 * 
 * @author wzp
 *
 */

 public class TextReaderView extends View implements OnGestureListener
 {	 
	 private static final String TAG = "TextReaderView";
	 private static final int MARGIN_WIDTH = 10;		///左右与边缘的距离
	 private static final int MARGIN_HEIGHT = 10;		//上下与边缘的距离
	 private static final int FLING_MIN_DISTANCE = 100;	//滑动的距离
	 private static final String CHARSET_NAME = "GB18030";//编码格式，默认为GB18030
	 
	 private Context mContext = null;
	 private Bitmap mCurPageBitmap = null;
	 private Canvas mCurPageCanvas = null;	//当前画布
	 private Paint mPaint = null;
	 private int mLineSpace = 10;			//行间距
	 private float mTextSize = 60.0f;		//字体大小
	 private int mTextColor = Color.WHITE;	//字体颜色
	 private int mBkColor = Color.BLACK;	//背景颜色
	 private int mLineCount = 0; 			//每页可以显示的行数
	 private int mWidth = 0;				//页面控件的宽
	 private int mHeight = 0;				//页面控件的高
	 private float mVisibleWidth; 			//绘制内容的宽
	 private float mVisibleHeight;			//绘制内容的高
	 private boolean mIsFirstPage = false;	//是否是第一屏
	 private boolean mIsLastPage = false;	//是否是最后一屏
	 private ArrayList<LineInfo> mLineInfoList = new ArrayList<LineInfo>();	//保存分行信息
	 private byte[] mMbBuf = null;			//内存中的图书字符
	 private int mLineNumber = 0;			//当前页起始位置(行号)
	 private int mMbBufLen = 0; 			//图书总长度
	 private int mCurPage = 1;				//当前页
	 private GestureDetector mGestureDetector = null;	//手势
	 private OnPageFlingListener mOnPageFlingListener = null;
	 
	 public interface OnPageFlingListener 
	 {
		 public void onLoadCompleted( int pageCount );		//加载完成
		 public void onPageFlingToTop();	//翻到头了
		 public void onPageFlingToBottom();	//翻到尾了
		 public void onPageFlingCompleted( int curPage );	//翻页完成
	 }
	 
	 public TextReaderView(Context context) 
	 {
		 super(context);
		 mContext = context;
		 
		 mGestureDetector = new GestureDetector( context, this );
		 mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//画笔
		 mPaint.setTextAlign(Align.LEFT);			//做对齐
	 }
	 
	 public TextReaderView(Context context, AttributeSet attrs) 
	 {
		 super(context, attrs);
		 mContext = context;
		 
		 mGestureDetector = new GestureDetector( context, this );
		 mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//画笔
		 mPaint.setTextAlign(Align.LEFT);			//做对齐
	 }

	 public TextReaderView(Context context, AttributeSet attrs, int defStyle) 
	 {
		 super(context, attrs, defStyle);
		 mContext = context;
		 
		 mGestureDetector = new GestureDetector( context, this );
		 mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//画笔
		 mPaint.setTextAlign(Align.LEFT);			//做对齐
	 }

	 //设置翻页监听器
	 public void setOnPageFlingListener( OnPageFlingListener listener )
	 {
		 mOnPageFlingListener = listener;
	 }
	 
	 //设置背景色
	 @Override
	 public void setBackgroundColor( int color )
	 {
		 super.setBackgroundColor(color);
		 mBkColor = color;
	 }
	 
	 //设置字体颜色
	 public void setTextColor( int color )
	 {
		 mTextColor = color;
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
	 
	 //得到字体大小
	 public float getTextSize()
	 {
		 return	mTextSize;
	 }
	 
	 //得到行间距
	 public int getSpaceSize() 
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
	 
	 //得到指定行文本
	 private String getLineText( final int lineNumber )
	 {
		 int size = mLineInfoList.size();
		 if( lineNumber >= 0  && lineNumber < size )
		 {
			 LineInfo li = mLineInfoList.get(lineNumber);
			 
			 try 
			 {
				 return	new String(mMbBuf, li.startPos, li.len, CHARSET_NAME);	//转换成指定编码
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
	  * 
	  */
	 public void openBook(byte[] buffer, String charsetName, int lineNumber) 
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
		 }
		 mMbBufLen = (int)mMbBuf.length;
		 mLineNumber = lineNumber; 
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
		 
		 int startPos = 0;
		 
		 while( startPos < mMbBufLen ) 
		 {
			 int len = getNextParagraphLength(startPos);
			 if( len <= 0 )
			 {
				 break;
			 }
			 
			 int ll = len;
			 int home = startPos;
			 int end = startPos+len-1;
			 if( ( 0x0d == mMbBuf[end] ) || ( 0x0a == mMbBuf[end] ) )
			 {
				 ll--;
			 }
			 
			 end--;
			 if( end >= home )
			 {
				 if( ( 0x0d == mMbBuf[end] ) || ( 0x0a == mMbBuf[end] ) )
				 {
					 ll--;
				 }
			 }
			 
			 
			 if( (int)(ll*mTextSize) / 2 <= mVisibleWidth ) 
			 {
				 LineInfo li = new LineInfo(startPos, len);
				 mLineInfoList.add(li);				 
			 }
			 else
			 {
				 byte[] buffer = new byte[len];
				 for( int i = 0; i < len; i++ )
				 {
					 buffer[i] = mMbBuf[startPos+i];
				 }
				 
				 int textWidth = 0;
				 int start = startPos;
				 home = 0;
				 int i = 0;
				 for( i = 0; i < buffer.length; i++ )
				 {
					 if( 0x0d == buffer[i] || 0x0a == buffer[i] )
					 {
						 continue;
					 }
					 
					 if( buffer[i] < 0x80 && buffer[i] >= 0x0 )	//ascii
					 {
						 textWidth += ((int)mTextSize/2);
						 if( textWidth >= mVisibleWidth )
						 {
							 int length = i-home;
							 
							 LineInfo li = new LineInfo(start, length);
							 mLineInfoList.add(li);
							 
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
						 if( textWidth >= mVisibleWidth )
						 {
							 int length = i-home;
							 LineInfo li = new LineInfo(start, length);
							 mLineInfoList.add(li);
							 
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
					 LineInfo li = new LineInfo(start, length);
					 mLineInfoList.add(li);
					 
					 start += length;
					 textWidth = 0;
				 }
			 }
			 
			 startPos += len;						//每次读取后，记录结束点位置，该位置是段落结束位置
		 }
	 }

	 //得到总页数
	 public int getPageCount()
	 {
		 return	( mLineInfoList.size() + mLineCount - 1 ) / mLineCount;
	 }
	 
	 /**
	  * 向后翻页
	  * 
	  */
	 public boolean nextPage() 
	 {
		 if( mLineNumber+mLineCount >= mLineInfoList.size() ) 
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
		 
		 if( 0 == mLineInfoList.size() )
		 {
			 divideLines();
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
		 
		 if( mLineInfoList.size() > 0 ) 
		 {
			 int y = MARGIN_HEIGHT;
			 for( int i = mLineNumber, j = 0; i < mLineInfoList.size() && j < mLineCount; i++, j++ ) 
			 {
				 y += mTextSize;
				 y += mLineSpace;
				 mCurPageCanvas.drawText(getLineText(i), MARGIN_WIDTH, y, mPaint);
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
		 if( e1.getX() - e2.getX() > FLING_MIN_DISTANCE ) 
		 {
			 //向左滑动
			 if( nextPage() )
			 {
				 this.postInvalidate();
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
			 }
		 } 
		 else if( e2.getX() - e1.getX() > FLING_MIN_DISTANCE ) 
		 {
			 //向右滑动
			 if( prePage() )
			 {
				 this.postInvalidate();
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
		 }
		 
		 return false; 
	 }
	 
	 //分行信息类
	 private class LineInfo
	 {
		 public int startPos;	//开始位置
		 public int len;		//长度
		 
		 public LineInfo( int s, int l )
		 {
			 startPos = s;
			 len = l;
		 }
	 }	 
}