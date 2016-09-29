package com.sunteam.ebook.util;

import com.sunteam.common.utils.PromptDialogNospeech;

import android.content.Context;
import android.os.Handler;

/**
 * 自定义Toast类。
 * 
 * @author wzp
 */
public class CustomToast 
{
	private static PromptDialogNospeech mPromptDialogNospeech;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() 
        {
        	mPromptDialogNospeech.cancel();
        	mPromptDialogNospeech = null;
        }
    };

    public static void showToast(Context mContext, String text, int duration) 
    {
    	mHandler.removeCallbacks(r);
    	if (mPromptDialogNospeech != null)
    	{
    		mPromptDialogNospeech.setTitle(text);
    	}
    	else
    	{
    		mPromptDialogNospeech = new PromptDialogNospeech( mContext, text );
    	}
    	
    	mHandler.postDelayed(r, 1500);
    	mPromptDialogNospeech.show();
    }

    public static void showToast(Context mContext, int resId, int duration) 
    {
    	showToast(mContext, mContext.getResources().getString(resId), duration);
    }
}	
