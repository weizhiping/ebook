package com.sunteam.ebook.entity;

import java.io.Serializable;

/**
 * 文件实体类
 * @author sylar
 *
 */
public class FileInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String name;
	public String path;
	public boolean isFolder;
	public int catalog;//1为txt文档，2为word文档,3为disay
	public int flag;//0为目录浏览，1为我的收藏，2为最近使用，3为目录浏览中文件
	public int part;//部分
	public int line;//行号
	public int startPos;//反显开始点
	public int len;//长度
	public int checksum;//校验
	public FileInfo(){
	
	}
	
	public FileInfo(String name, String path,boolean isFolder,int type,int flag) {
		this.name = name;
		this.path = path;
		this.isFolder = isFolder;
		this.catalog = type;
		this.flag = flag;
	}

}
