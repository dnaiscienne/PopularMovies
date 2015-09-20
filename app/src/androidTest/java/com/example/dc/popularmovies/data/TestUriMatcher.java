package com.example.dc.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by DS on 9/19/2015.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final int FILM_QUERY = 111;

    // content://com.example.dc.popularmovies/film"
    private static final Uri TEST_FILM_DIR = FilmContract.FilmEntry.CONTENT_URI;
    // content://com.example.dc.popularmovies/film/111"
    private static final Uri TEST_FILM_WITH_FILM_ID = FilmContract.FilmEntry.buildFilmIdUri(FILM_QUERY);

    public void testUriMatcher() {
        UriMatcher testMatcher = FilmProvider.buildUriMatcher();

        assertEquals("Error: The Film URI was matched incorrectly.",
                testMatcher.match(TEST_FILM_DIR), FilmProvider.FAVORITE_FILMS);
        assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_FILM_WITH_FILM_ID), FilmProvider.FAVORITE_FILM_BY_ID);
    }
}
