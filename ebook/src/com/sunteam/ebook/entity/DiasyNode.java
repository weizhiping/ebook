package com.sunteam.ebook.entity;

import java.io.Serializable;

/**
 * Diasy 节点类
 * 
 * @author wzp
 * 
 */
public class DiasyNode implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7345178245843082550L;
	
	public int father; // 父节点序号
	public int seq; // 节点序号
	public int level; // 节点等级
	public String name; // 节点名称
	public String href; // 子链接
	public String label; // 标签
}
