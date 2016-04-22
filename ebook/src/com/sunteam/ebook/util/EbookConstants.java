package com.sunteam.ebook.util;

import com.sunteam.ebook.R;

/**
 * 该类定义了基础常量。
 * 
 * @author wzp
 */
public class EbookConstants 
{
	public static final String APPID = "5714cb7e";	//TTS的appid
	
	public static final int[] ViewBkDrawable = {
		R.drawable.black,
		R.drawable.white,
		R.drawable.green,
		R.drawable.black,
		R.drawable.blue,
		R.drawable.white,
		R.drawable.yellow,
		R.drawable.blue,
	};	//View背景色
	
	public static final int[] ViewBkColorID = {
		R.color.black,
		R.color.white,
		R.color.green,
		R.color.black,
		R.color.blue,
		R.color.white,
		R.color.yellow,
		R.color.blue,
	};	//View背景色
	
	public static final int[] FontColorID = {
		R.color.white,
		R.color.black,
		R.color.black,
		R.color.green,
		R.color.white,
		R.color.blue,
		R.color.blue,
		R.color.yellow,
	};	//字体颜色
	
	public static final int[] SelectBkColorID = {
		R.color.red,
		R.color.green,
		R.color.white,
		R.color.blue,
		R.color.ltred,
		R.color.green,
		R.color.white,
		R.color.red,
	};	//选中背景色
	
	//数据库表字段
	public static final String BOOKS_TABLE = "books";
	public static final String BOOK_NAME = "name";
	public static final String BOOK_PATH = "path";
	public static final String BOOK_FOLDER = "folder";	//0为文件，1为文件夹
	public static final String BOOK_TYPE = "type";		//1为收藏，2为最近浏览
	public static final String BOOK_TXT = "txt";		
	public static final String BOOK_WORD = "doc";	
	public static final String BOOK_WORDX = "docx";	
	public static final String BOOK_DAISY = "daisy";
	
	public static final int MAX_PARAGRAPH = 0x400000;	//最大段落长度
	
}
