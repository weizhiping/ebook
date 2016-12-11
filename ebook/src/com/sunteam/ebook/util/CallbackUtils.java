package com.sunteam.ebook.util;

import java.util.ArrayList;

import com.sunteam.ebook.entity.CallbackBundleEntity;
import com.sunteam.ebook.entity.CallbackBundleType;

import android.os.Bundle;

/**
 * 回调方法管理工具类
 * 
 * @author wzp
 */
public class CallbackUtils
{
	private static ArrayList<CallbackBundleEntity> mCallbackBundleEntityList = null;	//回调函数集合

	//注册回调函数
	public static void registerCallback( String key, CallbackBundleType type, CallbackBundle cb )
	{
		if( null == mCallbackBundleEntityList )
		{
			mCallbackBundleEntityList = new ArrayList<CallbackBundleEntity>();
		}
		
		CallbackBundleEntity entity = new CallbackBundleEntity();
		entity.key = key;
		entity.type = type;
		entity.cb = cb;
		
		int size = mCallbackBundleEntityList.size();
		for( int i = 0; i < size; i++ )
		{
			if( ( mCallbackBundleEntityList.get( i ).key.equals( key ) ) && ( mCallbackBundleEntityList.get( i ).type == type ) )
			{
				mCallbackBundleEntityList.remove( i );	//删除旧的
				break;
			}
		}
		
		mCallbackBundleEntityList.add( entity );
	}
	
	//注销回调函数
	public static void unRegisterCallback( String key, CallbackBundleType type )
	{
		if( null != mCallbackBundleEntityList )
		{
			int size = mCallbackBundleEntityList.size();
			for( int i = 0; i < size; i++ )
			{
				if( ( mCallbackBundleEntityList.get( i ).key.equals( key ) ) && ( mCallbackBundleEntityList.get( i ).type == type ) )
				{
					mCallbackBundleEntityList.remove( i );	//删除旧的
					break;
				}
			}
		}
	}
	
	//清除所有注册的回调函数
	public static void clearCallback()
	{
		if( null != mCallbackBundleEntityList )
		{
			mCallbackBundleEntityList.clear();
		}
	}

	//调用回调
	public static boolean callCallback( String key, CallbackBundleType type, Bundle bundle )
	{
		if( mCallbackBundleEntityList != null )
        {
			int size = mCallbackBundleEntityList.size();
			for( int i = 0; i < size; i++ )
			{
				if( ( key.equals(mCallbackBundleEntityList.get( i ).key) ) && ( type== mCallbackBundleEntityList.get( i ).type ) )
				{
					CallbackBundle cb = mCallbackBundleEntityList.get( i ).cb;
	            	if( cb != null )
	            	{
	    				// 调用事先设置的回调函数
	    				cb.callback( bundle );
	    				return	true;
	            	}
				}
			}
        }
		
		return	false;
	}
	
	//调用回调
	public static boolean callCallback( CallbackBundleType type, Bundle bundle )
	{
		if( mCallbackBundleEntityList != null )
        {
			int size = mCallbackBundleEntityList.size();
			for( int i = 0; i < size; i++ )
			{
				if( type== mCallbackBundleEntityList.get( i ).type )
				{
					CallbackBundle cb = mCallbackBundleEntityList.get( i ).cb;
	            	if( cb != null )
	            	{
	    				// 调用事先设置的回调函数
	    				cb.callback( bundle );
	            	}
				}
			}
        }
		
		return	true;
	}
	
	//逆序调用回调
	public static boolean callCallbackEx( CallbackBundleType type, Bundle bundle )
	{
		if( mCallbackBundleEntityList != null )
        {
			int size = mCallbackBundleEntityList.size();
			for( int i = size-1; i >= 0; i-- )
			{
				if( type== mCallbackBundleEntityList.get( i ).type )
				{
					CallbackBundle cb = mCallbackBundleEntityList.get( i ).cb;
	            	if( cb != null )
	            	{
	    				// 调用事先设置的回调函数
	    				cb.callback( bundle );
	            	}
				}
			}
        }
		
		return	true;
	}	
}
