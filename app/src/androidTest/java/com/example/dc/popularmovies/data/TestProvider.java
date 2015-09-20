package com.example.dc.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.example.dc.popularmovies.data.FilmContract.FilmEntry;
/**
 * Created by DS on 9/19/2015.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                FilmEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                FilmEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Film table during delete", 0, cursor.getCount());
        cursor.close();
    }


    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                FilmProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: FilmProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + FilmContract.CONTENT_AUTHORITY,
                    providerInfo.authority, FilmContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }


    public void testGetType() {
        // content://com.example.dc.popularmovies/film
        String type = mContext.getContentResolver().getType(FilmEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.dc.popularmovies/film
        assertEquals("Error: the FilmEntry CONTENT_URI should return FilmEntry.CONTENT_TYPE",
                FilmEntry.CONTENT_TYPE, type);

        int testFilmId = 111;
        // content://com.example.dc.popularmovies/film/111
        type = mContext.getContentResolver().getType(
                FilmEntry.buildFilmIdUri(testFilmId));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the FilmEntry CONTENT_URI with Film ID should return FilmEntry.CONTENT_ITEM_TYPE",
                FilmEntry.CONTENT_ITEM_TYPE, type);

    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testFilmQuery() {
        // insert our test records into the database
        FilmDbHelper dbHelper = new FilmDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createFilmValues();
        long filmRowId = TestUtilities.insertFilmValues(mContext);
        assertTrue("Unable to Insert FilmEntry into the Database", filmRowId != -1);
        db.close();

        // Test the basic content provider query
        Cursor filmCursor = mContext.getContentResolver().query(
                FilmEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testFilmQuery", filmCursor, testValues);
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    filmCursor.getNotificationUri(), FilmEntry.CONTENT_URI);
        }
    }



    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createFilmValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FilmEntry.CONTENT_URI, true, tco);
        Uri filmUri = mContext.getContentResolver().insert(FilmEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long filmRowId = ContentUris.parseId(filmUri);

        assertTrue(filmRowId != -1);


        Cursor cursor = mContext.getContentResolver().query(
                FilmEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating FilmEntry.",
                cursor, testValues);


    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver filmObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FilmEntry.CONTENT_URI, true, filmObserver);

        deleteAllRecordsFromProvider();

        filmObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(filmObserver);
    }

}
