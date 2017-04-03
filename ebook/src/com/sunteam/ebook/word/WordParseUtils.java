package com.sunteam.ebook.word;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sunteam.ebook.util.EbookConstants;

import android.os.MemoryFile;
import android.util.Xml;

/**
 * Word解析工具类。
 * 
 * @author wzp
 */
public class WordParseUtils 
{
	public static final String WORD_MEMORY_FILE = "WordMemoryFile";	//
	private static final boolean isUseMemFile = true;				//是否使用Memory File
	private static MemoryFile mMemoryFile = null;
	
	//创建内存文件
	public static MemoryFile openMemoryFile( String filename, int length )
	{
		try 
		{
			MemoryFile memoryFile = new MemoryFile(filename, length);
			if( memoryFile != null )
			{
				memoryFile.allowPurging(false);
			}
			
			return	memoryFile;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return	null;
	}
	
	//关闭内存文件
	public static void closeMemoryFile()
	{
		if( mMemoryFile != null )
		{
			mMemoryFile.close();
			mMemoryFile = null;
		}
	}
	
	public static MemoryFile getMemoryFile()
	{
		return	mMemoryFile;
	}
	
	//得到隐藏txt临时文件路径
	public static String getHideTxtFilePath( String path )
	{
		if( isUseMemFile )
		{
			return	WORD_MEMORY_FILE;
		}
		else
		{
			String fullpath = "";
			
			int home = path.lastIndexOf("/");
			fullpath = path.substring(0, home+1)+".";
			int end = path.lastIndexOf(".");
			fullpath += path.substring(home+1, end+1)+EbookConstants.BOOK_TXT;
			
			return	fullpath;
		}
	}
	
	//将doc文件转为txt文件
	public static String doc2txt( String docPath )
	{
		if( isUseMemFile )
		{
			closeMemoryFile();	//先关闭已经存在的内存文件
			File file = new File(docPath);
			if( !file.exists() )
			{
				return	null;
			}
			int length = (int) file.length();
			mMemoryFile = openMemoryFile( WORD_MEMORY_FILE, length );
			if( null == mMemoryFile )
			{
				return	null;
			}
			
	        try 
	        {
	        	OutputStream outstream = mMemoryFile.getOutputStream();
		        OutputStreamWriter out = new OutputStreamWriter(outstream);
		        
		        FileInputStream in = new FileInputStream(file);	//创建输入流用来读取doc文件
	            WordExtractorEx extractor = null;	//创建WordExtractor
	            extractor = new WordExtractorEx();
	            extractor.extractText(in, out);	//进行提取对doc文件
	            
	            in.close();
	            out.close();
	            outstream.close();
	        }
	        catch (FileNotFoundException e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
	        
			return	WORD_MEMORY_FILE;	
		}
		else
		{
			String txtPath = getHideTxtFilePath(docPath);
			
			File txtFile = new File(txtPath);
	        try 
	        {
	        	OutputStream outstream = new FileOutputStream(txtFile);
		        OutputStreamWriter out = new OutputStreamWriter(outstream);
		        
		        FileInputStream in = new FileInputStream(new File(docPath));	//创建输入流用来读取doc文件
	            WordExtractorEx extractor = null;	//创建WordExtractor
	            extractor = new WordExtractorEx();
	            extractor.extractText(in, out);	//进行提取对doc文件
	            
	            in.close();
	            out.close();
	            outstream.close();
	        }
	        catch (FileNotFoundException e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
	        
	        return txtPath;
		}
	}
	
	//将docx文件转为txt文件
	public static String docx2txt( String docxPath )
	{
		if( isUseMemFile )
		{
			closeMemoryFile();	//先关闭已经存在的内存文件
			File file = new File(docxPath);
			if( !file.exists() )
			{
				return	null;
			}
			int length = (int) file.length();
			mMemoryFile = openMemoryFile( WORD_MEMORY_FILE, length );
			if( null == mMemoryFile )
			{
				return	null;
			}
			
			try 
	        {
		        OutputStream outstream = mMemoryFile.getOutputStream();
		        OutputStreamWriter out = new OutputStreamWriter(outstream);
		                
		        ZipFile docxFile = new ZipFile(new File(docxPath));
	        	ZipEntry sharedStringXML = docxFile.getEntry("word/document.xml");
	        	InputStream inputStream = docxFile.getInputStream(sharedStringXML);
	        	XmlPullParser xmlParser = Xml.newPullParser();
	        	xmlParser.setInput(inputStream, "utf-8");
	        	int evtType = xmlParser.getEventType();
	        	while (evtType != XmlPullParser.END_DOCUMENT) 
	        	{
	        		switch (evtType) 
	        		{
	        			case XmlPullParser.START_TAG:
	        				String tag = xmlParser.getName();
	                        if (tag.equalsIgnoreCase("t")) 
	                        {
	                        	String str = xmlParser.nextText()+"\n";
	            				out.write(str);
	                        }
	                        break;
	        			case XmlPullParser.END_TAG:
	        				break;
	        			default:
	        				break;
	        		}
	        		evtType = xmlParser.next();
	        	}
	        	inputStream.close();
	        	docxFile.close();
		        out.close();
		        outstream.close();
	        }
	        catch (ZipException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        } 
	        catch (IOException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        } 
	        catch (XmlPullParserException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
			
			return	WORD_MEMORY_FILE;	
		}
		else
		{
			String txtPath = getHideTxtFilePath(docxPath);
			
			File txtFile = new File(txtPath);
	        try 
	        {
		        OutputStream outstream = new FileOutputStream(txtFile);
		        OutputStreamWriter out = new OutputStreamWriter(outstream);
		                
		        ZipFile docxFile = new ZipFile(new File(docxPath));
	        	ZipEntry sharedStringXML = docxFile.getEntry("word/document.xml");
	        	InputStream inputStream = docxFile.getInputStream(sharedStringXML);
	        	XmlPullParser xmlParser = Xml.newPullParser();
	        	xmlParser.setInput(inputStream, "utf-8");
	        	int evtType = xmlParser.getEventType();
	        	while (evtType != XmlPullParser.END_DOCUMENT) 
	        	{
	        		switch (evtType) 
	        		{
	        			case XmlPullParser.START_TAG:
	        				String tag = xmlParser.getName();
	                        if (tag.equalsIgnoreCase("t")) 
	                        {
	                        	String str = xmlParser.nextText()+"\n";
	            				out.write(str);
	                        }
	                        break;
	        			case XmlPullParser.END_TAG:
	        				break;
	        			default:
	        				break;
	        		}
	        		evtType = xmlParser.next();
	        	}
	        	inputStream.close();
	        	docxFile.close();
		        out.close();
		        outstream.close();
	        }
	        catch (ZipException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        } 
	        catch (IOException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        } 
	        catch (XmlPullParserException e) 
	        {
	        	e.printStackTrace();
	        	
	        	return	null;
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	            
	            return	null;
	        }
			
			return	txtPath;
		}
	}
}
