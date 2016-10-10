package com.sunteam.ebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;

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
	private int afterNum;
	private boolean isKeyNum = true;
	private boolean isMax;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ebook_activity_num_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		Intent intent = getIntent();
		currentPage = intent.getIntExtra("page_cur", 0);
		totalPage = intent.getIntExtra("page_count", 1);
		number = currentPage;
		initViews();
	}

	private void initViews() {

		String title = getIntent().getStringExtra("edit_name");
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.ebook_menu_layout);
		TextView titleView = (TextView) findViewById(R.id.ebook_title_menu);
		numView = (EditText) findViewById(R.id.ebook_num_edit);
		View line = findViewById(R.id.ebook_menu_line);
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
		
		String tips = String.format(getResources().getString(R.string.ebook_page_read_tips), currentPage,totalPage );
		numView.setText(tips);
		numView.setFocusable(false);
		TTSUtils.getInstance().speakMenu(title+"，"+ tips);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回

			break;
		case KeyEvent.KEYCODE_DPAD_UP: // 上
			isKeyNum = true;
			number--;
			if(number < 1)
			number = totalPage;
			setPage();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN: // 下
			isKeyNum = true;
			number++;
			setPage();
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左
			isKeyNum = true;
			String num = String.valueOf(number);
			if(1 == num.length()){
				number = 0;
			}else{
				num = num.substring(0, num.length()-1);
				number = Integer.valueOf(num);
			}
			setPage();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右
			isKeyNum = true;
			number = number*10;
			if(number > totalPage){
				number = totalPage;
			}
			setPage();
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER: // 确定
		case KeyEvent.KEYCODE_ENTER:
			if(0 == number){
				PublicUtils.showToast(this, getResources().getString(R.string.ebook_input_page_num));
			}else{
				Intent intent = new Intent(EbookConstants.MENU_PAGE_EDIT);
				intent.putExtra("result_flag", 0);
				intent.putExtra("page", number);
				sendBroadcast(intent);
				ScreenManager.getScreenManager().popAllActivityExceptOne();
			}
			return true;
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_NUMPAD_5:		
			afterNum = 5;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+5));
			setPage();
			break;
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_NUMPAD_7:	
			afterNum = 7;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+7));
			setPage();
			break;
		case KeyEvent.KEYCODE_9:
		case KeyEvent.KEYCODE_NUMPAD_9:	
			afterNum = 9;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+9));
			setPage();
			break;
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_NUMPAD_4:	
			afterNum = 4;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+4));
			setPage();
			break;
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_NUMPAD_6:		
			afterNum = 6;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+6));
			setPage();
			break;
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_NUMPAD_2:		
			afterNum = 2;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+2));
			setPage();
			break;
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_NUMPAD_8:		
			afterNum = 8;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+8));
			setPage();
			break;
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_NUMPAD_3:	
			afterNum = 3;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+3));
			setPage();
			break;
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_NUMPAD_0:	
			afterNum = 0;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+0));
			setPage();
			break;
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_NUMPAD_1:	
			afterNum = 1;
			if(isKeyNum){
				isKeyNum = false;
				number = 0;
			}
			number = Integer.valueOf((String.valueOf(number)+1));
			setPage();
			break;
		default:
			PublicUtils.showToast(this, getResources().getString(R.string.ebook_input_page_num)+ "，" + number);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setPage(){
		if(0 == number){
			numView.setText("");
			PublicUtils.showToast(this, getResources().getString(R.string.ebook_input_page_num));
		}else{
			if(number > totalPage){
				if(isKeyNum){
					number = 1;
					TTSUtils.getInstance().speakMenu(number+"");
				}else{
					if(!isMax){
						isMax = true;
						number = totalPage;
						PublicUtils.showToast(this, getResources().getString(R.string.ebook_input_page_max),new PromptListener() {
							
							@Override
							public void onComplete() {
								TTSUtils.getInstance().speakMenu(number+"");
								return;
							}
						});
					}else{
						isMax = false;
						number = afterNum;
						TTSUtils.getInstance().speakMenu(number+"");
					}
				}
			}else if(number < 1){
				number = totalPage;
				TTSUtils.getInstance().speakMenu(number+"");
			}else{
				TTSUtils.getInstance().speakMenu(number+"");
			}
			numView.setText(number + "");
			
		}
	}
}
