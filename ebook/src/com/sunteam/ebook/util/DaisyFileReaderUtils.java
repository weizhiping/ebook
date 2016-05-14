package com.sunteam.ebook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sunteam.ebook.entity.DiasyNode;

/**
 * Daisy文件读取工具类。
 * 
 * @author wzp
 */
public class DaisyFileReaderUtils 
{
	private static final String TAG_BODY_START = "<body>";
	private static final String TAG_BODY_END = "</body>";
	private static final String TAG_H_START = "<h";
	private static final String TAG_H_END = "</h";
	private static final String TAG_A_START = "<a href=\"";
	private static final String TAG_A_END = "</a>";
	
	private static DaisyFileReaderUtils instance = null;

	private String mStrCharsetName = "utf-8";		//编码格式，默认为utf-8
	private ArrayList<DiasyNode> mDiasyNodeList = new ArrayList<DiasyNode>();	//保存索引列表
	
	public static DaisyFileReaderUtils getInstance()
	{
		if( null == instance )
		{
			instance = new DaisyFileReaderUtils();
		}
		
		return instance;
	}
	
	/**
	 * 根据父节点得到此节点下所有子节点信息
	 * @param fatherSeq：父节点在他那一级别中的序号
	 * @return 此父节点的所有子节点列表
	*/
	public ArrayList<DiasyNode> getChildNodeList( int fatherSeq  )
	{
		ArrayList<DiasyNode> list = new ArrayList<DiasyNode>();
		
		int size = mDiasyNodeList.size();
		
		if( -1 == fatherSeq )	//得到第一级别子节点
		{	
			for( int i = 0; i < size; i++ )
			{
				DiasyNode node = mDiasyNodeList.get(i);
				if( 1 == node.level )
				{
					list.add(node);
				}
			}
		}
		else
		{
			DiasyNode fatherNode = mDiasyNodeList.get(fatherSeq);	//先得到父节点信息
			for( int i = fatherSeq+1; i < size; i++ )
			{
				DiasyNode node = mDiasyNodeList.get(i);
				if( fatherNode.level+1 == node.level )
				{
					list.add(node);
				}
				else
				{
					break;
				}
			}
		}
		
		return	list;
	}

	//初始化
	@SuppressWarnings("resource")
	public void init( final String fullpath )
	{		
		mDiasyNodeList.clear();
		
		try 
		{
			IdentifyEncoding ie = new IdentifyEncoding();
			mStrCharsetName = ie.GetEncodingName( fullpath );	//得到文本编码
			
			File file = new File(fullpath);
			if( !file.exists() )
			{
				return;
			}
			int length = (int)file.length();
			if( 0 == length )
			{
				return;
			}
			
			//先将索引文件读取到内存
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
		
			String data = new String(buffer, mStrCharsetName);
			int start = data.indexOf(TAG_BODY_START);
			int end = data.lastIndexOf(TAG_BODY_END);
			
			if( ( -1 == start ) || ( -1 == end ) )
			{
				//body为空
				return;
			}
			
			String body = data.substring(start+TAG_BODY_START.length(), end);	//得到body内容
			
			while( true )
			{
				start = body.indexOf(TAG_H_START);
				end = body.indexOf(TAG_H_END);
				
				if( ( -1 == start ) || ( -1 == end ) )
				{
					break;
				}
				
				int oldEnd = end;
				String item = body.substring(start+TAG_H_START.length(), end);	//取得一个item
				String[] split = item.split(" ");
				
				start = item.indexOf(TAG_A_START);
				end = item.indexOf(TAG_A_END);
				String href = item.substring(start+TAG_A_START.length(), end);
				href = href.replaceAll("\">", "#");
				
				String[] splitStr = href.split("#");
				
				DiasyNode node = new DiasyNode();
				node.seq = mDiasyNodeList.size();			//序号
				node.level = Integer.parseInt(split[0]);	//等级
				node.href = splitStr[0];					//子链接
				node.label = splitStr[1];					//标签
				node.name = splitStr[2];					//节点名称
				
				mDiasyNodeList.add(node);
				
				body = body.substring(oldEnd+TAG_H_END.length());
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
