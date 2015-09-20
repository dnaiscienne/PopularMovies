package com.example.dc.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by DS on 9/19/2015.
 */
public class TestFilmContract extends AndroidTestCase {

    private static final String TEST_FILM = "/111";
    private static final int TEST_FILM_ID = 111;

    public void testBuildFilm() {
        Uri filmUri = FilmContract.FilmEntry.buildFilmIdUri(TEST_FILM_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFilmIdUri in " +
                        "FilmContract.",
                filmUri);
//        assertEquals("Error: Film ID not properly appended to the end of the Uri",
//                TEST_FILM, filmUri.getLastPathSegment());
        assertEquals("Error: Film Uri doesn't match our expected result",
                filmUri.toString(),
                "content://com.example.dc.popularmovies/film/111");
    }
}
