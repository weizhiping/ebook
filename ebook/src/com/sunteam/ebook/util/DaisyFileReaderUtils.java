package com.sunteam.ebook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.text.TextUtils;

import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.DiasySentenceNode;

/**
 * Daisy文件读取工具类。
 * 
 * @author wzp
 */
public class DaisyFileReaderUtils 
{
	private static final String TAG_BODY_START = "<body>";
	private static final String TAG_BODY_END = "</body>";
	
	//以下TAG主要在ncc.*文件中使用
	private static final String TAG_H_START = "<h";
	private static final String TAG_H_END = "</h";
	private static final String TAG_A_START = "<a href=\"";
	private static final String TAG_A_END = "</a>";
	
	//以下TAG主要在*.smil文件中使用
	private static final String TAG_TEXT_START = "<text";
	private static final String TAG_TEXT_END = "/>";
	private static final String TAG_AUDIO_START = "<audio";
	private static final String TAG_AUDIO_END = "/>";
	
	//以下TAG主要在*.opf文件中使用
	private static final String TAG_MANIFEST_START = "<manifest>";
	private static final String TAG_MANIFEST_END = "</manifest>";
	private static final String TAG_ITEM_START = "<item";
	private static final String TAG_ITEM_END = "/>";
	
	//以下TAG主要在*.ncx文件中使用
	private static final String TAG_NAVMAP_START = "<navMap";
	private static final String TAG_NAVMAP_END = "</navMap>";
	private static final String TAG_NAVPOINT_START = "<navPoint";
	private static final String TAG_NAVPOINT_END = "</navPoint>";
	private static final String TAG_NAVTEXT_START = "<text>";
	private static final String TAG_NAVTEXT_END = "</text>";
	private static final String TAG_CONTENT_START = "<content";
	private static final String TAG_CONTENT_END = "/>";
	
	private static DaisyFileReaderUtils instance = null;

	private ArrayList<DiasyNode> mDiasyNodeList = new ArrayList<DiasyNode>();	//保存索引列表
	private String mSentencePath = null;	//保存句子的路径
	private String mSentenceData = null;	//保存所有的句子数据
	private DaisyType mDaisyType = DaisyType.DAISY2;
	
	enum DaisyType
	{
		DAISY2,
		DAISY3,
	}	//Daisy文件格式
	
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
			String strCharsetName = ie.GetEncodingName( fullpath );	//得到文本编码
			
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
			
			String filename = file.getName();			//文件名
			int seq = filename.lastIndexOf(".");	
			String suffix = filename.substring(seq+1);	//后缀名
			if( "opf".equalsIgnoreCase(suffix) )
			{
				mDaisyType = DaisyType.DAISY3;
			}
			else
			{
				mDaisyType = DaisyType.DAISY2;
			}
			
