package com.example.dc.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DS on 9/18/2015.
 */
public class FetchDetailsTask extends AsyncTask<Film, Void, Film> {

    public interface OnTaskCompleted {
        void onFetchDetailsTaskCompleted(Film film);
    }

    private final String LOG_TAG = FetchDetailsTask.class.getSimpleName();
    private OnTaskCompleted listener;

    public FetchDetailsTask(OnTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected Film doInBackground(Film... params) {

        if(params.length == 0){
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String filmJsonStr = null;
        final String API_KEY= Constants.API_KEY;
        Film film = params[0];
        String appendedParams = "trailers,reviews";

        try{
            final String MDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY_PARAM = "api_key";
            final String APPEND_PARAM = "append_to_response";

            Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                    .appendPath(film.mFilmId)
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .appendQueryParameter(APPEND_PARAM, appendedParams)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v("Detail URL", builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                return null;
            }

            filmJsonStr = buffer.toString();
            Log.v("Detail JSON", filmJsonStr);
        }catch (IOException e){

            Log.e(LOG_TAG, "Error", e);
            return null;

        }finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("DetailActivityFragment", "Error closing stream", e);
                }
            }

        }

        try{
            return getFilmDetailFromJson(film ,filmJsonStr);
        }catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private Film getFilmDetailFromJson(Film film, String filmJsonStr)
            throws  JSONException{

        final String MDB_TRAILERS = "trailers";
        final String MDB_YOUTUBE = "youtube";
        final String MDB_TRAILER_NAME = "name";
        final String MDB_TRAILER_SOURCE = "source";

        final String MDB_REVIEWS = "reviews";
        final String MDB_REVIEWS_RESULTS = "results";
        final String MDB_REVIEW_ID = "id";
        final String MDB_REVIEW_AUTHOR = "author";
        final String MDB_REVIEW_CONTENT = "content";


        JSONObject filmJson = new JSONObject(filmJsonStr);
        JSONObject filmTrailersJson = filmJson.getJSONObject(MDB_TRAILERS);
        JSONArray youtubeTrailersJson = filmTrailersJson.getJSONArray(MDB_YOUTUBE);
        List<Trailer> trailers = new ArrayList<>();
        for(int i = 0; i < youtubeTrailersJson.length(); i++){
            String name = youtubeTrailersJson.getJSONObject(i).getString(MDB_TRAILER_NAME);
            String source = youtubeTrailersJson.getJSONObject(i).getString(MDB_TRAILER_SOURCE);
            trailers.add(new Trailer(name, source));
        }
        Log.v("Trailer count", Integer.toString(trailers.size()));
        film.mTrailers = trailers;

        JSONObject filmReviewsJson = filmJson.getJSONObject(MDB_REVIEWS);
        JSONArray filmReviewsResultsJson = filmReviewsJson.getJSONArray(MDB_REVIEWS_RESULTS);
        List<Review> reviews = new ArrayList<>();
        for(int i = 0; i < filmReviewsResultsJson.length(); i++){
            String id = filmReviewsResultsJson.getJSONObject(i).getString(MDB_REVIEW_ID);
            String author = filmReviewsResultsJson.getJSONObject(i).getString(MDB_REVIEW_AUTHOR);
            String content = filmReviewsResultsJson.getJSONObject(i).getString(MDB_REVIEW_CONTENT);
            reviews.add(new Review(id, author, content));
        }
        Log.v("Review count", Integer.toString(reviews.size()));
        film.mReviews = reviews;

        return film;
    }


    @Override
    protected void onPostExecute(Film film){
        if (film != null){
            listener.onFetchDetailsTaskCompleted(film);
        }
    }
}