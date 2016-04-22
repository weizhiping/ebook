package com.sunteam.ebook.util;

import android.content.Context;

/**
 * 可重用的方法工具类。
 * 
 * @author wzp
 */
public class PublicUtils 
{
	private static int mColorSchemeIndex = 0;	//配色方案索引
	
	//从系统配置文件中得到配色方案索引
	public static int getSysColorSchemeIndex()
	{
		return	(int)(System.currentTimeMillis()%7);
	}
	
	//设置配色方案
	public static void setColorSchemeIndex( int index )
	{
		mColorSchemeIndex = index;
	}
	
	//得到配色方案
	public static int getColorSchemeIndex()
	{
		return	mColorSchemeIndex;
	}
	
	//dip转px
	public static int dip2px( Context context, float dipValue )
	{ 
		final float scale = context.getResources().getDisplayMetrics().density;
		
        return (int)(dipValue * scale + 0.5f); 
	} 

	//px转dip
	public static int px2dip( Context context, float pxValue )
	{ 
		final float scale = context.getResources().getDisplayMetrics().density; 
        
		return (int)(pxValue / scale + 0.5f); 	
	}
	
	//byte转char
	public static char byte2char( byte[] buffer, int offset )
	{
		if( buffer[offset] >= 0 )
		{
			return	(char)buffer[offset];
		}
		 
		int hi = (int)(256+buffer[offset]);
		int li = (int)(256+buffer[offset+1]);
		 
		return	(char)((hi<<8)+li);
	}

	//byte转int
	public static int byte2int( byte[] buffer, int offset )
	{
		int[] temp = new int[4];
		
		for( int i = offset, j = 0; i < offset+4; i++, j++ )
		{
			if( buffer[i] < 0 )
			{
				temp[j] = 256+buffer[i];
			}
			else
			{
				temp[j] = buffer[i];
			}
		}
		
		int result = 0;
		
		for( int i = 0; i < 4; i++ )
		{
			result += (temp[i]<<(8*(i)));
		}
		
		return	result;
	}	
}	
