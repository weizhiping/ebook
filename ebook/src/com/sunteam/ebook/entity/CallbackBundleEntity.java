package com.sunteam.ebook.entity;

import com.sunteam.ebook.util.CallbackBundle;

/**
 * 回调接口参数类
 * 
 * @author wzp
 */
public class CallbackBundleEntity
{
	public String key;								//关键字
	public CallbackBundleType type;		//回调类型
	public CallbackBundle cb;					//回调函数
}
