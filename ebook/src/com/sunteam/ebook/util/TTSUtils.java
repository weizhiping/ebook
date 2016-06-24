package com.sunteam.ebook.util;

import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;
import com.iflytek.business.speech.SpeechServiceUtil.ISpeechInitListener;
import com.iflytek.business.speech.SynthesizerListener;
import com.iflytek.business.speech.TextToSpeech;
import com.sunteam.ebook.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

/**
 * TTS工具类。
 * 
 * @author wzp
 */
public class TTSUtils
{
    private static final String TAG = "TTSUtils";
    private static final int DEFAULT_ROLE_CN = 3;	//默认中文发音人
    private static final int DEFAULT_ROLE_EN = 17;	//默认英文发音人
    private static final int DEFAULT_SPEED = 65;	//默认语速
    
	private static TTSUtils instance = null;
	private Context mContext;
	private SpeechServiceUtil mService;	
    private SharedPreferences mSharedPreferences;
	private boolean isSuccess = false;
	private OnTTSListener mOnTTSListener = null;
	private SpeakStatus mSpeakStatus = SpeakStatus.STOP;
	private SpeakForm mSpeakForm = SpeakForm.TIPS;
	
	private static final int[] mRoleCn = {
		4,	//晓峰，国语男声
		3,	//晓燕，国语女声
		15,	//晓美，粤语女声
		55,	//许小宝，童声
	};	//中文发音人
	
	private static final int[] mRoleEn = {
		17,	//John，英语男声
		20,	//凯瑟琳，英语女声
	};	//英文发音人
	
	private static final int[] mEffect = {
		0,	//原声	
		2,	//回声
		3,	//机器人
		7,	//阴阳怪气
	};	//音效
	
	public interface OnTTSListener 
	{
		public void onSpeakCompleted();		//朗读完成
		public void onSpeakError();			//朗读错误
	}
	
	public enum SpeakForm
	{
		TIPS,	//提示
		CONTENT,//内容
	}	//朗读形式
	
	public enum SpeakStatus
	{
		STOP, 	//停止
		PAUSE,	//暂停
		SPEAK,	//朗读
	}	//朗读状态
	
	//设置监听器
	public void OnTTSListener( OnTTSListener listener )
	{
		mOnTTSListener = listener;
	}
	
	//得到当前TTS状态
	public SpeakStatus getSpeakStatus()
	{
		return	mSpeakStatus;
	}
	
	public static TTSUtils getInstance()
	{
		if( null == instance )
		{
			instance = new TTSUtils();
		}
		
		return instance;
	}
	
	//得到TTS对象
	public SpeechServiceUtil getTextToSpeech()
	{
		return	mService;
	}
	
	//初始化
	public void init( Context context )
	{
		mContext = context.getApplicationContext();
		
		mSharedPreferences = mContext.getSharedPreferences(EbookConstants.TTS_SETTINGS, Activity.MODE_PRIVATE);
		//String resType = mSharedPreferences.getString("tts_resource", "0");
        // 合成对象初始化
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra(SpeechIntent.SERVICE_LOG_ENABLE, true);
        mService = new SpeechServiceUtil(mContext, mInitListener, serviceIntent);
	}
	
	//销毁
	public void destroy()
	{
		if(mService != null)
		{
			mService.destroy();
			mService = null;
		}
	}

	//是否初始化成功
	public boolean isSuccess()
	{
		return	isSuccess;
	}
	
	//暂停朗读
	public void pause()
	{
		if( isSuccess && mService != null )
		{
			if( SpeakStatus.SPEAK == mSpeakStatus )
			{
				//mService.pauseSpeaking();
				mSpeakStatus = SpeakStatus.PAUSE;
			}	//如果正在朗读，先暂停
		}
	}
	
	//恢复朗读
	public void resume()
	{
		if( isSuccess && mService != null )
		{
			if( SpeakStatus.PAUSE == mSpeakStatus )
			{
				//mService.resumeSpeaking();
				mSpeakStatus = SpeakStatus.SPEAK;
			}	//如果正在暂停，先恢复
		}
	}
	
