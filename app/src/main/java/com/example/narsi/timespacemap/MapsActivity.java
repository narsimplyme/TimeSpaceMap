package com.example.narsi.timespacemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.example.narsi.timespacemap.models.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener {


    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "TAG";

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DateFormat dateFormat;

    LatLng userLocation;
    Location myLocation;

    android.os.Handler customHandler;
    Map<Marker, String> markerMap;
    Map<String, Post> postMap;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAuth = FirebaseAuth.getInstance();
        markerMap = new HashMap<Marker, String>();
        postMap = new HashMap<String, Post>();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(MapsActivity.this, NewPostActivity.class);
                startActivity(newPost);
            }
        });
        findViewById(R.id.fab_list_post).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent go_post = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(go_post);
            }
        });
        findViewById(R.id.fab_logout).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MapsActivity.this, SignInActivity.class));
                finish();
            }
        });


        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Post post = messageSnapshot.getValue(Post.class);

                    updateMarker(post,messageSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        customHandler = new android.os.Handler();
        customHandler.postDelayed(checkMarker, 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private Runnable checkMarker = new Runnable() {
        public void run(){
            for (Map.Entry<Marker, String> entry: markerMap.entrySet() ) {
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
            customHandler.postDelayed(this, 1000);
        }
    };



    private void updateMarker(Post post,String postKey) {
        Marker postMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(post.lat,post.lng)).title(post.title));
        try {
            if(post.beginDate != null){
                if(new Date().before(dateFormat.parse(post.beginDate))) postMarker.setVisible(false);
                if(new Date().after(dateFormat.parse(post.beginDate))) postMarker.setVisible(true);
            }
            if(post.endDate != null){
                if(new Date().after(dateFormat.parse(post.endDate))) postMarker.setVisible(false);
                if(new Date().before(dateFormat.parse(post.endDate))) postMarker.setVisible(true);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        markerMap.put(postMarker, postKey);
        postMap.put(postKey,post);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            Intent login;
            login = new Intent(this, SignInActivity.class);
            startActivity(login);
        }
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (myLocation == null) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = lm.getBestProvider(criteria, true);
                myLocation = lm.getLastKnownLocation(provider);
            }

            if(myLocation!=null){
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14), 1500, null);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                String postKey = markerMap.get(marker);
                Intent intent = new Intent(MapsActivity.this, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                startActivity(intent);
                return false;
            }
        });
        mMap.setMinZoomPreference(14f);
        mMap.setMaxZoomPreference(14f);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

    }


    @Override
    public void onCameraIdle() {
        customHandler = new android.os.Handler();
        customHandler.postDelayed(checkMarker, 0);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public boolean onMyLocationButtonClick() {

        userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        return false;
    }
}
