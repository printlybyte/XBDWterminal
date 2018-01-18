package com.baidu.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {

	// 数据库版本
    private static final int DATABASE_VERSION = 1;

    // 数据库名
    private static final String DATABASE_NAME = "Locationmsg";

    //Contact表名
    private static final String TABLE_CONTACTS = "Locationmsg";

    //Contact表的列名
    private static final String KEY_SQLTIME = "sqltime";
    private static final String KEY_SQLMSG = "sqlmsg";
    
    public DatabaseHelper(Context context, String name, CursorFactory factory,  
            int version) {  
        super(context, name, factory, version);  
          
    }  
    //带两个参数的构造函数，调用的其实是带三个参数的构造函数  
    public DatabaseHelper(Context context,String name){  
        this(context,name,DATABASE_VERSION);  
    }  
    //带三个参数的构造函数，调用的是带所有参数的构造函数  
    public DatabaseHelper(Context context,String name,int version){  
        this(context, name,null,version);  
    }  
    //创建表
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("+ KEY_SQLTIME + " INTEGER PRIMARY KEY," + KEY_SQLMSG + " TEXT"+ ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
	}
	//更新表（构造方法）
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}
