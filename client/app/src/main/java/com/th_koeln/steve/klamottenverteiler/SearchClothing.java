package com.th_koeln.steve.klamottenverteiler;

import android.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOfferAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.services.RecyclerListener;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Steffen Owtschinnikow on 01.11.17.
 */

public class SearchClothing extends AppCompatActivity
        implements View.OnClickListener, LocationListener {

    public static final int CHOOSE_OPTION = 77;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uId = firebaseAuth.getCurrentUser().getUid();

    private ArrayList<ClothingOffer> ListForAdapter;
    private RecyclerView searchRecyclerView;
    private Toolbar searchClothingToolbar;
    private LocationManager locationManager;

    /*Default Koordinaten ist die Koelner Innenstadt.
    Nicht ideal, aber falls persoenliche Koordinaten nicht
    abgerufen werden koennen soll dennoch ein Ergebniss erscheinen*/
    private double latitude = 50.935534250455916;
    private double longitude = 6.960927844047546;
    private int vicinity = 100;
    private boolean gotGPSDATA = false;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_clothing_2);

        //Permission Check
        checkGPSPermission();
        //Holt die Location
        getLocation();

        searchRecyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Listener fuer die Items in der RecyclerView
        searchRecyclerView.addOnItemTouchListener(
                new RecyclerListener(this, searchRecyclerView, new RecyclerListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent showIntent = new Intent(getApplicationContext(), ShowClothing.class);
                        showIntent.putExtra("clothingID",ListForAdapter.get(position).getId());
                        startActivity(showIntent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //Weitere moeglichkeiten
                    }
                }));

        ListForAdapter = new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Trying to get clothing..\n");
        progressDialog.show();

        searchClothingToolbar = (Toolbar) findViewById(R.id.searchClothingToolbar);
        setSupportActionBar(searchClothingToolbar);

        //BroadcastReceiver zum erhalten von Responses bei gesendeten Requests
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("clothing"));
        searchTheClothing("0","0","0","0","0",longitude,latitude);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHFAIL")) {
                showDialog("Error", "Could not get clothing!");
                progressDialog.dismiss();
            } else if (from.equals("SEARCHPREFCLOTHINGFAIL")) {
                showDialog("Error", "Could not get clothing!");
                progressDialog.dismiss();
            } else if (from.equals("SEARCH")){
                try {
                    // get clothing results from HTTP-Service
                    String clothinglist = intent.getStringExtra("clothing");
                    JSONArray clothinglistJsonArray = new JSONArray(clothinglist);

                    for (int i = 0; i < clothinglistJsonArray.length(); i++) {

                        JSONObject tmpObj = clothinglistJsonArray.getJSONObject(i);
                        String absPath = "";

                        if (!tmpObj.isNull("image")) {
                            String filename = "img" + i;
                            String string = clothinglistJsonArray.getJSONObject(i).getString("image");
                            FileOutputStream outputStream;
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            byte[] decodedBytes = Base64.decode(string, 0);
                            outputStream.write(decodedBytes);
                            outputStream.close();
                            clothinglistJsonArray.getJSONObject(i).put("imagepath", filename);
                            clothinglistJsonArray.getJSONObject(i).remove("image");

                            absPath = SearchClothing.this.getFilesDir().getAbsolutePath() + "/" + filename;
                        }

                        ListForAdapter.add(new ClothingOffer(tmpObj.getString("id"),tmpObj.getString("uId"),
                                tmpObj.getString("art"),tmpObj.getString("size"),tmpObj.getString("style"),
                                tmpObj.getString("gender"),"",tmpObj.getString("fabric"),
                                tmpObj.getString("notes"),tmpObj.getString("brand"),absPath,tmpObj.getDouble("distance")));
                    }

                    fillView(ListForAdapter);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(SearchClothing.this).create();
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

    private void fillView(ArrayList<ClothingOffer> options) {
        ClothingOfferAdapter optAdapter;
        optAdapter = new ClothingOfferAdapter(this, options);
        searchRecyclerView.setAdapter(optAdapter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_OPTION && resultCode == CHOOSE_OPTION) {
            if(data.hasExtra("art")){
                String art = checkEmptyString(data.getStringExtra("art"));
                String size = checkEmptyString(data.getStringExtra("size"));
                //String gender = checkEmptyString(data.getStringExtra("gender"));
                String style = checkEmptyString(data.getStringExtra("style"));
                String color = checkEmptyString(data.getStringExtra("color"));
                //String fabric = checkEmptyString(data.getStringExtra("fabric"));
                String brand = checkEmptyString(data.getStringExtra("brand"));
                vicinity = data.getIntExtra("distance", 50);
                ListForAdapter = new ArrayList<>();
                searchTheClothing(brand,style,color,art,size,longitude,latitude);
            }
        }
    }

    public String checkEmptyString(String s){
        if(s.equals("")){
            s = "0";
        }
        return s;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search_clothing_menu, menu);
        return true;
    }

    //Toolbar Item Listener fuer den Filter und Search Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int actionID = item.getItemId();

        //Search Button
        if(actionID == R.id.action_search){
            Intent showDetailActivity = new Intent(getApplicationContext(), SearchClothingFilter.class);
            showDetailActivity.putExtra("distance",vicinity);
            startActivityForResult(showDetailActivity, CHOOSE_OPTION);
        }

        //Filter Button
        if(actionID == R.id.action_filter){
            View menuItemView = findViewById(R.id.action_filter);
            PopupMenu popup = new PopupMenu(SearchClothing.this, menuItemView);
            MenuInflater inflate = popup.getMenuInflater();
            inflate.inflate(R.menu.popup_search,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch(menuItem.getItemId()){
                        case R.id.distanceAsc:
                            Collections.sort(ListForAdapter, new Comparator<ClothingOffer>() {
                                @Override
                                public int compare(ClothingOffer c1, ClothingOffer c2) {
                                    if(c1.getDistance() < c2.getDistance())return -1;
                                    if(c1.getDistance() > c2.getDistance())return 1;
                                    return 0;
                                }
                            });
                            fillView(ListForAdapter);
                            break;

                        case R.id.distanceDesc:
                            Collections.sort(ListForAdapter, new Comparator<ClothingOffer>() {
                                @Override
                                public int compare(ClothingOffer c1, ClothingOffer c2) {
                                    if(c1.getDistance() < c2.getDistance())return 1;
                                    if(c1.getDistance() > c2.getDistance())return -1;
                                    return 0;
                                }
                            });
                            fillView(ListForAdapter);
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }

        return super.onOptionsItemSelected(item);
    }


    //Funktion um Angebote vom Server zu holen
    public void searchTheClothing(String brand, String style, String color, String art, String size, double lng, double lat){
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        //Paramter fuer den eigenen HTTP Service definieren und starten
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SEARCH");
        myIntent.putExtra("url",getString(R.string.DOMAIN) +"/clothing/"
                + brand + "/" + style + "/" + color + "/" + art + "/" + size + "/"
                + lat + "/" + lng + "/" + vicinity);
        startService(myIntent);
    }

    @Override
    public void onClick(View view) {    }

    //Prueft ob die Berechtigung vorhanden ist GPS zu nutzen
    private void checkGPSPermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    //Holt die aktuelle Position
    private void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Sobald die Koordinaten abgerufen werden koennen
        //soll auch mit diesen die Angebote in der naehe geholt werden
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        if(!gotGPSDATA) {
            gotGPSDATA = true;
            //searchTheClothing("0","0","0","0","0",longitude,latitude);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getApplicationContext(),"Aktiviere GPS & Internet",Toast.LENGTH_SHORT).show();
        searchTheClothing("0","0","0","0","0",longitude,latitude);
    }
}