	//停止朗读
	public void stop()
	{
		if( isSuccess && mService != null )
		{
			if( SpeakStatus.STOP != mSpeakStatus )
			{
				mService.stopSpeak();
				mSpeakStatus = SpeakStatus.STOP;
			}	//如果正在朗读，先停止
		}
	}

	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakContent( final String text ) 
	{
		if( isSuccess && mService != null )
		{
			Intent intent = setContentParam();	//设置参数
	        mService.speak(text, intent);
	        mSpeakStatus = SpeakStatus.SPEAK;
	        mSpeakForm = SpeakForm.CONTENT;
		}
    }
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakTips( final String text ) 
	{
		if( isSuccess && mService != null )
		{
			Intent intent = setTipsParam();	//设置参数
	        mService.speak(text, intent);
	        //用于提示信息朗读，不记录状态
        	mSpeakForm = SpeakForm.TIPS;
		}
    }	

	
	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakTest( final String text, String key, int value ) 
	{
		if( isSuccess && mService != null )
		{
	        Intent intent = setTestParam(key, value);	//设置参数
	        mService.speak(text, intent);
	        //用于提示信息朗读，不记录状态
        	mSpeakForm = SpeakForm.TIPS;
		}
    }	

	//测试中文发音人
	public boolean testRoleCn( String role, final String text )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleCn = res.getStringArray(R.array.array_menu_voice_china);
		
		for( int i = 0; i < ttsRoleCn.length; i++ )
		{
			if( ttsRoleCn[i].equals(role) )
			{
				speakTest( text, TextToSpeech.KEY_PARAM_ROLE_CN, mRoleCn[i] );
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//设置中文发音人
	public boolean setRoleCn( String role )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleCn = res.getStringArray(R.array.array_menu_voice_china);
		
		for( int i = 0; i < ttsRoleCn.length; i++ )
		{
			if( ttsRoleCn[i].equals(role) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putString( TextToSpeech.KEY_PARAM_ROLE_CN, mRoleCn[i]+"" );
				editor.commit();
				
				PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前中文发音人
	public String getCurRoleCn()
	{
		Resources res = mContext.getResources();
		String[] ttsRoleCn = res.getStringArray(R.array.array_menu_voice_china);
		
		String role = mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, DEFAULT_ROLE_CN+"");
		for( int i = 0; i < mRoleCn.length; i++ )
		{
			if( role.equals(mRoleCn[i]+"") )
			{
				return	ttsRoleCn[i];
			}
		}
		
		return	ttsRoleCn[0];
	}
	
	//得到当前中文发音人列表
	public String[] getRoleCnList()
	{
		Resources res = mContext.getResources();
		return	res.getStringArray(R.array.array_menu_voice_china);
	}
	
	//测试英文发音人
	public boolean testRoleEn( String role, final String text )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleEn = res.getStringArray(R.array.array_menu_voice_english);
		
		for( int i = 0; i < ttsRoleEn.length; i++ )
		{
			if( ttsRoleEn[i].equals(role) )
			{
				speakTest( text, TextToSpeech.KEY_PARAM_ROLE_EN, mRoleEn[i] );
				
				return	true;
			}
		}
		
		return	false;
	}
		
	//设置英文发音人
	public boolean setRoleEn( String role )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleEn = res.getStringArray(R.array.array_menu_voice_english);
		
		for( int i = 0; i < ttsRoleEn.length; i++ )
		{
			if( ttsRoleEn[i].equals(role) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putString( TextToSpeech.KEY_PARAM_ROLE_EN, mRoleEn[i]+"" );
				editor.commit();
				
				PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前英文发音人
	public String getCurRoleEn()
	{
		Resources res = mContext.getResources();
		String[] ttsRoleEn = res.getStringArray(R.array.array_menu_voice_english);
		
		String role = mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_EN, DEFAULT_ROLE_EN+"");
		for( int i = 0; i < mRoleEn.length; i++ )
		{
			if( role.equals(mRoleEn[i]+"") )
			{
				return	ttsRoleEn[i];
			}
		}
		
		return	ttsRoleEn[0];
	}
	
	//得到当前英文发音人列表
	public String[] getRoleEnList()
	{
		Resources res = mContext.getResources();
		return	res.getStringArray(R.array.array_menu_voice_english);
	}
	
	//测试语速
	public void testSpeed( int speed, final String text )
	{
		speakTest( text, TextToSpeech.KEY_PARAM_SPEED, speed*5 );
	}
		
	//设置语速
	public void setSpeed( int speed )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_SPEED, speed*5 );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到语速
	public int getSpeed()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, DEFAULT_SPEED)/5;
	}
	
