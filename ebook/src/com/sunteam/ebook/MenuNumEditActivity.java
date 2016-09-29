package com.sunteam.ebook;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.ebook.entity.ScreenManager;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
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
	private int flage;//0 语速，1为语调,2为背景音乐强度
	private String title;
	private EditText numView;
	private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_num_edit);
		ScreenManager.getScreenManager().pushActivity(this);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
		
		Tools tools = new Tools(this);
		
		layout.setBackgroundColor(tools.getBackgroundColor());	// 设置View的背景色
		titleView.setTextColor(tools.getFontColor());			// 设置title的背景色
		line.setBackgroundColor(tools.getFontColor());			// 设置分割线的背景色
		numView.setTextColor(tools.getFontColor());
		
		final float scale = this.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数;
		float fontSize = tools.getFontSize() * scale;
		titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale); // 设置title字号
		numView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale);		
		titleView.setHeight((int)fontSize); // 设置控件高度
		
		if(2 == flage){
			int currentMusic = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
			number = (int)(currentMusic/1.5);
			playMusic();
		}
		numView.setText(number + "");
		
		String str = title+"，"+ number;
		
		if(0 == flage){
			TTSUtils.getInstance().testSpeed(number,str);
		}else if(1 == flage){
			TTSUtils.getInstance().testPitch(number,str);
		}else{
			TTSUtils.getInstance().speakMenu(str);
		}
	}
	

	private void playMusic(){
		SharedPreferences shared = getSharedPreferences(EbookConstants.SETTINGS_TABLE,Context.MODE_PRIVATE);
		String path = shared.getString(EbookConstants.MUSICE_PATH, null);
		if(null == path){
			path = FileOperateUtils.getFirstMusicInDir();
		}else{
			File file = new File(path);
			if(!file.exists()){
				path = FileOperateUtils.getFirstMusicInDir();
			}
		}
		if(null != path){
			MediaPlayerUtils.getInstance().play(path);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:// 返回
			if(2 == flage){
				MediaPlayerUtils.getInstance().stop();
			}
			return super.onKeyDown(keyCode, event);
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
			}else if(2 == flage){
				int volume = (int)(number * 1.5);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 1);
				PublicUtils.showToast(MenuNumEditActivity.this, getResources().getString(R.string.setting_success));
				MediaPlayerUtils.getInstance().stop();
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
		if(0 == flage){
			TTSUtils.getInstance().testSpeed(number,number+"");
		}else if(1 == flage){
			TTSUtils.getInstance().testPitch(number,number+"");
		}else{
			TTSUtils.getInstance().speakMenu(number +"");
			int volume = (int)(number * 1.5);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 1);
		}
		return super.onKeyDown(keyCode, event);
	}
}
