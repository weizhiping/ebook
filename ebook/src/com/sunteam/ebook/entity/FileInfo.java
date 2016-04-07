package com.sunteam.ebook.entity;
/**
 * 文件实体类
 * @author sylar
 *
 */
public class FileInfo {
	public String name;
	public String path;
	public boolean isFolder;
	
	public FileInfo(String name, String path,boolean isFolder) {
		this.name = name;
		this.path = path;
		this.isFolder = isFolder;
	}
}
