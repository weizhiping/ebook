package com.sunteam.ebook.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sunteam.ebook.entity.ReadMode;
import com.sunteam.ebook.entity.ReverseInfo;
import com.sunteam.ebook.entity.SplitInfo;

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
	 private static final float MARGIN_WIDTH = 0;		//左右与边缘的距离
	 private static final float MARGIN_HEIGHT = 0;		//上下与边缘的距离
	 private static final String CHARSET_NAME = "GB18030";//编码格式，默认为GB18030
	 
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
	 private int mLineNumber = 0;			//当前页起始位置(行号)
	 private int mMbBufLen = 0; 			//图书总长度
	 private int mCurPage = 1;				//当前页
	 private GestureDetector mGestureDetector = null;	//手势
	 private OnPageFlingListener mOnPageFlingListener = null;
	 private ReadMode mReadMode = ReadMode.READ_MODE_WORD;	//朗读模式，默认无朗读
	 private ReverseInfo mReverseInfo = new ReverseInfo();	//反显信息
	 
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
		 mContext = context;
		 
		 mGestureDetector = new GestureDetector( context, this );
		 mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//画笔
		 mPaint.setTextAlign(Align.LEFT);			//做对齐
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
		 float asciiWidth = mPaint.measureText(" ");	//一个ascii字符宽度
		 
		 int startPos = 0;
		 
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
					 if( textWidth >= mVisibleWidth )
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
					 if( textWidth >= mVisibleWidth )
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
		 	case READ_MODE_NIL:			//无朗读
		 		break;
		 	case READ_MODE_ALL:			//全文朗读
		 		break;
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		break;
		 	case READ_MODE_SENTENCE:	//逐句朗读
		 		break;
		 	case READ_MODE_WORD:		//逐字朗读
		 		nextReverseWord();
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
						 if( mReverseInfo.startPos+mReverseInfo.len <= si.startPos+si.len )	//反显结束也在当前行
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
							 
							 try 
							 {
								 str = new String(mMbBuf, mReverseInfo.startPos, mReverseInfo.len, CHARSET_NAME);	//转换成指定编码
							 } 
							 catch (UnsupportedEncodingException e) 
							 {
								 e.printStackTrace();
							 }
							 
							 RectF rect = new RectF(xx, y+(fontMetrics.ascent-fontMetrics.top), (xx+mPaint.measureText(str)), y+(fontMetrics.descent-fontMetrics.top) );
							 mCurPageCanvas.drawRect(rect, paint);
						 }
						 else
						 {
							 
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
	 
	 //向后翻行
	 public void down()
	 {
		 switch( mReadMode )
		 {
		 	case READ_MODE_NIL:			//无朗读
		 		if( nextLine() )
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
		 				mOnPageFlingListener.onPageFlingToBottom();
		 			}
		 		}
		 		break;
		 	case READ_MODE_ALL:			//全文朗读
		 		break;
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		break;
		 	case READ_MODE_SENTENCE:	//逐句朗读
		 		break;
		 	case READ_MODE_WORD:		//逐字朗读
		 		nextReverseWord();
		 		break;
		 	default:
		 		break;
		 }
	 }
	 
	 //向前翻行
	 public void up()
	 {
		 switch( mReadMode )
		 {
		 	case READ_MODE_NIL:			//无朗读
		 		if( preLine() )
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
		 			}
		 		}
		 		break;
		 	case READ_MODE_ALL:			//全文朗读
		 		break;
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 		break;
		 	case READ_MODE_SENTENCE:	//逐句朗读
		 		break;
		 	case READ_MODE_WORD:		//逐字朗读
		 		break;
		 	default:
		 		break;
		 }
	 }
	 
	 //向前翻页
	 public void left()
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
			 }
		 }
	 }
	 
	 //向后翻页
	 public void right()
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
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
		 }
	 }
	 
	 //确定
	 public void enter()
	 {
		 
	 }
	 
	 //反显下一个字
	 private void nextReverseWord()
	 {
		 int start = mReverseInfo.startPos+mReverseInfo.len;
		 if( start == mMbBufLen-1 )	//已经到底了
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
			 return;
		 }
		 
		 for( int i = start; i < mMbBufLen; i++ )
		 {
			 if( mMbBuf[i] < 0 )	//汉字
			 {
				 mReverseInfo.startPos = i;
				 mReverseInfo.len = 2;
				 
				 recalcLineNumber();	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 return;
			 }
			 else if( isAlpha( mMbBuf[i] ) )	//英文
			 {
				 mReverseInfo.startPos = i;
				 mReverseInfo.len = 1;
				 for( int j = i+1; j < mMbBufLen; j++ )
				 {
					 if( isAlpha( mMbBuf[j] ) )
					 {
						 mReverseInfo.len++;
					 }
					 else
					 {
						 break;
					 }
				 }
				 
				 recalcLineNumber();	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 return;
			 }
			 else if( isNumber( mMbBuf[i] ) )	//数字
			 {
				 mReverseInfo.startPos = i;
				 mReverseInfo.len = 1;
				 for( int j = i+1; j < mMbBufLen; j++ )
				 {
					 if( isNumber( mMbBuf[j] ) )
					 {
						 mReverseInfo.len++;
					 }
					 else
					 {
						 break;
					 }
				 }
				 
				 recalcLineNumber();	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 return;
			 }
			 else if( isEscape( mMbBuf[i]) )	//如果是特殊转义字符
			 {
				 continue;
			 }
			 else
			 {
				 mReverseInfo.startPos = i;
				 mReverseInfo.len = 1;
				 
				 recalcLineNumber();	//重新计算当前页起始位置(行号)
				 this.invalidate();
				 return;
			 }
		 }
		 
		 //如果执行到此处，证明已经没有可以反显的字符了
	 }
	 
	 //根据反显位置重新计算当前页起始位置(行号)
	 private void recalcLineNumber()
	 {
		 if( mReverseInfo.len <= 0 )	//如果没有反显
		 {
			 return;
		 }
		 
		 int size = mSplitInfoList.size();
		 int curPageLine = Math.min( mLineCount, (size-mLineNumber) );	//当前屏最大行数
		 
		 SplitInfo si = mSplitInfoList.get(mLineNumber+curPageLine-1);	//得到当前屏最后一行的信息
		 if( mReverseInfo.startPos >= si.startPos+si.len )				//反显开始在下一页
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
			 }
		 }	//将内容翻到下一行
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
		 //if( ( ch >= '0' && ch <= '9' ) || ( '.' == ch ) )
		 if( ch >= '0' && ch <= '9' )
		 {
			 return	true;
		 }
		 
		 return	false;
	 }
	 
	 //是否是特殊的转义字符，比如换行符/回车符/制表符
	 private boolean isEscape( byte ch )
	 {
		 if( 0x0d == ch || 0x0a == ch || 0x09 == ch )
		 {
			 return	true;
		 }
		 
		 return	false;
	 }
}