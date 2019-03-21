package com.th_koeln.steve.klamottenverteiler;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ShowOnMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private double cLatitude, cLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_results);
        //Map Fragment definieren und Benachrichtigung erhalten wenn es bereit ist
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Initialisiere Map System + View
        mapFragment.getMapAsync(this);
        //Hol die Koordinaten um den Marker zu platzieren
        cLatitude = getIntent().getDoubleExtra("lat",-1);
        cLongitude = getIntent().getDoubleExtra("lng",-1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Definiere das Map-Element
        mMap = googleMap;
        //Platziere den Marker und stelle die Farbe ein
        mMap.addMarker(new MarkerOptions().position(new LatLng(cLatitude, cLongitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //Bewegt die Kamera zu dem Marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cLatitude, cLongitude), 12));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12),2000,null);
    }
}

