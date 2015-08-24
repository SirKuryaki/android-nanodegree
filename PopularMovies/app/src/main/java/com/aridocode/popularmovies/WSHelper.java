package com.aridocode.popularmovies;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sirkuryaki on 8/18/15.
 */
class WSHelper {

    public static JSONObject getJSONFromUri(Uri uri) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        URL url;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            url = null;
        }

        if (url == null) {
            return null;
        }

        JSONObject jsonObject;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                return null;
            }

            jsonObject = new JSONObject(buffer.toString());
        } catch (IOException | JSONException e) {
            jsonObject = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ignored) {
                }
            }
        }

        return jsonObject;
    }
}
