package com.example.nfc_call_taxi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class main extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener{
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilter;
    private String[][] NFCTechLists;
    EditText NAMEedit,PHONENUMBERedit;
    Button CALLbutton;
    String Latitude,Longitude,PhoneNumber;
    TextView TESTtext;
    int j;
    GoogleMap map;
    GoogleApiClient googleApiClient;
    DatabaseReference mDatabase;
    Query query;

    long now = System.currentTimeMillis ();
    Date date = new Date(now);
    SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
    String Time = sdfNow.format(date);

    AlertDialog.Builder dialog;
    void init(){
        NAMEedit = findViewById(R.id.NAMEedit);
        PHONENUMBERedit = findViewById(R.id.PHONENUMBERedit);
        CALLbutton = findViewById(R.id.CALLbutton);
        TESTtext = findViewById(R.id.TESTtext);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this,0,
                intent,0);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
    void click(){
        CALLbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NAMEedit.getText().toString().equals("") || PHONENUMBERedit.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(),"정보를 모두 입력해주세요.",Toast.LENGTH_SHORT).show();
                else{
                    PhoneNumber = PHONENUMBERedit.getText().toString();
                    Data_call data_call = new Data_call(NAMEedit.getText().toString(),PHONENUMBERedit.getText().toString(),Time,Latitude,Longitude,"","","");
                    mDatabase.child("call").push().setValue(data_call);

                    Toast.makeText(getApplicationContext(),"택시를 호출했습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
        click();

        query = mDatabase.child("call").orderByChild("phonenumber").equalTo(PhoneNumber);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Data_call data_call = snapshot.getValue(Data_call.class);
                    if(!data_call.getTaxi_number().equals("") && !data_call.getTaxi_phonenumber().equals("")){
                        DIALOG(data_call.getTaxi_number(),data_call.getTaxi_phonenumber()); //TODO : 다이얼로그 문제 해결해야됨.
                        dialog.show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
    }
    private void updateGPS(Intent intent)
    {
        String s = ""; // 글씨를 띄우는데 사용
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES); // EXTRA_NDEF_MESSAGES : 여분의 배열이 태그에 존재한다.
        if(data != null)
        {
            try{
                for (int i =0; i<data.length; i++){
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for(j = 0; j<recs.length; j++)
                    {
                        if(recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(recs[j].getType(), NdefRecord.RTD_URI)){
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200)==0)?"UTF-8":"UTF-16";
                            int langCodeLen = payload[0] & 0077;
                            s += ("\n"+ new String(payload, langCodeLen + 1, payload.length - langCodeLen -1, textEncoding));
                        }
                    }
                }
            }
            catch(Exception e) { }
        }
        TESTtext.setText(s);
        Latitude = s.split(":")[1].split(",")[0];
        Longitude = s.split(":")[1].split(",")[1];
        Log.e("gps",Latitude+"," + Longitude);
        moveMap(Double.valueOf(Latitude),Double.valueOf(Longitude));
    }
    @Override
    protected void onNewIntent(Intent intent) { //테그데이터를 전달받았을때 태그정보를 화면에 보여줌.
        super.onNewIntent(intent);
        updateGPS(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveMap(37.566643, 126.978279);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
        moveMap(37.566643, 126.978279);
    }
    void moveMap(Double Latitude,Double Longitude){
        LatLng gpsLatLng = new LatLng(Latitude, Longitude);
        CameraPosition position = new CameraPosition.Builder().target(gpsLatLng).zoom(15).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        map.addMarker(new MarkerOptions().position(gpsLatLng).title("출발 위치"));
    }
    void DIALOG(String TaxiNumber,String TaxiPhonenumber) {
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle("택시 호출")
                .setMessage("택시가 호출되었습니다." +
                        "\n택시번호 : " + TaxiNumber +
                        "\n전화번호 : " + TaxiPhonenumber)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
    }
}