package com.sunteam.ebook;

import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;
import com.sunteam.ebook.view.TextReaderView;

import android.app.Activity;
import android.os.Bundle;
/**
 * TXT文件显示
 * @author sylar
 *
 */
public class ReadTxtActivity extends Activity {
	private int mColorSchemeIndex = 0;	//系统配色索引
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_txt);
		
		//此处需要从系统配置文件中得到配色方案索引
    	PublicUtils.setColorSchemeIndex(mColorSchemeIndex);
    	this.getWindow().setBackgroundDrawableResource(EbookConstants.ViewBkDrawable[mColorSchemeIndex]);
		String filePath = getIntent().getStringExtra("path");
		int part = getIntent().getIntExtra("part", 0);
		TextReaderView textView = (TextReaderView) findViewById(R.id.read_txt_view);
		textView.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
	    textView.openBook(TextFileReaderUtils.getInstance().getParagraphBuffer(part), TextFileReaderUtils.getInstance().getCharsetName(), 0);
	}
}
