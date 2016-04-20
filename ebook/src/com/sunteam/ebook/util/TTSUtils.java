package com.sunteam.ebook.util;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

/**
 * TTS工具类。
 * 
 * @author wzp
 */
public class TTSUtils
{
	private static TTSUtils instance = null;
	private Context mContext;
	private SpeechSynthesizer mTts;		//语音合成对象
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
	public SpeechSynthesizer getTextToSpeech()
	{
		return	mTts;
	}
	
	//初始化
	public void init( Context context )
	{
		mContext = context.getApplicationContext();
		mTts = SpeechSynthesizer.createSynthesizer( mContext, new InitListener() {
			@Override
			public void onInit(int code) 
			{
				if( ErrorCode.SUCCESS == code ) 
				{
					isSuccess = true;
				}
            }
        });	//初始化合成对象
	}
	
	//销毁
	public void destroy()
	{
		mTts.destroy();
	}

	//是否初始化成功
	public boolean isSuccess()
	{
		return	isSuccess;
	}
	
	//停止朗读
	public void stop()
	{
		if( isSuccess && mTts != null )
		{
			if( mTts.isSpeaking() )
			{
				mTts.stopSpeaking();
			}	//如果正在朗读，先停止
		}
	}

	/**
     * 开始语音合成
     *
     * @param text
     */
	public void speak( final String text ) 
	{
		if( isSuccess && mTts != null )
		{
	        setParam();	//设置参数
	        int code = mTts.startSpeaking(text, mTtsListener);
	        if( code != ErrorCode.SUCCESS ) 
	        {
	        	//Toast.makeText(mContext, "语音合成失败,错误码: " + code, Toast.LENGTH_SHORT).show();
	        }
		}
    }

    /**
     * 合成回调监听。
     */
	private SynthesizerListener mTtsListener = new SynthesizerListener() 
	{
		//开始合成
		@Override
		public void onSpeakBegin() 
		{
		}

		//暂停合成
		@Override
		public void onSpeakPaused() 
		{
		}

		//继续合成
		@Override
		public void onSpeakResumed() 
		{
		}

		//传冲进度
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) 
        {
        }

        //合成进度
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) 
        {
        }

        //合成完成
		@Override
		public void onCompleted(SpeechError error) 
		{
			// TODO Auto-generated method stub
			if( null == error )
			{
				//合成完成
			}
			else
			{
				//合成错误
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) 
		{
			// TODO Auto-generated method stub
		}
    };

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() 
    {
    	mTts.setParameter(SpeechConstant.PARAMS, null);								//清空参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);	//设置使用本地引擎
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());			//设置发音人资源路径
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaofeng");					//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");								//设置语速
        mTts.setParameter(SpeechConstant.PITCH, "50");								//设置音调
        mTts.setParameter(SpeechConstant.VOLUME, "100");							//设置音量
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");							//设置播放器音频流类型
    }

	//获取发音人资源路径
    private String getResourcePath() 
    {
    	StringBuffer tempBuffer = new StringBuffer();
        
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, "tts/common.jet"));		//合成通用资源
        tempBuffer.append(";");
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, "tts/xiaofeng.jet"));	//发音人资源
        
        return tempBuffer.toString();
    }
}
