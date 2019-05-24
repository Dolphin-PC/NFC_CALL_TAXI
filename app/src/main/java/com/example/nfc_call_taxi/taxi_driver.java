package com.example.nfc_call_taxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class taxi_driver extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener{
    Button button500,button1000,button1500,CallButton,CancelButton;
    TextView CallText,Name,NameText,Phonenumber,PhonenumberText;
    ImageView imageView;
    DatabaseReference mDatabase;
    GoogleMap map;
    GoogleApiClient googleApiClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location mLastKnownLocation;
    Task<Location> location;
    LatLng gpsLatLng;
    LatLng CallLatLng;
    CircleOptions circle;
    private boolean mLocationPermissionGranted;
    double DISTANCE = 0.009;
    void init(){
        button500 = findViewById(R.id.button500);
        button1000 = findViewById(R.id.button1000);
        button1500 = findViewById(R.id.button1500);
        CallButton = findViewById(R.id.CallButton);
        CancelButton = findViewById(R.id.CancelButton);
        CallText = findViewById(R.id.CallText);
        Name = findViewById(R.id.Name);
        NameText = findViewById(R.id.NameText);
        Phonenumber = findViewById(R.id.Phonenumber);
        PhonenumberText = findViewById(R.id.PhonenumberText);
        imageView = findViewById(R.id.imageView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    void click(){
        //case 500M: 0.0045
        button500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawCirlce(500);
                DISTANCE = 0.0045;
            }
        });
        button1000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE = 0.009;
                DrawCirlce(1000);
            }
        });
        button1500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE = 0.0135;
                DrawCirlce(1500);
            }
        });
        CallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = mDatabase.child("call").orderByChild("phonenumber").equalTo(PhonenumberText.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String key = snapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                            String path = "/" + dataSnapshot.getKey() + "/" + key;
                            Data_call data_call = snapshot.getValue(Data_call.class);
                            HashMap<String,Object> Hash1 = new HashMap<>();
                            Hash1.put("taxi_name","박찬영");
                            Hash1.put("taxi_phonenumber","010-4744-3358");
                            Hash1.put("taxi_number","춘천 가 1234");

                            /*Hash1.put(PhonenumberText.getText().toString(),new Data_call(data_call.getName()
                                    ,data_call.getPhonenumber()
                                    ,data_call.getTime()
                                    ,data_call.getLatitude()
                                    ,data_call.getLongitude()
                                    ,"박찬영"
                                    ,"010-4744-3358"
                                    ,"춘천 가 1234"));*/
                            mDatabase.child(path).updateChildren(Hash1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //TODO : gpsLatLng 와 CallLatLng 로 카카오맵 지도 켜주기
            }
        });
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InVisible();
                map.clear();
            }
        });
    }
    void DrawCirlce(int D){
        if(circle!=null) {
            map.clear();
            circle = new CircleOptions().center(gpsLatLng) //원점
                    .radius(D)      //반지름 단위 : m
                    .strokeWidth(3f);  //선너비 0f : 선없음;
            map.addCircle(circle);
        }
        else {
            circle = new CircleOptions().center(gpsLatLng) //원점
                    .radius(D)      //반지름 단위 : m
                    .strokeWidth(3f);  //선너비 0f : 선없음;
            map.addCircle(circle);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_driver);
        init();
        click();
        getLocationPermission();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*
        Log.e("gps.lati",gpsLatLng.latitude+"");
        Log.e("gps.lati",gpsLatLng.longitude+"");*/
        mDatabase.child("call").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Data_call data_call = dataSnapshot.getValue(Data_call.class);
                CallLatLng = new LatLng(Double.valueOf(data_call.getLatitude()),Double.valueOf(data_call.getLongitude()));
                if(Math.abs(gpsLatLng.latitude - CallLatLng.latitude) <= DISTANCE
                        && Math.abs(gpsLatLng.longitude - CallLatLng.longitude) <= DISTANCE
                        && data_call.getTaxi_name().equals("")) {      //설정한 범위내에 콜이 있다면
                    Visible();
                    NameText.setText(data_call.getName());
                    PhonenumberText.setText(data_call.getPhonenumber());
                    moveMap(CallLatLng);
                    map.addMarker(new MarkerOptions().position(new LatLng(CallLatLng.latitude, CallLatLng.longitude)).title("출발 위치")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    void Visible() {
        CallText.setText("콜이 들어왔습니다.");
        Name.setVisibility(View.VISIBLE);
        NameText.setVisibility(View.VISIBLE);
        Phonenumber.setVisibility(View.VISIBLE);
        PhonenumberText.setVisibility(View.VISIBLE);
        CallButton.setVisibility(View.VISIBLE);
        CancelButton.setVisibility(View.VISIBLE);
        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        ConstraintLayout.LayoutParams prams = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
        prams.bottomMargin = width/2;
        imageView.setLayoutParams(prams);
    }
    void InVisible(){
        CallText.setText("콜을 기다리고 있습니다.");
        Name.setVisibility(View.INVISIBLE);
        NameText.setVisibility(View.INVISIBLE);
        Phonenumber.setVisibility(View.INVISIBLE);
        PhonenumberText.setVisibility(View.INVISIBLE);
        CallButton.setVisibility(View.INVISIBLE);
        CancelButton.setVisibility(View.INVISIBLE);
        ConstraintLayout.LayoutParams prams = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
        prams.bottomMargin = 0;
        imageView.setLayoutParams(prams);
        moveMap(gpsLatLng);
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = fusedLocationProviderClient.getLastLocation();
            My_Location();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = fusedLocationProviderClient.getLastLocation();
                    My_Location();
                }
            } else {
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        getLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onCameraIdle() { }
    @Override
    public void onCameraMove() { }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
        if (mLocationPermissionGranted){
            My_Location();
        }
        else
            setmDefaultLocation();
    }
    @SuppressLint("MissingPermission")
    void My_Location() {
        if (location != null && map != null) {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
            locationTask.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    LatLng MyLocation = new LatLng(location.getResult().getLatitude(), location.getResult().getLongitude());
                    CameraPosition position = new CameraPosition.Builder().target(MyLocation).zoom(15).build();
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    map.setMyLocationEnabled(mLocationPermissionGranted);
                    gpsLatLng = MyLocation;
                }
            });

        }else{
            setmDefaultLocation();
        }
    }
    void setmDefaultLocation(){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.566643, 126.978279),15));
    }
    void moveMap(LatLng latLng){
        CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(15).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }
}
