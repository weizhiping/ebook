package com.sunteam.ebook.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.tts.TtsUtils.TtsListener;
import com.sunteam.ebook.R;

/**
 * TTS工具类。
 * 
 * @author wzp
 */
public class TTSUtils
{
    private static final String TAG = "TTSUtils";
	private static final String ROLE_EN = "VOICE_EN";

    private static final String DEFAULT_ROLE_CN = "xiaofeng";	//默认中文发音人
    private static final String DEFAULT_ROLE_EN = "catherine";	//默认英文发音人
    private static final String DEFAULT_SPEED = "65";	//默认语速
    private static final String DEFAULT_TONE = "65";	//默认语调
    private static final String DEFAULT_VOLUME = "80";	//默认音量
    
    
	private static TTSUtils instance = null;
	private Context mContext;
//	private SpeechServiceUtil mService;	
	private TtsUtils mTtsUtils;
    private SharedPreferences mSharedPreferences;
	private boolean isSuccess = false;
	private OnTTSListener mOnTTSListener = null;
	private SpeakStatus mSpeakStatus = SpeakStatus.STOP;
	@SuppressWarnings("unused")
	private SpeakForm mSpeakForm = SpeakForm.TIPS;
	private String mStrContent = null;
	
	private static final String[] mRoleCn = {
		"xiaofeng",	//晓峰，国语男声
		"xiaoyan",	//晓燕，国语女声
		"xiaomei",	//晓美，粤语女声
		"nannan",	//许小宝，童声
	};	//中文发音人
	
	private static final String[] mRoleEn = {
		"catherine",	//John，英语男声
		"catherine",	//凯瑟琳，英语女声
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
	/*public SpeechServiceUtil getTextToSpeech()
	{	
		return	mService;
		
	}*/
	
	//初始化
	public void init( Context context )
	{
		mContext = context.getApplicationContext();
		
		mSharedPreferences = mContext.getSharedPreferences(EbookConstants.TTS_SETTINGS, Activity.MODE_PRIVATE);
		//String resType = mSharedPreferences.getString("tts_resource", "0");
        // 合成对象初始化
//        Intent serviceIntent = new Intent();
//        serviceIntent.putExtra(SpeechIntent.SERVICE_LOG_ENABLE, true);
//        mService = new SpeechServiceUtil(mContext, mInitListener, serviceIntent);
        mTtsUtils = new TtsUtils(context, ttsListener);
	}
	
	//销毁； 暂时不要销毁，因为讯飞TTS语音合成类是单例类，销毁了需要重新初始化，影响响应速度。
	public void destroy()
	{
		if(mTtsUtils != null)
		{
			/*mTtsUtils.destroy();
			mTtsUtils = null;*/
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
		if( isSuccess && mTtsUtils != null )
		{
			if( SpeakStatus.SPEAK == mSpeakStatus )
			{
				mTtsUtils.pause();
				mSpeakStatus = SpeakStatus.PAUSE;
			}	//如果正在朗读，先暂停
		}
	}
	
	//恢复朗读
	public void resume()
	{
		if( isSuccess && mTtsUtils != null )
		{
			if( SpeakStatus.PAUSE == mSpeakStatus )
			{
				mTtsUtils.resume();
				mSpeakStatus = SpeakStatus.SPEAK;
			}	//如果正在暂停，先恢复
		}
	}
	
	//停止朗读
	public void stop()
	{
		if( isSuccess && mTtsUtils != null )
		{
			if( SpeakStatus.STOP != mSpeakStatus )
			{
				mTtsUtils.stop();
				mSpeakStatus = SpeakStatus.STOP;
			}	//如果正在朗读，先停止
		}
	}
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakMenu( final String text ) 
	{
		if( isSuccess && mTtsUtils != null )
		{
			mTtsUtils.restoreSettingParameters();	//设置参数
			mTtsUtils.speak(text);
	        //用于提示信息朗读，不记录状态
        	mSpeakForm = SpeakForm.TIPS;
		}
    }
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakContent( final String text ) 
	{
		if( isSuccess && mTtsUtils != null )
		{
			setContentParam();	//设置参数
			mTtsUtils.speak(text);
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
		if( isSuccess && mTtsUtils != null )
		{
			setTipsParam();	//设置参数
			mTtsUtils.speak(text);
	        //用于提示信息朗读，不记录状态
        	mSpeakForm = SpeakForm.TIPS;
		}
    }
	
	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speakTest( final String text, String key, String value ) 
	{
		if( isSuccess && mTtsUtils != null )
		{
	        setTestParam(key, value);	//设置参数
	        mTtsUtils.speak(text);
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
				speakTest( text, SpeechConstant.VOICE_NAME, mRoleCn[i] ); // 暂时未区分中文发音人和英文发音人
				
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
				editor.putString( SpeechConstant.VOICE_NAME, mRoleCn[i]+"" );
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
		
		String role = mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_CN+"");
		for( int i = 0; i < mRoleCn.length; i++ )
		{
			if( role.equals(mRoleCn[i]+"") )
			{
				return	ttsRoleCn[i];
			}
		}
		
		return	ttsRoleCn[0];
	}
	
	//得到当前中文发音人序号
	public int getCurRoleCnIndex()
	{
		String role = mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_CN+"");
		for( int i = 0; i < mRoleCn.length; i++ )
		{
			if( role.equals(mRoleCn[i]+"") )
			{
				return	i;
			}
		}
		
		return	0;
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
				speakTest( text, SpeechConstant.VOICE_NAME, mRoleEn[i] );
				
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
				editor.putString( SpeechConstant.VOICE_NAME, mRoleEn[i]+"" );
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
		
		String role = mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_EN+"");
		for( int i = 0; i < mRoleEn.length; i++ )
		{
			if( role.equals(mRoleEn[i]+"") )
			{
				return	ttsRoleEn[i];
			}
		}
		
		return	ttsRoleEn[0];
	}
	
	//得到当前英文发音人序号
	public int getCurRoleEnIndex()
	{
		String role = mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_EN+"");
		for( int i = 0; i < mRoleEn.length; i++ )
		{
			if( role.equals(mRoleEn[i]+"") )
			{
				return	i;
			}
		}
		
		return	0;
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
		speakTest( text, SpeechConstant.SPEED, (speed*5)+"" );
	}
		
