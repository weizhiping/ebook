package com.sunteam.ebook.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.sunteam.ebook.R;
import com.sunteam.ebook.entity.DiasySentenceNode;
import com.sunteam.ebook.entity.ReadMode;
import com.sunteam.ebook.entity.ReverseInfo;
import com.sunteam.ebook.entity.SplitInfo;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TTSUtils.OnTTSListener;
import com.sunteam.ebook.util.TTSUtils.SpeakStatus;

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
 * Daisy电子书阅读器控件
 * 
 * @author wzp
 *
 */

 public class DaisyReaderView extends View implements OnGestureListener, OnDoubleTapListener, OnTTSListener
 {	 
	 private static final String TAG = "DaisyReaderView";
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
	 private int mOffset = 0;				//图书字符真正开始位置，有BOM的地方需要跳过
	 private int mLineNumber = 0;			//当前页起始位置(行号)
	 private int mMbBufLen = 0; 			//图书总长度
	 private int mCurPage = 1;				//当前页
	 private GestureDetector mGestureDetector = null;	//手势
	 private OnPageFlingListener mOnPageFlingListener = null;
	 private ReadMode mReadMode = ReadMode.READ_MODE_ALL;	//朗读模式
	 private ReverseInfo mReverseInfo = new ReverseInfo();	//反显信息

	 private int mCheckSum = 0;				//当前buffer的checksum
	 private ArrayList<DiasySentenceNode> mDiasySentenceNodeList = null;
	 
	 public interface OnPageFlingListener 
	 {
		 public void onLoadCompleted( int pageCount, int curPage );		//加载完成
		 public void onPageFlingToTop();	//翻到头了
		 public void onPageFlingToBottom();	//翻到尾了
		 public void onPageFlingCompleted( int curPage );	//翻页完成
	 }
	 
	 public DaisyReaderView(Context context) 
	 {
		 super(context);
		 
		 initReaderView( context );
	 }
	 
	 public DaisyReaderView(Context context, AttributeSet attrs) 
	 {
		 super(context, attrs);
		 
		 initReaderView( context );
	 }

	 public DaisyReaderView(Context context, AttributeSet attrs, int defStyle) 
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
	 
	 //得到当前反显句子的序号
	 private int getCurReversePosition()
	 {
		 int length = 0;
		 for( int i = 0; i < mDiasySentenceNodeList.size(); i++ )
		 {
			 if( mReverseInfo.startPos == length )
			 {
				 return	i;
			 }	//查找反显开始的句子序号
			 
			 length += mDiasySentenceNodeList.get(i).sentence.length;
		 }
		 
		 return	0;
	 }
	 
	 /**
	  * 
	  * @param list
	  * 			所有句子列表
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
	 public boolean openBook(ArrayList<DiasySentenceNode> list, int lineNumber, int startPos, int len, int checksum) 
	 {
		 mDiasySentenceNodeList = list;
		 mMbBufLen = 0;
		 int size = list.size();
		 for( int i = 0; i < size; i++ )
		 {
			 mMbBufLen += list.get(i).sentence.length;
		 }
		 
		 mMbBuf = new byte[mMbBufLen];
		 int n = 0;
		 for( int i = 0; i < size; i++ )
		 {
			 for( int j = 0; j < list.get(i).sentence.length; j++ )
			 {
				 mMbBuf[n++] = list.get(i).sentence[j];
			 }
		 }
		 
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
		 
		 mCurPage = pi.page;
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
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 this.invalidate();
		 
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
		 switch( mReadMode )
		 {
		 	case READ_MODE_ALL:			//全文朗读
		 	case READ_MODE_PARAGRAPH:	//逐段朗读
		 	case READ_MODE_SENCENTE:	//逐句朗读
		 		nextSentence(true);
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
		 
		 if( nextLine() )
		 {
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
	 
		 if( nextPage() )
		 {
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

	 //到上一个句子
	 private void preSentence()
	 {
		 int position = getCurReversePosition();
		 if( 0 == position )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToTop();
			 }
			 
			 return;
		 }
		 
		 position--;
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 for( int i = 0; i < mDiasySentenceNodeList.size(); i++ )
		 {
			 if( position == i )
			 {
				 mReverseInfo.len = mDiasySentenceNodeList.get(i).sentence.length;
				 break;
			 }
			 mReverseInfo.startPos += mDiasySentenceNodeList.get(i).sentence.length;
		 }
		 
		 readReverseText(false);				//朗读反显文字
		 recalcLineNumber(Action.PRE_LINE);		//重新计算当前页起始位置(行号)
		 this.invalidate();
	 }	 
	 
	 //到下一个句子
	 private void nextSentence( boolean isSpeakPage )
	 {
		 int position = getCurReversePosition();
		 if( position >= mDiasySentenceNodeList.size()-1 )
		 {
			 if( mOnPageFlingListener != null )
			 {
				 mOnPageFlingListener.onPageFlingToBottom();
			 }
			 
			 return;
		 }
		 
		 position++;
		 mReverseInfo.startPos = 0;
		 mReverseInfo.len = 0;
		 for( int i = 0; i < mDiasySentenceNodeList.size(); i++ )
		 {
			 if( position == i )
			 {
				 mReverseInfo.len = mDiasySentenceNodeList.get(i).sentence.length;
				 break;
			 }
			 mReverseInfo.startPos += mDiasySentenceNodeList.get(i).sentence.length;
		 }
		 
		 readReverseText(isSpeakPage);			//朗读反显文字
		 recalcLineNumber(Action.NEXT_LINE);	//重新计算当前页起始位置(行号)
		 this.invalidate();
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