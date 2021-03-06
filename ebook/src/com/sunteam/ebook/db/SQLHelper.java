package com.sunteam.ebook.db;

import com.sunteam.ebook.util.EbookConstants;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
	/**
	 * The name of the apps SQLite database.
	 */
	public static final String DATABASE = "ebook.db";
	private static final int DB_VERSION = 13;
	
	public SQLHelper(Context context) {
		super(context, DATABASE, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			StringBuilder createOrder = new StringBuilder();
			createOrder.append("CREATE TABLE ");
			createOrder.append(EbookConstants.BOOKS_TABLE);
			createOrder.append("(");
			createOrder.append("id");
			createOrder.append(" INTEGER PRIMARY KEY,");
			createOrder.append(EbookConstants.BOOK_NAME);
			createOrder.append(" TEXT,");
			createOrder.append(EbookConstants.BOOK_PATH);
			createOrder.append(" TEXT,");
			createOrder.append(EbookConstants.BOOK_DIASY_PATH);
			createOrder.append(" TEXT,");
			createOrder.append(EbookConstants.BOOK_DIASY_FLAG);
			createOrder.append(" TEXT,");
			createOrder.append(EbookConstants.BOOK_DIASY);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_FOLDER);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_CATALOG);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_FLAG);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_STORAGE);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_PART);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_START);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_LINE);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_LEN);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_CHECKSUM);
			createOrder.append(" INTEGER,");
			createOrder.append(EbookConstants.BOOK_TIME);
			createOrder.append(" TEXT,");
			createOrder.append(EbookConstants.BOOK_TYPE);
			createOrder.append(" INTEGER);");
			db.execSQL(createOrder.toString());
			
			StringBuilder createMark = new StringBuilder();
			createMark.append("CREATE TABLE ");
			createMark.append(EbookConstants.MARKS_TABLE);
			createMark.append("(");
			createMark.append("id");
			createMark.append(" INTEGER PRIMARY KEY,");
			createMark.append(EbookConstants.BOOK_NAME);
			createMark.append(" TEXT,");
			createMark.append(EbookConstants.BOOK_PATH);
			createMark.append(" TEXT,");
			createMark.append(EbookConstants.BOOK_DIASY_PATH);
			createMark.append(" TEXT,");
			createMark.append(EbookConstants.BOOK_PART);
			createMark.append(" INTEGER,");
			createMark.append(EbookConstants.BOOK_LINE);
			createMark.append(" INTEGER,");
			createMark.append(EbookConstants.BOOK_START);
			createMark.append(" INTEGER,");
			createMark.append(EbookConstants.BOOK_LEN);
			createMark.append(" INTEGER,");
			createMark.append(EbookConstants.BOOK_TIME);
			createMark.append(" TEXT);");
			db.execSQL(createMark.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " +EbookConstants.BOOKS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " +EbookConstants.MARKS_TABLE);
		onCreate(db);
	}
}
