package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class map_results extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private JSONArray jsonArray;
    private String clothing_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_results);
        // define map fragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // initialize map system + view
        mapFragment.getMapAsync(this);
        // get Elements to show in map
        clothing_list = getIntent().getStringExtra("clothing_list");

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showdetails"));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // define map-element
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        try {
            jsonArray = new JSONArray(clothing_list);
            // iterate result-JSONArray and Place Marker with extra information
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(row.getDouble("latitude"),
                        row.getDouble("longitude"))).title(row.getString("art")).snippet("Größe: " + row.getString("size") + "\n" + "Distance: " + row.getString("distance")));
                marker.setTag(row.getString("id"));
            }
        } catch (JSONException e) {
            showDialog("Error", "Obtained clothing data could not be processed!");
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("SHOWDETAILSFAIL")) {
                showDialog("Error","Could not get clothing from Server!");
            } else {
                // get clothing results from HTTP-Service
                String clothing = intent.getStringExtra("clothing");
                Intent myIntent = new Intent(getApplicationContext(),ShowClothing.class);
                myIntent.putExtra("clothing",clothing);
                startActivity(myIntent);
            }



        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object cId = marker.getTag();
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWDETAILS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + cId);
        startService(myIntent);
        return false;
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(map_results.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing())
        alertDialog.show();
    }
}
