package com.sunteam.ebook.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Environment;

/**
 * 通用文件操作
 * @author sylar
 *
 */
public class FileOperateUtils {
	private static String targetFolder = "";
	private static String targetPath = "";
	
	public static String getTargetFolder() {
		return targetFolder;
	}

	public static void setTargetFolder(String strTargetFolder) {
		targetFolder = strTargetFolder;
		targetPath = getSDPath() + targetFolder + "/";
	}
	
	public static String getSDPath(){
		String sdPath = Environment.getExternalStorageDirectory().getPath() + "/";
		return sdPath;
	}
	
	/** 
	 * 获取扩展存储路径，TF卡、U盘 
	 */  
	public static String getTFDirectory(){  
	    String dir = null;  
	    try {  
	        Runtime runtime = Runtime.getRuntime();  
	        Process proc = runtime.exec("mount");  
	        InputStream is = proc.getInputStream();  
	        InputStreamReader isr = new InputStreamReader(is);  
	        String line;  
	        BufferedReader br = new BufferedReader(isr);  
	        while ((line = br.readLine()) != null) {  
	            if (line.contains("secure")) continue;  
	            if (line.contains("asec")) continue;  
	              
	            if (line.contains("fat")) {  
	                String columns[] = line.split(" ");  
	                if (columns != null && columns.length > 1) {  
	                    dir = dir.concat(columns[1] + "\n");  
	                }  
	            } else if (line.contains("fuse")) {  
	                String columns[] = line.split(" ");  
	                if (columns != null && columns.length > 1) {  
	                    dir = dir.concat(columns[1] + "\n");  
	                }  
	            }  
	        }  
	    } catch (FileNotFoundException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	    }
	    
	    return dir;  
	}  
		
	public static String getTargetPath(){
		if(targetPath.isEmpty()){
			if(targetFolder.isEmpty())
				targetPath = getSDPath();
			else
				targetPath = getSDPath() + targetFolder + "/";
		}
		return targetPath;
	}
	
	/**
	 * @param filePath
	 * @return 
	 */
	public static String getParentPath(String filePath){
		String parent = "";
		if(!filePath.contains("/"))
			return parent;
		
		parent = filePath.substring(0,filePath.lastIndexOf("/"));		
		return parent;
	}
	
