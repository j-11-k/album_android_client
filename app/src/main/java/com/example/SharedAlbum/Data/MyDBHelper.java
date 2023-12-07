package com.example.SharedAlbum.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
   public MyDBHelper(Context c) {
      super(c, "mydb", null, 1);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE UPLOAD (AID INTEGER NOT NULL,PATH VARCHAR(256) NOT NULL,PID INTEGER,PRIMARY KEY (AID,PATH))");
      db.execSQL("CREATE TABLE DOWNLOAD (PID INTEGER PRIMARY KEY,PATH VARCHAR(256) NOT NULL,UNFINISHED INTEGER NOT NULL)");
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

   }
}
