package com.sunteam.ebook.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.sunteam.ebook.entity.FileInfo;

/**
 * 通用文件操作
 * 
 * @author sylar
 * 
 */
public class FileOperateUtils {

	public static String getSDPath() {
		String sdPath = Environment.getExternalStorageDirectory().getPath()
				+ "/";
		return sdPath;
	}

	/**
	 * 获取扩展存储路径，TF卡、U盘
	 */
	public static String getTFDirectory() {
		String dir = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;

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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dir;
	}
	
	public static String getMusicPath() {
		String musicPath = Environment.getExternalStorageDirectory().getPath()
				+ "/book/music/";
		return musicPath;
	}

	/**
	 * @param filePath
	 * @return
	 */
	public static String getParentPath(String filePath) {
		String parent = "";
		if (!filePath.contains("/"))
			return parent;

		parent = filePath.substring(0, filePath.lastIndexOf("/"));
		return parent;
	}

	public static String getFileName(String filepath) {
		String name = "";
		if (!filepath.isEmpty())
			name = filepath.substring(filepath.lastIndexOf("/") + 1,
					filepath.length());
		return name;
	}

	/**
	 * @ 遍历目录下的所有文件和目录
	 * 
	 * @param dir
	 * @param nameList
	 *            保存目录下的所以文件名
	 * @return
	 */
	public static boolean getDirFiles(File dir, List<String> nameList) {
		if (dir == null || !dir.isDirectory())
			return false;

		File[] fileChildren = dir.listFiles();
		if (fileChildren == null)
			return false;

		if (!nameList.isEmpty())
			nameList.clear();
		for (File f : fileChildren) {
			String fname = f.getName();
			nameList.add(fname);
		}

		return true;
	}

	/**
	 * @ 遍历目录下的指定后缀的文件和目录
	 * 
	 * @param path
	 *            路径
	 * @param suffix
	 *            后缀名 保存目录下的所有文件
	 * @return
	 */
	public static ArrayList<File> getFilesInDir(String path, String suffix,
			String suffixDocx) {
		File dir = new File(path);
		ArrayList<File> fileList = new ArrayList<File>();
		if (dir == null || !dir.isDirectory())
			return null;
		File[] files = dir.listFiles();
		if (files == null)
			return null;
		if (!fileList.isEmpty())
			fileList.clear();
		for (File f : files) {
			if (!isHideFile(f)) {
				if (f.isDirectory()) {
					if (hasSuffixFile(f, suffix, suffixDocx)) {
						fileList.add(f);
					}
				} else {
					if (suffix.equalsIgnoreCase(getFileExtensions(f))
							|| getFileExtensions(f)
									.equalsIgnoreCase(suffixDocx)) {
						fileList.add(0, f);
					}
				}
			}
		}
		return fileList;
	}

	/**
	 * @ 遍历目录下的Daisy文件目录
	 * 
	 * @param path 路径
	 * @return
	 */
	public static ArrayList<FileInfo> getDaisyInDir(int catalog) {
		File dir = new File(FileOperateUtils.getSDPath());
		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		if (dir == null || !dir.isDirectory())
			return null;
		File[] files = dir.listFiles();
		if (files == null)
			return null;
		if (!fileList.isEmpty())
			fileList.clear();
		for (File f : files) {
			if (!isHideFile(f)) {
				if (f.isDirectory()) {
					String path = hasDaisyNccFile(f);
					if(null != path){
						FileInfo info = new FileInfo(f.getName(), f.getPath(), true,
								catalog, 0, 0);
						info.diasyPath = path;
						fileList.add(info);
					}else{
						path = hasDaisyOpfFile(f);
						if(null != path){
							FileInfo info = new FileInfo(f.getName(), f.getPath(), true,
									catalog, 0, 0);
							info.diasyPath = path;
							fileList.add(0,info);
						}
					}
				}
			}
		}
		return fileList;
	}

