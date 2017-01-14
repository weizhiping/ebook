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
	public String diasyPath;//diasy可以查看节点的路径
	public String diasyFlag;//0_1_2  父节点_节点序号_节点等级
	public int catalog;//1为txt文档，2为word文档,3为disay
	public int flag;//0为目录浏览，1为我的收藏，2为最近使用，3为目录浏览中文件
	public int storage;//0为内置存储，1为外置存储
	public int part;//部分
	public int line;//行号
	public int startPos;//反显开始点
	public int len;//长度
	public int checksum;//校验
	public int item;//文件列表中的位置
	public int count;	//文件总部分数目
	public int hasDaisy;//判断是否有daisy的二级目录
	public FileInfo(){
	
	}
	
	public FileInfo(String name, String path,boolean isFolder,int type,int flag,int storage) {
		this.name = name;
		this.path = path;
		this.isFolder = isFolder;
		this.catalog = type;
		this.flag = flag;
		this.storage = storage;
	}

}
