package com.sunteam.ebook.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import com.sunteam.ebook.entity.SplitInfo;

import android.os.Environment;

/**
 * 文本文件读取工具类。
 * 
 * @author wzp
 */
public class TextFileReaderUtils 
{
	private static TextFileReaderUtils instance = null;
	private File mBookFile = null;
	private RandomAccessFile mRandomAccessFile = null;
	private FileChannel  mFileChannel  = null;
	private String mStrCharsetName = "GB18030";		//编码格式，默认为GB18030
	private MappedByteBuffer mMbBuf = null;			//内存中的图书字符
	private int mMbBufLen = 0; 						//图书总长度
	private ArrayList<SplitInfo> mSplitInfoList  = null;	//分段信息
	private boolean isInsideSDPath = true;				//是否内部SD卡路径
	
	public static TextFileReaderUtils getInstance()
	{
		if( null == instance )
		{
			instance = new TextFileReaderUtils();
		}
		
		return instance;
	}
	
	public TextFileReaderUtils()
	{
		mSplitInfoList = new ArrayList<SplitInfo>();
	}
	
	//是否是内部SD卡路径
	public boolean isInsideSDPath()
	{
		return	isInsideSDPath;
	}
	
	public void destroy()
	{
		try
		{
			if( mMbBuf != null )
			{
				mMbBuf.clear();
				mMbBuf = null;
			}
			
			if( mFileChannel != null )
			{
				mFileChannel.close();
				mFileChannel = null;
			}
			
			if( mRandomAccessFile != null )
			{
				mRandomAccessFile.close();
				mRandomAccessFile = null;
			}
			
			mSplitInfoList.clear();	//先清除上次保存的信息
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	//初始化
	@SuppressWarnings("resource")
	public void init( final String fullpath ) throws Exception
	{
		String insideSDPath = Environment.getExternalStorageDirectory().getPath();	//得到内置SD卡路径
		if( ( fullpath != null ) && ( insideSDPath != null ) && ( fullpath.indexOf(insideSDPath) == 0 ) )
		{
			isInsideSDPath = true;
		}
		else
		{
			isInsideSDPath = false;
		}
		mSplitInfoList.clear();	//先清除上次保存的信息
		
		IdentifyEncoding ie = new IdentifyEncoding();
		mStrCharsetName = ie.GetEncodingName( fullpath );	//得到文本编码
		
		mBookFile = new File(fullpath);
		mRandomAccessFile = new RandomAccessFile( mBookFile, "r");
		mFileChannel = mRandomAccessFile.getChannel();
		long lLen = mBookFile.length();
		mMbBufLen = (int)lLen;
		mMbBuf = mFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, lLen);	//读入虚拟内存
		
		int begin = 0;
		
		while( begin >= 0 )
		{
			begin = paragraph( mMbBuf, begin );
		}	//得到分段信息
	}
	
	/**
	  * 得到文本分段数
	  */	
	public int getParagraphCount()
	{
		return	mSplitInfoList.size();
	}
	
	/**
	  * 得到指定部分的文本内容
	  */	
	public byte[] getParagraphBuffer( int part )
	{
		if( part < 0 || part >= mSplitInfoList.size() )
		{
			return	null;
		}
		
		byte[] buffer = new byte[mSplitInfoList.get(part).len];		//分段buf
		mMbBuf.position(mSplitInfoList.get(part).startPos);			//先移动到开始位置
		mMbBuf.get( buffer, 0, mSplitInfoList.get(part).len );		//读入物理内存
		
		return	buffer;
	}
	
	//得到编码格式
	public String getCharsetName()
	{
		return	mStrCharsetName;
	}
	
	/**
	  * 将文本分段
	  * 
	  * @param mbb
	  * 			虚拟内存
	  * @param begin
	  *				开始位置
	  * 
	  * @throws IOException
	  */
	private int paragraph( MappedByteBuffer mbb, int begin )
	{
		int len = Math.min(EbookConstants.MAX_PARAGRAPH, (mMbBufLen-begin));	//可以读取的长度
		if( len <= 0 )
		{
			return	-1;
		}
		else if( len < EbookConstants.MAX_PARAGRAPH )
		{
			SplitInfo pi = new SplitInfo(begin, len);
			mSplitInfoList.add(pi);
			
			return	-1;
		}
		
		byte[] buffer = new byte[len];		//分段buf
		mbb.position(begin);	//先移动到开始位置
		mbb.get( buffer, 0, len );	//读入物理内存
		
		int paragraphLen = len;		//段落的长度
		
		for( int i = len-1; i >= 0; i-- )
		{
			if( ( 0x0a == buffer[i] ) || ( 0x0d == buffer[i] ) )
			{
				paragraphLen = i+1;
				break;
			}
		}
		
		SplitInfo pi = new SplitInfo(begin, paragraphLen);
		mSplitInfoList.add(pi);
		
		return	(begin+paragraphLen);	//返回下一段开始位置	
	}
}
