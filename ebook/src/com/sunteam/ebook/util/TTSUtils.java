package com.sunteam.ebook.util;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

/**
 * TTS工具类。
 * 
 * @author wzp
 */
public class TTSUtils implements OnInitListener
{
	private static TTSUtils instance = null;
	private TextToSpeech tts = null;
	private boolean isSuccess = false;
	
	public static TTSUtils getInstance()
	{
		if( null == instance )
		{
			instance = new TTSUtils();
		}
		
		return instance;
	}
	
	//得到TTS对象
	public TextToSpeech getTextToSpeech()
	{
		return	tts;
	}
	
	//初始化
	public void init( Context context )
	{
		tts = new TextToSpeech( context.getApplicationContext(), this );
	}
	
	//销毁
	public void destroy()
	{
		tts.shutdown();
	}

	//是否初始化成功
	public boolean isSuccess()
	{
		return	isSuccess;
	}
	
	@Override
	public void onInit(int status) 
	{
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.CHINA);
			if( result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED )
			{
				isSuccess = false;
			}
			else
			{
				isSuccess = true;
			}
		}
		else
		{
			isSuccess = false;
		}
	}
	
	//朗读
	public void speak( final String content )
	{
		if( isSuccess && tts != null )
		{
			if( tts.isSpeaking() )
			{
				tts.stop();
			}	//如果正在朗读，先停止
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
			tts.speak(content, TextToSpeech.QUEUE_FLUSH, map );
		}
	}
	
	//停止朗读
	public void stop()
	{
		if( isSuccess && tts != null )
		{
			if( tts.isSpeaking() )
			{
				tts.stop();
			}	//如果正在朗读，先停止
		}
	}
}