	//设置语速
	public void setSpeed( int speed )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putString( SpeechConstant.SPEED, (speed*5)+"" );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到语速
	public int getSpeed()
	{
		String speed = mSharedPreferences.getString(SpeechConstant.SPEED, DEFAULT_SPEED);
		return	Integer.parseInt(speed)/5;
	}
	
	//测试语调
	public void testPitch( int pitch, final String text )
	{
		speakTest( text, SpeechConstant.PITCH, (pitch*5)+"" );
	}
		
	//设置语调
	public void setPitch( int pitch )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putString( SpeechConstant.PITCH, (pitch*5)+"" );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到语调
	public int getPitch()
	{
		String pitch = mSharedPreferences.getString(SpeechConstant.PITCH, DEFAULT_TONE);
		return	Integer.parseInt(pitch)/5;
	}
	
	//测试音量
	public void testVolume( int volume, final String text )
	{
		speakTest( text, SpeechConstant.VOLUME, (volume*5)+"" );
	}
		
	//设置音量
	public void setVolume( int volume )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putString( SpeechConstant.VOLUME, (volume*5)+"" );
		editor.commit();
		
		PublicUtils.showToast(mContext, mContext.getString(R.string.setting_success));
	}
	
	//得到音量
	public int getVolume()
	{
		String volume = mSharedPreferences.getString(SpeechConstant.VOLUME, DEFAULT_VOLUME);
		return	Integer.parseInt(volume)/5;
	}
	
	//测试音效
	public boolean testEffect( String effect, final String text )
	{
		/*Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		for( int i = 0; i < ttsEffect.length; i++ )
		{
			if( ttsEffect[i].equals(effect) )
			{
				speakTest( text, TextToSpeech.KEY_PARAM_EFFECT, mEffect[i] );

				return	true;
			}
		}*/
		
		return	false;
	}
	
	//设置音效
	public boolean setEffect( String effect )
	{
		/*Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		for( int i = 0; i < ttsEffect.length; i++ )
		{
			if( ttsEffect[i].equals(effect) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putInt( TextToSpeech.KEY_PARAM_EFFECT, mEffect[i] );
				editor.commit();
				
				PublicUtils.showToastEx(mContext, mContext.getString(R.string.setting_success));
				
				return	true;
			}
		}*/
		
		return	false;
	}
	
	//得到当前音效
	public String getCurEffect()
	{
		/*Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.array_menu_voice_effect);
		
		int effect = mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT);
		for( int i = 0; i < mEffect.length; i++ )
		{
			if( effect == mEffect[i] )
			{
				return	ttsEffect[i];
			}
		}
		
		return	ttsEffect[0];*/
		return "";
	}
	
	//得到当前音效序号
	public int getCurEffectIndex()
	{
		/*
		int effect = mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT);
		for( int i = 0; i < mEffect.length; i++ )
		{
			if( effect == mEffect[i] )
			{
				return	i;
			}
		}
		*/
		return	0;
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
	/*private Intent setContentParam() 
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
    	ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, DEFAULT_ROLE_EN));						//TTS英文发音人参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));				//TTS音效参数
		ttsParamsIntent.putExtra(SpeechConstant.SPEED, mSharedPreferences.getInt(SpeechConstant.SPEED, DEFAULT_SPEED));								//TTS语速参数
		ttsParamsIntent.putExtra(SpeechConstant.PITCH, mSharedPreferences.getInt(SpeechConstant.PITCH, DEFAULT_TONE));					//TTS语调参数
		ttsParamsIntent.putExtra(SpeechConstant.VOLUME, mSharedPreferences.getInt(SpeechConstant.VOLUME, DEFAULT_VOLUME));				//TTS音量参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }*/
	
    private void setContentParam() 
    {	
    	mTtsUtils.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);

		// 设置发音人
    	mTtsUtils.setParameter(SpeechConstant.VOICE_NAME, mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_CN));

		// 设置合成语速
    	mTtsUtils.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString(SpeechConstant.SPEED, DEFAULT_SPEED));

		// 设置合成音调
    	mTtsUtils.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString(SpeechConstant.PITCH, DEFAULT_TONE));

		// 设置合成音量; 使用语记中的默认音量即可
		mTtsUtils.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString(SpeechConstant.VOLUME, DEFAULT_VOLUME));

		mTtsUtils.setParameter(SpeechConstant.STREAM_TYPE, ""+android.media.AudioManager.STREAM_MUSIC); // 为何不是TTS类型?

		// mTtsUtils.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false"); // 设置播放合成音频打断音乐播放，默认为true;
		
    }
		
	
    /**
     * 参数设置(对菜单朗读有效)
     *
     * @return
     */
    /*private Intent setTipsParam() 
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
    	ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, DEFAULT_ROLE_EN));						//TTS英文发音人参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));				//TTS音效参数
		ttsParamsIntent.putExtra(SpeechConstant.SPEED, mSharedPreferences.getInt(SpeechConstant.SPEED, DEFAULT_SPEED));								//TTS语速参数
		ttsParamsIntent.putExtra(SpeechConstant.PITCH, mSharedPreferences.getInt(SpeechConstant.PITCH, DEFAULT_TONE));					//TTS语调参数
		ttsParamsIntent.putExtra(SpeechConstant.VOLUME, mSharedPreferences.getInt(SpeechConstant.VOLUME, DEFAULT_VOLUME));				//TTS音量参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }*/
    
    private void setTipsParam() 
    {
    	mTtsUtils.restoreSettingParameters();	//设置参数
    	
    	/*
    	mTtsUtils.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);

		// 设置发音人
    	mTtsUtils.setParameter(SpeechConstant.VOICE_NAME, mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_CN));

		// 设置合成语速
    	mTtsUtils.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString(SpeechConstant.SPEED, DEFAULT_SPEED));

		// 设置合成音调
    	mTtsUtils.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString(SpeechConstant.PITCH, DEFAULT_TONE));

		// 设置合成音量; 使用语记中的默认音量即可
		mTtsUtils.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString(SpeechConstant.VOLUME, DEFAULT_VOLUME));

		mTtsUtils.setParameter(SpeechConstant.STREAM_TYPE, ""+android.media.AudioManager.STREAM_MUSIC); // 为何不是TTS类型?

		// mTtsUtils.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false"); // 设置播放合成音频打断音乐播放，默认为true;
   		*/
    }
	
    /**
     * 参数设置(对测试朗读有效)
     *
     * @return
     */
    /*private Intent setTestParam( String key, int value ) 
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
    	
    	if( ROLE_EN.equals(key) )
    	{
    		ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, value));								//TTS英文发音人参数
    	}
    	else
    	{
    		ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, DEFAULT_ROLE_EN));					//TTS英文发音人参数
    	}
    	
    	if( TextToSpeech.KEY_PARAM_EFFECT.equals(key) )
		{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, value));								//TTS音效参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));			//TTS音效参数
    	}
    	
    	if( SpeechConstant.SPEED.equals(key) )
		{
    		ttsParamsIntent.putExtra(SpeechConstant.SPEED, mSharedPreferences.getInt(SpeechConstant.SPEED, value));									//TTS语速参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(SpeechConstant.SPEED, mSharedPreferences.getInt(SpeechConstant.SPEED, DEFAULT_SPEED));							//TTS语速参数
    	}
    	
    	if( SpeechConstant.PITCH.equals(key) )
		{
    		ttsParamsIntent.putExtra(SpeechConstant.PITCH, mSharedPreferences.getInt(SpeechConstant.PITCH, value));									//TTS语调参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(SpeechConstant.PITCH, mSharedPreferences.getInt(SpeechConstant.PITCH, DEFAULT_TONE));				//TTS语调参数
    	}
    	
    	if( SpeechConstant.VOLUME.equals(key) )
		{
    		ttsParamsIntent.putExtra(SpeechConstant.VOLUME, mSharedPreferences.getInt(SpeechConstant.VOLUME, value));								//TTS音量参数
		}
    	else
    	{
    		ttsParamsIntent.putExtra(SpeechConstant.VOLUME, mSharedPreferences.getInt(SpeechConstant.VOLUME, DEFAULT_VOLUME));			//TTS音量参数
    	}
    	
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
		
		return	ttsParamsIntent;
    }*/
    private void setTestParam( String key, String value ) {
		// TTS中文发音人参数
		if (SpeechConstant.VOICE_NAME.equals(key)) {
			mTtsUtils.setParameter(SpeechConstant.VOICE_NAME, value);
		} else {
			mTtsUtils.setParameter(SpeechConstant.VOICE_NAME, mSharedPreferences.getString(SpeechConstant.VOICE_NAME, DEFAULT_ROLE_CN + ""));
		}
    	
    	/*if( ROLE_EN.equals(key) )
    	{
    		ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, value));								//TTS英文发音人参数
    	}
    	else
    	{
    		ttsParamsIntent.putExtra(ROLE_EN, mSharedPreferences.getInt(ROLE_EN, DEFAULT_ROLE_EN));					//TTS英文发音人参数
    	}*/
    	
		/*if (TextToSpeech.KEY_PARAM_EFFECT.equals(key)) {
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, value));								//TTS音效参数
		} else {
    		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));			//TTS音效参数
    	}*/
    	
    	// 设置合成语速
		if (SpeechConstant.SPEED.equals(key)) {
			mTtsUtils.setParameter(SpeechConstant.SPEED, value);
		} else {
			mTtsUtils.setParameter(SpeechConstant.SPEED, "" + mSharedPreferences.getString(SpeechConstant.SPEED, DEFAULT_SPEED));
		}
    	
		// 设置合成音调
		if (SpeechConstant.PITCH.equals(key)) {
			mTtsUtils.setParameter(SpeechConstant.PITCH, value);
		} else {
			mTtsUtils.setParameter(SpeechConstant.PITCH, "" + mSharedPreferences.getString(SpeechConstant.PITCH, DEFAULT_TONE));
		}

		// 设置合成音量
		if (SpeechConstant.VOLUME.equals(key)) {
    		mTtsUtils.setParameter(SpeechConstant.VOLUME, value);
		} else {
    		mTtsUtils.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString(SpeechConstant.VOLUME, DEFAULT_VOLUME)+"");
    	}

    }
      
    private TtsListener ttsListener = new TtsListener() {
    	// 初始化
		public void onInit(int code) {
			if (0 == code) {
				isSuccess = true;
			} // 初始化成功
		}

		// 发音开始
		public void onSpeakBegin() {

		}

		// 暂停合成
		public void onSpeakPaused() {

		}

		// 恢复合成
		public void onSpeakResumed() {

		}

		// 合成进度
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

		}

		// 发音进度
		public void onSpeakProgress(int percent, int beginPos, int endPos) {

		}

		// 发音结束
		public void onCompleted(String error) {
			Log.d(TAG, "onPlayCompletedCallBack----error= " + error);
			if( SpeakForm.TIPS == mSpeakForm )
			{ 
				return; 
			}
			
			mSpeakStatus = SpeakStatus.STOP;
			
			if (null == error)
			{
				// 合成完成
				if (mOnTTSListener != null) 
				{
					mOnTTSListener.onSpeakCompleted();
				}
			}
			else 
			{
				// 合成错误
				if (mOnTTSListener != null) 
				{
					mOnTTSListener.onSpeakError();
				}
			}
		}		
	};
}
