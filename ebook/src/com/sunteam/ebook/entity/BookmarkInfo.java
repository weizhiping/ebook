package com.sunteam.ebook.entity;

import java.io.Serializable;

/**
 * 书签信息
 * 
 * @author wzp
 * 
 */
public class BookmarkInfo implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3960969094131433143L;
	
	public int seq; 	// 节点序号或者部分序号
	public int line;	// 行号
	public int start;	// 反显开始位置
	public int len;		// 反显长度
}
