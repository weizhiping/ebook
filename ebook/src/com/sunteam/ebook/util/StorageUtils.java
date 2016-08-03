package com.sunteam.ebook.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;

/**
 * Storage工具类。
 * 
 * @author wzp
 */
public class StorageUtils
{
	private Context mContext;
	private StorageManager mStorageManager;
	private Method mMethodGetPaths;

	public StorageUtils(Context context) 
	{
		mContext = context;
		if (mContext != null) 
		{
			mStorageManager = (StorageManager)mContext.getSystemService(Activity.STORAGE_SERVICE);
			try 
			{
				mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
			} 
			catch (NoSuchMethodException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public String[] getVolumePaths()
	{
		String[] paths = null;
		try 
		{
			paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
		} 
		catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		}
		
		return paths;
	}
}
