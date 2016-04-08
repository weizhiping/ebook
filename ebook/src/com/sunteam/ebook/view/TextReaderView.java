package com.sunteam.ebook.view;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
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
	 private static final int BUFFER_SIZE = 0x100000;	//buffer的大小
	 private static final int MARGIN_WIDTH = 10;		///左右与边缘的距离
	 private static final int MARGIN_HEIGHT = 10;		//上下与边缘的距离
	 private static final int FLING_MIN_DISTANCE = 100;	//滑动的距离
	 
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
	 private String mStrCharsetName = "GBK";//编码格式，默认为GBK
	 private File mBookFile = null;
	 private Vector<String> mLines = new Vector<String>();
	 private MappedByteBuffer mMbBuf = null;//内存中的图书字符
	 private int mMbBufBegin = 0;			//当前页起始位置
	 private int mMbBufEnd = 0;				//当前页终点位置
	 private int mMbBufLen = 0; 			//图书总长度
	 private int mPageCount = 0;			//总页数
	 private int mCurPage = 1;				//当前页
	 private GestureDetector mGestureDetector = null;	//手势
	 private OnPageFlingListener mOnPageFlingListener = null;
	 
	 public interface OnPageFlingListener 
	 {
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
	 
	 //设置页面起始点
	 public void setPageBegin( int begin ) 
	 {
		 this.mMbBufBegin = begin;
	 }
	 
	 //设置页面结束点
	 public void setPageEnd( int end ) 
	 {
		 this.mMbBufEnd = end;
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
	 
	 //得到页面起始点
	 public int getPageBegin() 
	 {
		 return	mMbBufBegin;
	 }
	 
	 //得到页面结束点
	 public int getPageEnd() 
	 {
		 return	mMbBufEnd;
	 }
	 
	 //得到当前屏第一行文本
	 public String getFirstLineText() 
	 {
		 return mLines.size() > 0 ? mLines.get(0) : "";
	 }
	 
	 /**
	  * 
	  * @param strFilePath
	  * @param begin
	  *            表示书签记录的位置，读取书签时，将begin值给m_mbBufEnd，在读取nextpage，及成功读取到了书签
	  *            记录时将m_mbBufBegin开始位置作为书签记录
	  * 
	  * @throws IOException
	  */
	 @SuppressWarnings("resource")
	 public void openBook(String strFilePath, int begin) throws IOException 
	 {
		 IdentifyEncoding ie = new IdentifyEncoding();
		 mStrCharsetName = ie.GetEncodingName( strFilePath );
		 mBookFile = new File(strFilePath);
		 long lLen = mBookFile.length();
		 mMbBufLen = (int)lLen;
		 mMbBuf = new RandomAccessFile( mBookFile, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
		 
		 //设置已读进度
		 if( begin >= 0 ) 
		 {
			 mMbBufBegin = begin;
			 mMbBufEnd = begin;
		 }
		 
		 //calcPageCount();
	 }
	 
	 //计算总页数
	 private void calcPageCount()
	 {
		 long time1 = System.currentTimeMillis();
		 byte[] buffer = new byte[mMbBufLen];
		 long time2 = System.currentTimeMillis();
		 mMbBuf.get(buffer, 0, mMbBufLen);
		 long time3 = System.currentTimeMillis();
		 
		 String content = null;
		 try 
		 {
			 content = new String( buffer, mStrCharsetName );
		 } 
		 catch (UnsupportedEncodingException e) 
		 {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 long time4 = System.currentTimeMillis();
		 
		 String[] strParagraph = content.split("\r\n");
		 int lineCount = 0;	//总行数
		 
		 for( int i = 0; i < strParagraph.length; i++ )
		 {
			 String paragraph = strParagraph[i].replaceAll("\r\n", "");
			 //如果是空白行，直接添加
			 if( paragraph.length() == 0 ) 
			 {
				 lineCount++;
				 continue;
			 }
			 
			 while( paragraph.length() > 0 ) 
			 {
				 //画一行文字
				 int nSize = mPaint.breakText( paragraph, true, 1060, null );
				 paragraph = paragraph.substring( nSize );
				 lineCount++;
			 }
		 }
		 long time5 = System.currentTimeMillis();
		 
		 Log.e( TAG, "wzp debug 00000000 time = "+(time2-time1) );
		 Log.e( TAG, "wzp debug 11111111 time = "+(time3-time2) );
		 Log.e( TAG, "wzp debug 22222222 time = "+(time4-time3) );
		 Log.e( TAG, "wzp debug 33333333 time = "+(time5-time4) );
		 Log.e( TAG, "wzp debug ======== time = "+(time5-time1) );
	 }
	 
	 /**
	  * 向后翻页
	  * 
	  * @throws IOException
	  */
	 public boolean nextPage() throws IOException 
	 {
		 if( mMbBufEnd >= mMbBufLen ) 
		 {
			 mIsLastPage = true;
			 return false;
		 } 
		 else
		 {
			 mIsLastPage = false;
		 }
		 
		 mLines.clear();
		 mMbBufBegin = mMbBufEnd;// 下一页页起始位置=当前页结束位置
		 mLines = pageDown();
		 
		 mCurPage++;
		 
		 return	true;
	 }
	 
	 /**
	  * 当前页
	  * 
	  * @throws IOException
	  */
	 public void currentPage() throws IOException 
	 {
		 mLines.clear();
		 mLines = pageDown();
	 }
	 
	 /**
	  * 向前翻页
	  * 
	  * @throws IOException
	  */
	 public boolean prePage() throws IOException 
	 {
		 if( mMbBufBegin <= 0 ) 
		 {
			 mMbBufBegin = 0;
			 mIsFirstPage = true;
			 
			 return	false;
		 } 
		 else
		 {
			 mIsFirstPage = false;
		 }

		 mLines.clear();
		 pageUpUp();
		 mLines = pageDown();
		 
		 mCurPage--;
		 
		 return	true;
	 }
	 
	 /**
	  * 读取指定位置的下一个段落
	  * 
	  * @param nFromPos
	  * @return byte[]
	  */
	 private byte[] readParagraphForward( int nFromPos ) 
	 {
		 int nStart = nFromPos;
		 int i = nStart;
		 byte b0, b1;
		 
		 //根据编码格式判断换行
		 if( mStrCharsetName.equals("utf-16le") ) 
		 {
			 while( i < mMbBufLen - 1 ) 
			 {
				 b0 = mMbBuf.get(i++);
				 b1 = mMbBuf.get(i++);
				 if( b0 == 0x0a && b1 == 0x00 ) 
				 {
					 break;
				 }
			 }
		 } 
		 else if( mStrCharsetName.equals("utf-16be") ) 
		 {
			 while( i < mMbBufLen - 1 ) 
			 {
				 b0 = mMbBuf.get(i++);
				 b1 = mMbBuf.get(i++);
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
				 b0 = mMbBuf.get(i++);
				 if( b0 == 0x0a ) 
				 {
					 break;
				 }
			 }
		 }
		 
		 int nParaSize = i - nStart;
		 byte[] buf = new byte[nParaSize];
		 for( i = 0; i < nParaSize; i++ ) 
		 {
			 buf[i] = mMbBuf.get(nFromPos + i);
		 }
		 
		 return buf;
	 }

	/**
	 * 画指定页的下一页
	 * 
	 * @return 下一页的内容 Vector<String>
	 */
	 private Vector<String> pageDown() 
	 {
		 mPaint.setTextSize(mTextSize);
		 mPaint.setColor(mTextColor);
		 
		 String strParagraph = "";
		 Vector<String> lines = new Vector<String>();
		 while( lines.size() < mLineCount && mMbBufEnd < mMbBufLen ) 
		 {
			 byte[] paraBuf = readParagraphForward(mMbBufEnd);
			 mMbBufEnd += paraBuf.length;						//每次读取后，记录结束点位置，该位置是段落结束位置
			 
			 try 
			 {
				 strParagraph = new String(paraBuf, mStrCharsetName);	//转换成指定GBK编码
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 Log.e(TAG, "pageDown->转换编码失败", e);
			 }
			 
			 String strReturn = "";
			 //替换掉回车换行符

			 if( strParagraph.indexOf("\r\n") != -1 ) 
			 {
				 strReturn = "\r\n";
				 strParagraph = strParagraph.replaceAll("\r\n", "");
			 } 
			 else if( strParagraph.indexOf("\n") != -1 ) 
			 {
				 strReturn = "\n";
				 strParagraph = strParagraph.replaceAll("\n", "");
			 }

			 if( strParagraph.length() == 0 ) 
			 {
				 lines.add(strParagraph);
			 }
				
			 while( strParagraph.length() > 0 ) 
			 {
				 //画一行文字
				 int nSize = mPaint.breakText( strParagraph, true, mVisibleWidth, null );
				 lines.add( strParagraph.substring( 0, nSize ) );
				 strParagraph = strParagraph.substring( nSize );// 得到剩余的文字
					
				 //超出最大行数则不再画
				 if( lines.size() >= mLineCount ) 
				 {
					 break;
				 }
			 }
				
			 //如果该页最后一段只显示了一部分，则从新定位结束点位置
			 if( strParagraph.length() != 0 ) 
			 {
				 try 
				 {
					 mMbBufEnd -= (strParagraph + strReturn).getBytes(mStrCharsetName).length;
				 } 
				 catch (UnsupportedEncodingException e) 
				 {
					 Log.e(TAG, "pageDown->记录结束点位置失败", e);
				 }
			 }
		 }

		 return lines;
	 }
	 
	 /**
	  * 读取指定位置的上一个段落
	  * 
	  * @param nFromPos
	  * @return byte[]
	  */
	 private byte[] readParagraphBack( int nFromPos ) 
	 {
		 int nEnd = nFromPos;
		 int i;
		 byte b0, b1;
		 
		 if( mStrCharsetName.equals("utf-16le") )  
		 {
			 i = nEnd - 2;
			 while( i > 0 ) 
			 {
				 b0 = mMbBuf.get(i);
				 b1 = mMbBuf.get(i + 1);
				 if( b0 == 0x0a && b1 == 0x00 && i != nEnd - 2 ) 
				 {
					 i += 2;
					 break;
				 }
				 i--;
			 }
		 } 
		 else if( mStrCharsetName.equals("utf-16be") ) 
		 {
			 i = nEnd - 2;
			 while( i > 0 ) 
			 {
				 b0 = mMbBuf.get(i);
				 b1 = mMbBuf.get(i + 1);
				 if( b0 == 0x00 && b1 == 0x0a && i != nEnd - 2 ) 
				 {
					 i += 2;
					 break;
				 }
				 i--;
			 }
		 } 
		 else 
		 {
			 i = nEnd - 1;
			 while( i > 0 ) 
			 {
				 b0 = mMbBuf.get(i);
				 if( b0 == 0x0a && i != nEnd - 1 )	//0x0a表示换行符 
				 {
					 i++;
					 break;
				 }
				 i--;
			 }
		 }
		 
		 if( i < 0 )
		 {
			 i = 0;
		 }
		 
		 int nParaSize = nEnd - i;
		 int j;
		 byte[] buf = new byte[nParaSize];
		 for( j = 0; j < nParaSize; j++ ) 
		 {
			 buf[j] = mMbBuf.get(i + j);
		 }
		 
		 return buf;
	 }
	 
	 /**
	  * 得到上上页的结束位置
	  */
	 private void pageUpUp() 
	 {
		 if( mMbBufBegin < 0)
		 {
			 mMbBufBegin = 0;
		 }
			
		 Vector<String> lines = new Vector<String>();
		 String strParagraph = "";
		 while( lines.size() < mLineCount && mMbBufBegin > 0 ) 
		 {
			 Vector<String> paraLines = new Vector<String>();
			 byte[] paraBuf = readParagraphBack( mMbBufBegin );
			 mMbBufBegin -= paraBuf.length;						//每次读取一段后,记录开始点位置,是段首开始的位置
			 
			 try 
			 {
				 strParagraph = new String( paraBuf, mStrCharsetName );
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 Log.e(TAG, "pageUp->转换编码失败", e);
			 }
			 
			 strParagraph = strParagraph.replaceAll("\r\n", "");
			 strParagraph = strParagraph.replaceAll("\n", "");
			 //如果是空白行，直接添加
			 if( strParagraph.length() == 0 ) 
			 {
				 paraLines.add(strParagraph);
			 }
			 
			 while( strParagraph.length() > 0 ) 
			 {
				 //画一行文字
				 int nSize = mPaint.breakText( strParagraph, true, mVisibleWidth, null );
				 paraLines.add( strParagraph.substring( 0, nSize ) );
				 strParagraph = strParagraph.substring( nSize );
			 }
			 lines.addAll(0, paraLines);
		 }

		 while( lines.size() > mLineCount ) 
		 {
			 try 
			 {
				 mMbBufBegin += lines.get(0).getBytes( mStrCharsetName).length;
				 lines.remove(0);
			 } 
			 catch (UnsupportedEncodingException e) 
			 {
				 Log.e(TAG, "pageUp->记录起始点位置失败", e);
			 }
		 }
		 
		 mMbBufEnd = mMbBufBegin;	//上上一页的结束点等于上一页的起始点
		 
		 return;
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
		 
		 if( mLines.size() == 0 )
		 {
			 mLines = pageDown();
		 }
		 
		 if( mLines.size() > 0 ) 
		 {
			 int y = MARGIN_HEIGHT;
			 for( String strLine : mLines ) 
			 {
				 y += mTextSize;
				 y += mLineSpace;
				 mCurPageCanvas.drawText(strLine, MARGIN_WIDTH, y, mPaint);
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
			 try 
			 {
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
			 catch (IOException e) 
			 {
				 e.printStackTrace();
			 }
		 } 
		 else if( e2.getX() - e1.getX() > FLING_MIN_DISTANCE ) 
		 {
			 //向右滑动
			 try 
			 {
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
			 catch (IOException e) 
			 {
				 e.printStackTrace();
			 }
		 }
		 
		 return false; 
	 }
}