	public static String getFileName(String filepath){
		String name = "";
		if(!filepath.isEmpty())
			name = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());
		return name;	
	}
	
	/**
	 * @ 遍历目录下的所有文件和目录
	 * @param dir
	 * @param nameList 保存目录下的所以文件名
	 * @return
	 */
	public static boolean getDirFiles(File dir, List<String> nameList){
		if(dir == null || !dir.isDirectory())
			return false;
	
		File[] fileChildren = dir.listFiles();
		if(fileChildren == null)
			return false;
		
		if(!nameList.isEmpty())
			nameList.clear();
		for(File f:fileChildren){
			String fname = f.getName();
			nameList.add(fname);
		}
		
		return true;
	}
	
	/**
	 * @ 遍历目录下的所有文件和目录
	 * @param dir
	 * @param nameList 保存目录下的所有文件
	 * @return
	 */
	public static ArrayList<File> getFilesInDir(String path){
		File dir = new File(path);
		ArrayList<File> fileList = new ArrayList<File>();
		if(dir == null || !dir.isDirectory())
			return null;
		File[] files = dir.listFiles();
		if(files == null)
			return null;
		if(!fileList.isEmpty())
			fileList.clear();
		for(File f:files)
			fileList.add(f);
		
		return fileList;
	}
	
	/**
	 * 搜索rootDir目录下文件名包含关键字seaName的文件及目录
	 * @param rootDir 搜索根目录
	 * @param seaName 搜索关键字
	 * @param resultList 包含关键字的File集合
	 * @return
	 */
	public static boolean searchFilesWithName(File rootDir, String seaName, List<File> resultList){
		if(rootDir == null || !rootDir.exists() || !rootDir.isDirectory())
			return false;
		for(File f: rootDir.listFiles()){
			if(f.getName().contains(seaName))
				resultList.add(f);
			if(f.isDirectory())
				searchFilesWithName(f, seaName, resultList);
		}
		return true;
	}
	
	/**
	 * 搜索rootDir目录下文件名包含关键字seaName的文件及目录
	 * @param rootDir 搜索根目录
	 * @param seaName 搜索关键字
	 * @param resultList 包含关键字的File集合
	 * @param exceptFile 屏蔽的文件
	 * @return
	 */
	public static boolean searchFilesWithNameWithoutCache(File rootDir, String seaName, List<File> resultList, File exceptFile){
		if(rootDir == null || !rootDir.exists() || !rootDir.isDirectory())
			return false;
		for(File f: rootDir.listFiles()){
			if(f.equals(exceptFile))
				continue;
			
			if(f.getName().contains(seaName))
				resultList.add(f);
			if(f.isDirectory())
				searchFilesWithNameWithoutCache(f, seaName, resultList, exceptFile);
		}
		return true;
	}
	
	/**
	 * 删除存在的文件
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file){
		if(file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}
	
	/**
	 * 删除存在的文件夹
	 * @param dir
	 * @return
	 */
	public static boolean delDir(File dir){
		if(dir == null || !dir.exists() || !dir.isDirectory())
			return false;

		for(File f:dir.listFiles()){
			if(f.isDirectory()){
				delDir(f);
			}
			else
				delFile(f);
		}
		return dir.delete();
	}
	
	/**
	 * 清空文件夹下的内容
	 * @param dir
	 * @return false:文件夹不存在, true:成功清空
	 */
	public static boolean clearDir(File dir){
		
		if(dir == null || !dir.exists() || !dir.isDirectory())
			return false;

		for(File f:dir.listFiles()){
			if(f.isDirectory()){
				delDir(f);
			}
			else
				delFile(f);
		}
		
		return true;
	}
	
	/**
	 * 获取文件或文件夹大小
	 * @param srcFile
	 * @return double
	 */
	public static double getFileSizeDou(File srcFile){
		double size = 0;
		if(srcFile == null || !srcFile.exists())
			return size;
		if(!srcFile.isDirectory()){//文件
			size = srcFile.length();
			return size;
		}
		
		for(File f:srcFile.listFiles()){//文件夹
			size += getFileSizeDou(f);
		}
		
		return size;		
	}
	
	/**
	 * 获取文件或文件夹大小
	 * @param srcFile
	 * @return String
	 */
	public static String getFileSizeString(File srcFile){
		return formatFileSize(getFileSizeDou(srcFile));
	}
	
	public static String formatFileSize(double fileSize){
		String strFileSize = "";
		DecimalFormat df = new DecimalFormat("#.00");
		
		if(fileSize < 1024){
			strFileSize = df.format(fileSize) + "B";
		}
		else if(fileSize < 1048576){
			strFileSize = df.format(fileSize/1024) +  "K";
		}
		else if(fileSize < 1073741824){
			strFileSize = df.format(fileSize/1048576) +  "M";
		}
		else{
			strFileSize = df.format(fileSize/1073741824) +  "G";
		}
		
		return strFileSize;
	}
	
	/**
	 * 判断文件夹是否未空
	 * @param dir
	 * @return
	 */
	public static boolean isDirEmpty(File dir){
		if(dir == null)
			return false;
		
		File[] lists = dir.listFiles();
		if(lists == null)
			return false;
		
		if(lists.length == 0)		
			return true;
		else
			return false;
	}
	
	/**
	 * 判断文件夹是否未空
	 * @param dirPath
	 * @return
	 */
	public static boolean isDirEmpty(String dirPath){
		File target = new File(dirPath);
		return isDirEmpty(target);
	}
	
	/**
	 * 判断fileName 是否已在当前文件parentPath中存在
	 * @param fileName
	 * @param parentPath
	 * @return
	 */
	public static boolean isNameExsit(String fileName, String parentPath){
		File parent = new File(parentPath);
		File[] lists = parent.listFiles();
		for(File f:lists)
		{
			if(fileName.equals(f.getName()))
				return true;
		}
		return false;
	}
	
	/**
	 * 获取文件后缀名
	 * @param srcfile 
	 * @return 文件不存在，返回“*”
	 */
	public static String getFileExtensions(File srcfile){
		String strEx = "*";
		if(srcfile == null || !srcfile.exists())
			return strEx;
		
		String fileName = srcfile.getName();
		strEx = getFileExtensions(fileName);		
		
		return strEx;
	}
	
	/**
	 * 获取文件后缀名
	 * @param fileName 文件名或路径
	 * @return 返回小写后缀名，后缀名不带“.”，没有后缀返回“*”，例如“txt”
	 */
	@SuppressLint("DefaultLocale")
	public static String getFileExtensions(String fileName){
		String strEx = "*";
		int dotIndex = fileName.lastIndexOf(".");
		if(dotIndex<0)
			return strEx;
		strEx = fileName.substring(dotIndex+1, fileName.length()).toLowerCase();
		return strEx;
	}
	
	/**
	 * 获取文件名称和后缀名
	 * @param fileName input 文件名
	 * @param outFileName output 文件名（光的）
	 * @param outFileExt output  原始文件后缀名（带"."），不做大小写处理
	 */
	public static void getFileExtensionsAndName(String fileName, String outFileName, String outFileExt ){
		int dotIndex = fileName.lastIndexOf(".");
		if(dotIndex<0){
			outFileExt = "";
			outFileName = fileName;
			return ;
		}
		outFileExt = fileName.substring(dotIndex, fileName.length());
		outFileName = fileName.replace(outFileExt, "");
	}
	
	public static boolean getIsImageFile( String fileName )
	{
		boolean bIsImageFile = false;
		String strEx = getFileExtensions(fileName);
		
		if(strEx.equals("bmp") ||strEx.equals("gif") ||strEx.equals("jpg") ||
				strEx.equals("jpeg") ||strEx.equals("png") ||strEx.equals("svg") ||strEx.equals("ico") ||strEx.equals("tif")){
			bIsImageFile = true;
		}	
		
		return bIsImageFile;
	}
	
	public static boolean getIsAudioFile( String fileName )
	{
		boolean bIsImageFile = false;
		String strEx = getFileExtensions(fileName);
		
		if(strEx.equals("mp3") ||strEx.equals("ogg") ||strEx.equals("wav") ||strEx.equals("wma")){
			bIsImageFile = true;
		}	
		
		return bIsImageFile;
	}
	
	public static boolean getIsFilter(String fileName)
	{
		boolean bIsFilter = true;
		String strEx = getFileExtensions(fileName);
		
		if(strEx.equals("mp4") ||strEx.equals("3gp") ||strEx.equals("avi") ||strEx.equals("mpg4")||strEx.equals("mkv") ||strEx.equals("asf")||strEx.equals("wmv")||strEx.equals("flv")||strEx.equals("dat")){
			bIsFilter = true;
		}
		else if(strEx.equals("doc") ||strEx.equals("docx")||strEx.equals("wps")){
			bIsFilter = false;
		}
		else if(strEx.equals("ppt") ||strEx.equals("pptx")){
			bIsFilter = false;
		}
		else if(strEx.equals("xlm") ||strEx.equals("xls") ||strEx.equals("xla")
			  ||strEx.equals("xlw") ||strEx.equals("xlt") ||strEx.equals("xlc")||strEx.equals("xlsx")){
			bIsFilter = false;
		}
		else if(strEx.equals("exe")){
			bIsFilter = true;
		}
		else if(strEx.equals("hht") || strEx.equals("hhtx") || strEx.equals("iwb")){
			bIsFilter = false;
		}
		else if(strEx.equals("bmp") ||strEx.equals("gif") ||strEx.equals("jpg") ||
				strEx.equals("jpeg") ||strEx.equals("png") ||strEx.equals("svg") ||strEx.equals("ico") ||strEx.equals("tif")){
			bIsFilter = true;
		}
		else if(strEx.equals("pdf")){
			bIsFilter = false;
		}
		else if(strEx.equals("mp3") ||strEx.equals("ogg") ||strEx.equals("wav") ||strEx.equals("wma")){
			bIsFilter = true;
		}
		else if(strEx.equals("rar") ||strEx.equals("7z") ||strEx.equals("zip") ||strEx.equals("gz") ||strEx.equals("tar") ){
			bIsFilter = true;
		}
		else if(strEx.equals("swf") ||strEx.equals("swfl") ){
			bIsFilter = true;
		}
		else if(strEx.equals("txt") ||strEx.equals("xml") ||strEx.equals("html") ){
			bIsFilter = false;
		}
		else{
			bIsFilter = true;
		}		
		
		return bIsFilter;
	}
}
