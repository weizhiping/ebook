package com.sunteam.ebook.util;

import java.util.Locale;

import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;
import com.iflytek.business.speech.SpeechServiceUtil.ISpeechInitListener;
import com.iflytek.business.speech.SynthesizerListener;
import com.iflytek.business.speech.TextToSpeech;
import com.sunteam.ebook.R;
import com.sunteam.ebook.entity.TTSRole;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
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
	private static TTSUtils instance = null;
	private Context mContext;
	private SpeechServiceUtil mService;
	private Intent ttsParamsIntent;	//合成参数设置Intent
    private SharedPreferences mSharedPreferences;
    private String resType = "";
	private boolean isSuccess = false;
	private OnTTSListener mOnTTSListener = null;
	private SpeakStatus mSpeakStatus = SpeakStatus.STOP;
	private SpeakForm mSpeakForm = SpeakForm.TIPS;
	
	private static final int[] mRoleCn = {
		3,	//晓燕
		4,	//晓峰
		15,	//晓美
		
		51,	//许久
		52,	//许多
		53,	//晓萍
		54,	//唐老鸭
		55,	//许小宝
		56,	//大龙
		
		7,	//楠楠
		9,	//嘉嘉
		11,	//小倩
		14,	//晓蓉
		22,	//晓琳
		24,	//小强
		25,	//小坤
	};	//中文发音人
	
	private static final int[] mRoleEn = {
		20,	//凯瑟琳
		
		3,	//晓燕
		4,	//晓峰
		15,	//晓美
		
		51,	//许久
		52,	//许多
		53,	//晓萍
		54,	//唐老鸭
		55,	//许小宝
		56,	//大龙
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
		resType = mSharedPreferences.getString("tts_resource", "0");
        // 合成对象初始化
        ttsParamsIntent = new Intent();
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
	        setParam();	//设置参数
	        mService.speak(text, ttsParamsIntent);
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
	        setParam();	//设置参数
	        mService.speak(text, ttsParamsIntent);
	        //用于提示信息朗读，不记录状态
        	mSpeakForm = SpeakForm.TIPS;
		}
    }
	
	//设置中文发音人
	public boolean setRoleCn( String role )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleCn = res.getStringArray(R.array.tts_role_cn);
		
		for( int i = 0; i < ttsRoleCn.length; i++ )
		{
			if( ttsRoleCn[i].equals(role) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putString( TextToSpeech.KEY_PARAM_ROLE_CN, mRoleCn[i]+"" );
				editor.commit();
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前中文发音人
	public String getCurRoleCn()
	{
		Resources res = mContext.getResources();
		String[] ttsRoleCn = res.getStringArray(R.array.tts_role_cn);
		
		String role = mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, TextToSpeech.DEFAULT_ROLE_CN+"");
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
		return	res.getStringArray(R.array.tts_role_cn);
	}
	
	//设置英文发音人
	public boolean setRoleEn( String role )
	{
		Resources res = mContext.getResources();
		String[] ttsRoleEn = res.getStringArray(R.array.tts_role_en);
		
		for( int i = 0; i < ttsRoleEn.length; i++ )
		{
			if( ttsRoleEn[i].equals(role) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putString( TextToSpeech.KEY_PARAM_ROLE_EN, mRoleEn[i]+"" );
				editor.commit();
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前英文发音人
	public String getCurRoleEn()
	{
		Resources res = mContext.getResources();
		String[] ttsRoleEn = res.getStringArray(R.array.tts_role_en);
		
		String role = mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_EN, TextToSpeech.DEFAULT_ROLE_EN+"");
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
		return	res.getStringArray(R.array.tts_role_en);
	}
	
	//设置语速
	public void setSpeed( int speed )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_SPEED, speed );
		editor.commit();
	}
	
	//得到语速
	public int getSpeed()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, TextToSpeech.DEFAULT_SPEED);
	}
	
	//设置语调
	public void setPitch( int pitch )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_PITCH, pitch );
		editor.commit();
	}
	
	//得到语速
	public int getPitch()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE);
	}
	
	//设置音量
	public void setVolume( int volume )
	{
		Editor editor = mSharedPreferences.edit();
		editor.putInt( TextToSpeech.KEY_PARAM_VOLUME, volume );
		editor.commit();
	}
	
	//得到音量
	public int getVolume()
	{
		return	mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME);
	}
	
	//设置音效
	public boolean setEffect( String effect )
	{
		Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.tts_effect);
		
		for( int i = 0; i < ttsEffect.length; i++ )
		{
			if( ttsEffect[i].equals(effect) )
			{
				Editor editor = mSharedPreferences.edit();
				editor.putInt( TextToSpeech.KEY_PARAM_EFFECT, mEffect[i] );
				editor.commit();
				
				return	true;
			}
		}
		
		return	false;
	}
	
	//得到当前音效
	public String getCurEffect()
	{
		Resources res = mContext.getResources();
		String[] ttsEffect = res.getStringArray(R.array.tts_effect);
		
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
		return	res.getStringArray(R.array.tts_effect);
	}
		
    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() 
    {
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL));	//TTS引擎类型，4097是本地 4098是网络
    	if( mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL) == TextToSpeech.TTS_ENGINE_LOCAL )
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_ROLE_CN, TextToSpeech.DEFAULT_ROLE_CN+""));//TTS中文发音人参数
		}
    	else
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString(TextToSpeech.KEY_PARAM_MSC_ROLE, "vixx"));						//网络TTS角色
		}
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_ROLE_EN, TextToSpeech.DEFAULT_ROLE_EN));			//TTS英文发音人参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_EFFECT, TextToSpeech.DEFAULT_EFFECT));				//TTS音效参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_SPEED, TextToSpeech.DEFAULT_SPEED));				//TTS语速参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_PITCH, TextToSpeech.DEFAULT_TONE));					//TTS语调参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_VOLUME, TextToSpeech.DEFAULT_VOLUME));				//TTS音量参数
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, mSharedPreferences.getInt(TextToSpeech.KEY_PARAM_STREAM, TextToSpeech.DEFAULT_STREAM));				//TTS播放类型参数

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, false);																			//TTS是否保存录音
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "com.sunteam.ebook");																//TTS保存录音路径
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
