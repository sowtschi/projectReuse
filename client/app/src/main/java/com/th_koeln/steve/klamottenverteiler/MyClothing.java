package com.th_koeln.steve.klamottenverteiler;


/**
 * MyClothing dient als Übersicht der im Besitzt befindlichen Kleidungsstücke.
 * Die Kleidungsstücke werden vom Server geladen und dem Benutzer präsentiert.
 * Sollten Kleidungsstücke vorhanden sein, können verschiedene Operationen, wie
 * das Löschen eines Kleidungsstücks oder das Bearbeiten initialisiert werden.
 *
 * Created by Steffen Owtschinnikow on 30.12.2017.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;

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

public class MyClothing extends AppCompatActivity {

    /*Da die hier die Kleidungsstuecke in einer Liste angezeigt werden
        sollen, wird hier das Layout und die Funktionen aus SearchClothing.class
        zum grossteil wiederverwendet*/

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uId = firebaseAuth.getCurrentUser().getUid();

    private ArrayList<ClothingOffer> ListForAdapter;
    private RecyclerView searchRecyclerView;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_clothing_2);

        searchRecyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Listener fuer die Items in der RecyclerView
        searchRecyclerView.addOnItemTouchListener(
                new RecyclerListener(this, searchRecyclerView, new RecyclerListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent showIntent = new Intent(getApplicationContext(), EditClothing.class);
                        showIntent.putExtra("cId",ListForAdapter.get(position).getId());
                        startActivity(showIntent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //Weitere moeglichkeiten
                        //Delete Option etc.
                    }
                }));

        ListForAdapter = new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Getting your clothing..\n");
        progressDialog.show();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("myclothing"));

        // Hole Kleidung des Benutzers vom Server
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload", "");
        myIntent.putExtra("method", "GET");
        myIntent.putExtra("from", "MYCLOTHING");
        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/clothing");
        //call http service
        startService(myIntent);

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String from = intent.getStringExtra("from");
            if (from.equals("MYCLOTHINGFAIL")) {
                // Kleidung des Benutzers konnte nicht geholt werden
                showDialog("Error","Could not get clothing from Server!");
            } else {
                try {
                    // get clothing results from HTTP-Service
                    String clothinglist = intent.getStringExtra("clothing");
                    JSONArray clothinglistJsonArray = new JSONArray(clothinglist);
                    if (clothinglistJsonArray.length() > 0) {
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

                                absPath = MyClothing.this.getFilesDir().getAbsolutePath() + "/" + filename;
                            }

                            ListForAdapter.add(new ClothingOffer(tmpObj.getString("id"), tmpObj.getString("uId"),
                                    tmpObj.getString("art"), tmpObj.getString("size"), tmpObj.getString("style"),
                                    tmpObj.getString("gender"), "", tmpObj.getString("fabric"),
                                    tmpObj.getString("notes"), tmpObj.getString("brand"), absPath, -300));
                        }

                        fillView(ListForAdapter);
                    }else{
                        // kein Kleidungsstück vorhanden
                        progressDialog.dismiss();
                        showDialog("No clothing", "You didn't create any clothing!");
                    }
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    showDialog("Error", "Could not process clothing data!");
                } catch (IOException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                progressDialog.dismiss();
            }

        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(MyClothing.this).create();
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

    /* Die folgenden zwei Funktionen dienen zum Entfernen eines JSON-Objekts auf einem Array und wurden von
    https://gist.github.com/emmgfx/0f018b5acfa3fd72b3f6 übernommen.
    Um eine hohe Kompabilität zu gewährleisten, wurde auf die Verwendung von der einfachen
    remove() Funktion verzichtet, da diese erst ab Android API19 verfügbar ist.

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }
    */
}


