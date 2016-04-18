package com.sunteam.ebook.entity;

/**
 * 分割信息类
 * 
 * @author wzp
 *
 */
public class SplitInfo 
{
	public int startPos;	//开始位置
	public int len;			//长度
	
	public SplitInfo()
	{
		
	}
	
	public SplitInfo( int s, int l )
	{
		startPos = s;
		len = l;
	}
}
