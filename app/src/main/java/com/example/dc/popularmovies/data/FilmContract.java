package com.example.dc.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by DS on 9/19/2015.
 */
public class FilmContract {

    public static final String CONTENT_AUTHORITY = "com.example.dc.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FILM = "film";

    public static final class FilmEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FILM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILM;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILM;

        public static final String TABLE_NAME = "film";

        public static final String COLUMN_FILM_ID = "film_id";
        public static final String COLUMN_FILM_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static Uri buildFilmUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFilmIdUri(int filmId){
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(filmId)).build();
        }

        public static int getFilmIdFromUri(Uri uri){
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

    }


}
