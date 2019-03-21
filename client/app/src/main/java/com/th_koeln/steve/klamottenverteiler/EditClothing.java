package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * "EditClothing" bietet dem Benutzer die Möglichkeit, Kleidungsstücke auch nach dem Einstellen weiter zu bearbeiten.
 * Die bereits verfügbaren Kleidunsstücke werden zu Beginn vom Server geladen passend dargestellt und es wird eine
 *  Möglichkeit geboten die jeweiligen Werte zu ändern.
 *
 *  Betätigt der Benutzer den "Senden"-Button, werden die aktuell eingetragenen Werte ins JSON Dateiformat umgewandelt
 *  und an den Server gesendet.
 *
 * Created by Steffen Owtschinnikow on 30.12.2017.
 */

public class EditClothing extends AppCompatActivity {


    private EditText txtFabric;
    private Button btnPutClothing;
    private String cId;
    private TextView txtShowClothing;
    private ImageView imgShowClothing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clothing);

        // Identifziert des aktuell zu bearbeitende Kleidungsstück
        cId = getIntent().getStringExtra("cId");

        txtFabric = (EditText) findViewById(R.id.txtFabric);
        btnPutClothing = (Button) findViewById(R.id.btnPutClothing);
        txtShowClothing = (TextView) findViewById(R.id.txtShowClothing);
        imgShowClothing = (ImageView) findViewById(R.id.imgShowClothing);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("editclothing"));


        btnPutClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject newProfile = new JSONObject();
                try {


                    // Zu ändernde Attribute in JSON Struktur festhalten
                    newProfile.put("fabric", txtFabric.getText().toString());
                    // Sende neue Attribute zum Server
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload", newProfile.toString());
                    myIntent.putExtra("method", "PUT");
                    myIntent.putExtra("from", "PUTCLOTHING");
                    myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cId);
                    startService(myIntent);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process your entries!");
                }

            }
        });

        // Hole betreffendes Kleidungsstück vom Server
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload", "");
        myIntent.putExtra("method", "GET");
        myIntent.putExtra("from", "EDITCLOTHING");
        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cId );
        startService(myIntent);



    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");

            if (from.equals("EDITCLOTHINGFAIL")) {
                // Kleidung konnte nicht vom Server geholt werden
                showDialog("Error", "Could not get clothing!");
            } else if (from.equals("PUTCLOTHINGFAIL")) {
                // Ändern des Kleidungsstücks fehlgeschlagen
                showDialog("Error", "Could not edit clothing!");
            } else if (from.equals("EDITCLOTHING")) {
                try {
                    // Zeige gewähltes Kleidungsstück an
                    String clothing = intent.getStringExtra("clothing");
                    JSONObject clothingJson=new JSONObject(clothing);
                    txtFabric.setText(clothingJson.getString("fabric"));
                    txtShowClothing.setText(clothingJson.toString());
                    // Bild in nötiges Format umwandeln (Base64 -> Bitmap)
                    byte[] decodedBytes = Base64.decode(clothingJson.getString("image"), 0);
                    Bitmap clothingPicture = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    // Zeige Bild des Kleidungsstücks an
                    imgShowClothing.setImageBitmap(clothingPicture);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process your entries!");
                }
            }


        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(EditClothing.this).create();
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
