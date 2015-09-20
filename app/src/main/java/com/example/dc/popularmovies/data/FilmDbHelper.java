package com.example.dc.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dc.popularmovies.data.FilmContract.FilmEntry;
/**
 * Created by DS on 9/19/2015.
 */
public class FilmDbHelper extends SQLiteOpenHelper {

    private static final int DATBASE_VERSION = 2;

    static final String DATABASE_NAME = "film.db";

    public FilmDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATBASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_FILM_TABLE = "CREATE TABLE " + FilmEntry.TABLE_NAME + " (" +
                FilmEntry._ID + " INTEGER PRIMARY KEY," +
                FilmEntry.COLUMN_FILM_ID + " INTEGER UNIQUE NOT NULL, " +
                FilmEntry.COLUMN_FILM_TITLE + " TEXT NOT NULL, " +
                FilmEntry.COLUMN_OVERVIEW + " TEXT, " +
                FilmEntry.COLUMN_RATING + " REAL, " +
                FilmEntry.COLUMN_RELEASE + " INTEGER, " +
                FilmEntry.COLUMN_POSTER_PATH + " TEXT" +
                " );";
        db.execSQL(SQL_CREATE_FILM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FilmEntry.TABLE_NAME);
    }
}
