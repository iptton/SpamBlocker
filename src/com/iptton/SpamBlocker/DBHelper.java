package com.iptton.SpamBlocker;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper{
	public static final String TB_NAME = "blockList";
	public static final String TB_SPAM_NAME = "spam";
	public static final String SENDER = "sender";
	public static final String MESSAGE_BODY = "message_body";
	public static final String ID = "_id";
	public static final String NUMBER = "phone_number";
	public static final int VERSION = 11;
	public final static String DB_NAME = "smsblocker.db";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TB_NAME +" (" +
				ID + " INT PRIMARY KEY," +
				NUMBER + " VARCHAR);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TB_SPAM_NAME +" (" +
				ID + " INT PRIMARY KEY," +
				SENDER + " VARCHAR," + 
				MESSAGE_BODY + " VARCHAR," +
				NUMBER + " VARCHAR);");
		db.execSQL("PRAGMA foreign_keys = ON;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO backup old data and upgrade
		db.execSQL("DROP TABLE IF EXISTS "+TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+TB_SPAM_NAME);
		onCreate(db);
	}

    public String[] getNumbers(SQLiteDatabase db){
        Cursor c = db.query(DBHelper.TB_NAME, null, null, null, null, null, null);
        final int ci = c.getColumnIndexOrThrow(DBHelper.NUMBER);
        int l = c.getCount();
        String[] numbers = new String[l];
        int i=0;
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
        	String num = c.getString(ci);
        	numbers[i] = num;
        	i++;
        }
        return numbers;
    }
    public void saveSpamMessage(SQLiteDatabase db,String sender,String msg){
    	ContentValues values = new ContentValues();
        values.put(DBHelper.SENDER,sender);
        values.put(MESSAGE_BODY, msg);
        db.insertWithOnConflict(DBHelper.TB_SPAM_NAME, DBHelper.ID, values,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public Cursor getSpamMessage(SQLiteDatabase db){
        Cursor c = db.query(DBHelper.TB_SPAM_NAME, null, null, null, null, null, null);
        return c;
    }
	
}