			//先将索引文件读取到内存
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			
			if( DaisyType.DAISY2 == mDaisyType )
			{
				initDaisy2(buffer, strCharsetName);
			}
			else
			{
				seq = fullpath.lastIndexOf("/");
				initDaisy3(fullpath.substring(0, seq), buffer, strCharsetName);
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
	
	/**
	 * 得到某个叶子节点的全部句子信息
	 * @param smilPath：此叶子节点的句子所在的文件
	 * 			lableName: 句子开始的标签(貌似无用)
	 * @return 所有句子节点列表
	*/	
	public ArrayList<DiasySentenceNode> getDiasySentenceNodeList( String path, String smil, String lableName )
	{		
		ArrayList<DiasySentenceNode> list = new ArrayList<DiasySentenceNode>();
		
		try 
		{
			final String smilPath = path+"/"+smil;
			
			IdentifyEncoding ie = new IdentifyEncoding();
			String strCharsetName = ie.GetEncodingName( smilPath );	//得到文本编码
			
			File file = new File(smilPath);
			if( !file.exists() )
			{
				return	list;
			}
			int length = (int)file.length();
			if( 0 == length )
			{
				return	list;
			}
			
			//先将smil文件读取到内存
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
		
			String data = new String(buffer, strCharsetName);
			int start = data.indexOf(TAG_BODY_START);
			int end = data.lastIndexOf(TAG_BODY_END);
			
			if( ( -1 == start ) || ( -1 == end ) )
			{
				//body为空
				return	list;
			}
			
			start += TAG_BODY_START.length();
			
			while( true )
			{
				start = data.indexOf(TAG_TEXT_START, start);
				end = data.indexOf(TAG_TEXT_END, start);
				if( ( -1 == start ) || ( -1 == end ) )
				{
					break;
				}
				
				String txtItem = data.substring(start+TAG_TEXT_START.length(), end);	//取得一个txt item
				String[] splitTextItem = txtItem.split(" ");
				if( ( null == splitTextItem ) || ( 0 == splitTextItem.length ) )
				{
					break;
				}
				
				int i = 0;
				for( ; i < splitTextItem.length; i++ )
				{
					if( splitTextItem[i].indexOf("src=\"") == 0 )
					{
						break;
					}
				}
				
				start = end+TAG_TEXT_END.length();
				
				if( i >= splitTextItem.length )
				{
					continue;
				}
				
				String temp = splitTextItem[i].replaceAll("\"", "#");
				if( TextUtils.isEmpty(temp) )
				{
					continue;
				}
				
				String[] splitText = temp.split("#");
				if( ( null == splitText ) || ( splitText.length < 3 ) )
				{
					continue;
				}
				
				DiasySentenceNode node = new DiasySentenceNode();
				String sentence = getEscapeString(getDaisySentence( path, splitText[1], splitText[2] ))+"\n";
				try
				{
					byte[] byteSentence  = sentence.getBytes("GB18030");	//转换成指定编码
					//别的编码转为gb18030的时候可能会加上BOM，gb18030的BOM是0x84 0x31 0x95 0x33，使用的时候需要跳过BOM
					if( ( byteSentence.length >= 4 ) && ( -124 == byteSentence[0] ) && ( 49 == byteSentence[1] ) && ( -107 == byteSentence[2] ) && ( 51 == byteSentence[3] ) )
					{
						node.sentence = new byte[byteSentence.length-4];
						for( int j = 0; j < node.sentence.length; j++ )
						{
							node.sentence[j] = byteSentence[j+4];
						}
					}
					else
					{
						node.sentence = byteSentence;
					}
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				
				start = getDaisySentenceAudioInfo(data, start, node);
				
				list.add(node);
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
		
		return	list;
	}

	//初始化Daisy2文件
	private void initDaisy2( byte[] buffer, String strCharsetName )
	{
		try
		{
			String data = new String(buffer, strCharsetName);
			int start = data.indexOf(TAG_BODY_START);
			int end = data.lastIndexOf(TAG_BODY_END);
			
			if( ( -1 == start ) || ( -1 == end ) )
			{
				//body为空
				return;
			}
			
			start += TAG_BODY_START.length();

			while( true )
			{
				start = data.indexOf(TAG_H_START, start);
				end = data.indexOf(TAG_H_END, start);
				
				if( ( -1 == start ) || ( -1 == end ) )
				{
					break;
				}
				
				int oldEnd = end;
				String item = data.substring(start+TAG_H_START.length(), end);	//取得一个item
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
				
				node.name = getEscapeString(splitHref[1]);	//得到转义字符串
				mDiasyNodeList.add(node);
				
				start = oldEnd+TAG_H_END.length();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//初始化Daisy3文件
	private void initDaisy3( String path, byte[] buffer, String strCharsetName )
	{
		try
		{
			String data = new String(buffer, strCharsetName);
			int start = data.indexOf(TAG_MANIFEST_START);
			int end = data.lastIndexOf(TAG_MANIFEST_END);
			
			if( ( -1 == start ) || ( -1 == end ) )
			{
				//manifest为空
				return;
			}
			
			start += TAG_MANIFEST_START.length();

			while( true )
			{
				start = data.indexOf(TAG_ITEM_START, start);
				end = data.indexOf(TAG_ITEM_END, start);
				
				if( ( -1 == start ) || ( -1 == end ) )
				{
					break;
				}
				
				String item = data.substring(start+TAG_ITEM_START.length(), end);	//取得一个item
				if( item.contains("media-type=\"application/x-dtbncx+xml\"") )
				{
					String[] splitItem = item.split(" ");
					for( int i = 0; i < splitItem.length; i++ )
					{
						if( splitItem[i].contains("href=") )
						{
							start = splitItem[i].indexOf("\"");
							end = splitItem[i].lastIndexOf("\"");
							initNcx(path+"/"+splitItem[i].substring(start+1, end));	//初始化ncx文件
							return;
						}
					}
				}
				else
				{
					start = end+TAG_ITEM_END.length();
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	//解析navPoint
	private void parseNavPoint( String navpoint, int level )
	{
		String text = "";
		String content = "";
		
		int s = navpoint.indexOf(TAG_NAVTEXT_START);
		int e = navpoint.indexOf(TAG_NAVTEXT_END, s);
		if( ( -1 == s ) || ( -1 == e ) )
		{
			return;
		}
		else
		{
			text = navpoint.substring(s+TAG_NAVTEXT_START.length(), e);
		}
		
		s = navpoint.indexOf(TAG_CONTENT_START);
		e = navpoint.indexOf(TAG_CONTENT_END, s);
		if( ( -1 == s ) || ( -1 == e ) )
		{
			return;
		}
		else
		{
			content = navpoint.substring(s+TAG_CONTENT_START.length(), e);
			s = content.indexOf("\"");
			e = content.lastIndexOf("\"");
			content = content.substring(s+1, e);
		}
		
		String[] splitStr = content.split("#");
		
		DiasyNode node = new DiasyNode();
		node.seq = mDiasyNodeList.size();			//序号
		node.level = level;							//等级
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
		
		node.name = getEscapeString(text);	//得到转义字符串
		mDiasyNodeList.add(node);
	}
	
	//初始化ncx文件，从中解析出Daisy3.0文件的目录结构
	private void initNcx( String fullpath )
	{
		try 
		{
			IdentifyEncoding ie = new IdentifyEncoding();
			String strCharsetName = ie.GetEncodingName( fullpath );	//得到文本编码
			
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
			
			String data = new String(buffer, strCharsetName);
			
			int start = data.indexOf(TAG_NAVMAP_START);
			int end = data.lastIndexOf(TAG_NAVMAP_END);
			
			if( ( -1 == start ) || ( -1 == end ) )
			{
				//navmap为空
				return;
			}
			
			start += TAG_NAVMAP_START.length();
			int level = 1;

			while( true )
			{
				start = data.indexOf(TAG_NAVPOINT_START, start);
				end = data.indexOf(TAG_NAVPOINT_END, start);
				
				if( ( -1 == start ) || ( -1 == end ) )
				{
					break;
				}
				
				start += TAG_NAVPOINT_START.length();
				
				int position = data.indexOf(TAG_NAVPOINT_START, start);
				if( ( -1 == position ) || ( position > end ) )
				{
					//当前Point是叶子节点
					String navpoint = data.substring(start, end);
					parseNavPoint( navpoint, level );
					
					start = end+TAG_NAVPOINT_END.length();
					
					while( true )
					{
						int s = data.indexOf(TAG_NAVPOINT_START, start);
						int e = data.indexOf(TAG_NAVPOINT_END, start);
						if( ( -1 == s ) && ( -1 == e ) )
						{
							break;
						}
						else if( ( s != -1 ) && ( -1 == e ) )
						{
							break;
						}
						else if( ( -1 == s ) && ( e != -1 ) )
						{
							level--;
							start = e+TAG_NAVPOINT_END.length();
						}
						else if ( s > e )
						{
							level--;
							start = e+TAG_NAVPOINT_END.length();
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					String navpoint = data.substring(start, position);
					parseNavPoint( navpoint, level );
					
					start = position;
					level++;
				}
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
	
	/**
	 * 得到句子内容
	 * @param sentencePath：句子内容所在的文件
	 * 			lableName: 句子所在的标签
	 * @return 句子内容
	*/	
	private String getDaisySentence( String path, String sentenceHref, String lableName )
	{
		try 
		{
			final String sentencePath = path + "/" + sentenceHref;
			
			if( sentencePath.equals(mSentencePath) )
			{
				
			}
			else
			{
				IdentifyEncoding ie = new IdentifyEncoding();
				String strCharsetName = ie.GetEncodingName( sentencePath );	//得到文本编码
				
				File file = new File(sentencePath);
				if( !file.exists() )
				{
					return	"";
				}
				int length = (int)file.length();
				if( 0 == length )
				{
					return	"";
				}
				
				//先将content文件读取到内存
				FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[length];
				fis.read(buffer);
				fis.close();
				
				mSentencePath = sentencePath;
				mSentenceData = new String(buffer, strCharsetName);
			}
		
			String data = mSentenceData;
			if( DaisyType.DAISY2 == mDaisyType )	//Daisy2.0
			{
				int start = data.indexOf(TAG_BODY_START);
				int end = data.lastIndexOf(TAG_BODY_END);
				
				if( ( -1 == start ) || ( -1 == end ) )
				{
					//body为空
					return	"";
				}
				
				start += TAG_BODY_START.length();
				
				while( true )
				{
					start = data.indexOf(lableName, start);
					if( -1 == start )
					{
						return	"";
					}
					
					start += lableName.length();
					
					start = data.indexOf(TAG_A_START, start);
					end = data.indexOf(TAG_A_END, start);
					if( ( -1 == start ) || ( -1 == end ) )
					{
						return	"";
					}
					
					String sentenceItem = data.substring(start+TAG_A_START.length(), end);	//取得一个sentence item
					String[] splitSentenceItem = sentenceItem.split(">");
					if( ( null == splitSentenceItem ) || ( 2 != splitSentenceItem.length ) )
					{
						return	"";
					}
					
					return	splitSentenceItem[1];
				}
			}
			else	//Daisy3.0
			{
				int start = data.indexOf("id=\""+lableName+"\"");
				if( -1 == start )
				{
					return	"";
				}
				start = data.indexOf(">", start );
				if( -1 == start )
				{
					return	"";
				}
				int end = data.indexOf("</", start);
				if( -1 == end )
				{
					return	"";
				}
				
				return	data.substring(start+1, end);
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

		return	"";
	}
	
	//得到句子对应的音频文件信息
	private int getDaisySentenceAudioInfo( String data, int offset, DiasySentenceNode node )
	{
		int n = 0;
		while( true )
		{
			int textStart = data.indexOf(TAG_TEXT_START, offset);
			int start = data.indexOf(TAG_AUDIO_START, offset);
			int end = data.indexOf(TAG_AUDIO_END, offset);
			if( ( -1 == start ) || ( -1 == end ) || ( textStart != -1 && start > textStart ) )
			{
				break;
			}
			
			String audioItem = data.substring(start+TAG_AUDIO_START.length(), end);	//取得一个audio item
			String[] splitAudioItem = audioItem.split(" ");
			if( ( null == splitAudioItem ) || ( 0 == splitAudioItem.length ) )
			{
				break;
			}
			
			int i = 0;
			for( ; i < splitAudioItem.length; i++ )
			{
				if( splitAudioItem[i].indexOf("src=\"") == 0 )	//音频文件
				{
					String[] splitAudio = splitAudioItem[i].replaceAll("\"", "#").split("#");
					node.audioFile = splitAudio[1];
				}
				else if( splitAudioItem[i].indexOf("clip-begin=\"npt=") == 0 )	//时间开始,daisy2.0
				{
					if( 0 == n )
					{
						String[] splitTime = splitAudioItem[i].replaceAll("s\"", "").split("=");
						node.startTime = (long)(Float.parseFloat(splitTime[2])*1000);
					}
					n++;
				}
				else if( splitAudioItem[i].indexOf("clip-end=\"npt=") == 0 )	//时间结束,daisy2.0
				{
					String[] splitTime = splitAudioItem[i].replaceAll("s\"", "").split("=");
					node.endTime = (long)(Float.parseFloat(splitTime[2])*1000);
				}
				else if( splitAudioItem[i].indexOf("clipBegin=\"") == 0 )	//时间开始,daisy3.0
				{
					if( 0 == n )
					{
						String[] splitTime = splitAudioItem[i].split("\"");
						if( splitTime[1].contains(".") )	//有毫秒
						{
							String[] splitStr1 = splitTime[1].split("\\.");
							String[] splitStr2 = splitStr1[0].split(":");
							
							node.startTime = ( Long.parseLong(splitStr2[0]) * 3600 + Long.parseLong(splitStr2[1]) * 60 + Long.parseLong(splitStr2[2]) ) * 1000L + Long.parseLong(splitStr1[1]);  
						}
						else
						{
							String[] splitStr2 = splitTime[1].split(":");
							node.startTime = ( Long.parseLong(splitStr2[0]) * 3600 + Long.parseLong(splitStr2[1]) * 60 + Long.parseLong(splitStr2[2]) ) * 1000L;
						}
					}
					n++;
				}
				else if( splitAudioItem[i].indexOf("clipEnd=\"") == 0 )	//时间结束,daisy3.0
				{
					String[] splitTime = splitAudioItem[i].split("\"");
					if( splitTime[1].contains(".") )	//有毫秒
					{
						String[] splitStr1 = splitTime[1].split("\\.");
						String[] splitStr2 = splitStr1[0].split(":");
						
						node.endTime = ( Long.parseLong(splitStr2[0]) * 3600 + Long.parseLong(splitStr2[1]) * 60 + Long.parseLong(splitStr2[2]) ) * 1000L + Long.parseLong(splitStr1[1]);  
					}
					else
					{
						String[] splitStr2 = splitTime[1].split(":");
						node.endTime = ( Long.parseLong(splitStr2[0]) * 3600 + Long.parseLong(splitStr2[1]) * 60 + Long.parseLong(splitStr2[2]) ) * 1000L;
					}
				}
			}
			
			offset = end+TAG_AUDIO_END.length();
		}
		
		return	offset;
	}
	
	//得到转义字符串
	private String getEscapeString( String str )
	{
		String name = "";
		String[] splitUnicode = str.split("&#");
		
		if( (null == splitUnicode ) || ( 0 == splitUnicode.length) )
		{
			return	str;					//节点名称
		}
		else
		{
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
						name = splitUnicode[i];
						continue;
					}
					
					String unicode = splitUnicode[i].substring(1, seq);
					try
					{
						int code = Integer.parseInt(unicode, 16);
						byte[] byteCode = new byte[2];
						byteCode[0] = (byte) ((code&0x0000ff00)>>8);
						byteCode[1] = (byte) (code&0x000000ff);
						
						name += new String(byteCode, "utf-16be");
						
						String temp = splitUnicode[i].substring(seq+1);
						if( null != temp )
						{
							name += temp;
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
						name = splitUnicode[i];
					}
				}
				else	//十进制
				{
					int seq = splitUnicode[i].indexOf(";");
					if( -1 == seq )
					{
						name = splitUnicode[i];
						continue;
					}
					
					String unicode = splitUnicode[i].substring(0, seq);
					try
					{
						int code = Integer.parseInt(unicode, 10);
						byte[] byteCode = new byte[2];
						byteCode[0] = (byte) ((code&0x0000ff00)>>8);
						byteCode[1] = (byte) (code&0x000000ff);
						
						name += new String(byteCode, "utf-16be");
						
						String temp = splitUnicode[i].substring(seq+1);
						if( null != temp )
						{
							name += temp;
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
						name = splitUnicode[i];
					}
				}
			}
		}
		
		return	name;
	}
}
