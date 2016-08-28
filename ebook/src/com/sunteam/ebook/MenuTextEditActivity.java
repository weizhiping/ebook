package com.sunteam.ebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;

/**
 * 增加书签界面
 * 
 * @author sylar
 */
public class MenuTextEditActivity extends Activity {
	private EditText numView;
	private DatabaseManager manager;
	private FileInfo info;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		manager = new DatabaseManager(this);
		initViews();
	}

	private void initViews() {
		Intent intent = getIntent();
		String title = intent.getStringExtra("edit_name");
		String currentText = intent.getStringExtra("page_text");
		int currentPage = intent.getIntExtra("page_cur", 1);
		info = (FileInfo) intent.getSerializableExtra("fileinfo");
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.menu_layout);
		TextView titleView = (TextView) findViewById(R.id.title_menu);
		numView = (EditText) findViewById(R.id.text_edit);
		View line = findViewById(R.id.menu_line);
		titleView.setText(title);
		
		Tools tools = new Tools(this);
		
		layout.setBackgroundColor(tools.getBackgroundColor());	// 设置View的背景色
		titleView.setTextColor(tools.getFontColor()); 			// 设置title的背景色
		line.setBackgroundColor(tools.getFontColor());			// 设置分割线的背景色
		numView.setTextColor(tools.getFontColor());
		
		final float scale = this.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数;
		float fontSize = tools.getFontSize() * scale;
		titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale); // 设置title字号
		numView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale);		
		titleView.setHeight((int)fontSize); // 设置控件高度
		
		if(null == info.diasyPath){
			numView.setText(String.format(getResources().getString(R.string.menu_text_tips), currentPage)+" " + currentText);
		}else{
			numView.setText(currentText + " " + String.format(getResources().getString(R.string.menu_text_tips), currentPage));
		}
		numView.addTextChangedListener(watcher);
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			Log.e("mark", "-------beforeTextChanged------------");
			numView.setFocusable(true);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.e("mark", "-------afterTextChanged------------");
			numView.setFocusable(false);
		}

	};
	
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
			Log.e("mark", "-------entrty-------------");
			if(!TextUtils.isEmpty(numView.getText())){
				info.name = numView.getText().toString();
				manager.insertMarkToDb(info);
			}
			ScreenManager.getScreenManager().popAllActivityExceptOne();
			return true;
		default:
			break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
