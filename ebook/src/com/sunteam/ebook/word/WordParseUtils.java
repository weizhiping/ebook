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

import org.textmining.text.extraction.WordExtractor;
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
	private static String getHideTxtFilePath( String path )
	{
		String fullpath = "";
		
		int home = path.lastIndexOf("/");
		fullpath = path.substring(0, home+1)+".";
		int end = path.lastIndexOf(".");
		fullpath += path.substring(home+1, end+1)+EbookConstants.BOOK_TXT;
		
		return	fullpath;
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
                        	out.write(xmlParser.nextText() + "\n");
                        }
                        break;
        			case XmlPullParser.END_TAG:
        				break;
        			default:
        				break;
        		}
        		evtType = xmlParser.next();
        	}
	        out.close();
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
		
		return	txtPath;
	}
	
    public String readDocEx(String file)
    {
        //创建输入流用来读取doc文件
        FileInputStream in;
        String text = null;
        try {
            in = new FileInputStream(new File(file));
            WordExtractorEx extractor = null;
            //创建WordExtractor
            extractor = new WordExtractorEx();
            //进行提取对doc文件
            text = extractor.extractText(in);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
    
    public String readDoc(String file){
        //创建输入流用来读取doc文件
        FileInputStream in;
        String text = null;
        try {
            in = new FileInputStream(new File(file));
            WordExtractor extractor = null;
            //创建WordExtractor
            extractor = new WordExtractor();
            //进行提取对doc文件
            text = extractor.extractText(in);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
    
    public static String readDocx(String path) 
    {
        String river = "";
        try 
        {
        	ZipFile xlsxFile = new ZipFile(new File(path));
        	ZipEntry sharedStringXML = xlsxFile.getEntry("word/document.xml");
        	InputStream inputStream = xlsxFile.getInputStream(sharedStringXML);
        	XmlPullParser xmlParser = Xml.newPullParser();
        	xmlParser.setInput(inputStream, "utf-8");
        	int evtType = xmlParser.getEventType();
        	while (evtType != XmlPullParser.END_DOCUMENT) 
        	{
        		switch (evtType) 
        		{
        			case XmlPullParser.START_TAG:
        				String tag = xmlParser.getName();
                        System.out.println(tag);
                        if (tag.equalsIgnoreCase("t")) 
                        {
                        	river += xmlParser.nextText() + "\n";
                        }
                        break;
        			case XmlPullParser.END_TAG:
        				break;
        			default:
        				break;
        		}
        		evtType = xmlParser.next();
        	}
        } 
        catch (ZipException e) 
        {
        	e.printStackTrace();
        } 
        catch (IOException e) 
        {
        	e.printStackTrace();
        } 
        catch (XmlPullParserException e) 
        {
        	e.printStackTrace();
        }
        
        if (river == null) 
        {
        	river = "解析文件出现问题";
        }

        return river;
    }
}
