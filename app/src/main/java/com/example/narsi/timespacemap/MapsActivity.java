package com.example.narsi.timespacemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.narsi.timespacemap.models.Post;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener {


    private static final int RC_SIGN_IN = 123;

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    LatLng userLocation;
    Location myLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(MapsActivity.this, NewPostActivity.class);
                startActivity(newPost);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Post post = messageSnapshot.getValue(Post.class);
                    updateMarker(post);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void updateMarker(Post post) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(post.lat,post.lng)).title(post.title));
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12), 1500, null);
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


    }


    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onMapClick(LatLng latLng) {
//        Intent newPost = new Intent(this, NewPostActivity.class);
//        newPost.putExtra("lat",latLng.latitude);
//        newPost.putExtra("lng",latLng.longitude);
//
//        startActivity(newPost);
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
