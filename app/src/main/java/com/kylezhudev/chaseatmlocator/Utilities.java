package com.kylezhudev.chaseatmlocator;


import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utilities {
    private static final String LOG_TAG = Utilities.class.getSimpleName();
    private static final String CHASE_LOCATION_URL = "https://m.chase.com/PSRWeb/location/list.action";
    private static final String LAT_KEY = "lat";
    private static final String LNG_KEY = "lng";
    private static final String LOCATION = "locations";

    //create url for network request
    public static URL builtLocationResultUrl(String lat, String lng) throws MalformedURLException {
        Uri builtUri = Uri.parse(CHASE_LOCATION_URL)
                .buildUpon()
                .appendQueryParameter(LAT_KEY, lat)
                .appendQueryParameter(LNG_KEY, lng)
                .build();
        return new URL(builtUri.toString());
    }

    //get json object from response via OkHttp
    public static String getRawJsonResults(URL url) throws IOException, JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        Log.i(LOG_TAG, "Network request executed");
        JSONObject rawJsonResults = new JSONObject(response.body().string());
        JSONArray rawJsonArray = rawJsonResults.getJSONArray(LOCATION);
        String resultString = rawJsonArray.toString();
        Log.i(LOG_TAG, "Raw Json Results: " + rawJsonArray.toString());
        return resultString;
    }

    //map json results to BranchAtmLocation class via Gson
    public static BranchAtmLocation[] getBranchAtmLocations(String rawJsonResults) throws IOException, JSONException {
        Gson gson = new Gson();
        BranchAtmLocation[] branchAtmLocations = gson.fromJson(rawJsonResults, BranchAtmLocation[].class);

        return branchAtmLocations;
    }
}
