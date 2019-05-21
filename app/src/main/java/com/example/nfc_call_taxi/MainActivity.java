package com.example.nfc_call_taxi;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MainActivity extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener{
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    EditText NAMEedit,PHONENUMBERedit;
    Button CALLbutton;
    String Latitude,Longitude;
    TextView TESTtext;
    int j;
    GoogleMap map;
    void init(){
        NAMEedit = findViewById(R.id.NAMEedit);
        PHONENUMBERedit = findViewById(R.id.PHONENUMBERedit);
        CALLbutton = findViewById(R.id.CALLbutton);
        TESTtext = findViewById(R.id.TESTtext);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this,0,intent,0);
    }
    void click(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        click();

    }
    @Override
    protected void onPause() {
        if(nfcAdapter != null){
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(nfcAdapter != null){
            nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) { //테그데이터를 전달받았을때 태그정보를 화면에 보여줌.
        super.onNewIntent(intent);
        String s = ""; // 글씨를 띄우는데 사용
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_DATA); // EXTRA_NDEF_MESSAGES : 여분의 배열이 태그에 존재한다.
        if(data != null)
        {
            try{
                for (int i =0; i<data.length; i++){
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for(j = 0; j<recs.length; j++)
                    {
                        if(recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)){
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
        Latitude = s.split(",")[0];
        Longitude = s.split(",")[1];
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

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
    }
}
