package com.sunteam.ebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;

/**
 * 增加书签界面
 * 
 * @author sylar
 */
public class MenuTextEditActivity extends Activity {
	private EditText numView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		initViews();
	}

	private void initViews() {
		Intent intent = getIntent();
		String title = intent.getStringExtra("edit_name");
		String currentText = intent.getStringExtra("page_text");
		int currentPage = intent.getIntExtra("page_cur", 1);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.menu_layout);
		TextView titleView = (TextView) findViewById(R.id.title_menu);
		numView = (EditText) findViewById(R.id.num_edit);
		View line = findViewById(R.id.menu_line);
		titleView.setText(title);
		numView.setFocusable(false);
		int mColorSchemeIndex = PublicUtils.getColorSchemeIndex(); // 得到系统配色索引
		layout.setBackgroundResource(EbookConstants.ViewBkColorID[mColorSchemeIndex]); // 设置View的背景色
		titleView.setTextColor(getResources().getColor(
				EbookConstants.FontColorID[mColorSchemeIndex])); // 设置title的背景色
		line.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
		numView.setTextColor(getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
		
		numView.setText(String.format(getResources().getString(R.string.menu_text_tips), currentPage)+" " + currentText);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回

			break;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
			ScreenManager.getScreenManager().popAllActivityExceptOne();
			return true;
		default:
			break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