	//测试语调
	public void testPitch( int pitch, final String text )
	{
		speakTest( text, TextToSpeech.KEY_PARAM_PITCH, pitch*5 );
	}
		
	//设置语调
	public void setPitch( int pitch )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_PITCH, pitch*5 );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到语调
	public int getPitch()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE)/5;
	}
	
	//测试音量
	public void testVolume( int volume, final String text )
	{
		speakTest( text, TextToSpeech.KEY_PARAM_VOLUME, volume*5 );
	}
		
	//设置音量
	public void setVolume( int volume )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_VOLUME, volume*5 );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到音量
	public int getVolume()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME)/5;
	}
	
	//测试音效
	public boolean testEffect( String effect, final String text )
	{
		Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		for( int i = 0; i < ttsEffect.length; i++ )
		{
			if( ttsEffect[i].equals(effect) )
			{
				speakTest( text, TextToSpeech.KEY_PARAM_EFFECT, mEffect[i] );

				return	true;
			}
		}
		
		return	false;
	}
	
	//设置音效
	public boolean setEffect( String effect )
	{
		Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		for( int i = 0; i < ttsEffect.length; i++ )
		{
			if( ttsEffect[i].equals(effect) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putInt( TextToSpeech.KEY_PARAM_EFFECT, mEffect[i] );
				editor.commit();
				
				PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前音效
	public String getCurEffect()
	{
		Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		int effect = mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT);
		for( int i = 0; i < mEffect.length; i++ )
		{
			if( effect == mEffect[i] )
			{
				return	ttsEffect[i];
			}
		}
		
		return	ttsEffect[0];
	}
	
	//得到当前音效列表
	public String[] getEffectList()
	{
		Resources res = mContext.getResources();
		return	res.getStringArray(R.array.array_menu_voice_effect);
	}
		
    /**
     * 参数设置(对正文朗读有效)
     *
     * @return
     */
    private Intent setContentParam() 
    {
    	Intent ttsParamsIntent = new Intent();	//合成参数设置Intent
    	
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL));	//TTS引擎类型，4097是本地 4098是网络
    	if( mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL) == TextToSpeech.TTS_ENGINE_LOCAL )
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, DEFAULT_ROLE_CN+""));				//TTS中文发音人参数
		}
    	else
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_MSC_ROLE, "vixx"));						//网络TTS角色
		}
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ROLE_EN, DEFAULT_ROLE_EN));						//TTS英文发音人参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));				//TTS音效参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, DEFAULT_SPEED));								//TTS语速参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE));					//TTS语调参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME));				//TTS音量参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }
	
    /**
     * 参数设置(对菜单朗读有效)
     *
     * @return
     */
    private Intent setTipsParam() 
    {
    	Intent ttsParamsIntent = new Intent();	//合成参数设置Intent
    	
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL));	//TTS引擎类型，4097是本地 4098是网络
    	if( mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL) == TextToSpeech.TTS_ENGINE_LOCAL )
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, DEFAULT_ROLE_CN+""));				//TTS中文发音人参数
		}
    	else
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_MSC_ROLE, "vixx"));						//网络TTS角色
		}
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ROLE_EN, DEFAULT_ROLE_EN));						//TTS英文发音人参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));				//TTS音效参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, DEFAULT_SPEED));								//TTS语速参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE));					//TTS语调参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME));				//TTS音量参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }
	
    /**
     * 参数设置(对测试朗读有效)
     *
     * @return
     */
    private Intent setTestParam( String key, int value ) 
    {
    	Intent ttsParamsIntent = new Intent();	//合成参数设置Intent
    	
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL));	//TTS引擎类型，4097是本地 4098是网络
    	if( mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL) == TextToSpeech.TTS_ENGINE_LOCAL )
    	{
    		if( TextToSpeech.KEY_PARAM_ROLE_CN.equals(key) )
    		{
    			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, value+""));					//TTS中文发音人参数
    		}
    		else
    		{
    			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, DEFAULT_ROLE_CN+""));			//TTS中文发音人参数
    		}
		}
    	else
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_MSC_ROLE, "vixx"));						//网络TTS角色
		}
    	
    	if( TextToSpeech.KEY_PARAM_ROLE_EN.equals(key) )
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ROLE_EN, value));								//TTS英文发音人参数
    	}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ROLE_EN, DEFAULT_ROLE_EN));					//TTS英文发音人参数
    	}
    	
    	if( TextToSpeech.KEY_PARAM_EFFECT.equals(key) )
		{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, value));								//TTS音效参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));			//TTS音效参数
    	}
    	
    	if( TextToSpeech.KEY_PARAM_SPEED.equals(key) )
		{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, value));									//TTS语速参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, DEFAULT_SPEED));							//TTS语速参数
    	}
    	
    	if( TextToSpeech.KEY_PARAM_PITCH.equals(key) )
		{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, value));									//TTS语调参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE));				//TTS语调参数
    	}
    	
    	if( TextToSpeech.KEY_PARAM_VOLUME.equals(key) )
		{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, value));								//TTS音量参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME));			//TTS音量参数
    	}
    	
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }
      
    private SynthesizerListener.Stub ttsListener = new SynthesizerListener.Stub() 
    {
    	//合成进度回调
    	@Override
    	public void onProgressCallBack(int arg0) throws RemoteException 
    	{
			//Log.d(TAG, "onProgressCallBack----arg0= " + arg0);
		}
		
    	//合成完成回调
    	@Override
    	public void onPlayCompletedCallBack(int arg0) throws RemoteException 
    	{
    		Log.d(TAG, "onPlayCompletedCallBack----arg0= " + arg0);
    		if( SpeakForm.TIPS == mSpeakForm )
			{
				return;
			}
			mSpeakStatus = SpeakStatus.STOP;
			if( 0 == arg0 )
			{
				//合成完成
				if( mOnTTSListener != null )
				{
					mOnTTSListener.onSpeakCompleted();
				}
			}
			else
			{
				//合成错误
				if( mOnTTSListener != null )
				{
					mOnTTSListener.onSpeakError();
				}
			}
		}
		
    	//合成开始回调
		@Override
		public void onPlayBeginCallBack() throws RemoteException 
		{
			Log.d(TAG, "onPlayBeginCallBack");
		}
		
		//合成中断回调
		@Override
		public void onInterruptedCallback() throws RemoteException 
		{
			Log.d(TAG, "onInterruptedCallback");
		}
		
		//合成初始化回调
		@Override
		public void onInit(int arg0) throws RemoteException 
		{
			Log.d(TAG, "onInit");
			if( 0 == arg0 )
			{
				isSuccess = true;
			}	//初始化成功
		}
	};
   
    private ISpeechInitListener mInitListener = new ISpeechInitListener()
    {
    	@Override
    	public void onSpeechInit(int arg0) 
    	{
    		Log.d(TAG, "onSpeechInit start----");
    		Intent ttsIntent = new Intent();
	        
    		String str = mSharedPreferences.getString("tts_resource", "0");
    		if( str.equals("0") )
    		{
    			ttsIntent.putExtra(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_FROM_ASSETS);
    		}
    		else if( str.equals("1") )
    		{
    			ttsIntent.putExtra(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_FROM_CLIENT);
    			//ttsIntent.putExtra(SpeechIntent.ARG_RES_PROVIDER_AUTHORITY, "com.example.speechsuittest123123.provider");
    			ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, "its/");
    		}
    		else if( str.equals("2") )
    		{   			
    			ttsIntent.putExtra(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_SPECIFIED);
    			String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Speechcloud"+"/";
    			ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, FILE_PATH);
    		}        	        	        
	        mService.initSynthesizerEngine(ttsListener, ttsIntent);
			Log.d(TAG, "onSpeechInit end-----------");
		}

		@Override
		public void onSpeechUninit() 
		{
			Log.d(TAG, "onSpeechUninit");
		}
    };    
}
