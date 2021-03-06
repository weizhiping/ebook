package com.sunteam.ebook.receiver;

import com.sunteam.ebook.entity.CallbackBundleType;
import com.sunteam.ebook.util.CallbackUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//SD卡插拔广播
public class UsbBroadCastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive( final Context context, Intent intent )
	{
		final String action = intent.getAction();
		
		if( Intent.ACTION_MEDIA_EJECT.equals(action) )
		{
			CallbackUtils.callCallbackEx(CallbackBundleType.CALLBACK_SDCARD_UNMOUNT, null);
			if( false == TextFileReaderUtils.getInstance().isInsideSDPath() )
			{
				TextFileReaderUtils.getInstance().destroy();
			}
			//Toast.makeText(context, "SD卡已拔出", Toast.LENGTH_SHORT).show();
		}
		else if( Intent.ACTION_MEDIA_MOUNTED.equals(action) )
		{
			//Toast.makeText(context, "SD卡已插入", Toast.LENGTH_SHORT).show();
		}
	}
}
