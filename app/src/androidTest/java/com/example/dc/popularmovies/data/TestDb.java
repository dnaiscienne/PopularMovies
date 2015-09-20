package com.example.dc.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by DS on 9/19/2015.
 */
public class TestDb extends AndroidTestCase{
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(FilmDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(FilmContract.FilmEntry.TABLE_NAME);

        mContext.deleteDatabase(FilmDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new FilmDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );


        c = db.rawQuery("PRAGMA table_info(" + FilmContract.FilmEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> filmColumnHashSet = new HashSet<String>();
        filmColumnHashSet.add(FilmContract.FilmEntry._ID);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_FILM_ID);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_FILM_TITLE);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_OVERVIEW);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_RATING);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_RELEASE);
        filmColumnHashSet.add(FilmContract.FilmEntry.COLUMN_POSTER_PATH);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            Log.d("column", columnName);
            filmColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required film entry columns",
                filmColumnHashSet.isEmpty());
        db.close();
    }

    public void testFilmTable(){
        FilmDbHelper dbHelper = new FilmDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createFilmValues();

        long filmRowId;
        filmRowId = db.insert(FilmContract.FilmEntry.TABLE_NAME, null, testValues);

        assertTrue(filmRowId != -1);

        Cursor cursor = db.query(
                FilmContract.FilmEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue( "Error: No Records returned from film query", cursor.moveToFirst() );

        TestUtilities.validateCurrentRecord("Error: Film Query Validation Failed",
                cursor, testValues);

        assertFalse( "Error: More than one record returned from film query",
                cursor.moveToNext() );

        cursor.close();
        db.close();
    }

}
