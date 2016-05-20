package com.sunteam.ebook.entity;

import java.io.Serializable;

/**
 * Diasy 句子节点类
 * 
 * @author wzp
 * 
 */
public class DiasySentenceNode implements Serializable
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7359722933553886963L;
	
	public byte[] sentence;		//句子
	public String audioFile;	//音频文件
	public long startTime;		//开始时间
	public long endTime;		//结束时间
}
