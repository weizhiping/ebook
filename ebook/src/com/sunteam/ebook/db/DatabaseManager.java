package com.sunteam.ebook.db;

import java.util.ArrayList;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
	public void insertBookToDb(FileInfo file,int type) {
		boolean hasOrder = hasDataInBase(EbookConstants.BOOKS_TABLE, file.path);
		if (!hasOrder) {
			db = helper.getWritableDatabase();
			ContentValues newValues = new ContentValues();
			newValues.put(EbookConstants.BOOK_NAME, file.name);
			newValues.put(EbookConstants.BOOK_PATH, file.path);
			newValues.put(EbookConstants.BOOK_FOLDER, file.isFolder);
			newValues.put(EbookConstants.BOOK_TYPE, type);
			db.insert(EbookConstants.BOOKS_TABLE, null, newValues);
			db.close();
		}
	}

	// 查询电子书数据
	public ArrayList<FileInfo> queryOrders(int type) {
		db = helper.getWritableDatabase();
		 String sql= "select * from " + EbookConstants.BOOKS_TABLE +  " where type=" + type;  
		 Cursor cursor = db.rawQuery(sql, null);
		ArrayList<FileInfo> orderList = null;
		try { 
			if (null != cursor) {
				if (cursor.getCount() > 0) {
					orderList = new ArrayList<FileInfo>();
					while (cursor.moveToNext()) {
						FileInfo book = new FileInfo();
						book.name = cursor.getString(cursor
								.getColumnIndex(EbookConstants.BOOK_NAME));
						book.path = cursor.getString(cursor
								.getColumnIndex(EbookConstants.BOOK_PATH));
						int folder = cursor.getInt(cursor
								.getColumnIndex(EbookConstants.BOOK_FOLDER));
						if(0 == folder){
							book.isFolder = false;
						}else{
							book.isFolder = true;
						}
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

	// 查找数据库中是否已经存在某一条数据
	private boolean hasDataInBase(String table, String path) {
		Cursor cursor = null;
		db = helper.getWritableDatabase();
		cursor = db.query(table, null, "path=?", new String[] { path }, null,
				null, null);
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
	//删除数据,Path为null表示删除所有数据
	public void deleteFile(String table,String path,int flag){
		db = helper.getWritableDatabase();
		if(null != path){
			db.delete(table, "path=? and type=?", new String[] { path,String.valueOf(flag)});
		}else{
			Cursor cursor = db.query(table, null, "type=?", new String[] {String.valueOf(flag)}, null,
					null, null);
			if (null != cursor) {
				while (cursor.moveToNext()) {
					int type = cursor.getInt(cursor
								.getColumnIndex(EbookConstants.BOOK_TYPE));
					db.delete(table, "type=?", new String[] {String.valueOf(type)});
				}
				cursor.close();
			}
		}
		
		db.close();
		
	}
	//删除所有相关数据
	public void deleteAllFile(String table,int flag){
		Cursor cursor = null;
		db = helper.getWritableDatabase();
		cursor = db.query(table, null, "type=?", new String[] {String.valueOf(flag)}, null,
				null, null);
		if (null != cursor) {
			
			cursor.close();
		}
		db.close();
		
	}
	// 数据库更新数据
//	public void updateToDb(String name) {
//		db = helper.getWritableDatabase();
//		ContentValues newValues = new ContentValues();
//		newValues.put(SQLHelper.TABLE_TIME, System.currentTimeMillis());
//		db.update(SQLHelper.SHOP_TABLE, newValues, SQLHelper.TABLE_NAME + "=?",
//				new String[] { name });
//		db.close();
//	}
}
