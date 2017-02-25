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

import android.util.Xml;

/**
 * Word解析工具类。
 * 
 * @author wzp
 */
public class WordParseUtils 
{
	//得到隐藏txt临时文件路径
	public static String getHideTxtFilePath( String path )
	{
		String fullpath = "";
		
		int home = path.lastIndexOf("/");
		fullpath = path.substring(0, home+1)+".";
		int end = path.lastIndexOf(".");
		fullpath += path.substring(home+1, end+1)+EbookConstants.BOOK_TXT;
		
		return	fullpath;
	}
	
	//将doc文件转为txt文件
	public static String doc2txt( String docPath )
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
	
	//将docx文件转为txt文件
	public static String docx2txt( String docxPath )
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
