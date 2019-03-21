package com.th_koeln.steve.klamottenverteiler;

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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOfferAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Steffen Owtschinnikow on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity implements View.OnClickListener, LocationListener {

    //Elemente aus dem Layout
    private RecyclerView headRecycler, layer1Recycler,
            layer2Recycler, layer3Recycler,
            bottomRecycler, shoesRecycler;
    private Spinner spinnerMissingClothing;
    private Toolbar addOutfitTB;
    private Button btnSendClothingRequest;
    private Button btnSubscribeMissingClothing;

    //Speichert die erhaltenen Ergebnisse der Outfitzusammenstellung fuer jeden Bereich in einer Liste
    private ArrayList<ClothingOffer> HeadForAdapter = new ArrayList<>(), Layer1ForAdapter = new ArrayList(),
            Layer2ForAdapter = new ArrayList(), Layer3ForAdapter = new ArrayList(),
            BottomForAdapter = new ArrayList(), ShoesForAdapter = new ArrayList();

    /*Alles bis zum naechsten "//" ist fuer den Filter.
    Da der Filter ziemlich schnell geschrieben werden musste,
    ist vieles nicht elegant geworden*/
    private String[] genderArr = new String[]{"Geschlecht wählen","Weiblich","Männlich"};
    private ArrayList<String> headSize = new ArrayList<>()
            , topSize = new ArrayList<>(), bottomSize = new ArrayList<>()
            , shoesSizeWomen = new ArrayList<>(), shoesSizeMen = new ArrayList<>();
    private String selGender = "0",selHead = "0",selTop = "0",selBottom = "0", selShoes = "0";
    private int selGenderIn = 0, selHeadIn = 0, selTopIn = 0,
            selBottomIn = 0, selShoesIn = 0, vicinity = 100;
    /*Default Koordinaten ist die Koelner Innenstadt.
    Nicht ideal, aber falls persoenliche Koordinaten nicht
    abgerufen werden koennen soll dennoch ein Ergebniss erscheinen*/
    private LocationManager locationManager;
    private double latitude = 50.935534250455916;
    private double longitude = 6.960927844047546;
    //

    //Falls Bereiche keine Ergebnisse liefern werden in dieser
    //Liste die fehlenden Objekte gespeichert
    private ArrayList<String> miss = new ArrayList();
    private ArrayAdapter<String> missingAdapter;

    private ProgressDialog progressDialog;
    private String model, selected;
    private int index = 0;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId = firebaseAuth.getCurrentUser().getUid();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit_2);

        //Permission Check
        checkGPSPermission();
        //Holt die Location
        getLocation();

        //RecyclerView fuer die Kopfbedeckungen
        headRecycler = (RecyclerView) findViewById(R.id.headRecycler);
        headRecycler.setHasFixedSize(true);
        headRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper0 = new PagerSnapHelper();
        helper0.attachToRecyclerView(headRecycler);

        //RecyclerView fuer die erste Oberkoerperschicht
        layer1Recycler = (RecyclerView) findViewById(R.id.layer1Recycler);
        layer1Recycler.setHasFixedSize(true);
        layer1Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper1 = new PagerSnapHelper();
        helper1.attachToRecyclerView(layer1Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        layer2Recycler = (RecyclerView) findViewById(R.id.layer2Recycler);
        layer2Recycler.setHasFixedSize(true);
        layer2Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper2 = new PagerSnapHelper();
        helper2.attachToRecyclerView(layer2Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        layer3Recycler = (RecyclerView) findViewById(R.id.layer3Recycler);
        layer3Recycler.setHasFixedSize(true);
        layer3Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper3 = new PagerSnapHelper();
        helper3.attachToRecyclerView(layer3Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        bottomRecycler = (RecyclerView) findViewById(R.id.bottomRecycler);
        bottomRecycler.setHasFixedSize(true);
        bottomRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper4 = new PagerSnapHelper();
        helper4.attachToRecyclerView(bottomRecycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        shoesRecycler = (RecyclerView) findViewById(R.id.shoesRecycler);
        shoesRecycler.setHasFixedSize(true);
        shoesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper5 = new PagerSnapHelper();
        helper5.attachToRecyclerView(shoesRecycler);

        spinnerMissingClothing = (Spinner) findViewById(R.id.spinnerMissingClothing);

        btnSendClothingRequest = (Button) findViewById(R.id.btnSendClothingRequest);
        btnSendClothingRequest.setOnClickListener(this);
        btnSubscribeMissingClothing = (Button) findViewById(R.id.btnSubscribeMissingClothing);
        btnSubscribeMissingClothing.setOnClickListener(this);

        addOutfitTB = (Toolbar) findViewById(R.id.addOutfitTB);
        setSupportActionBar(addOutfitTB);

        IntentFilter filter = new IntentFilter();
        filter.addAction("getClothingDetail2");
        filter.addAction("showoutfit");
        filter.addAction("clothingOptions");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,filter);

        getClothingOption();
    }

    public void getDetailedData(String outfit){
        HeadForAdapter.clear();
        Layer1ForAdapter.clear();
        Layer2ForAdapter.clear();
        Layer3ForAdapter.clear();
        BottomForAdapter.clear();
        ShoesForAdapter.clear();
        miss.clear();
        try {
            JSONObject outfitsArray = new JSONObject(outfit);
            JSONArray layers = outfitsArray.getJSONArray("layers");
            model = outfitsArray.getString("model");
            String objects;
            for(int ilayer = 0; ilayer < layers.length(); ilayer++)
            {
                objects = outfitsArray.getString((String) layers.get(ilayer));
                if (objects.equals("[]")) {
                    miss.add((String) layers.get(ilayer));
                } else {
                    switch (ilayer){
                        case 0: JSONArray headArray = outfitsArray.getJSONArray("head");
                            for (int j=0;headArray.length()>j;j++){
                                sendClothingRequest((String) headArray.get(j),"head");
                            }
                            break;
                        case 1: JSONArray layer1Array = outfitsArray.getJSONArray("layer1");
                            for (int j=0;layer1Array.length()>j;j++){
                                sendClothingRequest((String) layer1Array.get(j),"layer1");
                            }
                            break;
                        case 2: JSONArray layer2Array = outfitsArray.getJSONArray("layer2");
                            for (int j=0;layer2Array.length()>j;j++){
                                sendClothingRequest((String) layer2Array.get(j),"layer2");
                            }
                            break;
                        case 3: JSONArray layer3Array = outfitsArray.getJSONArray("layer3");
                            for (int j=0;layer3Array.length()>j;j++){
                                sendClothingRequest((String) layer3Array.get(j),"layer3");
                            }
                            break;
                        case 4: JSONArray bottomArray = outfitsArray.getJSONArray("bottom");
                            for (int j=0;bottomArray.length()>j;j++){
                                sendClothingRequest((String) bottomArray.get(j),"bottom");
                            }
                            break;
                        case 5: JSONArray shoesArray = outfitsArray.getJSONArray("shoes");
                            for (int j=0;shoesArray.length()>j;j++){
                                sendClothingRequest((String) shoesArray.get(j),"shoes");
                            }
                            break;
                    }
                }
            }
            missingAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, miss);
            spinnerMissingClothing.setAdapter(missingAdapter);

        } catch (JSONException e) {
            showDialog("Error", "Could not process outfit data!");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btnSendClothingRequest:
                if(HeadForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) headRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(HeadForAdapter.get(position));
                }
                if(Layer1ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer1Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer1ForAdapter.get(position));
                }
                if(Layer2ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer2Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer2ForAdapter.get(position));
                }
                if(Layer3ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer3Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer3ForAdapter.get(position));
                }
                if(BottomForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) bottomRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(BottomForAdapter.get(position));
                }
                if(ShoesForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) shoesRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(ShoesForAdapter.get(position));
                }
                showDialog("Success", "Send requests to selected items!");
                break;

            case R.id.btnSubscribeMissingClothing:
                if(spinnerMissingClothing.getSelectedItem() !=null) {
                    JSONObject subscribe = new JSONObject();
                    try {
                        subscribe.put("model", model);
                        subscribe.put("missing", spinnerMissingClothing.getSelectedItem().toString());

                        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                        myIntent.putExtra("payload", subscribe.toString());
                        myIntent.putExtra("method", "POST");
                        myIntent.putExtra("from", "SUBSCRIBECLOTHING");
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/search");
                        startService(myIntent);
                        showDialog("Success", "Subscribed to selected item!");
                    } catch (JSONException e) {
                        showDialog("Error", "Could not subscribe for missing clothing!");
                    }
                }
                break;
        }
    }

    public void sendClothingRequest(String clothingID, String layer){
        Intent reqIntent = new Intent(getApplicationContext(), HttpsService.class);
        reqIntent.putExtra("method","GET");
        reqIntent.putExtra("from","GETCLOTHINGDETAIL2");
        reqIntent.putExtra("layer", layer);
        reqIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + clothingID);
        startService(reqIntent);
    }

    private void sendRequest(ClothingOffer cOff){
        try {
            JSONObject request = new JSONObject();
            request.put("uId", uId);
            request.put("ouId", cOff.getuId());

            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
            myIntent.putExtra("payload", request.toString());
            myIntent.putExtra("method", "POST");
            myIntent.putExtra("from", "NEWREQUEST");
            myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cOff.getId());
            startService(myIntent);
        }catch (JSONException e) {
            showDialog("Error", "Could not send Requests");
        }
    }

    private void sendOutfitRequest(String selected){
        // Outfits vom Server abrufen
        if(selected.equals("Winter")) {
            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
            myIntent.putExtra("method", "GET");
            myIntent.putExtra("from", "SEARCHOUTFIT");
            myIntent.putExtra("url", getString(R.string.DOMAIN) + "/outfit/" + "winter/"
                    + selGender +"/"+ selHead +"/"+ selTop +"/"+ selBottom +"/"+ selShoes
                    +"/"+ longitude +"/"+ latitude +"/"+ vicinity);
            startService(myIntent);
            progressDialog = new ProgressDialog(ShowOutfit.this);
            progressDialog.setMessage("Trying to get outfit..");
            progressDialog.show();
        }
    }

    private void getClothingOption(){
        Intent optionsIntent = new Intent(getApplicationContext(), HttpsService.class);
        optionsIntent.putExtra("method","GET");
        optionsIntent.putExtra("from","CLOTHINGOPTIONS");
        optionsIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothingOptions");
        startService(optionsIntent);
        progressDialog = new ProgressDialog(ShowOutfit.this);
        progressDialog.setMessage("Getting Information..");
        progressDialog.show();
    }

    private void fillView(ArrayList<ClothingOffer> options, RecyclerView putRecyc) {
        ClothingOfferAdapter optAdapter;
        optAdapter = new ClothingOfferAdapter(this, options);
        putRecyc.setAdapter(optAdapter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getStringExtra("from").equals("GETCLOTHINGDETAIL2")){
                ClothingOffer tmpOffer = null;
                try {
                    JSONObject request = new JSONObject(intent.getStringExtra("clothing"));
                    String absPath = "";

                    if (!request.isNull("image")) {
                        String filename = "img" + index++;
                        FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        byte[] decodedBytes = Base64.decode(request.getString("image"), 0);
                        outputStream.write(decodedBytes);
                        outputStream.close();
                        absPath = ShowOutfit.this.getFilesDir().getAbsolutePath() + "/" + filename;
                    }

                    tmpOffer = new ClothingOffer(request.getString("id"),request.getString("uId"),
                            request.getString("art"),request.getString("size"),request.getString("style"),
                            request.getString("gender"),"",request.getString("fabric"), request.getString("notes"),
                            request.getString("brand"),absPath,-200);

                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(tmpOffer != null) {
                    switch (intent.getStringExtra("layer")) {
                        case "head":
                            HeadForAdapter.add(tmpOffer);
                            fillView(HeadForAdapter,headRecycler);
                            break;
                        case "layer1":
                            Layer1ForAdapter.add(tmpOffer);
                            fillView(Layer1ForAdapter,layer1Recycler);
                            break;
                        case "layer2":
                            Layer2ForAdapter.add(tmpOffer);
                            fillView(Layer2ForAdapter,layer2Recycler);
                            break;
                        case "layer3":
                            Layer3ForAdapter.add(tmpOffer);
                            fillView(Layer3ForAdapter,layer3Recycler);
                            break;
                        case "bottom":
                            BottomForAdapter.add(tmpOffer);
                            fillView(BottomForAdapter,bottomRecycler);
                            break;
                        case "shoes":
                            ShoesForAdapter.add(tmpOffer);
                            fillView(ShoesForAdapter,shoesRecycler);
                            break;
                    }
                }
            }

            if (intent.getStringExtra("from").equals("SEARCHOUTFITFAIL")) {
                // Fehler beim Suchen der Outfits
                showDialog("Error!", "Could not get outfit!");
                progressDialog.dismiss();
            }
            if (intent.getStringExtra("from").equals("SEARCHOUTFIT")) {
                progressDialog.dismiss();
                // Starte Aktivität um gelieferte Kleidung anzuzeigen.
                String outfit = intent.getStringExtra("clothing");
                getDetailedData(outfit);
            }
            if(intent.getStringExtra("from").equals("CLOTHINGOPTIONS")){
                String rawData = intent.getStringExtra("optionsData");
                /*Die Daten zum Filtern sollen nun extrahier werden.
                 *Sehr unschoene Loesung, aber eine schnelle Loesung war von noeten
                 */
                try {
                    JSONArray clothingOptions = new JSONArray(rawData);
                    for(int j = 0;clothingOptions.length()>j;j++){
                        JSONObject tmpOBJ = clothingOptions.getJSONObject(j);
                        if(tmpOBJ.getString("topic").equals("Size")){
                            JSONArray sizeOptions1 = tmpOBJ.getJSONArray("options");
                            for(int k=0;sizeOptions1.length()>k;k++){
                                JSONObject sizeTopic = sizeOptions1.getJSONObject(k);
                                switch (sizeTopic.getString("topic")){
                                    case "Kopfbedeckung Größe":
                                        headSize.add(sizeTopic.getString("topic"));
                                        JSONArray sizeOptions2 = sizeTopic.getJSONArray("options");
                                        for(int h=0;sizeOptions2.length()>h;h++){
                                            headSize.add(sizeOptions2.getString(h));
                                        }
                                        break;
                                    case "Oberbekleidung Größe":
                                        topSize.add(sizeTopic.getString("topic"));
                                        JSONArray sizeOptions3 = sizeTopic.getJSONArray("options");
                                        for(int h=0;sizeOptions3.length()>h;h++){
                                            topSize.add(sizeOptions3.getString(h));
                                        }
                                        break;
                                    case "Unterbekleidung Größe":
                                        bottomSize.add(sizeTopic.getString("topic"));
                                        JSONArray sizeOptions4 = sizeTopic.getJSONArray("options");
                                        for(int h=0;sizeOptions4.length()>h;h++){
                                            bottomSize.add(sizeOptions4.getString(h));
                                        }
                                        break;
                                    case "Fußbekleidung Größe (Herren)":
                                        shoesSizeMen.add(sizeTopic.getString("topic"));
                                        JSONArray sizeOptions5 = sizeTopic.getJSONArray("options");
                                        for(int h=0;sizeOptions5.length()>h;h++){
                                            shoesSizeMen.add(sizeOptions5.getString(h));
                                        }
                                        break;
                                    case "Fußbekleidung Größe (Frauen)":
                                        shoesSizeWomen.add(sizeTopic.getString("topic"));
                                        JSONArray sizeOptions6 = sizeTopic.getJSONArray("options");
                                        for(int h=0;sizeOptions6.length()>h;h++){
                                            shoesSizeWomen.add(sizeOptions6.getString(h));
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }catch(JSONException e){
                    progressDialog.dismiss();
                    showDialog("Error", "Could not process request data!");
                }
                progressDialog.dismiss();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.outfit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int actionID = item.getItemId();

        //Search Button
        if(actionID == R.id.action_add){
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowOutfit.this);
            final String[] tmpString = new String[]{"Winter", "Sommer", "Winter-Sport", "Herbst"};
            builder.setTitle("Choose context")
                    .setSingleChoiceItems(tmpString,0, null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ListView lw = ((AlertDialog)dialogInterface).getListView();
                            selected = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            sendOutfitRequest(selected);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        /*Filter Button
        Sehr unschoene, aber eine schnelle Loesung war noetig
        */
        if(actionID == R.id.action_filt){
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowOutfit.this);
            View mView = getLayoutInflater().inflate(R.layout.outfit_filter_layout, null);
            builder.setTitle("Filter");
            //
            final Spinner spinnerGender = (Spinner) mView.findViewById(R.id.spinnerGender);
            spinnerGender.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, genderArr));
            spinnerGender.setSelection(selGenderIn);
            //
            final Spinner spinnerHead = (Spinner) mView.findViewById(R.id.spinnerHead);
            spinnerHead.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, headSize));
            spinnerHead.setSelection(selHeadIn);
            //
            final Spinner spinnerTop = (Spinner) mView.findViewById(R.id.spinnerTop);
            spinnerTop.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, topSize));
            spinnerTop.setSelection(selTopIn);
            //
            final Spinner spinnerBottom = (Spinner) mView.findViewById(R.id.spinnerBottom);
            spinnerBottom.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, bottomSize));
            spinnerBottom.setSelection(selBottomIn);
            //
            final Spinner spinnerShoesWomen = (Spinner) mView.findViewById(R.id.spinnerShoesWomen);
            spinnerShoesWomen.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, shoesSizeWomen));
            spinnerShoesWomen.setSelection(selShoesIn);
            //
            final Spinner spinnerShoesMen = (Spinner) mView.findViewById(R.id.spinnerShoesMen);
            spinnerShoesMen.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, shoesSizeMen));
            spinnerShoesMen.setSelection(selShoesIn);
            //
            final TextView textViewProgress = (TextView) mView.findViewById(R.id.textViewProgress);
            SeekBar seekBarDistance = (SeekBar) mView.findViewById(R.id.seekBarDistance);
            seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    textViewProgress.setText("Entfernung in KM: " + i);
                    vicinity = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBarDistance.setProgress(vicinity);
            textViewProgress.setText("Entfernung in KM: "+vicinity);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Die Ausgewaehlten Daten muessen nun gespeichert werden
                    switch(spinnerGender.getSelectedItemPosition()){
                        case 1:selGender = "W";selGenderIn = 1;break;
                        case 2:selGender = "M";selGenderIn = 2;break;
                        case 0:selGender = "0";selGenderIn = 0;break;
                    }
                    if((selHeadIn = spinnerHead.getSelectedItemPosition())>0){selHead = spinnerHead.getSelectedItem().toString();}
                    else{selHead = "0";selHeadIn = 0;}
                    if((selTopIn = spinnerTop.getSelectedItemPosition())>0){selTop = spinnerTop.getSelectedItem().toString();}
                    else{selTop = "0";selTopIn = 0;}
                    if((selBottomIn = spinnerBottom.getSelectedItemPosition())>0){selBottom = spinnerBottom.getSelectedItem().toString();}
                    else{selBottom = "0";selBottomIn = 0;}
                    if((selShoesIn = spinnerShoesMen.getSelectedItemPosition())>0){
                        selShoes = spinnerShoesMen.getSelectedItem().toString();
                    }else if((selShoesIn = spinnerShoesWomen.getSelectedItemPosition())>0) {
                        selShoes = spinnerShoesWomen.getSelectedItem().toString();
                    }else{selShoes = "0";selShoesIn = 0;}
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setView(mView);
            builder.create();
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowOutfit.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing()) alertDialog.show();
    }

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
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}