package com.example.narsi.timespacemap;

import android.os.AsyncTask;

import com.example.narsi.timespacemap.models.Post;
import com.google.android.gms.maps.model.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class checkMarker extends AsyncTask<HashMap, Integer, Long> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    protected Long doInBackground(HashMap[] hashMaps) {
        HashMap<Marker, String> markerMap = hashMaps[0];
        HashMap<String, Post> postMap = hashMaps[1];

        for (Map.Entry<Marker, String> entry : markerMap.entrySet()) {
            Post post = postMap.get(entry.getValue());
            try {
                if (post.beginDate != null) {
                    if (new Date().before(dateFormat.parse(post.beginDate)))
                        entry.getKey().setVisible(false);
                    if (new Date().after(dateFormat.parse(post.beginDate)))
                        entry.getKey().setVisible(true);
                }
                if (post.endDate != null) {
                    if (new Date().after(dateFormat.parse(post.endDate)))
                        entry.getKey().setVisible(false);
                    if (new Date().before(dateFormat.parse(post.endDate)))
                        entry.getKey().setVisible(true);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    protected void onProgressUpdate(Integer... progress) {
    }


    protected void onPostExecute(Long result) {
    }
}