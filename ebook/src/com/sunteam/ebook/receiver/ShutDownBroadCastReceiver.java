package com.sunteam.ebook.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//关机广播
public class ShutDownBroadCastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive( final Context context, Intent intent )
	{
		final String action = intent.getAction();
		
		if( Intent.ACTION_SHUTDOWN.equals(action) )
		{
			Intent intent1  = new Intent("android.intent.action.ACTION_CUSTOM_SHUTDOWN");
			intent1.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			context.sendBroadcast(intent);
		}
	}
}
