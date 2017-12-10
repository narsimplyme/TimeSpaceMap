package com.example.narsi.timespacemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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

/*
*  MainActivity 대신 실행되는 MapsActivity이다.
*  구글맵스를 사용함
*/
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
        markerMap = new HashMap<Marker, String>();                              // Marker와 postKey 연동
        postMap = new HashMap<String, Post>();                                  // postKey와 실제 post를 매칭
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  // 데이터베이스 저장 용이와 가독성을 위해 날짜 형식 지정


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


        //데이터베이스를 읽어오는 부분
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


        //주기적으로 볼아가는 메소드. 시작날짜와 종료날짜를 확인하기 위해 돌아간다
        customHandler = new android.os.Handler();
        customHandler.postDelayed(checkMarker, 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
    /*
    *  주기적으로 Marker의 Date를 체크하여 지도에 표시하거나 숨기거나 하는 메소드
    *  아래에 UpdateMarker 메소드와 유사하다.
    */
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

    /*
    *  마커를 추가해주는 메소드.
    *  Date()를 써서 추가할 때 BeginDate와 EndDate를 비교하여 실제로 표시할 지 숨길지를 정한다.
    */

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

        // HashMap을 써서 Marker와 post의 Key를 매핑해둔다.
        markerMap.put(postMarker, postKey);
        // HashMap<Marker,HashMap>가 간결하긴 하나 대신 관리가 편하게 Hashmap을 하나 더 둔다.
        postMap.put(postKey,post);

    }

    @Override
    public void onStart() {
        super.onStart();
    /*
    *  첫 실행 시 로그인이 안되어 있으면 바로 로그인 액티비티로 전환
    */
        if (mAuth.getCurrentUser() == null) {
            Intent login;
            login = new Intent(this, SignInActivity.class);
            startActivity(login);
        }
    }


    /*
    *  마쉬멜로우부터 권한 획득이 바뀜에 따라 Manifest 외에도 권한을 불러오게 해줘야함
    */
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
                // 내 위치가 확인이 되면 카메라 위치를 변경한다
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

        //마커를 선택했을 때 HashMap인 markermap에서 마커의 정보를 토대로 key를 불러와 포스트를 불러온다
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

        //줌 아웃이 너무 과도하게 되지 않게 하기 위해 제한
        mMap.setMinZoomPreference(14f);

        // 핀치&줌 외의 제스쳐 제한
        mMap.getUiSettings().setScrollGesturesEnabled(false);

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
