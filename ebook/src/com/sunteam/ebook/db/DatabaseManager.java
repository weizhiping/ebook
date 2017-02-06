package com.sunteam.ebook.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;

/**
 * 数据库操作类
 * 
 * @author SYLAR
 * 
 */
public class DatabaseManager {
	private Context context;
	private SQLHelper helper;//系统数据库帮助类
	private SQLiteDatabase db; //数据库操作类

	public DatabaseManager(Context c) {
		this.context = c;
		helper = new SQLHelper(context);
	}

	// 收藏和最近浏览数据库插入数据  1为收藏，2为最近
	public boolean insertBookToDb(FileInfo file, int type) {
		try
		{
			boolean hasbook = hasDataInBase(EbookConstants.BOOKS_TABLE, file.path,
					type);
			Log.e("database", "-----------has book---:" + hasbook + "--type-:" + type);
			if (!hasbook) {
				db = helper.getWritableDatabase();
				ContentValues newValues = new ContentValues();
				newValues.put(EbookConstants.BOOK_NAME, file.name);
				newValues.put(EbookConstants.BOOK_PATH, file.path);
				newValues.put(EbookConstants.BOOK_DIASY_PATH, file.diasyPath);
				newValues.put(EbookConstants.BOOK_DIASY_FLAG, file.diasyFlag);
				newValues.put(EbookConstants.BOOK_DIASY, file.hasDaisy);
				newValues.put(EbookConstants.BOOK_FOLDER, file.isFolder);
				newValues.put(EbookConstants.BOOK_CATALOG, file.catalog);
				newValues.put(EbookConstants.BOOK_FLAG, file.flag);
				newValues.put(EbookConstants.BOOK_STORAGE, file.storage);
				newValues.put(EbookConstants.BOOK_PART, file.part);
				newValues.put(EbookConstants.BOOK_START, file.startPos);
				newValues.put(EbookConstants.BOOK_LINE, file.line);
				newValues.put(EbookConstants.BOOK_LEN, file.len);
				newValues.put(EbookConstants.BOOK_CHECKSUM, file.checksum);
				newValues.put(EbookConstants.BOOK_TIME, System.currentTimeMillis());
				newValues.put(EbookConstants.BOOK_TYPE, type);
				db.insert(EbookConstants.BOOKS_TABLE, null, newValues);
				db.close();
			} else {
				updateToDb(file, type);
			}
			return hasbook;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	false;
		}
	}

