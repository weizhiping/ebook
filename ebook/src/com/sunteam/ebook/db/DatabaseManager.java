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
	private SQLHelper helper;
	private SQLiteDatabase db;

	public DatabaseManager(Context c) {
		this.context = c;
		helper = new SQLHelper(context);
	}

	// 收藏和最近浏览数据库插入数据
	public boolean insertBookToDb(FileInfo file, int type) {
		boolean hasbook = hasDataInBase(EbookConstants.BOOKS_TABLE, file.path,
				type);
		Log.e("database", "-----------has book---:" + hasbook);
		if (!hasbook) {
			db = helper.getWritableDatabase();
			ContentValues newValues = new ContentValues();
			newValues.put(EbookConstants.BOOK_NAME, file.name);
			newValues.put(EbookConstants.BOOK_PATH, file.path);
			newValues.put(EbookConstants.BOOK_DIASY_PATH, file.diasyPath);
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
			updateToDb(file.path, file.flag, type);
		}
		return hasbook;
	}

	// 查询电子书数据
	public ArrayList<FileInfo> querybooks(int type, int catalog) {
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

	// 查询电子书数据最后一条
	public FileInfo queryLastBook(int type) {
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

	// 查找数据库中是否已经存在某一条数据
	private boolean hasDataInBase(String table, String path, int type) {
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
		return false;
	}

	// 获取分页数据。
	public Cursor getRawScrollData(int startResult, int maxResult) {
		db = helper.getReadableDatabase();
		return db.rawQuery("select * from person limit ?,?", new String[] {
				String.valueOf(startResult), String.valueOf(maxResult) });

	}

	// 删除数据,Path为null表示删除所有数据
	public void deleteFile(String path, int flag) {
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

	// 删除所有相关数据
	public void deleteAllFile(String table, int flag) {
		Cursor cursor = null;
		db = helper.getWritableDatabase();
		cursor = db.query(table, null, "type=?",
				new String[] { String.valueOf(flag) }, null, null, null);
		if (null != cursor) {

			cursor.close();
		}
		db.close();
	}

	// 数据库更新数据
	public void updateToDb(String path, int flag, int type) {
		db = helper.getWritableDatabase();
		ContentValues newValues = new ContentValues();
		newValues.put(EbookConstants.BOOK_TIME, System.currentTimeMillis());
		newValues.put(EbookConstants.BOOK_FLAG, flag);
		db.update(EbookConstants.BOOKS_TABLE, newValues,
				EbookConstants.BOOK_PATH + "=?", new String[] { path });
		db.close();
	}

	// 书签插入数据
	public boolean insertMarkToDb(FileInfo file) {
		boolean hasbook = hasMarkInBase(EbookConstants.MARKS_TABLE, file.path,
				file.name);
		Log.i("database", "-----------has mark---:" + hasbook);
		if (!hasbook) {
			db = helper.getWritableDatabase();
			ContentValues newValues = new ContentValues();
			newValues.put(EbookConstants.BOOK_NAME, file.name);
			newValues.put(EbookConstants.BOOK_PATH, file.path);
			newValues.put(EbookConstants.BOOK_LINE, file.line);
			newValues.put(EbookConstants.BOOK_START, file.startPos);
			newValues.put(EbookConstants.BOOK_LEN, file.len);
			newValues.put(EbookConstants.BOOK_TIME, System.currentTimeMillis());
			db.insert(EbookConstants.MARKS_TABLE, null, newValues);
			db.close();
		} else {
			// updateToDb(file.path,file.flag,type);
		}
		return hasbook;
	}

	// 查找数据库中是否已经存在某一条数据
	private boolean hasMarkInBase(String table, String path, String name) {
		Cursor cursor = null;
		db = helper.getWritableDatabase();
		cursor = db.query(table, null, "path=? and name=?", new String[] {
				path, name }, null, null, null);
		int count = 0;
		if (null != cursor) {
			count = cursor.getCount();
			cursor.close();
		}
		db.close();
		if (count != 0) {
			return true;
		}
		return false;
	}

	// 删除书签数据,Path为null表示删除所有数据
	public void deleteMarkFile(String path, String name) {
		db = helper.getWritableDatabase();
		if (null != name) {
			db.delete(EbookConstants.MARKS_TABLE, "path=? and name=?",
					new String[] { path, name });
		} else {
			Cursor cursor = db.query(EbookConstants.MARKS_TABLE, null,
					"path=?", new String[] { path }, null, null, null);
			if (null != cursor) {
				while (cursor.moveToNext()) {
					db.delete(EbookConstants.MARKS_TABLE, "path=?",
							new String[] { path });
				}
				cursor.close();
			}
		}
		db.close();
	}

	// 查询书签数据
	public ArrayList<FileInfo> queryMarks(String path) {
		db = helper.getWritableDatabase();
		Cursor cursor = db.query(EbookConstants.MARKS_TABLE, null, "path=?",
				new String[] { path }, null, null, "time desc");
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
}
