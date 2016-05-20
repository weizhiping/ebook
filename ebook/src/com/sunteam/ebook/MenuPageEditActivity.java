package com.sunteam.ebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;

/**
 * 选择页码界面
 * 
 * @author sylar
 */
public class MenuPageEditActivity extends Activity {
	private int number;
	private EditText numView;
	private int currentPage;
	private int totalPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_num_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		Intent intent = getIntent();
		currentPage = intent.getIntExtra("page_cur", 1);
		totalPage = intent.getIntExtra("page_count", 1);
		number = currentPage;
		initViews();
	}

	private void initViews() {

		String title = getIntent().getStringExtra("edit_name");
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.menu_layout);
		TextView titleView = (TextView) findViewById(R.id.title_menu);
		numView = (EditText) findViewById(R.id.num_edit);
		View line = findViewById(R.id.menu_line);
		titleView.setText(title);

		int mColorSchemeIndex = PublicUtils.getColorSchemeIndex(); // 得到系统配色索引
		layout.setBackgroundResource(EbookConstants.ViewBkColorID[mColorSchemeIndex]); // 设置View的背景色
		titleView.setTextColor(getResources().getColor(
				EbookConstants.FontColorID[mColorSchemeIndex])); // 设置title的背景色
		line.setBackgroundResource(EbookConstants.FontColorID[mColorSchemeIndex]);
		numView.setTextColor(getResources().getColor(EbookConstants.FontColorID[mColorSchemeIndex]));
		String tips = String.format(getResources().getString(R.string.page_read_tips), currentPage,totalPage );
		numView.setText(tips);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回

			break;
		case KeyEvent.KEYCODE_DPAD_UP: // 上
			number--;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN: // 下
			number++;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左
			String num = String.valueOf(number);
			if(1 == num.length()){
				number = 0;
			}else{
				num = num.substring(0, num.length()-1);
				number = Integer.valueOf(num);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右
		//	Log.e("page", "--right------page--:" );
			number = number*10;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
		//	Log.e("page", "-----enter num--:" + number);
			ScreenManager.getScreenManager().popAllActivityExceptOne();
			return true;
		default:
			break;
		}
		if(number > totalPage){
			number = totalPage;
		}else if(number < 1){
			number = 1;
		}
		numView.setText(number + "");
		return super.onKeyDown(keyCode, event);
	}
}