	/**
	 * @ 遍历目录下的音频文件
	 * 
	 * @param path
	 *            路径
	 * @param suffix
	 *            后缀名 保存目录下的所有文件
	 * @return
	 */
	public static ArrayList<File> getMusicInDir() {
		File dir = new File(getMusicPath());
		ArrayList<File> fileList = new ArrayList<File>();
		if (dir == null || !dir.isDirectory())
			return null;
		File[] files = dir.listFiles();
		if (files == null)
			return null;
		if (!fileList.isEmpty())
			fileList.clear();
		for (File f : files) {
			if (!isHideFile(f)) {
				if (f.isDirectory()) {
					
				}else{
					if(isMusic(f.getName())){
						fileList.add(f);
					}
				}
			}
		}
		return fileList;
	}
	/**
	 * @ 获取目录下的音频第一个文件
	 * 
	 * @param path
	 *            路径
	 * @param suffix
	 *            后缀名 保存目录下的所有文件
	 * @return
	 */
	public static String getFirstMusicInDir() {
		String musicPath = null;
		File dir = new File(getMusicPath());
		if (dir == null || !dir.isDirectory())
			return null;
		File[] files = dir.listFiles();
		if (files == null)
			return null;
		for (File f : files) {
			if (!isHideFile(f)) {
				if (!f.isDirectory() && isMusic(f.getName())) {
					musicPath = f.getPath();
					return musicPath;
				}
			}
		}
		return musicPath;
	}
	/**
	 * 判断是否为隐藏文件
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isHideFile(File f) {
		String name = f.getName();
		if (name.substring(0, 1).equals(".")) {
			return true;
		}
		return false;
	}

	/**
	 * 搜索rootDir目录下文件名包含关键字seaName的文件及目录
	 * 
	 * @param rootDir
	 *            搜索根目录
	 * @param seaName
	 *            搜索关键字
	 * @param resultList
	 *            包含关键字的File集合
	 * @return
	 */
	public static boolean searchFilesWithName(File rootDir, String seaName,
			List<File> resultList) {

		if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory())
			return false;
		for (File f : rootDir.listFiles()) {

			if (f.getName().contains(seaName))
				resultList.add(f);
			if (f.isDirectory())
				searchFilesWithName(f, seaName, resultList);
		}
		return true;
	}

	/**
	 * 判断文件夹是否未空
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isDirEmpty(File dir) {
		if (dir == null)
			return false;

		File[] lists = dir.listFiles();
		if (lists == null)
			return false;

		if (lists.length == 0)
			return true;
		else
			return false;
	}

	/**
	 * 判断文件夹是否未空
	 * 
	 * @param dirPath
	 * @return
	 */
	public static boolean isDirEmpty(String dirPath) {
		File target = new File(dirPath);
		return isDirEmpty(target);
	}

	/**
	 * 判断fileName 是否已在当前文件parentPath中存在
	 * 
	 * @param fileName
	 * @param parentPath
	 * @return
	 */
	public static boolean isNameExsit(String fileName, String parentPath) {
		File parent = new File(parentPath);
		File[] lists = parent.listFiles();
		for (File f : lists) {
			if (fileName.equals(f.getName()))
				return true;
		}
		return false;
	}

	/**
	 * 获取文件后缀名
	 * 
	 * @param srcfile
	 * @return 文件不存在，返回“*”
	 */
	public static String getFileExtensions(File srcfile) {
		String strEx = "*";
		if (srcfile == null || !srcfile.exists())
			return strEx;

		String fileName = srcfile.getName();
		strEx = getFileExtensions(fileName);

		return strEx;
	}

	/**
	 * 获取文件后缀名
	 * 
	 * @param fileName
	 *            文件名或路径
	 * @return 返回小写后缀名，后缀名不带“.”，没有后缀返回“*”，例如“txt”
	 */
	public static String getFileExtensions(String fileName) {
		String strEx = "*";
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex < 0)
			return strEx;
		strEx = fileName.substring(dotIndex + 1, fileName.length())
				.toLowerCase();
		return strEx;
	}
	/**
	 * 获取文件最后一个"."之前的名字
	 * @param fileName
	 * @return
	 */
	public static String getFileExtensionsName(String fileName) {
		String strEx = "*";
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex < 0)
			return strEx;
		strEx = fileName.substring(0, dotIndex);
		return strEx;
	}
	
	/**
	 * 获取是否包含文件
	 * 
	 * @param file
	 * @param suffix
	 *            后缀名
	 * @param suffixTwo
	 *            后缀名
	 * @return
	 */
	public static boolean hasSuffixFile(File file, String suffix,
			String suffixTwo) {
		boolean bIsFilter = false;
		if (file == null || !file.exists())
			return bIsFilter;
		for (File f : file.listFiles()) {
			if (!f.isDirectory()) {
				if (suffix.equalsIgnoreCase(getFileExtensions(f))
						|| getFileExtensions(f).equalsIgnoreCase(suffixTwo)) {
					return true;
				}
			} else {
				hasSuffixFile(f, suffix, suffixTwo);
			}
		}
		return bIsFilter;
	}
	
	/**
	 * 获取是否包含diasy文件
	 * 
	 * @param file
	 * @return
	 */
	public static String hasDaisyNccFile(File file) {
		String path = null;
		if (file == null || !file.exists())
			return null;
		for (File f : file.listFiles()) {
			if (!f.isDirectory()) {
				String name = f.getName();
				
				if (EbookConstants.BOOK_DAISY_NCC.equalsIgnoreCase(getFileExtensionsName(name))) {
					return f.getPath();
				}
			} else {
				String result = hasDaisyNccFile(f);
				if( result != null ){
					return	result;
				}
			}
		}
		return path;
	}
	
	/**
	 * 获取是否包含diasy文件
	 * 
	 * @param file
	 * @return
	 */
	public static String hasDaisyOpfFile(File file) {
		String path = null;
		if (file == null || !file.exists())
			return null;
		for (File f : file.listFiles()) {
			if (!f.isDirectory()) {
				String name = f.getName();
				if (EbookConstants.BOOK_DAISY_OPF.equalsIgnoreCase(getFileExtensions(name))) {
					return f.getPath();
				}
			} else {
				hasDaisyOpfFile(f);
			}
		}
		return path;
	}
	
	public static boolean isMusic(String fileName){
		boolean bIsImageFile = false;
		String strEx = getFileExtensions(fileName);
		if(strEx.equalsIgnoreCase("mp3") ||strEx.equalsIgnoreCase("aac") ||strEx.equalsIgnoreCase("wav") ||
				strEx.equalsIgnoreCase("wmv") ||strEx.equalsIgnoreCase("amr") ||strEx.equalsIgnoreCase("ogg") ||strEx.equalsIgnoreCase("mp2")
				||strEx.equalsIgnoreCase("m4a")|| strEx.equalsIgnoreCase("flac") ||strEx.equalsIgnoreCase("MID")){
			bIsImageFile = true;
		}	
		return bIsImageFile;
	}
}
