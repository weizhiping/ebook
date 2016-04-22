package com.sunteam.ebook.word;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.textmining.text.extraction.WordExtractor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * Word解析工具类。
 * 
 * @author wzp
 */
public class WordParseUtils 
{
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
