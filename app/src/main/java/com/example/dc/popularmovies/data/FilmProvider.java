package com.example.dc.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by DS on 9/19/2015.
 */
public class FilmProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FilmDbHelper mOpenHelper;

    static final int FAVORITE_FILMS = 100;
    static final int FAVORITE_FILM_BY_ID = 101;

    private static final SQLiteQueryBuilder sFavoriteFilmsQueryBuilder;

    static{
        sFavoriteFilmsQueryBuilder = new SQLiteQueryBuilder();
        sFavoriteFilmsQueryBuilder.setTables(FilmContract.FilmEntry.TABLE_NAME);
    }

    private static final String sFavoriteFilmSelection =
            FilmContract.FilmEntry.TABLE_NAME +
                    "." + FilmContract.FilmEntry.COLUMN_FILM_ID + " = ? ";

    private Cursor getFavoriteFilms(Uri uri, String[] projection, String sortOrder){
        return sFavoriteFilmsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getFavoriteFilmByFilmId(Uri uri, String[] projection, String sortOrder){
        int filmId = FilmContract.FilmEntry.getFilmIdFromUri(uri);
        String[] selectionArgs;
        selectionArgs = new String[]{Integer.toString(filmId)};
        return sFavoriteFilmsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sFavoriteFilmSelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FilmContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FilmContract.PATH_FILM, FAVORITE_FILMS);
        matcher.addURI(authority,FilmContract.PATH_FILM + "/*", FAVORITE_FILM_BY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FilmDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITE_FILMS:
                return FilmContract.FilmEntry.CONTENT_TYPE;
            case FAVORITE_FILM_BY_ID:
                return FilmContract.FilmEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)){
            case FAVORITE_FILMS:
            {
                retCursor = getFavoriteFilms(uri, projection, sortOrder);
                break;
            }
            case FAVORITE_FILM_BY_ID:
            {
                retCursor = getFavoriteFilmByFilmId(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case FAVORITE_FILMS: {
                long _id = db.insert(FilmContract.FilmEntry.TABLE_NAME, null, values);
                if(_id > 0 )
                    returnUri = FilmContract.FilmEntry.buildFilmUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if(selection == null) selection = "1";
        switch (match){
            case FAVORITE_FILMS:
                rowsDeleted = db.delete(
                        FilmContract.FilmEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        if(rowsDeleted !=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case FAVORITE_FILMS:
                rowsUpdated = db.update(
                        FilmContract.FilmEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        if(rowsUpdated !=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
