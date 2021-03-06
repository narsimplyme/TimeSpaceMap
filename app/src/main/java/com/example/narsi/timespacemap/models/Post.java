package com.example.narsi.timespacemap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public double lat, lng;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();
    public String beginDate;
    public String endDate;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body, double lat, double lng) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.lat = lat;
        this.lng = lng;
    }

    public void setBeginDate(String date){
        this.beginDate = date;
    }
    public void setEndDate(String date){
        this.endDate = date;
    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("lat",lat);
        result.put("lng",lng);
        if(beginDate != null)
            result.put("beginDate",beginDate);
        if(endDate != null)
            result.put("endDate",endDate);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
