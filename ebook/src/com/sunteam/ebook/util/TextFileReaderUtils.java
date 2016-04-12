package com.sunteam.ebook.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * 文本文件读取工具类。
 * 
 * @author wzp
 */
public class TextFileReaderUtils 
{
	private static TextFileReaderUtils instance = null;
	private File mBookFile = null;
	private String mStrCharsetName = "GBK";		//编码格式，默认为GBK
	private MappedByteBuffer mMbBuf = null;		//内存中的图书字符
	private int mMbBufLen = 0; 					//图书总长度
	private ArrayList<ParagrapInfo> mParagrapInfoList  = null;	//分段信息
	
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
		mParagrapInfoList = new ArrayList<ParagrapInfo>();
	}
	
	//初始化
	@SuppressWarnings("resource")
	public void init( final String fullpath ) throws IOException
	{
		mParagrapInfoList.clear();	//先清除上次保存的信息
		
		IdentifyEncoding ie = new IdentifyEncoding();
		mStrCharsetName = ie.GetEncodingName( fullpath );	//得到文本编码
		
		mBookFile = new File(fullpath);
		long lLen = mBookFile.length();
		mMbBufLen = (int)lLen;
		mMbBuf = new RandomAccessFile( mBookFile, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);	//读入虚拟内存
		
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
		return	mParagrapInfoList.size();
	}
	
	/**
	  * 得到指定部分的文本内容
	  */	
	public byte[] getParagraphBuffer( int part )
	{
		if( part < 0 || part >= mParagrapInfoList.size() )
		{
			return	null;
		}
		
		byte[] buffer = new byte[mParagrapInfoList.get(part).len];		//分段buf
		mMbBuf.position(mParagrapInfoList.get(part).startPos);			//先移动到开始位置
		mMbBuf.get( buffer, 0, mParagrapInfoList.get(part).len );		//读入物理内存
		
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
		
		byte[] buffer = new byte[len];		//分段buf
		mbb.position(begin);	//先移动到开始位置
		mbb.get( buffer, 0, len );	//读入物理内存
		
		int paragraphLen = len;		//段落的长度
		int i = 0;
		byte b0, b1;
		
		//根据编码格式判断换行
		if( mStrCharsetName.equals("utf-16le") ) 
		{
			while( i < len - 1 ) 
			{
				b0 = buffer[i++];
				b1 = buffer[i++];
				if( b0 == 0x0a && b1 == 0x00 ) 
				{
					paragraphLen = i;
					continue;
				}
			}
		} 
		else if( mStrCharsetName.equals("utf-16be") ) 
		{
			while( i < len - 1 ) 
			{
				b0 = buffer[i++];
				b1 = buffer[i++];
				if( b0 == 0x00 && b1 == 0x0a ) 
				{
					paragraphLen = i;
					continue;
				}
			}
		} 
		else 
		{
			while( i < len ) 
			{
				b0 = buffer[i++];
				if( b0 == 0x0a ) 
				{
					paragraphLen = i;
					continue;
				}
			}
		}
		
		ParagrapInfo pi = new ParagrapInfo();
		pi.startPos = begin;
		pi.len = paragraphLen;
		
		mParagrapInfoList.add(pi);
		
		return	(begin+paragraphLen);	//返回下一段开始位置	
	}
	
	//分段信息类
	private class ParagrapInfo
	{
		public int startPos;	//开始位置
		public int len;			//长度
	}
}
