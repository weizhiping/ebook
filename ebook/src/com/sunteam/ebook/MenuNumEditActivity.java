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
import com.sunteam.ebook.util.TTSUtils;

/**
 * 数字选择界面
 * 
 * @author sylar
 */
public class MenuNumEditActivity extends Activity {
	private int number;
	private int maxNum;
	private int flage;//0 语速，1为语调
	private String title;
	private EditText numView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_num_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		initViews();
	}

	private void initViews() {
		Intent intent = getIntent();
		title = intent.getStringExtra("edit_name");
		maxNum = intent.getIntExtra("edit_max", 0);
		number = intent.getIntExtra("edit_current", 0);
		flage = intent.getIntExtra("edit_flage", 0);
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
		numView.setText(1+"");
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
		case KeyEvent.KEYCODE_DPAD_UP: // 上
			number--;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN: // 下
			number++;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左
			number--;
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右
			number++;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
			if(0 == flage){
				TTSUtils.getInstance().setSpeed(number);
			}else if(1 == flage){
				TTSUtils.getInstance().setPitch(number);
			}
			ScreenManager.getScreenManager().popAllActivityExceptOne();
			return true;
		default:
			break;
		}
		if(number > maxNum){
			number = 1;
		}else if(number < 1){
			number = maxNum;
		}
		numView.setText(number + "");
		TTSUtils.getInstance().speakTips(title + number);
		return super.onKeyDown(keyCode, event);
	}
}
