package com.th_koeln.steve.klamottenverteiler;

/**
 * Die Aktivität "Choose Context" bietet die Möglichkeit einen bestimmten
 * Kontext für ein Outfit zu wählen, welches der Benutzer anfordern möchte.
 *
 * Created by Steffen Owtschinnikow on 25.12.2017.
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
import android.view.View;
import android.widget.Button;

import com.th_koeln.steve.klamottenverteiler.services.HttpsService;



public class ChooseContext extends AppCompatActivity {

    private Button btnWinterOutfit;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_context);

        progressDialog = new ProgressDialog(this);

        btnWinterOutfit = (Button) findViewById(R.id.btnWinterOutfit);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showoutfit"));


        btnWinterOutfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Outfits vom Server abrufen
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCHOUTFIT");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/outfit/"+ "winter");
                startService(myIntent);
                progressDialog.setMessage("Trying to get outfit..");
                progressDialog.show();
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHOUTFITFAIL")) {
                // Fehler beim Suchen der Outfits
                showDialog("Error!", "Could not get outfit!");
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                // Starte Aktivität um gelieferte Kleidung anzuzeigen.
                String outfit = intent.getStringExtra("clothing");
                Intent showClothing = new Intent(getApplicationContext(), ShowOutfit.class);
                showClothing.putExtra("outfit", outfit);
                showClothing.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(showClothing);
                //finish();
            }
        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ChooseContext.this).create();
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
