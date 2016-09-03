package com.sunteam.ebook.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * 自定义Toast类。
 * 
 * @author wzp
 */
public class CustomToast 
{
	private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() 
        {
            mToast.cancel();
        }
    };

    public static void showToast(Context mContext, String text, int duration) 
    {
    	mHandler.removeCallbacks(r);
    	if (mToast != null)
    	{
    		mToast.setText(text);
    	}
    	else
    	{
    		mToast = Toast.makeText(mContext, text, duration);
    	}
    	
    	mHandler.postDelayed(r, 3000);
    	mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) 
    {
    	showToast(mContext, mContext.getResources().getString(resId), duration);
    }
}	
