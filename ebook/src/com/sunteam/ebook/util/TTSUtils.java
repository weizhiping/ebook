package com.sunteam.ebook.util;

import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;
import com.iflytek.business.speech.SpeechServiceUtil.ISpeechInitListener;
import com.iflytek.business.speech.SynthesizerListener;
import com.iflytek.business.speech.TextToSpeech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() 
    {
    	ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, Integer.valueOf(mSharedPreferences.getString("engine_preference", "4097")));
    	if(mSharedPreferences.getString("engine_preference", "4097").equals("4097"))
    	{
    		//ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN,"55");
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN,mSharedPreferences.getString("rolecn_preference", "3"));
		}
    	else
    	{
			ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString("rolecn_preference", "vixx"));
		}
    	//ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_EN,
    	//Integer.valueOf(mSharedPreferences.getString("roleen_preference", "5")));
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_EFFECT,Integer.valueOf(mSharedPreferences.getString("effect_preference", "6")));
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_SPEED,Integer.valueOf(mSharedPreferences.getString("speed_preference", "60")));
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PITCH,Integer.valueOf(mSharedPreferences.getString("pitch_preference", "60")));
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_VOLUME,Integer.valueOf(mSharedPreferences.getString("volume_preference", "60")));
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, Integer.valueOf(mSharedPreferences.getString("stream_preference", "3")));
		//ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_MSC_ROLE, mSharedPreferences.getString("rolemsc_preference", "vixx"));
		//ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_ONLINE);

		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, true);
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_DEST_LOG, "tts_test333");
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
