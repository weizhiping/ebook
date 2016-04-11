package com.sunteam.ebook;

import java.io.IOException;

import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
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
		TextReaderView textView = (TextReaderView) findViewById(R.id.read_txt_view);
		textView.setTextColor(this.getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
	    try {
	    	textView.openBook(filePath, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
