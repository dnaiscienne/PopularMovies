package com.example.dc.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
 * A placeholder fragment containing a simple view.
 */
public class PosterFragment extends Fragment {

    private PosterAdapter mPosterAdapter;


    public PosterFragment() {
    }

    public void loadFilms(){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = pref.getString("order", "");
        if(isNetworkAvailable()) {
            FetchFilmsTask filmsTask = new FetchFilmsTask();
            filmsTask.execute(sortOrder);
        }else{
            Toast.makeText(getActivity(), "No Network Connection Available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFilms();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPosterAdapter = new PosterAdapter(
                getActivity(),
                new ArrayList<Film>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mPosterAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                  Film film = mPosterAdapter.getItem(position);
                  Bundle b = new Bundle();
                  b.putParcelable("film", film);
                  Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra("bundle", b);
                  startActivity(intent);
            }
        });
        return rootView;
    }

    public class FetchFilmsTask extends AsyncTask<String, Void, List<Film>>{

        private final String LOG_TAG = FetchFilmsTask.class.getSimpleName();

        @Override
        protected List<Film> doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String filmsJsonStr = null;
            final String API_KEY= Constants.API_KEY;

            try{
                final String MDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String API_KEY_PARAM = "api_key";
                final String SORT_ORDER_PARAM = "sort_by";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_ORDER_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, API_KEY )
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v("URL", builtUri.toString());
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

                filmsJsonStr = buffer.toString();
                Log.v("FilmJSON", filmsJsonStr);
            }catch(IOException e){
                Log.e(LOG_TAG, "Error", e);

                return null;
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e("PosterFragment", "Error closing stream", e);
                    }
                }
            }

            try{
                return getFilmsFromJson(filmsJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        private List<Film> getFilmsFromJson(String filmsJsonStr)
                throws JSONException{

            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_OVERVIEW = "overview";
            final String MDB_VOTE_AVG = "vote_average";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_POSTER = "poster_path";
            final String MDB_ID = "id";

            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185";


            JSONObject filmsJson = new JSONObject(filmsJsonStr);
            JSONArray filmArray = filmsJson.getJSONArray(MDB_RESULTS);


            List<Film> filmList = new ArrayList<Film>();

            for(int i =0; i < filmArray.length(); i++){
                JSONObject filmJson = filmArray.getJSONObject(i);
                String posterUrl = POSTER_BASE_URL + filmJson.getString(MDB_POSTER);
                Log.v("Image URL", posterUrl);
                filmList.add(new Film(
                        checkDetailAvailability(filmJson.getString(MDB_ID)),
                        checkDetailAvailability(filmJson.getString(MDB_TITLE)),
                        checkDetailAvailability(filmJson.getString(MDB_OVERVIEW)),
                        checkDetailAvailability(filmJson.getString(MDB_VOTE_AVG)),
                        checkDetailAvailability(filmJson.getString(MDB_RELEASE_DATE)),
                        posterUrl
                ));
            }
            return filmList;
        }

        private String checkDetailAvailability(String filmDetail){

            if (filmDetail.equals("null")){
                filmDetail = "Not Available";
            }
            return filmDetail;
        }

        @Override
        protected void onPostExecute(List<Film> filmList) {
            if(filmList != null){
                mPosterAdapter.clear();
                mPosterAdapter.addAll(filmList);
//                for (Film film : filmList){
//                    mPosterAdapter.add(film);
//                }
            }

        }
    }



}
