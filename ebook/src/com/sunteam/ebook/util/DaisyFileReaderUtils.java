package com.sunteam.ebook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
				if( fatherNode.seq == node.father )
				{
					list.add(node);
				}
			}
		}
		
		return	list;
	}

	//初始化
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
				String[] splitItem = item.split(" ");
				
				start = item.indexOf(TAG_A_START);
				end = item.indexOf(TAG_A_END);
				String href = item.substring(start+TAG_A_START.length(), end);
				String[] splitHref = href.split("\">");
				String[] splitStr = splitHref[0].split("#");
				
				DiasyNode node = new DiasyNode();
				node.seq = mDiasyNodeList.size();			//序号
				node.level = Integer.parseInt(splitItem[0]);//等级
				node.href = splitStr[0];					//子链接
				node.label = splitStr[1];					//标签
				if( 1 == node.level )	//如果节点等级为第一等级
				{
					node.father = -1;
				}
				else
				{
					for( int i = node.seq-1; i >= 0; i-- )
					{
						if( node.level-1 == mDiasyNodeList.get(i).level )
						{
							node.father = mDiasyNodeList.get(i).seq;
							break;
						}
					}
				}
				
				String[] splitUnicode = splitHref[1].split("&#");
				if( (null == splitUnicode ) || ( 0 == splitUnicode.length) )
				{
					node.name = splitHref[1];					//节点名称
				}
				else
				{
					node.name = "";
					for( int i = 0; i < splitUnicode.length; i++ )
					{
						if( "".equals(splitUnicode[i]) )
						{
							continue;
						}
						String ch = splitUnicode[i].substring(0, 1);
						if( "x".equals(ch) || "X".equals(ch) )	//十六进制
						{
							int seq = splitUnicode[i].indexOf(";");
							if( -1 == seq )
							{
								node.name = splitUnicode[i];
								continue;
							}
							
							String unicode = splitUnicode[i].substring(1, seq);
							try
							{
								int code = Integer.parseInt(unicode, 16);
								byte[] byteCode = new byte[2];
								byteCode[0] = (byte) ((code&0x0000ff00)>>8);
								byteCode[1] = (byte) (code&0x000000ff);
								
								node.name += new String(byteCode, "utf-16be");
								
								String temp = splitUnicode[i].substring(seq+1);
								if( null != temp )
								{
									node.name += temp;
								}
							}
							catch( Exception e )
							{
								e.printStackTrace();
								node.name = splitUnicode[i];
							}
						}
						else	//十进制
						{
							int seq = splitUnicode[i].indexOf(";");
							if( -1 == seq )
							{
								node.name = splitUnicode[i];
								continue;
							}
							
							String unicode = splitUnicode[i].substring(0, seq);
							try
							{
								int code = Integer.parseInt(unicode, 10);
								byte[] byteCode = new byte[2];
								byteCode[0] = (byte) ((code&0x0000ff00)>>8);
								byteCode[1] = (byte) (code&0x000000ff);
								
								node.name += new String(byteCode, "utf-16be");
								
								String temp = splitUnicode[i].substring(seq+1);
								if( null != temp )
								{
									node.name += temp;
								}
							}
							catch( Exception e )
							{
								e.printStackTrace();
								node.name = splitUnicode[i];
							}
						}
					}
				}
				
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
