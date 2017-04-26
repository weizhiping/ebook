package com.sunteam.ebook.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.menu.MenuConstant;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.SharedPrefUtils;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.R;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.FileOperateUtils;
import com.sunteam.ebook.util.MediaPlayerUtils;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;

/**
 * @Destryption 音乐强度设置，已经把背景音STREAM_TYPE由STREAM_ALARM改回STRAM_MUSIC，以便与TTS保持一致。
 * @Author Jerry
 * @Date 2017-2-8 上午10:12:27
 * @Note 背景音强度设置为:很弱-适中-较强-很强, 分别是STRAM_MUSIC音的0.25倍、0.5倍、0.75倍和1倍。
 */
public class MusicVolume extends MenuActivity {
	private SharedPreferences musicShared;
//	private VolumeReceiver mVolumeReceiver;
	private int lastKey = 0; // 这是因为在公用菜单中【确认】键是抬起有效，而电子书中都是按下有效，导致进入该界面后就收到了【确认】键

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initView();
		super.onCreate(savedInstanceState);
//		registerVolumeReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		unregisterVolumeReceiver();
		TTSUtils.getInstance().init(this);	//恢复电子书中TTS回调; 因为在公用菜单中设置了自己的TtsListener实例
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		// 强制停止背景音乐
		MediaPlayerUtils.getInstance().stop();
		
		// 恢复上次音量
		int index = musicShared.getInt(EbookConstants.MUSIC_INTENSITY, EbookConstants.DEFAULT_MUSICE_INTENSITY);
		setMusicVlolume(index);
	}		

	// 保存背景音乐强度设置
	private void setMusicVlolume(int index) {
		MediaPlayerUtils.getInstance().setBackgroundVolume(index);
	}

	@SuppressWarnings("deprecation")
	private void playMusic() {
		int mode = Context.MODE_WORLD_READABLE + Context.MODE_MULTI_PROCESS;
		String path = SharedPrefUtils.getSharedPrefString(this, EbookConstants.SETTINGS_TABLE, mode, EbookConstants.MUSICE_PATH, null);
		if (null == path) {
			path = FileOperateUtils.getFirstMusicInDir();
		} else {
			File file = new File(path);
			if (!file.exists()) {
				path = FileOperateUtils.getFirstMusicInDir();
			}
		}
		if (null != path) {
			MediaPlayerUtils.getInstance().play(path);
			int index = SharedPrefUtils.getSharedPrefInt(this, EbookConstants.SETTINGS_TABLE, mode, EbookConstants.MUSIC_INTENSITY, EbookConstants.DEFAULT_MUSICE_INTENSITY);
			MediaPlayerUtils.getInstance().setBackgroundVolume(index);
		}
	}

	/*private void stopMusic() {
		boolean isMusic = musicShared.getBoolean(EbookConstants.MUSICE_STATE, false);
		if (!isMusic) {
			MediaPlayerUtils.getInstance().stop();
		}
	}*/

	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		if (selectItem >= mMenuList.size()) {
			selectItem = 0;
		}
		
		// 保存设置
		SharedPrefUtils.saveSettings(this, EbookConstants.SETTINGS_TABLE, EbookConstants.MUSIC_INTENSITY, selectItem);

		String tips = getResources().getString(R.string.ebook_setting_success);
		PublicUtils.showToast(this, tips, new PromptListener() {
			
			@Override
			public void onComplete() {
				returnFatherActivity();
			}
		});
	}

	// 在语速语调设置中，所有文字字号统一用大字号: 40sp, 已经在布局文件中初始化，不必在此与功能设置中的字号设置挂钩
	@SuppressLint("WorldReadableFiles") @SuppressWarnings("deprecation")
	private void initView() {
		Intent intent = getIntent();
		mTitle = intent.getStringExtra(MenuConstant.INTENT_KEY_TITLE);
		String[] list = getResources().getStringArray(R.array.ebook_array_menu_music_intensity);
		mMenuList = ArrayUtils.strArray2List(list);
		int mode = Context.MODE_WORLD_READABLE + Context.MODE_MULTI_PROCESS;
		musicShared = getSharedPreferences(EbookConstants.SETTINGS_TABLE, mode);
		selectItem = musicShared.getInt(EbookConstants.MUSIC_INTENSITY, EbookConstants.DEFAULT_MUSICE_INTENSITY);
		setMusicVlolume(selectItem);
		playMusic();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		lastKey = keyCode;
		boolean ret = super.onKeyDown(keyCode, event);

		if (KeyEvent.KEYCODE_DPAD_UP == keyCode || KeyEvent.KEYCODE_DPAD_DOWN == keyCode || KeyEvent.KEYCODE_DPAD_LEFT == keyCode
				|| KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
			selectItem = getSelectItem();
			setMusicVlolume(selectItem);
		}
		return ret;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((KeyEvent.KEYCODE_DPAD_CENTER == keyCode || KeyEvent.KEYCODE_ENTER == keyCode) && lastKey != keyCode) {
			// 如果【确认】键没有按下，在不处理，可能是上一个界面遗留的抬起事件未处理。因为在公用菜单中，【确认】键在抬起时处理。
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}	

	// 不能直接返回，需要等发音结束后再返回，否则发音被中断了。
	private void returnFatherActivity() {
		MediaPlayerUtils.getInstance().stop();
		setResult(Activity.RESULT_OK);
		finish();
	}

	/*private void registerVolumeReceiver() {
		if (null == mVolumeReceiver) {
			mVolumeReceiver = new VolumeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.media.VOLUME_CHANGED_ACTION"); // 音量变化
			registerReceiver(mVolumeReceiver, filter);
		}
	}

	private void unregisterVolumeReceiver() {
		if (null != mVolumeReceiver) {
			unregisterReceiver(mVolumeReceiver);
			mVolumeReceiver = null;
		}
	}

	private class VolumeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (!action.equals("android.media.VOLUME_CHANGED_ACTION")) {
				return;
			}
			// AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE,EXTRA_VOLUME_STREAM_VALUE 是隐藏属性
			int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", 0);
			int newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
			int oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0);
			if (AudioManager.STREAM_MUSIC == streamType) {
				setMusicVlolume(selectItem);
				MenuGlobal.debug("[MusicVolume][VolumeReceiver] action = VOLUME_CHANGED_ACTION, streamType = " + streamType + ", newVolume = " + newVolume + ", oldVolume = " + oldVolume);
			}
		}
	}*/

}