	// 查询电子书数据
	public ArrayList<FileInfo> querybooks(int type, int catalog) {
		try
		{
			db = helper.getWritableDatabase();
			String sql = "select * from " + EbookConstants.BOOKS_TABLE
					+ " where type=" + type + " and catalog=" + catalog
					+ " order by " + EbookConstants.BOOK_TIME + " desc";
			Cursor cursor = db.rawQuery(sql, null);
			ArrayList<FileInfo> orderList = new ArrayList<FileInfo>();
			try {
				if (null != cursor) {
					if (cursor.getCount() > 0) {
						while (cursor.moveToNext()) {
							FileInfo book = new FileInfo();
							book.name = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_NAME));
							book.path = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_PATH));
							book.diasyPath = cursor
									.getString(cursor
											.getColumnIndex(EbookConstants.BOOK_DIASY_PATH));
							book.diasyFlag = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_DIASY_FLAG));
							book.hasDaisy = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_DIASY));
							
							int folder = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_FOLDER));
							if (0 == folder) {
								book.isFolder = false;
							} else {
								book.isFolder = true;
							}
							book.catalog = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_CATALOG));
							book.flag = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_FLAG));
							book.storage = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_STORAGE));
							book.part = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_PART));
							book.startPos = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_START));
							book.line = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LINE));
							book.len = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LEN));
							book.checksum = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_CHECKSUM));
							orderList.add(book);
						}
					}
				}
			} finally {
				if (null != cursor) {
					cursor.close();
				}
				if (null != db) {
					db.close();
				}
			}
			return orderList;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	null;
		}
	}

	// 查询电子书数据最后一条
	public FileInfo queryLastBook(int type) {
		try
		{
			db = helper.getWritableDatabase();
			String sql = "select * from " + EbookConstants.BOOKS_TABLE
					+ " where type=" + type + " order by "
					+ EbookConstants.BOOK_TIME + " desc";
			Cursor cursor = db.rawQuery(sql, null);
			FileInfo book = null;
			try {
				if (null != cursor) {
					if (cursor.getCount() > 0) {
						book = new FileInfo();
						if (cursor.moveToFirst()) {
							book.name = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_NAME));
							book.path = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_PATH));
							book.diasyPath = cursor
									.getString(cursor
											.getColumnIndex(EbookConstants.BOOK_DIASY_PATH));
							book.diasyFlag = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_DIASY_FLAG));
							book.hasDaisy = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_DIASY));
							int folder = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_FOLDER));
							if (0 == folder) {
								book.isFolder = false;
							} else {
								book.isFolder = true;
							}
							book.catalog = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_CATALOG));
							book.flag = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_FLAG));
							book.storage = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_STORAGE));
							book.part = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_PART));
							book.startPos = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_START));
							book.line = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LINE));
							book.len = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LEN));
							book.checksum = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_CHECKSUM));
						}
					}
				}
			} finally {
				if (null != cursor) {
					cursor.close();
				}
				if (null != db) {
					db.close();
				}
			}
			return book;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	null;
		}
	}

	// 更新电子书断点数据
		public void updateQueryBook(FileInfo info,int part) {
			try{
				db = helper.getWritableDatabase();
				Cursor cursor;
				if(-1 == part){
					cursor = db.query(EbookConstants.BOOKS_TABLE, null, "path=? and type=?", new String[] {
							info.path, String.valueOf(EbookConstants.BOOK_RECENT) }, null, null, null);
				}else{
					cursor = db.query(EbookConstants.BOOKS_TABLE, null, "path=? and type=? and part=?", new String[] {
							info.path, String.valueOf(EbookConstants.BOOK_RECENT),String.valueOf(info.part) }, null, null, null);
				}
				
				try {
					if (null != cursor) {
						if (cursor.getCount() > 0) {
							cursor.moveToPosition(0);
								info.startPos = cursor.getInt(cursor
										.getColumnIndex(EbookConstants.BOOK_START));
								info.line = cursor.getInt(cursor
										.getColumnIndex(EbookConstants.BOOK_LINE));
								info.len = cursor.getInt(cursor
										.getColumnIndex(EbookConstants.BOOK_LEN));
								info.checksum = cursor.getInt(cursor
										.getColumnIndex(EbookConstants.BOOK_CHECKSUM));
								info.part = cursor.getInt(cursor
										.getColumnIndex(EbookConstants.BOOK_PART));
								info.diasyFlag = cursor.getString(cursor
										.getColumnIndex(EbookConstants.BOOK_DIASY_FLAG));
						}else{
							info.startPos = 0;
							info.line = 0;
							info.len = 0;
							info.checksum = 0;
						}
					}
				} finally {
					if (null != cursor) {
						cursor.close();
					}
					if (null != db) {
						db.close();
					}
				}
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
	// 查找数据库中是否已经存在某一条数据
	private boolean hasDataInBase(String table, String path, int type) {
		try
		{
			Cursor cursor = null;
			db = helper.getWritableDatabase();
			cursor = db.query(table, null, "path=? and type=?", new String[] {
					path, String.valueOf(type) }, null, null, null);
			int count = 0;
			if (null != cursor) {
				count = cursor.getCount();
				cursor.close();
			}
			db.close();
			if (count != 0) {
				return true;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return false;
	}

	// 获取分页数据。
	public Cursor getRawScrollData(int startResult, int maxResult) {
		try
		{
			db = helper.getReadableDatabase();
			return db.rawQuery("select * from person limit ?,?", new String[] {
					String.valueOf(startResult), String.valueOf(maxResult) });
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	null;
		}

	}

	// 删除数据,Path为null表示删除所有数据
	public void deleteFile(String path, int flag) {
		try
		{
			db = helper.getWritableDatabase();
			if (null != path) {
				db.delete(EbookConstants.BOOKS_TABLE, "path=? and type=?",
						new String[] { path, String.valueOf(flag) });
			} else {
				Cursor cursor = db.query(EbookConstants.BOOKS_TABLE, null,
						"type=?", new String[] { String.valueOf(flag) }, null,
						null, null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						// int type = cursor.getInt(cursor
						// .getColumnIndex(EbookConstants.BOOK_TYPE));
						db.delete(EbookConstants.BOOKS_TABLE, "type=?",
								new String[] { String.valueOf(flag) });
					}
					cursor.close();
				}
			}
			db.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 删除所有相关数据
	public void deleteAllFile(String table, int flag) {
		try
		{
			Cursor cursor = null;
			db = helper.getWritableDatabase();
			cursor = db.query(table, null, "type=?",
					new String[] { String.valueOf(flag) }, null, null, null);
			if (null != cursor) {
	
				cursor.close();
			}
			db.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 数据库更新数据
	public void updateToDb(FileInfo file, int type) {
		try
		{
			db = helper.getWritableDatabase();
			ContentValues newValues = new ContentValues();
			newValues.put(EbookConstants.BOOK_TIME, System.currentTimeMillis());
			newValues.put(EbookConstants.BOOK_DIASY_FLAG, file.diasyFlag);
			newValues.put(EbookConstants.BOOK_FLAG, file.flag);
			newValues.put(EbookConstants.BOOK_START, file.startPos);
			newValues.put(EbookConstants.BOOK_LINE, file.line);
			newValues.put(EbookConstants.BOOK_LEN, file.len);
			newValues.put(EbookConstants.BOOK_PART, file.part);
			newValues.put(EbookConstants.BOOK_CHECKSUM, file.checksum);
			db.update(EbookConstants.BOOKS_TABLE, newValues,
				EbookConstants.BOOK_PATH + "=? and " + 
				EbookConstants.BOOK_TYPE + "=?", new String[] { file.path,String.valueOf(type) });
			
			newValues.clear();
			newValues.put(EbookConstants.BOOK_FLAG, file.flag);
			newValues.put(EbookConstants.BOOK_START, file.startPos);
			newValues.put(EbookConstants.BOOK_LINE, file.line);
			newValues.put(EbookConstants.BOOK_LEN, file.len);
			newValues.put(EbookConstants.BOOK_PART, file.part);
			newValues.put(EbookConstants.BOOK_CHECKSUM, file.checksum);
			db.update(EbookConstants.BOOKS_TABLE, newValues,
				EbookConstants.BOOK_PATH + "=? and " + 
				EbookConstants.BOOK_TYPE + "=?", new String[] { file.path,"1" });
			
			db.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 书签插入数据
	public boolean insertMarkToDb(FileInfo file) {
		try
		{
			boolean hasbook = hasMarkInBase(EbookConstants.MARKS_TABLE, file);
			Log.i("database", "-----------has mark---:" + hasbook);
			if (!hasbook) {
				db = helper.getWritableDatabase();
				ContentValues newValues = new ContentValues();
				newValues.put(EbookConstants.BOOK_NAME, file.name);
				newValues.put(EbookConstants.BOOK_PATH, file.path);
				newValues.put(EbookConstants.BOOK_LINE, file.line);
				newValues.put(EbookConstants.BOOK_START, file.startPos);
				newValues.put(EbookConstants.BOOK_LEN, file.len);
				newValues.put(EbookConstants.BOOK_PART, file.part);
				newValues.put(EbookConstants.BOOK_TIME, System.currentTimeMillis());
				db.insert(EbookConstants.MARKS_TABLE, null, newValues);
				db.close();
			} else {
				// updateToDb(file.path,file.flag,type);
			}
			return hasbook;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	false;
		}
	}

	// 查找数据库中是否已经存在某一条数据
	private boolean hasMarkInBase(String table, FileInfo file) {
		try
		{
			Cursor cursor = null;
			db = helper.getWritableDatabase();
			cursor = db.query(table, null, "path=? and name=? and part=?", new String[] {
					file.path, file.name,String.valueOf(file.part) }, null, null, null);
			int count = 0;
			if (null != cursor) {
				count = cursor.getCount();
				cursor.close();
			}
			db.close();
			if (count != 0) {
				return true;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return false;
	}

	// 删除书签数据,Path为null表示删除所有数据
	public void deleteMarkFile(FileInfo file,boolean isAll) {
		try
		{
			db = helper.getWritableDatabase();
			if (!isAll) {
				db.delete(EbookConstants.MARKS_TABLE, "path=? and name=? and part=?",
						new String[] { file.path, file.name,String.valueOf(file.part) });
			} else {
				Cursor cursor = db.query(EbookConstants.MARKS_TABLE, null,
						"path=?", new String[] { file.path }, null, null, null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						db.delete(EbookConstants.MARKS_TABLE, "path=?",
								new String[] { file.path });
					}
					cursor.close();
				}
			}
			db.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 查询书签数据
	public ArrayList<FileInfo> queryMarks(FileInfo file) {
		try
		{
			db = helper.getWritableDatabase();
			Cursor cursor = null;
			if( file.isDaisy )
			{
				cursor = db.query(EbookConstants.MARKS_TABLE, null, "path=?",
						new String[] { file.path }, null, null, "time desc");
			}
			else
			{
				cursor = db.query(EbookConstants.MARKS_TABLE, null, "path=? and part=?",
						new String[] { file.path,String.valueOf(file.part) }, null, null, "time desc");
			}
			ArrayList<FileInfo> orderList = new ArrayList<FileInfo>();
			try {
				if (null != cursor) {
					if (cursor.getCount() > 0) {
						while (cursor.moveToNext()) {
							FileInfo book = new FileInfo();
							book.name = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_NAME));
							book.path = cursor.getString(cursor
									.getColumnIndex(EbookConstants.BOOK_PATH));
							book.line = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LINE));
							book.startPos = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_START));
							book.len = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_LEN));
							book.part = cursor.getInt(cursor
									.getColumnIndex(EbookConstants.BOOK_PART));
							orderList.add(book);
						}
					}
				}
			} finally {
				if (null != cursor) {
					cursor.close();
				}
				if (null != db) {
					db.close();
				}
			}
			return orderList;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			return	null;
		}
	}
}
